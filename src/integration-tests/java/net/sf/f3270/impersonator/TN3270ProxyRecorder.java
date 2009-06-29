package net.sf.f3270.impersonator;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.f3270.impersonator.DataByte.Direction;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class TN3270ProxyRecorder {

    Logger log = Logger.getLogger(TN3270ProxyRecorder.class);

    private Socket clientSocket;
    private Socket serverSocket;
    private final List<DataByte> data;
    private final String path;

    public TN3270ProxyRecorder(int clientPort, String serverHost, int serverPort, String path) {
        this.path = path;
        this.data = Collections.synchronizedList(new ArrayList<DataByte>(65536));
        startMainThread(clientPort, serverHost, serverPort);
    }

    private void startMainThread(final int clientPort, final String serverHost, final int serverPort) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    log.info("waiting for client to connect");
                    clientSocket = new ServerSocket(clientPort).accept();
                    log.info("client connected");
                    log.info("connecting to server");
                    serverSocket = new Socket(serverHost, serverPort);
                    log.info("connected to server, recording");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                startClientToServerThread();
                startServerToClientThread();
            }
        };
        new Thread(r).start();
    }

    private void startClientToServerThread() {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    while (true) {
                        int b = clientSocket.getInputStream().read();
                        serverSocket.getOutputStream().write(b);
                        data.add(new DataByte(Direction.CLIENT_TO_SERVER, b));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        new Thread(r).start();
    }

    private void startServerToClientThread() {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    while (true) {
                        int b = serverSocket.getInputStream().read();
                        clientSocket.getOutputStream().write(b);
                        data.add(new DataByte(Direction.SERVER_TO_CLIENT, b));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        new Thread(r).start();
    }

    public void dump() {
        StringBuilder sb = new StringBuilder();
        DataByte previous = null;
        for (DataByte d : data) {
            if (previous == null || previous.getDirection() != d.getDirection()) {
                sb.append("\n");
                sb.append(d.getDirection() == Direction.CLIENT_TO_SERVER ? ">" : "<");
            }
            sb.append(" " + d.getData());
            previous = d;
        }
        sb.deleteCharAt(0);

        try {
            FileUtils.writeStringToFile(new File(path), sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
