package com.clmcdonald.craftall.domain;

import lombok.Data;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.MessageFormat;

/**
 * Contains additional methods that can be applied to a player.
 */
@Data
public class CraftAllPlayer {
    @NonNull
    private Player bukkitPlayer;

    private boolean debugEnabled;

    /**
     * Send a message that will have color codes translated from ampersand
     * and be automatically formatted using MessageFormat.
     * @param message the message to send
     * @param params the parameters to use on the message format
     */
    public void sendMessage(String message, Object... params) {
        String formatted = MessageFormat.format(message, params);
        String colored = ChatColor.translateAlternateColorCodes('&', formatted);
        this.getBukkitPlayer().sendMessage(colored);
    }

    /**
     * Sends a message to the player only if debug messages are enabled.
     * Color codes and message formatting are supported.
     * @param message the message to send
     * @param params the parameters to use on the message format
     */
    public void sendDebugMessage(String message, Object... params) {
        if(debugEnabled) this.sendMessage(message, params);
    }

    /**
     * Get the CraftAllInventory version of the player's inventory
     * @return craft all inventory wrapper
     */
    public CraftAllInventory getInventory() {
        return new CraftAllInventory(this.getBukkitPlayer().getInventory());
    }
}
