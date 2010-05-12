package net.sf.f3270;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

public class TerminalScreenToConsoleObserver extends TerminalObserver {
    private Terminal terminal;
    private String screenContents;

    public TerminalScreenToConsoleObserver(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void screenUpdated() {
        super.screenUpdated();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(stream);
        terminal.printScreen(printStream);
        screenContents = stream.toString();
    }

    @Override
    public void commandIssued(String command, String returned, Parameter... parameters) {
        super.commandIssued(command, returned, parameters);
        String output = MessageFormat.format("{0}({1})", command, StringUtils.join(parameters, ", "));
        if (returned != null) {
            output += ("=" + returned);
        }
        System.out.println(output);
        delayedPrintScreen();
    }

    private void delayedPrintScreen() {
        if (screenContents != null) {
            System.out.println();
            System.out.print(screenContents);
            screenContents = null;
        }
    }
}
