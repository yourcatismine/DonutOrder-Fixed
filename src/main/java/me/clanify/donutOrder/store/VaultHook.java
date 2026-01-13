/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.milkbowl.vault.economy.Economy
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.plugin.RegisteredServiceProvider
 */
package me.clanify.donutOrder.store;

import me.clanify.donutOrder.DonutOrder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private final DonutOrder plugin;
    private Economy econ;

    public VaultHook(DonutOrder plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            this.econ = (Economy)rsp.getProvider();
        }
    }

    public boolean hooked() {
        return this.econ != null;
    }

    public boolean canAfford(OfflinePlayer p, double amount) {
        if (this.econ == null) {
            return false;
        }
        if (!Double.isFinite(amount)) {
            return false;
        }
        if (amount <= 0.0) {
            return true;
        }
        try {
            return this.econ.has(p, amount);
        }
        catch (Throwable ignored) {
            return this.bal(p) + 1.0E-9 >= amount;
        }
    }

    public boolean take(OfflinePlayer p, double amount) {
        if (this.econ == null) {
            return false;
        }
        if (!Double.isFinite(amount)) {
            return false;
        }
        if (amount <= 0.0) {
            return true;
        }
        if (!this.canAfford(p, amount)) {
            return false;
        }
        try {
            return this.econ.withdrawPlayer(p, amount).transactionSuccess();
        }
        catch (Throwable t) {
            this.plugin.getLogger().warning("Vault withdraw failed: " + t.getMessage());
            return false;
        }
    }

    public boolean give(OfflinePlayer p, double amount) {
        if (this.econ == null) {
            return false;
        }
        if (!Double.isFinite(amount)) {
            return false;
        }
        if (amount <= 0.0) {
            return true;
        }
        try {
            return this.econ.depositPlayer(p, amount).transactionSuccess();
        }
        catch (Throwable t) {
            this.plugin.getLogger().warning("Vault deposit failed: " + t.getMessage());
            return false;
        }
    }

    public double bal(OfflinePlayer p) {
        if (this.econ == null) {
            return 0.0;
        }
        try {
            return this.econ.getBalance(p);
        }
        catch (Throwable t) {
            this.plugin.getLogger().warning("Vault getBalance failed: " + t.getMessage());
            return 0.0;
        }
    }
}

