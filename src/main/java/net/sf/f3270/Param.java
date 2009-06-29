package net.sf.f3270;

public class Param {

    private String name;
    private String value;

    public Param(final String name, final String value) {
        this.name = name;
        this.value = "\"" + value + "\"";
    }

    public Param(final String name, final int value) {
        this.name = name;
        this.value = "" + value;
    }

    public Param(final String name, final boolean value) {
        this.name = name;
        this.value = "" + value;
    }

    public Param(final String name, final Terminal.MatchMode value) {
        this.name = name;
        this.value = "" + value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
