import exception.AlreadyRunningException;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.HashMap;

public class TCPClient {
    private final InetSocketAddress address;
    private final boolean logConsole;
    private SSLSocket socket;
    private Status status = Status.STOPPED;
    private Thread listener;
    private final HashMap<String, Executable> executables = new HashMap<>();

    public TCPClient(String address, int port, boolean logConsole) {
        this.address = new InetSocketAddress(address, port);
        this.logConsole = logConsole;
        start();
    }

    public void start() {
        if (status == Status.STOPPED) {
            status = Status.Starting;
            onLog("Starting...");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (status == Status.RUNNING) {
                    System.out.println("Forced stop while Client was running." +
                            "\nPlease stop the Client manually by the console.");
                } else if (status != Status.STOPPED) {
                    System.err.println("Forced stop while Client was changing Mode. This could cause some Data to get corrupted!" +
                            "\n  -- Forced Shutdown --  ");
                }
            }));

            login();
            startListener();

            status = Status.RUNNING;
            onLog("Connected to " + address.getHostName());
        } else {
            throw new AlreadyRunningException();
        }
    }

    public void stop() {
        status = Status.Stopping;
        if (listener.isAlive()) {
            listener.interrupt();
        }
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = null;
    }

    private void login() {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("TCPcc"), "TCP-Connect-Client".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(ks, "TCP-Connect-Client".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ks);

            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sc.init(kmf.getKeyManagers(), trustManagers, null);

            SSLSocketFactory ssf = sc.getSocketFactory();
            socket = (SSLSocket) ssf.createSocket(address.getAddress(), address.getPort());
            socket.startHandshake();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[TCPClient] Socket creation Failed");
        }
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

    private void onLog(String s) {
        if (logConsole) System.out.println("[Client] " + s);
    }

    public enum Status {
        STOPPED,
        Starting,
        RUNNING,
        Stopping
    }
}
