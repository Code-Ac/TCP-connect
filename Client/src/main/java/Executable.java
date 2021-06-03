import java.net.Socket;

public interface Executable {
    void run(DataPackage data, Socket socket);
}
