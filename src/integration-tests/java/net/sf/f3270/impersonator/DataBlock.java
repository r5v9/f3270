package net.sf.f3270.impersonator;

import java.util.Arrays;

public class DataBlock {

    private int[] in;
    private int[] out;

    public DataBlock(int[] in, int[] out) {
        this.in = in;
        this.out = out;
    }

    public int[] getIn() {
        return in;
    }

    public int[] getOut() {
        return out;
    }

    @Override
    public String toString() {
        return "{in:" + Arrays.toString(in) + " out:" + Arrays.toString(out) + "}";
    }

}
