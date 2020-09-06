package com.clmcdonald.craftall.domain;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CraftAllPlayerTest {

    @Mock
    Player playerMock;
    @Mock
    PlayerInventory playerInventoryMock;

    @Test
    public void sendMessageShouldFormatAndTranslateColorCodes() {
        String testMessage = "&1{0} &2{1}";
        String arg1 = "test";
        String arg2 = "message";
        String expected = ChatColor.DARK_BLUE + "test " + ChatColor.DARK_GREEN + "message";

        CraftAllPlayer craftAllPlayer = new CraftAllPlayer(playerMock);
        craftAllPlayer.sendMessage(testMessage, arg1, arg2);

        verify(playerMock).sendMessage(expected);
    }

    @Test
    public void sendDebugMessageShouldNotSendMessageIfDebugDisabled() {
        String testMessage = "test-message";
        CraftAllPlayer craftAllPlayer = new CraftAllPlayer(playerMock);
        craftAllPlayer.setDebugEnabled(false);

        craftAllPlayer.sendDebugMessage(testMessage);
        verify(playerMock, times(0)).sendMessage(testMessage);
        assertFalse(craftAllPlayer.isDebugEnabled());
    }

    @Test
    public void sendDebugMessageShouldSendMessageIfDebugEnabled() {
        String testMessage = "test-message";
        CraftAllPlayer craftAllPlayer = new CraftAllPlayer(playerMock);
        craftAllPlayer.setDebugEnabled(true);

        craftAllPlayer.sendDebugMessage(testMessage);
        verify(playerMock, times(1)).sendMessage(testMessage);
        assertTrue(craftAllPlayer.isDebugEnabled());
    }

    @Test
    public void getInventoryShouldGetBukkitPlayerInventory() {
        when(playerMock.getInventory()).thenReturn(playerInventoryMock);
        CraftAllPlayer craftAllPlayer = new CraftAllPlayer(playerMock);

        assertEquals(playerInventoryMock, craftAllPlayer.getInventory().getBukkitInventory());
    }
}
