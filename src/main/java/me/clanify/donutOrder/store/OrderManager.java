/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatMessageType
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package me.clanify.donutOrder.store;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import me.clanify.donutOrder.DonutOrder;
import me.clanify.donutOrder.Utils;
import me.clanify.donutOrder.data.ItemKey;
import me.clanify.donutOrder.data.Order;
import me.clanify.donutOrder.util.TaskUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class OrderManager {
    private final DonutOrder pl;
    private final Map<UUID, Order> orders = new LinkedHashMap<UUID, Order>();
    private final File ordersDir;

    public OrderManager(DonutOrder pl) {
        this.pl = pl;
        if (!pl.getDataFolder().exists()) {
            pl.getDataFolder().mkdirs();
        }

        // New directory for individual order files
        this.ordersDir = new File(pl.getDataFolder(), "orders");
        if (!this.ordersDir.exists()) {
            this.ordersDir.mkdirs();
        }

        // Migration logic: Check for old orders.db
        File oldOrdersFile = new File(pl.getDataFolder(), "orders.db");
        if (oldOrdersFile.exists()) {
            pl.getLogger().info("Found legacy orders.db, migrating to individual files...");
            YamlConfiguration oldCfg = YamlConfiguration.loadConfiguration(oldOrdersFile);

            int migratedCount = 0;
            for (String key : oldCfg.getKeys(false)) {
                try {
                    // Extract data from the old monolothic file
                    UUID id = UUID.fromString(key);
                    String ownerStr = oldCfg.getString(key + ".owner");
                    String itemStr = oldCfg.getString(key + ".item");

                    if (ownerStr == null || itemStr == null)
                        continue;

                    Order o = new Order();
                    o.id = id;
                    o.owner = UUID.fromString(ownerStr);
                    o.key = ItemKey.deserialize(itemStr);
                    o.requested = oldCfg.getInt(key + ".requested");
                    o.delivered = oldCfg.getInt(key + ".delivered");
                    o.priceEach = oldCfg.getDouble(key + ".priceEach");
                    o.paid = oldCfg.getDouble(key + ".paid");
                    o.canceled = oldCfg.getBoolean(key + ".canceled");
                    o.completed = oldCfg.getBoolean(key + ".completed");

                    List<?> raw = oldCfg.getList(key + ".storage");
                    if (raw != null) {
                        for (Object ois : raw) {
                            if (ois instanceof ItemStack) {
                                o.storage.add((ItemStack) ois);
                            }
                        }
                    }

                    // Put in map and save to new individual file
                    this.orders.put(o.id, o);
                    this.saveOrder(o);
                    migratedCount++;

                } catch (Exception e) {
                    pl.getLogger().severe("Failed to migrate order " + key + ": " + e.getMessage());
                }
            }

            pl.getLogger().info("Migrated " + migratedCount + " orders.");

            // Rename old file to prevent re-migration
            File backup = new File(pl.getDataFolder(), "orders.db.bak");
            if (oldOrdersFile.renameTo(backup)) {
                pl.getLogger().info("Renamed orders.db to orders.db.bak");
            } else {
                pl.getLogger().warning("Could not rename orders.db! Please remove it manually.");
            }
        }

        // Check for even older saves.db (from original code, just in case)
        File legacyFile = new File(pl.getDataFolder(), "saves.db");
        if (legacyFile.exists() && !oldOrdersFile.exists() && this.ordersDir.list().length == 0) {
            // Handle extremely old migration if needed, or just ignore since we handled
            // orders.db
            // For safety, let's just log a warning if it exists but we didn't migrate from
            // orders.db
            pl.getLogger().info("Found ancient saves.db but no orders.db. Ignoring for now as we use new system.");
        }

        this.loadAll();
    }

    public Collection<Order> all() {
        return this.orders.values();
    }

    public Order create(UUID owner, Material chosenMaterial, int amount, double priceEach) {
        return this.create(owner, ItemKey.of(chosenMaterial), amount, priceEach);
    }

    public Order create(UUID owner, ItemKey key, int amount, double priceEach) {
        Order o = new Order();
        o.id = UUID.randomUUID();
        o.owner = owner;
        o.key = key;
        o.requested = Math.max(1, amount);
        o.delivered = 0;
        o.priceEach = priceEach;
        o.paid = o.totalPrice();
        o.canceled = false;
        o.completed = false;
        this.orders.put(o.id, o);
        this.saveOrder(o);
        return o;
    }

    public void cancel(Order o) {
        o.canceled = true;
        int remaining = o.remainingAmount();
        double refund = (double) remaining * o.priceEach;
        OfflinePlayer owner = Bukkit.getOfflinePlayer((UUID) o.owner);
        if (owner.isOnline()) {
            this.pl.vault().give((OfflinePlayer) owner.getPlayer(), refund);
        }
        o.requested = o.delivered;
        o.completed = true;
        this.saveOrder(o);
    }

    public void applyDelivery(Order o, List<ItemStack> accepted, int acceptedAmount, UUID deliverer) {
        if (acceptedAmount <= 0) {
            return;
        }
        for (ItemStack it : accepted) {
            if (it == null || it.getType() == Material.AIR || it.getAmount() <= 0)
                continue;
            o.storage.add(it);
        }
        double receive = (double) acceptedAmount * o.priceEach;
        Player delivererPlayer = Bukkit.getPlayer((UUID) deliverer);
        if (delivererPlayer != null) {
            this.pl.vault().give((OfflinePlayer) delivererPlayer, receive);
        }
        o.delivered += acceptedAmount;
        if (o.delivered >= o.requested) {
            o.completed = true;
        }
        o.paid = Math.max(0.0, o.totalPrice() - (double) o.delivered * o.priceEach);
        this.saveOrder(o);
        this.sendReceiverActionbar(o, deliverer, acceptedAmount);
    }

    private void sendReceiverActionbar(Order o, UUID deliverer, int acceptedAmount) {
        Player receiver = Bukkit.getPlayer((UUID) o.owner);
        if (receiver == null) {
            return;
        }
        TaskUtil.runEntity((Plugin) this.pl, (Entity) receiver, () -> {
            String delivererName = "Someone";
            Player d = Bukkit.getPlayer((UUID) deliverer);
            if (d != null && d.getName() != null) {
                delivererName = d.getName();
            } else {
                OfflinePlayer op = Bukkit.getOfflinePlayer((UUID) deliverer);
                if (op != null && op.getName() != null) {
                    delivererName = op.getName();
                }
            }
            HashMap<String, String> ph = new HashMap<String, String>();
            ph.put("player", delivererName);
            ph.put("amount", String.valueOf(acceptedAmount));
            ph.put("item", o.key.displayName());
            String raw = this.pl.cfg().cfg().getString("messages.received_actionbar",
                    "&a{player} has delivered you {amount} {item}!");
            String msg = Utils.applyPlaceholders(raw, ph);
            msg = Utils.formatColors(msg);
            receiver.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText((String) msg));
        });
    }

    private void loadAll() {
        this.orders.clear();
        if (!ordersDir.exists())
            return;

        File[] files = ordersDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null)
            return;

        for (File f : files) {
            try {
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);

                // The root of the file is the order itself now, not key-nested
                // However, preserving the simpler key-value structure inside might be easier?
                // Actually, cleaner is just to set keys at root.
                // Let's assume the saveOrder method saves at root.

                String idStr = f.getName().replace(".yml", "");
                UUID id;
                try {
                    id = UUID.fromString(idStr);
                } catch (IllegalArgumentException e) {
                    // Try to read 'id' from inside if filename is weird, or skip
                    String internalId = cfg.getString("id");
                    if (internalId != null) {
                        id = UUID.fromString(internalId);
                    } else {
                        pl.getLogger().warning("Skipping invalid order file: " + f.getName());
                        continue;
                    }
                }

                String ownerStr = cfg.getString("owner");
                String itemStr = cfg.getString("item");

                if (ownerStr == null || itemStr == null) {
                    // Attempt legacy migration for format if it was saved strangely?
                    // No, this is fresh load of new files.
                    continue;
                }

                Order o = new Order();
                o.id = id;
                o.owner = UUID.fromString(ownerStr);
                o.key = ItemKey.deserialize(itemStr);
                o.requested = cfg.getInt("requested");
                o.delivered = cfg.getInt("delivered");
                o.priceEach = cfg.getDouble("priceEach");
                o.paid = cfg.getDouble("paid");
                o.canceled = cfg.getBoolean("canceled");
                o.completed = cfg.getBoolean("completed");

                List<?> raw = cfg.getList("storage");
                if (raw != null) {
                    for (Object ois : raw) {
                        if (ois instanceof ItemStack) {
                            o.storage.add((ItemStack) ois);
                        }
                    }
                }
                this.orders.put(o.id, o);
            } catch (Exception ex) {
                this.pl.getLogger().warning("Skipping corrupt order file '" + f.getName() + "': " + ex.getMessage());
            }
        }
    }

    public void saveOrder(Order o) {
        File f = new File(ordersDir, o.id.toString() + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();

        cfg.set("id", o.id.toString()); // Save ID just in case
        cfg.set("owner", o.owner.toString());
        cfg.set("item", o.key.serialize());
        cfg.set("requested", o.requested);
        cfg.set("delivered", o.delivered);
        cfg.set("priceEach", o.priceEach);
        cfg.set("paid", o.paid);
        cfg.set("canceled", o.canceled);
        cfg.set("completed", o.completed);
        cfg.set("storage", o.storage);

        try {
            cfg.save(f);
        } catch (IOException ex) {
            pl.getLogger().severe("Failed to save order " + o.id + ": " + ex.getMessage());
        }
    }

    public void saveAll() {
        for (Order o : this.orders.values()) {
            this.saveOrder(o);
        }
    }

    private void saveRoot() {
        // Deprecated/Unused in new system
    }

    public static String nice(Material m) {
        String s = m.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
        String[] parts = s.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty())
                continue;
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return out.toString().trim();
    }
}
