/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.inventory.InventoryHolder
 */
package me.clanify.donutOrder.gui;

import me.clanify.donutOrder.DonutOrder;
import me.clanify.donutOrder.gui.MenuOwner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener
implements Listener {
    private final DonutOrder plugin;

    public MenuListener(DonutOrder pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (!(inventoryHolder instanceof MenuOwner)) {
            return;
        }
        MenuOwner owner = (MenuOwner)inventoryHolder;
        owner.onClick(e);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (inventoryHolder instanceof MenuOwner) {
            MenuOwner owner = (MenuOwner)inventoryHolder;
            owner.onClose(e);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (!(inventoryHolder instanceof MenuOwner)) {
            return;
        }
        MenuOwner owner = (MenuOwner)inventoryHolder;
        owner.onDrag(e);
    }
}

