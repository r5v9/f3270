package net.sf.f3270;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

public class TerminalScreenToConsoleObserver extends TerminalObserver {
    private Terminal terminal;

    public TerminalScreenToConsoleObserver(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void screenUpdated() {
        terminal.printScreen();
    }

    @Override
    public void commandIssued(String command, String returned, Param... params) {
        String output = MessageFormat.format("{0}({1})", command, StringUtils.join(params, ", "));
        if (returned != null) {
            output += ("=" + returned);
        }
        System.out.println(output);
    }
}
