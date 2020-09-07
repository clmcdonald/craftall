package com.clmcdonald.craftall;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.clmcdonald.craftall.commands.CraftAllCommand;
import com.clmcdonald.craftall.services.MaterialsService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class CraftAllTest {

    @Mock
    private CommandSender commandSenderMock;
    @Mock
    private Command commandMock;

    private PluginCommand command;
    private TabCompleter tabCompleter;

    @BeforeEach
    public void beforeEach() {
        MockBukkit.mock();
        CraftAll craftAll = MockBukkit.load(CraftAll.class);

        command = craftAll.getCommand("craftall");
        assertNotNull(command);

        tabCompleter = command.getTabCompleter();
        assertNotNull(tabCompleter);
    }

    @AfterEach
    public void afterEach() {
        MockBukkit.unmock();
    }

    @Test
    public void onEnableShouldSetCommandExecutor() {
        assertTrue(command.getExecutor() instanceof CraftAllCommand);
    }

    @Test
    public void tabCompleterShouldReturnEmptyListWithNoArgs() {
        List<String> suggestions = tabCompleter.onTabComplete(commandSenderMock, commandMock, "", new String[] {});
        assertNotNull(suggestions);
        assertEquals(suggestions.size(), 0);
    }

    @Test
    public void tabCompleterShouldReturnMaterialsForOneArg() {
        Set<String> materials = Stream.of("test1", "test2", "random").collect(Collectors.toSet());

        try (MockedStatic<MaterialsService> materialsServiceMock = mockStatic(MaterialsService.class)) {
            materialsServiceMock.when(MaterialsService::retrieveMaterials).thenReturn(new HashSet<>(materials));

            List<String> suggestions = tabCompleter.onTabComplete(commandSenderMock, commandMock, "", new String[] {""});
            assertNotNull(suggestions);
            assertEquals(materials, new HashSet<>(suggestions));
        }
    }

    @Test
    public void tabCompleterShouldFilterMaterialsForOneArg() {
        Set<String> materials = Stream.of("test1", "test2", "random").collect(Collectors.toSet());

        try (MockedStatic<MaterialsService> materialsServiceMock = mockStatic(MaterialsService.class)) {
            materialsServiceMock.when(MaterialsService::retrieveMaterials).thenReturn(new HashSet<>(materials));

            String arg = "test";
            List<String> suggestions = tabCompleter.onTabComplete(commandSenderMock, commandMock, "", new String[] {arg});

            Set<String> expected = materials.stream().filter((material) -> material.startsWith(arg)).collect(Collectors.toSet());

            assertNotNull(suggestions);
            assertEquals(expected, new HashSet<>(suggestions));
        }
    }

    @Test
    public void tabCompleterShouldReturnNumbersForTwoArgs() {
        List<String> suggestions = tabCompleter.onTabComplete(commandSenderMock, commandMock, "", new String[] {"", ""});

        Set<String> expected = Stream.of("1", "64").collect(Collectors.toSet());

        assertNotNull(suggestions);
        assertEquals(expected, new HashSet<>(suggestions));
    }

    @Test
    public void tabCompleterShouldFilterNumbersForTwoArgs() {
        List<String> suggestions = tabCompleter.onTabComplete(commandSenderMock, commandMock, "", new String[] {"", "6"});

        Set<String> expected = Stream.of("64").collect(Collectors.toSet());

        assertNotNull(suggestions);
        assertEquals(expected, new HashSet<>(suggestions));
    }

    @Test
    public void tabCompleterShouldReturnEmptyForMoreArgs() {
        List<String> suggestions = tabCompleter.onTabComplete(commandSenderMock, commandMock, "", new String[] {"", "", ""});
        assertNotNull(suggestions);
        assertEquals(suggestions.size(), 0);
    }
}
