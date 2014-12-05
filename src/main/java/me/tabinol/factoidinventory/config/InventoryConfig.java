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
package me.tabinol.factoidinventory.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import me.tabinol.factoidapi.FactoidAPI;
import me.tabinol.factoidapi.lands.IDummyLand;
import me.tabinol.factoidapi.parameters.IFlagType;
import me.tabinol.factoidapi.parameters.IFlagValue;
import me.tabinol.factoidinventory.FactoidInventory;
import me.tabinol.factoidinventory.inventories.InventorySpec;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class InventoryConfig {

    public static final String GLOBAL = "Default"; // Means it is assigned to all
    public static final String PERM_USE = "finv.use";
    public static final String PERM_RELOAD = "finv.reload";
    public static final String PERM_FORCESAVE = "finv.forcesave";
    public static final String PERM_DEFAULT = "finv.default";
    public static final String PERM_LOADDEATH = "finv.loaddeath";
    public static final String PERM_IGNORE_CREATIVE_INV = "finv.ignorecreativeinv";
    public static final String PERM_IGNORE_INV = "finv.ignoreinv";
    public static final String PERM_IGNORE_DISABLED_COMMANDS = "finv.ignoredisabledcommands";
    private final IFlagType invFlag; // Registered inventory Flag (Factoid)
    private final FactoidInventory thisPlugin;
    private FileConfiguration config;
    private HashMap<String, InventorySpec> invList; // World-->Land-->Inventory

    public InventoryConfig() {

        thisPlugin = FactoidInventory.getThisPlugin();
        thisPlugin.saveDefaultConfig();

        // Connect to the data file and register flag to Factoid
        invFlag = FactoidAPI.iParameters().registerFlagType("INVENTORY", new String());

        config = thisPlugin.getConfig();
        invList = new HashMap<String, InventorySpec>();
    }

    public void reLoadConfig() {

        thisPlugin.reloadConfig();
        config = thisPlugin.getConfig();
        invList = new HashMap<String, InventorySpec>();
        loadInventory();
    }

    public void loadConfig() {

        loadInventory();
    }

    private void loadInventory() {

        // Load World and Land inventories
        ConfigurationSection configSec = config.getConfigurationSection("Inventories");
        for (Map.Entry<String, Object> invEntry : configSec.getValues(false).entrySet()) {
            if (invEntry.getValue() instanceof ConfigurationSection) {
                boolean isCreativeChange = ((ConfigurationSection) invEntry.getValue()).getBoolean("SeparateCreative", true);
                boolean isSaveInventory = ((ConfigurationSection) invEntry.getValue()).getBoolean("SaveInventory", true);
                boolean isAllowDrop = ((ConfigurationSection) invEntry.getValue()).getBoolean("AllowDrop", true);
                List<String> disabledCommands = ((ConfigurationSection) invEntry.getValue()).getStringList("DisabledCommands");
                createInventoryEntry(invEntry.getKey(), isCreativeChange, isSaveInventory, isAllowDrop, disabledCommands);
            }
        }
    }

    private void createInventoryEntry(String key, boolean creativeChange, boolean saveInventory, boolean allowDrop,
            List<String> disabledCommands) {

        invList.put(key, new InventorySpec(key, creativeChange, saveInventory, allowDrop, disabledCommands));
    }

    public InventorySpec getInvSpec(IDummyLand dummyLand) {

        IFlagValue invFlagValue = dummyLand.getFlagAndInherit(invFlag);
        
        // If the flag is not set
        if(invFlagValue.getValueString().isEmpty()) {
            return invList.get(GLOBAL);
        }
        
        InventorySpec invSpec = invList.get(invFlagValue.getValueString());
        
        // If the flag is set with wrong inventory
        if(invSpec == null) {
            thisPlugin.getLogger().log(Level.WARNING, "Inventory name \"" + invFlagValue.getValueString() + "\" is not found "
                    + "in " + thisPlugin.getName() + "/plugin.yml!");
            return invList.get(GLOBAL);
        }
        
        return invSpec;
    }

    public InventorySpec getFromString(String invName) {

        InventorySpec invSpec = invList.get(invName);
        
        // Tu prevent null pointer
        if(invSpec == null) {
            return invList.get(GLOBAL);
        }
        
        return invSpec;
    }
}
