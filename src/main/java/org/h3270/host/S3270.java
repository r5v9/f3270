package org.h3270.host;

/*
 * Copyright (C) 2003-2006 akquinet framework solutions
 *
 * This file is part of h3270.
 *
 * h3270 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * h3270 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with h3270; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston,
 * MA 02110-1301 USA
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * A Terminal that connects to the host via s3270.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: S3270.java,v 1.26 2007/03/02 09:37:34 spiegel Exp $
 */
public class S3270 {

    public enum TerminalMode {
        MODE_80_24(2), MODE_80_32(3), MODE_80_43(4), MODE_132_27(5);
        private int mode;

        private TerminalMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }
    }

    public enum TerminalType {
        TYPE_3278("3278"), TYPE_3279("3279");
        private String type;

        private TerminalType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    private static final Logger logger = Logger.getLogger(S3270.class);

    private final String s3270Path;
    private String hostname;
    private final int port;
    private final TerminalType type;
    private final TerminalMode mode;

    private S3270Screen screen = null;

    /**
     * The subprocess that does the actual communication with the host.
     */
    private Process s3270 = null;

    /**
     * Used to send commands to the s3270 process.
     */
    private PrintWriter out = null;

    /**
     * Used for reading input from the s3270 process.
     */
    private BufferedReader in = null;

    /**
     * A thread that does a blocking read on the error stream from the s3270 process.
     */
    private ErrorReader errorReader = null;

    /**
     * Constructs a new S3270 object. The s3270 subprocess (which does the communication with the host) is immediately
     * started and connected to the target host. If this fails, the constructor will throw an appropriate exception.
     * 
     * @param hostname
     *            the name of the host to connect to
     * @param configuration
     *            the h3270 configuration, derived from h3270-config.xml
     * @throws org.h3270.host.UnknownHostException
     *             if <code>hostname</code> cannot be resolved
     * @throws org.h3270.host.HostUnreachableException
     *             if the host cannot be reached
     * @throws org.h3270.host.S3270Exception
     *             for any other error not matched by the above
     */
    public S3270(final String s3270Path, final String hostname, final int port, final TerminalType type,
            final TerminalMode mode) {

        this.s3270Path = s3270Path;
        this.hostname = hostname;
        this.port = port;
        this.type = type;
        this.mode = mode;
        this.screen = new S3270Screen();

        checkS3270PathValid(s3270Path);

        final String commandLine = String.format("%s -model %s-%d %s:%d", s3270Path, type.getType(), mode.getMode(),
                hostname, port);
        try {
            logger.info("starting " + commandLine);
            s3270 = Runtime.getRuntime().exec(commandLine);

            out = new PrintWriter(new OutputStreamWriter(s3270.getOutputStream(), "ISO-8859-1"));
            in = new BufferedReader(new InputStreamReader(s3270.getInputStream(), "ISO-8859-1"));
            errorReader = new ErrorReader();
            errorReader.start();

            waitFormat();
        } catch (final IOException ex) {
            throw new RuntimeException("IO Exception while starting s3270", ex);
        }
    }

    private void checkS3270PathValid(String path) {
        try {
            Runtime.getRuntime().exec(path + " -v");
        } catch (Exception e) {
            throw new RuntimeException("could not find s3270 executable in the path");
        }
    }

    private void assertConnected() {
        if (s3270 == null) {
            throw new RuntimeException("not connected");
        }
    }

    public String getS3270Path() {
        return s3270Path;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public TerminalType getType() {
        return type;
    }

    public TerminalMode getMode() {
        return mode;
    }

    /**
     * Represents the result of an s3270 command.
     */
    private class Result {
        private final List<String> data;
        private final String status;

        public Result(final List<String> data, final String status) {
            this.data = data;
            this.status = status;
        }

        public List<String> getData() {
            return data;
        }

        public String getStatus() {
            return status;
        }
    }

    /**
     * Perform an s3270 command. All communication with s3270 should go via this method.
     */
    public Result doCommand(final String command) {
        assertConnected();
        try {
            out.println(command);
            out.flush();
            if (logger.isDebugEnabled()) {
                logger.debug("---> " + command);
            }

            final List<String> lines = new ArrayList<String>();
            while (true) {
                final String line = in.readLine();
                if (line == null) {
                    checkS3270Process(); // will throw appropriate exception
                    // if we get here, it's a more obscure error
                    throw new RuntimeException("s3270 process not responding");
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("<--- " + line);
                }

                if (line.equals("ok")) {
                    break;
                }
                lines.add(line);
            }
            final int size = lines.size();
            if (size > 0) {
                return new Result(lines.subList(0, size - 1), (String) lines.get(size - 1));
            } else {
                throw new RuntimeException("no status received in command: " + command);
            }
        } catch (final IOException ex) {
            throw new RuntimeException("IOException during command: " + command, ex);
        }
    }

    /**
     * Performs a blocking read on the s3270 error stream. We do this asynchronously, because otherwise the error
     * message might already be lost when we get a chance to look for it. The message is kept in the instance variable
     * <code>message</code> for later retrieval.
     */
    private class ErrorReader extends Thread {
        private String message = null;

        public void run() {
            final BufferedReader err = new BufferedReader(new InputStreamReader(s3270.getErrorStream()));
            try {
                while (true) {
                    final String msg = err.readLine();
                    if (msg == null) {
                        break;
                    }
                    message = msg;
                }
            } catch (final IOException ex) {
                // ignore
            }
        }
    }

    private static final Pattern unknownHostPattern = Pattern.compile(
    // This message is hard-coded in s3270 as of version 3.3.5,
            // so we can rely on it not being localized.
            "Unknown host: (.*)");
    private static final Pattern unreachablePattern = Pattern.compile(
    // This is the hard-coded part of the error message in s3270 version 3.3.5.
            "Connect to ([^,]+), port ([0-9]+): (.*)");

    /**
     * Checks whether the s3270 process is still running, and if it isn't, tries to determine the cause why it failed.
     * This method throws an exception of appropriate type to indicate what went wrong.
     */
    private void checkS3270Process() {
        // Ideally, we'd like to call Process.waitFor() with a timeout,
        // but that is so complicated to implement that we take a
        // second-rate approach: wait a little while, and then check if
        // the process is already terminated.
        try {
            Thread.sleep(100);
        } catch (final InterruptedException ex) {
        }
        try {
            final int exitValue = s3270.exitValue();
            final String message = errorReader.message;
            if (exitValue == 1 && message != null) {
                Matcher m = unknownHostPattern.matcher(message);
                if (m.matches()) {
                    throw new UnknownHostException(m.group(1));
                } else {
                    m = unreachablePattern.matcher(message);
                    if (m.matches()) {
                        throw new HostUnreachableException(m.group(1), m.group(3));
                    }
                }
                throw new S3270Exception("s3270 terminated with code " + exitValue + ", message: "
                        + errorReader.message);
            }
        } catch (final IllegalThreadStateException ex) {
            // we get here if the process has still been running in the
            // call to s3270.exitValue() above
            throw new S3270Exception("s3270 not terminated, error: " + errorReader.message);
        }
    }

    /**
     * waits for a formatted screen
     */
    private void waitFormat() {
        for (int i = 0; i < 50; i++) {
            final Result r = doCommand("");
            if (r.getStatus().startsWith("U F")) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (final InterruptedException ex) {
            }
        }
    }

    public void disconnect() {
        assertConnected();
        out.println("quit");
        out.flush();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                    if (s3270 != null) {
                        s3270.destroy();
                    }
                } catch (final InterruptedException ex) {
                    if (s3270 != null) {
                        s3270.destroy();
                    }
                }
            }
        }).start();

        try {
            s3270.waitFor();
        } catch (final InterruptedException ex) { /* ignore */
        }
        try {
            in.close();
        } catch (final IOException ex) { /* ignore */
        }
        out.close();
        in = null;
        out = null;
        s3270 = null;
    }

    public boolean isConnected() {
        if (s3270 == null || in == null || out == null) {
            return false;
        } else {
            final Result r = doCommand("");
            if (r.getStatus().matches(". . . C.*")) {
                return true;
            } else {
                out.println("quit");
                out.flush();
                s3270.destroy();
                s3270 = null;
                in = null;
                out = null;
                return false;
            }
        }
    }

    public void dumpScreen(final String filename) {
        assertConnected();
        screen.dump(filename);
    }

    /**
     * Updates the screen object with s3270's buffer data.
     */
    public void updateScreen() {
        assertConnected();
        while (true) {
            final Result r = doCommand("readbuffer ascii");
            if (r.getData().size() > 0) {
                final String firstLine = (String) r.getData().get(0);
                if (firstLine.startsWith("data: Keyboard locked")) {
                    continue;
                }
            }
            screen.update(r.getStatus(), r.getData());
            break;
        }
    }

    public Screen getScreen() {
        assertConnected();
        return screen;
    }

    /**
     * Writes all changed fields back to s3270.
     */
    public void submitScreen() {
        assertConnected();
        for (final Iterator<Field> i = screen.getFields().iterator(); i.hasNext();) {
            final Field f = i.next();
            if ((f instanceof InputField) && ((InputField) f).isChanged()) {
                doCommand("movecursor (" + f.getStartY() + ", " + f.getStartX() + ")");
                doCommand("eraseeof");
                final String value = f.getValue();
                for (int j = 0; j < value.length(); j++) {
                    final char ch = value.charAt(j);
                    if (ch == '\n') {
                        doCommand("newline");
                    } else if (!Integer.toHexString(ch).equals("0")) {
                        doCommand("key (0x" + Integer.toHexString(ch) + ")");
                    }
                }
            }
        }
    }

    public void submitUnformatted(final String data) {
        assertConnected();
        int index = 0;
        for (int y = 0; y < screen.getHeight() && index < data.length(); y++) {
            for (int x = 0; x < screen.getWidth() && index < data.length(); x++) {
                final char newCh = data.charAt(index);
                if (newCh != screen.charAt(x, y)) {
                    doCommand("movecursor (" + y + ", " + x + ")");
                    if (!Integer.toHexString(newCh).equals("0")) {
                        doCommand("key (0x" + Integer.toHexString(newCh) + ")");
                    }
                }
                index++;
            }
            index++; // skip newline
        }
    }

    // s3270 actions below this line

    public void clear() {
        doCommand("clear");
    }

    public void enter() {
        doCommand("enter");
        waitFormat();
    }

    public void tab() {
        doCommand("tab");
    }

    public void newline() {
        doCommand("newline");
        waitFormat();
    }

    public void eraseEOF() {
        doCommand("eraseEOF");
    }

    public void pa(final int number) {
        doCommand("pa(" + number + ")");
        waitFormat();
    }

    public void pf(final int number) {
        doCommand("pf(" + number + ")");
        waitFormat();
    }

    public void reset() {
        doCommand("reset");
    }

    public void sysReq() {
        doCommand("sysReq");
    }

    public void attn() {
        doCommand("attn");
    }

    private static final Pattern FUNCTION_KEY_PATTERN = Pattern.compile("p(f|a)([0-9]{1,2})");

    @SuppressWarnings("unchecked")
    public void doKey(final String key) {
        assertConnected();
        final Matcher m = FUNCTION_KEY_PATTERN.matcher(key);
        if (m.matches()) { // function key
            final int number = Integer.parseInt(m.group(2));
            if (m.group(1).equals("f")) {
                this.pf(number);
            } else {
                this.pa(number);
            }
        } else if (key.equals("")) {
            // use ENTER as a default action if the actual key got lost
            this.enter();
        } else { // other key: find a parameterless method of the same name
            try {
                final Class c = this.getClass();
                final Method method = c.getMethod(key, new Class[] {});
                method.invoke(this, new Object[] {});
            } catch (final NoSuchMethodException ex) {
                throw new IllegalArgumentException("no such key: " + key);
            } catch (final IllegalAccessException ex) {
                throw new RuntimeException("illegal s3270 method access for key: " + key);
            } catch (final InvocationTargetException ex) {
                throw new RuntimeException("error invoking s3270 for key: " + key + ", exception: "
                        + ex.getTargetException());
            }
        }
    }

}