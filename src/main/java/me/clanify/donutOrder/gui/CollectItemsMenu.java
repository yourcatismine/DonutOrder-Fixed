/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.event.inventory.InventoryAction
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 */
package me.clanify.donutOrder.gui;

import java.util.Iterator;
import java.util.List;
import me.clanify.donutOrder.DonutOrder;
import me.clanify.donutOrder.data.Order;
import me.clanify.donutOrder.gui.EditOrderMenu;
import me.clanify.donutOrder.gui.MenuOwner;
import me.clanify.donutOrder.gui.YourOrdersMenu;
import me.clanify.donutOrder.util.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class CollectItemsMenu
implements InventoryHolder,
MenuOwner {
    private final DonutOrder pl;
    private final Player p;
    private final Order order;
    private Inventory inv;
    private final int requestedPage;
    private int currentPage = 0;
    private boolean internalPageSwitch = false;

    public CollectItemsMenu(DonutOrder pl, Player p, Order order) {
        this(pl, p, order, 0);
    }

    public CollectItemsMenu(DonutOrder pl, Player p, Order order, int page) {
        this.pl = pl;
        this.p = p;
        this.order = order;
        this.requestedPage = Math.max(0, page);
    }

    public Inventory getInventory() {
        return this.inv;
    }

    private int rows() {
        return this.pl.cfg().rows("collect", 6);
    }

    private int perPage() {
        return (this.rows() - 1) * 9;
    }

    private int maxPage() {
        int per = this.perPage();
        return Math.max(0, (this.order.storage.size() - 1) / Math.max(1, per));
    }

    public void open() {
        if (this.order.storage.isEmpty() && this.order.completed) {
            new YourOrdersMenu(this.pl, this.p).open();
            return;
        }
        int rows = this.rows();
        int per = this.perPage();
        int max = this.maxPage();
        this.currentPage = Math.max(0, Math.min(this.requestedPage, max));
        this.inv = Bukkit.createInventory((InventoryHolder)this, (int)(rows * 9), (String)this.pl.cfg().title("collect", "&#44b3ffOrders -> Collect Items"));
        int from = Math.max(0, Math.min(this.order.storage.size(), this.currentPage * per));
        int to = Math.min(this.order.storage.size(), from + per);
        for (int i = from; i < to; ++i) {
            ItemStack st = this.order.storage.get(i);
            if (st == null || st.getType() == Material.AIR) continue;
            this.inv.setItem(i - from, st.clone());
        }
        int prev = (rows - 1) * 9;
        int next = rows * 9 - 1;
        int drop = rows * 9 - 2;
        this.inv.setItem(prev, this.pl.cfg().button("gui.collect.items.prev", "ARROW", "&fPrevious Page", List.of()));
        this.inv.setItem(next, this.pl.cfg().button("gui.collect.items.next", "ARROW", "&fNext Page", List.of()));
        this.inv.setItem(drop, this.pl.cfg().button("gui.collect.items.drop", "DROPPER", "&fDROP LOOT", List.of("&fClick to drop all loot on the page")));
        ItemStack fill = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = fill.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a77 ");
            fill.setItemMeta(meta);
        }
        for (int s = (rows - 1) * 9; s < rows * 9; ++s) {
            if (this.inv.getItem(s) != null) continue;
            this.inv.setItem(s, fill);
        }
        this.p.openInventory(this.inv);
        this.pl.cfg().play(this.p, "sounds.open", "BLOCK_CHEST_OPEN", 0.7f, 1.0f);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTopInventory().getHolder() != this) {
            return;
        }
        int rows = this.rows();
        int prev = (rows - 1) * 9;
        int next = rows * 9 - 1;
        int drop = rows * 9 - 2;
        Inventory top = e.getView().getTopInventory();
        boolean clickedTop = e.getClickedInventory() != null && e.getClickedInventory().equals((Object)top);
        boolean clickedPlayer = e.getClickedInventory() != null && e.getClickedInventory().getHolder() == this.p;
        int slot = e.getSlot();
        if (clickedTop && slot >= (rows - 1) * 9) {
            e.setCancelled(true);
            if (slot == prev) {
                int prevPage = Math.max(0, this.currentPage - 1);
                this.pl.cfg().play(this.p, "sounds.page", "UI_BUTTON_CLICK", 1.0f, 1.1f);
                this.flushCurrentPageToStorage();
                this.pl.orders().saveAll();
                this.internalPageSwitch = true;
                TaskUtil.runEntityLater((Plugin)this.pl, (Entity)this.p, () -> new CollectItemsMenu(this.pl, this.p, this.order, prevPage).open(), 1L);
                return;
            }
            if (slot == next) {
                int nextPage = Math.min(this.maxPage(), this.currentPage + 1);
                this.pl.cfg().play(this.p, "sounds.page", "UI_BUTTON_CLICK", 1.0f, 1.1f);
                this.flushCurrentPageToStorage();
                this.pl.orders().saveAll();
                this.internalPageSwitch = true;
                TaskUtil.runEntityLater((Plugin)this.pl, (Entity)this.p, () -> new CollectItemsMenu(this.pl, this.p, this.order, nextPage).open(), 1L);
                return;
            }
            if (slot == drop) {
                this.dropCurrentPageInGui();
                this.flushCurrentPageToStorage();
                this.pl.orders().saveAll();
                this.pl.cfg().play(this.p, "sounds.click", "UI_BUTTON_CLICK", 1.0f, 1.0f);
                this.internalPageSwitch = true;
                TaskUtil.runEntityLater((Plugin)this.pl, (Entity)this.p, () -> new CollectItemsMenu(this.pl, this.p, this.order, this.currentPage).open(), 1L);
                return;
            }
            return;
        }
        if (clickedPlayer) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
            }
            return;
        }
        if (clickedTop) {
            if (e.getHotbarButton() != -1) {
                e.setCancelled(true);
                return;
            }
            InventoryAction a = e.getAction();
            switch (a) {
                case PLACE_ALL: 
                case PLACE_SOME: 
                case PLACE_ONE: 
                case SWAP_WITH_CURSOR: 
                case HOTBAR_SWAP: 
                case HOTBAR_MOVE_AND_READD: {
                    e.setCancelled(true);
                    return;
                }
                case MOVE_TO_OTHER_INVENTORY: {
                    e.setCancelled(false);
                    return;
                }
            }
            e.setCancelled(false);
            return;
        }
        e.setCancelled(true);
    }

    @Override
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() != this) {
            return;
        }
        int rows = this.rows();
        int topSize = rows * 9;
        int controlStart = (rows - 1) * 9;
        Iterator iterator = e.getRawSlots().iterator();
        while (iterator.hasNext()) {
            int raw = (Integer)iterator.next();
            if (raw >= 0 && raw < controlStart) {
                e.setCancelled(true);
                return;
            }
            if (raw < controlStart || raw >= topSize) continue;
            e.setCancelled(true);
            return;
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != this) {
            return;
        }
        if (this.internalPageSwitch) {
            this.internalPageSwitch = false;
            return;
        }
        this.flushCurrentPageToStorage();
        this.pl.orders().saveAll();
        if (this.order.completed && this.order.storage.isEmpty()) {
            TaskUtil.runEntityLater((Plugin)this.pl, (Entity)this.p, () -> new YourOrdersMenu(this.pl, this.p).open(), 1L);
        } else {
            TaskUtil.runEntityLater((Plugin)this.pl, (Entity)this.p, () -> new EditOrderMenu(this.pl, this.p, this.order).open(), 1L);
        }
    }

    private void flushCurrentPageToStorage() {
        int i;
        if (this.inv == null) {
            return;
        }
        int per = this.perPage();
        int from = this.currentPage * per;
        int maxRem = Math.min(per, Math.max(0, this.order.storage.size() - from));
        for (i = 0; i < maxRem; ++i) {
            if (from >= this.order.storage.size()) continue;
            this.order.storage.remove(from);
        }
        for (i = 0; i < per && i < this.inv.getSize(); ++i) {
            ItemStack cur = this.inv.getItem(i);
            if (cur == null || cur.getType() == Material.AIR || cur.getAmount() <= 0) continue;
            this.order.storage.add(Math.min(from + i, this.order.storage.size()), cur.clone());
        }
        this.order.storage.removeIf(it -> it == null || it.getType() == Material.AIR || it.getAmount() <= 0);
    }

    private void dropCurrentPageInGui() {
        int per = this.perPage();
        Location eye = this.p.getEyeLocation();
        for (int i = 0; i < per; ++i) {
            ItemStack cur = this.inv.getItem(i);
            if (cur == null || cur.getType() == Material.AIR) continue;
            this.inv.clear(i);
            Item drop = this.p.getWorld().dropItem(eye, cur);
            drop.setVelocity(eye.getDirection().multiply(0.25));
        }
    }
}

