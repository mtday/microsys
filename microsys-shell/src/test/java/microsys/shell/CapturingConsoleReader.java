package microsys.shell;

import org.mockito.Mockito;

import jline.Terminal;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Used to perform shell testing to capture the output written to the console.
 */
public class CapturingConsoleReader extends ConsoleReader {
    private final List<String> output = new LinkedList<>();
    private final List<String> lines = new LinkedList<>();

    private Optional<String> interrupt = Optional.empty();
    private boolean shutdown = false;

    public CapturingConsoleReader(final String... lines) throws IOException {
        if (lines != null) {
            this.lines.addAll(Arrays.asList(lines));
        }
        super.shutdown();
    }

    public void setInterrupt(final String partialLine) {
        this.interrupt = Optional.ofNullable(partialLine);
    }

    @Override
    public PrintWriter getOutput() {
        return new PrintWriter(new StringWriter()) {
            @Override
            public void write(@Nonnull final String line, final int offset, final int length) {
                if (!"\n".equals(line)) {
                    output.add(line);
                }
            }
        };
    }

    @Override
    public String readLine() {
        if (this.interrupt.isPresent()) {
            final String partial = this.interrupt.get();
            this.interrupt = Optional.empty();
            throw new UserInterruptException(partial);
        }
        if (this.lines.isEmpty()) {
            return null;
        }
        return this.lines.remove(0);
    }

    @Override
    public void print(final CharSequence s) {
        if (s != null) {
            this.output.add(s.toString());
        }
    }

    @Override
    public void println() {
        this.output.add("\n");
    }

    @Override
    public void println(final CharSequence line) {
        if (line != null) {
            this.output.add(line.toString());
        }
    }

    @Override
    public void shutdown() {
        this.shutdown = true;
    }

    @Override
    public Terminal getTerminal() {
        return Mockito.mock(Terminal.class);
    }

    public boolean isShutdown() {
        return this.shutdown;
    }

    public List<String> getOutputLines() {
        return this.output;
    }
}
