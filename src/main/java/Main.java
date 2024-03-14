import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            Socket clientSocket = serverSocket.accept();
            System.err.println("Got new connection on port " + clientSocket.getPort());
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }
}
