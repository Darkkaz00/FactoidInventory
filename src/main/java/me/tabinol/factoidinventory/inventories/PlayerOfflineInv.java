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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.tabinol.factoidinventory.FactoidInventory;

import org.bukkit.entity.Player;

public class PlayerOfflineInv {

    public static final int PLAYER_OFFLINE_VERSION = FactoidInventory.getMavenAppProperties().getPropertyInt("PlayerOfflineVersion");
    private final String fileName;
    private final File file;
    private Map<UUID, PlayerInvEntry> playersOfflineList; // Get information for offline player

    public PlayerOfflineInv() {

        fileName = FactoidInventory.getThisPlugin().getDataFolder() + "/" + "playeroffline.conf";
        file = new File(fileName);
    }
    
    public PlayerInvEntry getPlayerOfflineInv(Player player) {
        
        return playersOfflineList.get(player.getUniqueId());
    }
    
    public void putPlayerOfflineInv(Player player, PlayerInvEntry invEntry) {
        
        playersOfflineList.put(player.getUniqueId(), invEntry);
    }
    
    public void loadAll() {

        playersOfflineList = new TreeMap<UUID, PlayerInvEntry>();

        try {
            BufferedReader br;

            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException ex) {
                // Not existing? Trying the first backup
                try {
                    br = new BufferedReader(new FileReader(new File(fileName + ".back.1")));
                } catch (FileNotFoundException ex2) {
                    // Not existing? Nothing to load!
                    return;
                }
            }

            // Security copy
            File actFile = new File(fileName + ".back.9");
            if (actFile.exists()) {
                actFile.delete();
            }
            for (int t = 8; t >= 1; t--) {
                actFile = new File(fileName + ".back." + t);
                if (actFile.exists()) {
                    actFile.renameTo(new File(fileName + ".back." + Integer.toString(t + 1)));
                }
            }

            @SuppressWarnings("unused")
			int version = Integer.parseInt(br.readLine().split(":")[1]);
            
            br.readLine(); // Read remark line

            String str;

            while ((str = br.readLine()) != null && !str.equals("")) {
                // Read from String "PlayerUUID:LastInventoryName:isCreative"
                String[] strs = str.split(":");

                UUID minecraftUUID = UUID.fromString(strs[0]);
                InventorySpec invSpec = FactoidInventory.getConf().getFromString(strs[1]);
                boolean isCreative = Boolean.parseBoolean(strs[2]);
                playersOfflineList.put(minecraftUUID, new PlayerInvEntry(invSpec, isCreative));
            }
            br.close();

            // The if is renamed but will be saved later
            if (!file.getName().endsWith("1")) {
                file.renameTo(new File(fileName + ".back.1"));
            }
        } catch (IOException ex) {
            Logger.getLogger(PlayerOfflineInv.class.getName()).log(Level.SEVERE, "I can't load the players list", ex);
        }

    }

    public void saveAll() {

        try {
            BufferedWriter bw;

            try {
                bw = new BufferedWriter(new FileWriter(file));
            } catch (FileNotFoundException ex) {
                // Not existing? Nothing to load!
                return;
            }

            bw.write("Version:" + PLAYER_OFFLINE_VERSION);
            bw.newLine();
            bw.write("# PlayerUUID:LastInventoryName:isCreative");
            bw.newLine();

            for (Map.Entry<UUID, PlayerInvEntry> PlayerInvEntry : playersOfflineList.entrySet()) {
                // Write to String "PlayerUUID:LastInventoryName:isCreative"
                bw.write(PlayerInvEntry.getKey() + ":" + PlayerInvEntry.getValue().getActualInv().getInventoryName()
                        + ":" + PlayerInvEntry.getValue().isCreativeInv());
                bw.newLine();
            }
            bw.close();

        } catch (IOException ex) {
            Logger.getLogger(PlayerOfflineInv.class.getName()).log(Level.SEVERE, "I can't save the players list", ex);
        }
    }

}
