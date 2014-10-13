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

import java.io.File;

import me.tabinol.factoidinventory.config.InventoryConfig;
import me.tabinol.factoidinventory.inventories.InventorySpec;
import static me.tabinol.factoidinventory.inventories.InventoryStorage.DEFAULT_INV;
import static me.tabinol.factoidinventory.inventories.InventoryStorage.INV_DIR;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {

    private boolean comReturn;
    private final CommandSender sender;
    private final String[] args;

    public Commands(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        comReturn = true;
        this.sender = sender;
        this.args = args;

        if (args.length == 0) {
            sender.sendMessage("Possible commands are:");
            sender.sendMessage("/finv loaddeath <player name> [(1-9)] : Reload a player inventory before death, 1 = last death");
            sender.sendMessage("/finv default <save/remove>: Save (or remove) your inventory has the default inventory for players");
            sender.sendMessage("/finv forcesave : Save configuration and inventories");
            sender.sendMessage("/finv reload : Reload configuration");
            return;
        }

        if (args[0].equalsIgnoreCase("default")) {
            if(permissionOk(InventoryConfig.PERM_DEFAULT)) {
            saveDefault();
            }
        } else if (args[0].equalsIgnoreCase("loaddeath")) {
            if(permissionOk(InventoryConfig.PERM_LOADDEATH)) {
            loadDeath();
            }
        } else if (args[0].equalsIgnoreCase("forcesave")) {
            if(permissionOk(InventoryConfig.PERM_FORCESAVE)) {
            forceSave();
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if(permissionOk(InventoryConfig.PERM_RELOAD)) {
            reload();
            }
        } else {
            sender.sendMessage("Invalid argument! type: \"/finv\" for information.");
        }
    }

    private boolean permissionOk(String perm) {
        
        if(sender.hasPermission(perm)) {
            return true;
        } else {
            sender.sendMessage("You don't have the permission!");
            return false;
        }
    }
    
    private void saveDefault() {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is not available from console.");
        }
        
        Player player = (Player) sender;
        
        // Get the land name
        InventorySpec invSpec = FactoidInventory.getThisPlugin().getInventoryListener().getPlayerInvEntry(player).getActualInv();
        
        if(args.length >= 1 && args[1].equalsIgnoreCase("save")) {

            // Save the inventory
            FactoidInventory.getThisPlugin().getInventoryListener().saveDefaultInventory(player, invSpec);
            sender.sendMessage("Dafault inventory saved.");
            
        } else if(args.length >= 1 && args[1].equalsIgnoreCase("remove")) {
            
            // Remove inventory
            new File(FactoidInventory.getThisPlugin().getDataFolder() 
                    + "/" + INV_DIR + "/" + invSpec.getInventoryName() + "/" + DEFAULT_INV + ".yml").delete();
            sender.sendMessage("Dafault inventory removed.");
        
        } else {
            
            // Bad parameter
            sender.sendMessage("You want to \"save\" or \"remove\" the default inventory?");
        }
    }

    private void loadDeath() {

        if (args.length < 2) {
            sender.sendMessage("You must specify a player!");
            return;
        }

        // Check for player
        
        @SuppressWarnings("deprecation")
		Player player = Bukkit.getPlayer(args[1]);
        
        if (player == null) {
            sender.sendMessage("The player must be online!");
            return;
        }

        // Check for the last time version
        int lastTime;
        if (args.length > 2) {
            try {
                lastTime = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                sender.sendMessage("Number unreadable!");
                return;
            }
        } else {
            lastTime = 1;
        }

        // Execute
        if (!FactoidInventory.getThisPlugin().getInventoryListener().loadDeathInventory(player, lastTime)) {
                sender.sendMessage("This death save is not found or the player is at the wrong place!");
                return;
        }
        
        sender.sendMessage("Reload inventory done for \"" + player.getName() + "\"!");
    }
    
    private void forceSave() {
        FactoidInventory.getThisPlugin().getInventoryListener().forceSave();
        sender.sendMessage("Save done!");
    }
    
    private void reload() {
        FactoidInventory.getConf().reLoadConfig();
        sender.sendMessage("Reload done!");
        
    }

public boolean getComReturn() {
        
        return comReturn;
    }
}
