package net.sf.f3270;

import org.h3270.host.S3270;

public class TerminalWindowObserver extends TerminalObserver {
	private TerminalWindow terminalWindow;

    public void commandIssued(String command, String returned, Param... params) {
        terminalWindow.update(command, returned, params);
    }

    public void connect(S3270 s3270) {
        terminalWindow = new TerminalWindow(s3270);
        terminalWindow.update("new Terminal", null, new Param("hostname", s3270.getHostname()), new Param(
                "port", s3270.getPort()), new Param("type", s3270.getType().getType()), new Param("mode", s3270
                .getMode().getMode()));
    }

    public void disconnect() {
        terminalWindow.close();
    }

}
