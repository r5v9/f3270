package net.sf.f3270.impersonator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class TN3270Impersonator {

    Logger log = Logger.getLogger(TN3270Impersonator.class);

    public static void main(String[] args) {
        new TN3270Impersonator(1111, "net/sf/f3270/impersonator/data.txt");
    }

    private List<DataBlock> data;
    private final int port;

    public TN3270Impersonator(int port, String dataFilePath) {
        this.port = port;
        parseDataFile(dataFilePath);
        startMainThread();
    }

    private void startMainThread() {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    log.info("waiting for client to connect");
                    Socket socket = new ServerSocket(port).accept();
                    log.info("client connected");

                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream();

                    List<Integer> in = new ArrayList<Integer>();
                    int current = 0;
                    while (true) {
                        if (current >= data.size()) {
                            break;
                        }
                        DataBlock entry = data.get(current);

                        if (entry.getIn().length == 0) {
                            write(os, entry.getOut());
                            current++;
                            in.clear();
                            continue;
                        }

                        int b = is.read();
                        in.add(b);

                        if (isInputMatch(in, entry.getIn())) {
                            write(os, entry.getOut());
                            current++;
                            in.clear();
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                log.info("no more recorded data to replay");

                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        new Thread(r).start();
    }

    private void write(OutputStream os, int[] out) throws IOException {
        for (int b : out) {
            os.write(b);
        }
    }

    private boolean isInputMatch(List<Integer> list, int[] array) {
        if (list.size() != array.length) {
            return false;
        }
        for (int i = 0; i < array.length; i++) {
            if (list.get(i) != array[i]) {
                return false;
            }
        }
        return true;
    }

    private void parseDataFile(String dataFilePath) {
        data = new ArrayList<DataBlock>();
        List<String> lines = readLines(dataFilePath);
        int[] in = new int[] {};
        int[] out = new int[] {};
        for (String line : lines) {
            String[] tokens = line.split(" ");
            if (tokens[0].equals(">")) {
                in = toIntArray(tokens);
            } else {
                out = toIntArray(tokens);
                data.add(new DataBlock(in, out));
                in = new int[] {};
                out = new int[] {};
            }
        }
        if (in.length != 0 || out.length != 0) {
            data.add(new DataBlock(in, out));
        }
    }

    private int[] toIntArray(String[] tokens) {
        int[] a = new int[tokens.length - 1];
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];
            int b = Integer.parseInt(token);
            a[i - 1] = b;
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    private List<String> readLines(String dataFilePath) {
        try {
            return IOUtils.readLines(TN3270Impersonator.class.getClassLoader().getResourceAsStream(dataFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
