package net.sf.f3270.impersonator;

public class DataByte {

    public enum Direction {
        CLIENT_TO_SERVER, SERVER_TO_CLIENT
    }

    private Direction direction;
    private int data;

    public DataByte(Direction direction, int data) {
        this.direction = direction;
        this.data = data;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getData() {
        return data;
    }
}
