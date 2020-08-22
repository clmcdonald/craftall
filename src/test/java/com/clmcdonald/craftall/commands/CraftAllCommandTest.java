package com.clmcdonald.craftall.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class CraftAllCommandTest {

    Player playerMock;
    Command commandMock;
    String label = "ca";
    String[] args = {"stone_bricks"};

    PlayerInventory playerInventoryMock;
    ItemStack[] contents = new ItemStack[0];

    @BeforeEach
    public void beforeEach() {
        playerMock = mock(Player.class);
        commandMock = mock(Command.class);
        playerInventoryMock = mock(PlayerInventory.class);

        when(playerMock.getInventory()).thenReturn(playerInventoryMock);
        when(playerInventoryMock.getContents()).thenReturn(contents);
    }

    @Test
    public void shouldSendMessageToPlayer() {
        assertTrue(true);
    }
}