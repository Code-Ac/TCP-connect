import exception.AlreadyRunningException;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.util.HashMap;

public class TCPServer {
    private final int port;
    private final boolean logConsole;
    SSLServerSocket serverSocket;
    Status status = Status.STOPPED;
    private Thread listener;
    private final HashMap<String, Executable> executables = new HashMap<>();

    public TCPServer(int port, boolean logConsole) {
        this.port = port;
        this.logConsole = logConsole;

        start();
    }


    private void login() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("TCPcs"), "TCP-Connect-Server".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(keyStore, "TCP-Connect-Server".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sslContext.init(kmf.getKeyManagers(), trustManagers, null);

            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) ssf.createServerSocket(this.port);
            SSLSocket c = (SSLSocket) serverSocket.accept(); //TODO move it.
        } catch (Exception e) {
            System.err.println("Socket creation failed");
            e.printStackTrace();
        }

        startListener();
        status = Status.RUNNING;
    }

    public void start() {
        if (status == Status.STOPPED) {
            status = Status.Starting;
            onLog("Starting...");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (status == Status.RUNNING) {
                    System.err.println("Forced stop while Server was running." +
                            "\nPlease stop the Server manually by the console." +
                            "\n  -- All connections interrupted --  ");
                } else if (status != Status.STOPPED) {
                    System.err.println("Forced stop while Server was changing Mode. This could cause some Clients to get corrupted!" +
                            "\n  -- Forced Shutdown --  ");
                }
            }));
            login();
            status = Status.RUNNING;
            onLog("Started, listening on port " + port);
        } else {
            System.err.println("Server was already Running when tried to start it.");
            throw new AlreadyRunningException();
        }
    }

    public void stop() {
        status = Status.Stopping;
        if (listener != null && listener.isAlive()) {
            listener.interrupt();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = null;
        }
        status = Status.STOPPED;
    }

    public void addExecutable(String id, Executable executable) {
        this.executables.put(id, executable);
    }

    public void rmExecutable(String id) {
        executables.remove(id);
    }

    private void startListener() {
        if (listener == null || !listener.isAlive()) {
            listener = new Thread(() -> {
                while (status == Status.RUNNING) {
                    try {
                        Socket socket = serverSocket.accept();
                        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                        Object read = ois.readObject();
                        if (read instanceof DataPackage) {
                            new Thread(() ->
                                    executables.get(
                                            ((DataPackage) read).getId())
                                            .run(((DataPackage) read), socket))
                                    .start();
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
            listener.start();
        }
    }

    public void onLog(String s) {
        if (logConsole) {
            System.out.println("[Server] " + s);
        }
    }

    public enum Status {
        STOPPED,
        Starting,
        RUNNING,
        Stopping
    }
}
