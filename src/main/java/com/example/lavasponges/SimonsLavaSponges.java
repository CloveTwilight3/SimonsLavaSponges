package com.example.lavasponges;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class SimonsLavaSponges extends JavaPlugin implements Listener {

    private static final String LAVA_SPONGE_KEY = "lava_sponge";
    private static final String FILLED_LAVA_SPONGE_KEY = "filled_lava_sponge";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        createLavaSpongeRecipe();
    }

    private void createLavaSpongeRecipe() {
        ItemStack lavaSponge = new ItemStack(Material.SPONGE);
        NamespacedKey key = new NamespacedKey(this, LAVA_SPONGE_KEY);
        ShapedRecipe recipe = new ShapedRecipe(key, lavaSponge);
        recipe.shape(" B ", "BSB", " B ");
        recipe.setIngredient('B', Material.BUCKET);
        recipe.setIngredient('S', Material.SPONGE);
        getServer().addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerUseLavaSponge(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            ItemStack item = event.getItem();

            if (item != null && item.getType() == Material.SPONGE) {
                if (clickedBlock != null && clickedBlock.getType() == Material.LAVA) {
                    removeLavaArea(clickedBlock);
                    item.setType(Material.WET_SPONGE);  // Simulating filled lava sponge
                } else if (clickedBlock != null && clickedBlock.getType() == Material.CAULDRON) {
                    BlockState state = clickedBlock.getState();
                    if (state instanceof org.bukkit.block.data.Levelled levelled) {
                        if (item.getType() == Material.WET_SPONGE) {
                            levelled.setLevel(levelled.getMaximumLevel());  // Fill the cauldron
                            item.setType(Material.SPONGE);  // Reset to dry sponge
                            state.update();
                        }
                    }
                }
            }
        }
    }

    private void removeLavaArea(Block centerBlock) {
        int radius = 2;
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
