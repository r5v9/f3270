package net.sf.f3270;

import net.sf.f3270.impersonator.TN3270Impersonator;
import net.sf.f3270.impersonator.TN3270ProxyRecorder;

import org.h3270.host.S3270.TerminalMode;
import org.h3270.host.S3270.TerminalType;

public abstract class IntegrationTestBase {

    public enum Mode {
        DIRECT, RECORD, REPLAY;
    }

    protected abstract Mode getMode();

    protected abstract String getHostname();

    protected int getPort() {
        return 23;
    }

    protected int getImpersonatorPort() {
        return 2323;
    }

    protected Terminal terminal;
    private TN3270ProxyRecorder recorder;

    protected final void connect() {
        String dataFilePath = this.getClass().getPackage().getName().replace('.', '/') + "/"
                + this.getClass().getSimpleName() + ".txt";

        String hostname = null;
        int port = 0;
        if (getMode() == Mode.RECORD) {
            recorder = new TN3270ProxyRecorder(getImpersonatorPort(), getHostname(), getPort(), "src/integration-tests/java/"
                    + dataFilePath);
            hostname = "127.0.0.1";
            port = getImpersonatorPort();
        }
        if (getMode() == Mode.REPLAY) {
            new TN3270Impersonator(getImpersonatorPort(), dataFilePath);
            hostname = "127.0.0.1";
            port = getImpersonatorPort();
        }
        if (getMode() == Mode.DIRECT) {
            hostname = getHostname();
            port = 23;
        }
        
        String os = System.getProperty("os.name");
        String s3270Path = "s3270";
        if (os.toLowerCase().contains("windows")) {
            s3270Path = "s3270/cygwin/s3270";
        }

        terminal = new Terminal(s3270Path, hostname, port, TerminalType.TYPE_3279, TerminalMode.MODE_80_24, true);
        terminal.connect();
    }

    protected final void disconnect() {
        if (recorder != null) {
            recorder.dump();
        }
    }

    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
