package net.sf.f3270;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.h3270.host.Field;
import org.h3270.host.InputField;
import org.h3270.host.S3270;
import org.h3270.host.Screen;
import org.h3270.host.S3270.TerminalMode;
import org.h3270.host.S3270.TerminalType;
import org.h3270.render.TextRenderer;

public class Terminal {

    public enum MatchMode {
        EXACT, EXACT_AFTER_TRIM, REGEX, CONTAINS;
    };

    private static final MatchMode DEFAULT_MATCH_MODE = MatchMode.CONTAINS;

    private S3270 s3270;
    private List<TerminalObserver> observers = new ArrayList<TerminalObserver>();

    private final String s3270Path;
    private final String hostname;
    private final int port;
    private final TerminalType type;
    private final TerminalMode mode;
	private final boolean showTerminalWindow;

    public Terminal(final String s3270Path, final String hostname, final int port, final TerminalType type,
            final TerminalMode mode, final boolean showTerminalWindow) {
        this.s3270Path = s3270Path;
        this.hostname = hostname;
        this.port = port;
        this.type = type;
        this.mode = mode;
		this.showTerminalWindow = showTerminalWindow;
    }

    private void addDefaultObservers() {
        addObserver(new TerminalObserver() {
            public void screenUpdated() {
                printScreen();
            }
        });
        if (showTerminalWindow) {
        	addObserver(new TerminalWindowObserver());
		}
    }

    public void addObserver(TerminalObserver observer) {
        observers.add(observer);
    }

    public Terminal connect() {
        s3270 = new S3270(s3270Path, hostname, port, type, mode);
        updateScreen();
        addDefaultObservers();
        for (TerminalObserver observer : observers) {
            observer.connect(s3270);
        }
        return this;
    }

    public void disconnect() {
        assertConnected();
        s3270.disconnect();
        for (TerminalObserver observer : observers) {
            observer.disconnect();
        }
    }

    private void assertConnected() {
        if (s3270 == null) {
            throw new RuntimeException("not connected");
        }
    }

    private void commandIssued(String command, String returned, Param... params) {
        for (TerminalObserver observer : observers) {
            observer.commandIssued(command, returned, params);
        }
    }

    private void updateScreen() {
        s3270.updateScreen();
        for (TerminalObserver observer : observers) {
            observer.screenUpdated();
        }
    }

    public String getScreenText() {
        assertConnected();
        return new TextRenderer().render(s3270.getScreen());
    }

    public String getLine(final int line) {
        assertConnected();
        final Screen screen = s3270.getScreen();
        final StringBuilder sb = new StringBuilder();
        for (int col = 0; col < screen.getWidth(); col++) {
            sb.append(screen.charAt(col, line));
        }
        return sb.toString();
    }
    
    public int getWidth() {
        return s3270.getScreen().getWidth();
    }
    
    public int getHeight() {
        return s3270.getScreen().getHeight();
    }

    public void enter() {
        assertConnected();
        s3270.submitScreen();
        s3270.enter();
        updateScreen();
        commandIssued("enter", null);
    }

    public void pf(final int n) {
        assertConnected();
        s3270.pf(n);
        updateScreen();
        commandIssued("pf", null, new Param("n", n));
    }

    public void pa(final int n) {
        assertConnected();
        s3270.pa(n);
        updateScreen();
    }
    
    public void clear() {
        assertConnected();
        s3270.clear();
        updateScreen();
        commandIssued("clear", null);
    }

    public void type(final String text) {
        assertConnected();
        final InputField f = s3270.getScreen().getFocusedField();
        f.setValue(text);
        commandIssued("type", null, new Param("text", text));
    }

    public void write(final String label, final String value) {
        write(label, value, 1, DEFAULT_MATCH_MODE);
    }

    public void write(final String label, final String value, final MatchMode matchMode) {
        write(label, value, 1, matchMode);
    }

    public void write(final String label, final String value, final int skip) {
        write(label, value, skip, DEFAULT_MATCH_MODE);
    }

    public void write(final String label, final String value, final int skip, final MatchMode matchMode) {
        write(label, value, skip, 1, matchMode);
    }

    public void write(final String label, final String value, final int skip, final int matchNumber,
            final MatchMode matchMode) {
        assertConnected();
        final Field f = fieldAfterLabel(label, skip, matchNumber, matchMode);
        if (!(f instanceof InputField)) {
            throw new RuntimeException(String.format("field [%s] after match [%d] for [%s] with skip [%d] found with"
                    + " match mode [%s] is not an input field", f.getValue().trim(), matchNumber, label, skip,
                    matchMode));
        }
        s3270.getScreen().getInputFieldAt(f.getStartX(), f.getStartY()).setValue(value);

        commandIssued("write", null, buildParams(label, value, skip, matchNumber, matchMode));
    }

    public String read(final String label) {
        return read(label, 1, DEFAULT_MATCH_MODE);
    }

    public String read(final String label, final int skip) {
        return read(label, skip, DEFAULT_MATCH_MODE);
    }

    public String read(final String label, final MatchMode matchMode) {
        return read(label, 1, matchMode);
    }

    public String read(final String label, final int skip, final MatchMode matchMode) {
        return read(label, skip, 1, matchMode);
    }

    public String read(final String label, final int skip, final int matchNumber, final MatchMode matchMode) {
        assertConnected();
        final Field f = fieldAfterLabel(label, skip, matchNumber, matchMode);
        commandIssued("read", f.getValue(), buildParams(label, null, skip, matchNumber, matchMode));
        return f.getValue();
    }

    private Param[] buildParams(final String label, final String value, final int skip, final int matchNumber,
            final MatchMode matchMode) {
        final List<Param> params = new ArrayList<Param>();
        params.add(new Param("label", label));
        if (value != null) {
            params.add(new Param("value", value.replace('\u0000', ' ')));
        }
        if (skip != 1) {
            params.add(new Param("skip", skip));
        }
        if (matchNumber != 1) {
            params.add(new Param("matchNumber", matchNumber));
        }
        if (matchMode != DEFAULT_MATCH_MODE) {
            params.add(new Param("matchMode", matchMode));
        }
        final Param[] paramsArray = new Param[params.size()];
        return params.toArray(paramsArray);
    }

    public Field fieldAfterLabel(final String label, final int skip, final int matchNumber, final MatchMode matchMode) {
        assertConnected();
        final List<Field> fields = s3270.getScreen().getFields();
        final int i = getFieldIndex(label, matchNumber, matchMode);
        if (i == -1) {
            throw new RuntimeException(String.format("field [%s] could not be found using match mode [%s]", label,
                    matchMode));
        }
        final int index = i + skip;
        if (index >= fields.size()) {
            throw new RuntimeException(String.format("field [%s] at index [%i] plus skip [%i]"
                    + " exceed the number of available fields in the screen [%i]", label, i, skip, index));
        }
        return fields.get(index);
    }

    public int getFieldIndex(final String label, final int matchNumber, final MatchMode matchMode) {
        assertConnected();
        final List<Field> fields = s3270.getScreen().getFields();
        int matches = 0;
        for (int i = 0; i < fields.size(); i++) {
            final String value = fields.get(i).getValue().toLowerCase();
            if (match(value, label.toLowerCase(), matchMode)) {
                matches++;
                if (matches == matchNumber) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean match(final String value, final String label, final MatchMode matchMode) {
        return matchExact(value, label, matchMode) || matchExactAfterTrim(value, label, matchMode)
                || matchRegex(value, label, matchMode) || matchContains(value, label, matchMode);
    }

    private boolean matchExact(final String value, final String label, final MatchMode matchMode) {
        return matchMode == MatchMode.EXACT && value.equals(label);
    }

    private boolean matchExactAfterTrim(final String value, final String label, final MatchMode matchMode) {
        return matchMode == MatchMode.EXACT_AFTER_TRIM && value.trim().equals(label);
    }

    private boolean matchRegex(final String value, final String label, final MatchMode matchMode) {
        return matchMode == MatchMode.REGEX && value.matches(label);
    }

    private boolean matchContains(final String value, final String label, final MatchMode matchMode) {
        return matchMode == MatchMode.CONTAINS && value.contains(label);
    }

    public void printFields() {
        assertConnected();
        final List<Field> fields = s3270.getScreen().getFields();
        for (int i = 0; i < fields.size(); i++) {
            final String value = fields.get(i).getValue();
            println(String.format("%d=[%s]", i, value));
        }
    }

    public void printScreen() {
        assertConnected();
        final String[] lines = getScreenText().split("\n");
        final String sep = "+--------------------------------------------------------------------------------+";
        final String blanks = "                                                                                ";
        println(sep);
        for (String line : lines) {
            final String fixedLine = (line + blanks).substring(0, 80);
            println(String.format("|%s|", fixedLine));
        }
        println(sep);
    }

    private void println(final String s) {
        final PrintStream out = System.out;
        out.println(s);
    }

}
