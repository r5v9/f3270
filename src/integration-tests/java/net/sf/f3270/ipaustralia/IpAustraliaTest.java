package net.sf.f3270.ipaustralia;

import static org.junit.Assert.*;

import net.sf.f3270.FieldIdentifier;
import net.sf.f3270.IntegrationTestBase;
import net.sf.f3270.MatchMode;
import net.sf.f3270.Terminal;

import org.junit.Test;

public class IpAustraliaTest extends IntegrationTestBase {

    public String getHostname() {
        return "pericles.ipaustralia.gov.au";
    }

    public Mode getMode() {
        return Mode.REPLAY;
    }

    @Test
    public void testIpAustralia() {
        connect();

        assertText(terminal, "A U S T R A L I A");
        terminal.enter();
        assertText(terminal, "DISCLAIMER");
        terminal.enter();
        assertText(terminal, "Logon in progress...");
        sleep(100);
        terminal.enter();
        assertEquals(Boolean.TRUE, (Boolean)terminal.screenHasLabel(new FieldIdentifier("command")));
        assertEquals(Boolean.FALSE, (Boolean)terminal.screenHasLabel(new FieldIdentifier("rubbish_label")));
        terminal.write(new FieldIdentifier("command"), "1");
        terminal.read(new FieldIdentifier("command"));
        terminal.enter();
        terminal.enter();
        terminal.write(new FieldIdentifier("command"), "2");
        terminal.enter();
        terminal.write(new FieldIdentifier("trade mark number"), "123");

        disconnect();
    }

    private void assertText(Terminal terminal, String text) {
        assertTrue("screen doesn't contain " + text, terminal.getScreenText().contains(text));
    }

}
