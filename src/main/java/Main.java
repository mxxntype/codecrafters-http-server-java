import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try {
            // Start a server.
            ServerSocket server = new ServerSocket(4221);
            server.setReuseAddress(true);
            Socket client = server.accept();
            System.err.println("Got new connection on port " + client.getPort());

            // Read input.
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(client.getInputStream()));
            String in = reader.readLine();
            System.err.println("Received input: " + in);

            // Respond with HTTP OK (200)
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            writer.write("HTTP/1.1 200 OK\r\n\r\n");
            writer.flush();
            System.err.println("Wrote HTTP OK response (200)");

            // Stop the server.
            server.close();
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }
}
