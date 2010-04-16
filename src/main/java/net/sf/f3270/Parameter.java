package net.sf.f3270;

public class Parameter {

    private String name;
    private String value;

    public Parameter(final String name, final String value) {
        this.name = name;
        this.value = "\"" + value + "\"";
    }

    public Parameter(final String name, final int value) {
        this.name = name;
        this.value = "" + value;
    }

    public Parameter(final String name, final boolean value) {
        this.name = name;
        this.value = "" + value;
    }

    public Parameter(final String name, final MatchMode value) {
        this.name = name;
        this.value = "" + value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
