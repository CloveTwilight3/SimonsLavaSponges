/*
 * Copyright (c) 2025 Mazey-Jessica Emily Twilight
 * Copyright (c) 2025 UnifiedGaming Systems Ltd (Company Number: 16108983)
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.example.lavasponges;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class SimonsLavaSponges extends JavaPlugin implements Listener {

    private static final String LAVA_SPONGE_KEY = "lava_sponge";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        createLavaSpongeRecipe();
        addFurnaceFuelBehavior();
    }

    private void addFurnaceFuelBehavior() {
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onFurnaceBurn(FurnaceBurnEvent event) {
                ItemStack fuel = event.getFuel();

                getLogger().info("FurnaceBurnEvent triggered.");
                if (fuel != null) {
                    getLogger().info("Fuel type: " + fuel.getType());
                    if (fuel.hasItemMeta()) {
                        getLogger().info("Fuel name: " + fuel.getItemMeta().getDisplayName());
                    }
                }

                if (fuel != null && fuel.hasItemMeta() && "§cFilled Lava Sponge".equals(fuel.getItemMeta().getDisplayName())) {
                    getLogger().info("Recognized Filled Lava Sponge as fuel.");
                    event.setBurnTime(18000); // 900 blocks (18000 ticks)
                    event.setBurning(true); // Ensure the furnace starts burning

                    // Replace the Filled Lava Sponge with a Lava Sponge
                    fuel.setAmount(fuel.getAmount() - 1); // Remove one Filled Lava Sponge
                    ItemStack lavaSponge = createLavaSponge();
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), lavaSponge); // Drop the Lava Sponge
                }
            }

            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getClickedInventory() != null && event.getClickedInventory().getType() == org.bukkit.event.inventory.InventoryType.FURNACE) {
                    getLogger().info("InventoryClickEvent triggered in furnace.");

                    if (event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.FUEL) {
                        ItemStack item = event.getCursor(); // Item being placed in the slot
                        if (item != null && item.hasItemMeta() && "§cFilled Lava Sponge".equals(item.getItemMeta().getDisplayName())) {
                            getLogger().info("Allowing placement of Filled Lava Sponge in fuel slot.");
                            event.setCancelled(false); // Allow placing the custom fuel
                        } else {
                            getLogger().info("Preventing placement of other items in fuel slot.");
                            event.setCancelled(true); // Prevent placing other invalid items
                        }
                    }
                }
            }
        }, this);
    }

    private void createLavaSpongeRecipe() {
        // Create the lava sponge item
        ItemStack lavaSponge = new ItemStack(Material.SPONGE);
        ItemMeta meta = lavaSponge.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Lava Sponge"); // Custom name with color code for gold
            meta.setLore(Arrays.asList("§7A sponge that absorbs lava.", "§7Can dry lava areas."));

            // Add an enchantment for the glow effect
            meta.addEnchant(Enchantment.MULTISHOT, 1, true); // Use any enchantment

            // Hide all enchantment details except for the glow
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            lavaSponge.setItemMeta(meta);
        }

        // Define the recipe for the lava sponge
        NamespacedKey key = new NamespacedKey(this, LAVA_SPONGE_KEY);
        ShapedRecipe recipe = new ShapedRecipe(key, lavaSponge);
        recipe.shape(" B ", "BSB", " B ");
        recipe.setIngredient('B', Material.BUCKET);
        recipe.setIngredient('S', Material.SPONGE);
        getServer().addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerUseLavaSponge(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Block clickedBlock = event.getClickedBlock();

        if (item != null && item.hasItemMeta()) {
            String displayName = item.getItemMeta().getDisplayName();

            // Prevent placing custom Lava Sponge or Filled Lava Sponge
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                if ("§6Lava Sponge".equals(displayName) || "§cFilled Lava Sponge".equals(displayName)) {
                    event.setCancelled(true); // Cancel placement

                    // Interaction logic for Lava Sponge
                    if ("§6Lava Sponge".equals(displayName) && clickedBlock != null && clickedBlock.getType() == Material.LAVA) {
                        removeLavaArea(clickedBlock); // Clear the 3x3 area of lava
                        item.setAmount(item.getAmount() - 1); // Remove one Lava Sponge

                        // Create and give the Filled Lava Sponge
                        ItemStack filledLavaSponge = createFilledLavaSponge();
                        event.getPlayer().getInventory().addItem(filledLavaSponge);
                    }

                    // Interaction logic for Filled Lava Sponge
                    if ("§cFilled Lava Sponge".equals(displayName) && clickedBlock != null && clickedBlock.getType() == Material.CAULDRON) {
                        BlockState state = clickedBlock.getState();
                        if (state instanceof org.bukkit.block.data.Levelled levelled) {
                            if (levelled.getLevel() < levelled.getMaximumLevel()) {
                                levelled.setLevel(levelled.getMaximumLevel()); // Fill the cauldron
                                state.update();

                                // Replace the Filled Lava Sponge with a Lava Sponge
                                item.setAmount(item.getAmount() - 1); // Remove one Filled Lava Sponge
                                ItemStack lavaSponge = createLavaSponge(); // Create a new Lava Sponge item
                                event.getPlayer().getInventory().addItem(lavaSponge); // Add Lava Sponge to inventory
                            }
                        }
                    }
                }
            }
        }
    }

    private ItemStack createLavaSponge() {
        ItemStack lavaSponge = new ItemStack(Material.SPONGE);
        ItemMeta meta = lavaSponge.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Lava Sponge"); // Custom name with gold color
            meta.setLore(Arrays.asList("§7A sponge that absorbs lava.")); // Optional lore
            meta.addEnchant(Enchantment.MULTISHOT, 1, true); // Glow effect
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS); // Hide enchantment details
            lavaSponge.setItemMeta(meta);
        }
        return lavaSponge;
    }

    private ItemStack createFilledLavaSponge() {
        ItemStack filledLavaSponge = new ItemStack(Material.SPONGE);
        ItemMeta meta = filledLavaSponge.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cFilled Lava Sponge"); // Custom name in red
            meta.setLore(Arrays.asList("§7A sponge filled with lava.", "§7Can be used as fuel or emptied into a cauldron."));
            meta.addEnchant(Enchantment.MULTISHOT, 1, true); // Glow effect
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS); // Hide enchantment details
            filledLavaSponge.setItemMeta(meta);
        }
        return filledLavaSponge;
    }

    private void removeLavaArea(Block centerBlock) {
        int radius = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = centerBlock.getRelative(x, y, z);
                    if (block.getType() == Material.LAVA) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }
}
