package microsys.shell.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.Mockito;

import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Perform testing of the {@link ExitCommand} class.
 */
public class ExitCommandTest {
    @Test
    public void testGetRegistrations() {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        final ExitCommand exitCommand = new ExitCommand(shellEnvironment);

        final List<Registration> registrations = exitCommand.getRegistrations();
        assertEquals(2, registrations.size());

        final Registration exit = registrations.get(0);
        assertEquals(new CommandPath("exit"), exit.getPath());
        assertTrue(exit.getDescription().isPresent());
        assertEquals("exit the shell", exit.getDescription().get());
        assertFalse(exit.getOptions().isPresent());

        final Registration quit = registrations.get(1);
        assertEquals(new CommandPath("quit"), quit.getPath());
        assertTrue(quit.getDescription().isPresent());
        assertEquals("exit the shell", quit.getDescription().get());
        assertFalse(quit.getOptions().isPresent());
    }

    @Test
    public void testProcess() {
        final ShellEnvironment shellEnvironment = Mockito.mock(ShellEnvironment.class);
        final ExitCommand exitCommand = new ExitCommand(shellEnvironment);

        final UserCommand userCommand = Mockito.mock(UserCommand.class);
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter, true);

        final CommandStatus status = exitCommand.process(userCommand, writer);

        assertEquals(CommandStatus.TERMINATE, status);
        assertEquals("Terminating", StringUtils.trim(stringWriter.getBuffer().toString()));
    }
}
