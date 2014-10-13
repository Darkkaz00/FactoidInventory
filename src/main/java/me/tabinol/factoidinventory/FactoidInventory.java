/*
 FactoidInventory: Minecraft plugin for Inventory change (works with Factoid)
 Copyright (C) 2014  Michel Blanchet

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.tabinol.factoidinventory;

import me.tabinol.factoidinventory.config.InventoryConfig;
import me.tabinol.factoidinventory.inventories.InventoryListener;
import me.tabinol.factoidinventory.utils.MavenAppProperties;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class FactoidInventory extends JavaPlugin {

    private static FactoidInventory thisPlugin;
    private static MavenAppProperties mavenAppProperties;
    private static InventoryConfig config;
    private InventoryListener inventoryListener = null;

    @Override
    public void onEnable() {

        thisPlugin = this;

        mavenAppProperties = new MavenAppProperties();
        mavenAppProperties.loadProperties();

        // Config
        config = new InventoryConfig();
        config.loadConfig();

        // Enable InveotryListener
        inventoryListener = new InventoryListener();
        getServer().getPluginManager().registerEvents(inventoryListener, this);
    }

    @Override
    public void onDisable() {

        if (inventoryListener != null) {
            // Save inventories and remove online players
            inventoryListener.removeAndSave();
        }
    }

    public static FactoidInventory getThisPlugin() {

        return thisPlugin;
    }

    public static MavenAppProperties getMavenAppProperties() {

        return mavenAppProperties;
    }

    public static InventoryConfig getConf() {

        return config;
    }
    
    public InventoryListener getInventoryListener() {
        
        return inventoryListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        return new Commands(sender, cmd, commandLabel, args).getComReturn();
    }
}
