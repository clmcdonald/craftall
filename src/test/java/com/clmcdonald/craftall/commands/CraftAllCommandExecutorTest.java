package com.clmcdonald.craftall.commands;

import com.clmcdonald.craftall.domain.CraftAllPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CraftAllCommandExecutorTest {

    @Mock
    private Player playerMock;
    @Mock
    private Command commandMock;
    @Mock
    private CommandSender commandSenderMock;

    private String label;
    private String[] args;

    private CraftAllCommandExecutor commandExecutor;

    @BeforeEach
    public void beforeEach() {
        label = "ca";
        args = new String[] {"arg"};
    }

    @Test
    public void shouldCallOnPlayerCommandForPlayer() {
        commandExecutor = new CraftAllCommandExecutor() {
            @Override
            public boolean onPlayerCommand(CraftAllPlayer player, Command command, String label, String[] args) {
                assertEquals(playerMock, player.getBukkitPlayer());
                return true;
            }

            @Override
            public boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
                fail();
                return false;
            }
        };

        boolean result = commandExecutor.onCommand(playerMock, commandMock, label, args);
        assertTrue(result);
    }

    @Test
    public void shouldCallOnConsoleCommandForNonPlayer() {
        commandExecutor = new CraftAllCommandExecutor() {
            @Override
            public boolean onPlayerCommand(CraftAllPlayer player, Command command, String label, String[] args) {
                fail();
                return false;
            }

            @Override
            public boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
                assertEquals(commandSenderMock, sender);
                assertEquals(commandMock, command);
                return true;
            }
        };

        boolean result = commandExecutor.onCommand(commandSenderMock, commandMock, label, args);
        assertTrue(result);
    }

    @Test
    public void shouldSetDebugTrueIfDebugIsLastParameter() {
        commandExecutor = new CraftAllCommandExecutor() {
            @Override
            public boolean onPlayerCommand(CraftAllPlayer player, Command command, String label, String[] args) {
                assertTrue(player.isDebugEnabled());
                return true;
            }

            @Override
            public boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
                fail();
                return false;
            }
        };

        boolean result = commandExecutor.onCommand(playerMock, commandMock, label, new String[]{"test1", "test2", "debug"});
        assertTrue(result);
    }

    @Test
    public void shouldSetDebugFalseIfNoParameters() {
        commandExecutor = new CraftAllCommandExecutor() {
            @Override
            public boolean onPlayerCommand(CraftAllPlayer player, Command command, String label, String[] args) {
                assertFalse(player.isDebugEnabled());
                return true;
            }

            @Override
            public boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
                fail();
                return false;
            }
        };

        boolean result = commandExecutor.onCommand(playerMock, commandMock, label, new String[]{});
        assertTrue(result);
    }
}
