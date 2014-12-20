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
package me.tabinol.factoidinventory.inventories;

import me.tabinol.factoidapi.FactoidAPI;
import me.tabinol.factoidapi.event.LandModifyEvent;
import me.tabinol.factoidapi.event.PlayerLandChangeEvent;
import me.tabinol.factoidapi.event.LandModifyEvent.LandModifyReason;
import me.tabinol.factoidapi.lands.IDummyLand;
import me.tabinol.factoidinventory.FactoidInventory;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class InventoryListener implements Listener {

    private final InventoryStorage inventoryStorage;

    public InventoryListener() {

        inventoryStorage = new InventoryStorage();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        inventoryStorage.switchInventory(player,
                getDummyLand(player.getLocation()), player.getGameMode() == GameMode.CREATIVE, InventoryStorage.PlayerAction.JOIN);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {

        removePlayer(event.getPlayer());
    }

    /**
     * Called when there is a shutdown
     */
    public void removeAndSave() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            removePlayer(player);
        }
    }

    public void forceSave() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            inventoryStorage.saveInventory(player, null, true, true, false, false, false);
            InventorySpec invSpec = FactoidInventory.getConf().getInvSpec(getDummyLand(player.getLocation()));
            inventoryStorage.saveInventory(player, invSpec.getInventoryName(),
                    player.getGameMode() == GameMode.CREATIVE, false, false, false, false);
        }
    }

    public void removePlayer(Player player) {

        inventoryStorage.switchInventory(player,
                getDummyLand(player.getLocation()), player.getGameMode() == GameMode.CREATIVE, InventoryStorage.PlayerAction.QUIT);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PlayerGameModeChange(PlayerGameModeChangeEvent event) {

        Player player = event.getPlayer();

        inventoryStorage.switchInventory(player,
                getDummyLand(player.getLocation()), event.getNewGameMode() == GameMode.CREATIVE, InventoryStorage.PlayerAction.CHANGE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLandChange(PlayerLandChangeEvent event) {

        Player player = event.getPlayer();
        
        inventoryStorage.switchInventory(player,
                event.getLandOrOutside(), player.getGameMode() == GameMode.CREATIVE, InventoryStorage.PlayerAction.CHANGE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLandModify(LandModifyEvent event) {


        LandModifyReason reason = event.getLandModifyReason();
    	
    	// Test to be specific (take specific players)
        if(reason == LandModifyReason.AREA_ADD || reason == LandModifyReason.AREA_REMOVE
    			|| reason == LandModifyReason.AREA_REPLACE) {
        	
        	// Land area change, all players in the world affected
        	for(Player player : event.getLand().getWorld().getPlayers()) {
                inventoryStorage.switchInventory(player,
                		FactoidAPI.iLands().getLandOrOutsideArea(player.getLocation()), 
                		player.getGameMode() == GameMode.CREATIVE, InventoryStorage.PlayerAction.CHANGE);
            }
    	} else if(reason != LandModifyReason.PERMISSION_SET && reason != LandModifyReason.PERMISSION_SET
    			&& reason != LandModifyReason.RENAME) {
    	
    		// No land resize or area replace, only players in the land affected
    		for(Player player : event.getLand().getPlayersInLandAndChildren()) {
                inventoryStorage.switchInventory(player,
                		FactoidAPI.iLands().getLandOrOutsideArea(player.getLocation()), 
                		player.getGameMode() == GameMode.CREATIVE, InventoryStorage.PlayerAction.CHANGE);
    		}
    	}
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        inventoryStorage.switchInventory(player,
                getDummyLand(player.getLocation()), player.getGameMode() == GameMode.CREATIVE, InventoryStorage.PlayerAction.DEATH);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        Player player = event.getPlayer();
        PlayerInvEntry entry = inventoryStorage.getPlayerInvEntry(player);

        // For Citizens bugfix
        if(entry == null) {
        	return;
        }
        
        // Cancel if the world is no drop
        InventorySpec invSpec = entry.getActualInv();

        if (!invSpec.isAllowDrop()) {
            event.setCancelled(true);
        }
    }
    
    // On player death, prevent drop
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        
    	// Not a player
        if(event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        PlayerInvEntry invEntry = inventoryStorage.getPlayerInvEntry(player);

        // Is from Citizens plugin
        if(invEntry == null) {
        	return;
        }
        
        // Cancel if the world is no drop at death
        InventorySpec invSpec = invEntry.getActualInv();

        if (!invSpec.isAllowDrop()) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
        
        
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        
        Player player = event.getPlayer();
        
        if(inventoryStorage.getPlayerInvEntry(player).getActualInv().isDisabledCommand(event.getMessage().substring(1).split(" ")[0])) {
            event.setCancelled(true);
        }
    }

    public IDummyLand getDummyLand(Location location) {
        
        return FactoidAPI.iLands().getLandOrOutsideArea(location);
    }
    
    public PlayerInvEntry getPlayerInvEntry(Player player) {

        return inventoryStorage.getPlayerInvEntry(player);
    }

    public boolean loadDeathInventory(Player player, int deathVersion) {

        InventorySpec invSpec = inventoryStorage.getPlayerInvEntry(player).getActualInv();

        return inventoryStorage.loadInventory(player, invSpec.getInventoryName(), player.getGameMode() == GameMode.CREATIVE, true, deathVersion);
    }

    public void saveDefaultInventory(Player player, InventorySpec invSpec) {

        inventoryStorage.saveInventory(player, invSpec.getInventoryName(),
                player.getGameMode() == GameMode.CREATIVE, false, true, true, false);
    }
}
