import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(final String[] args) {
        try {
            // Start a server.
            final ServerSocket server = new ServerSocket(4221);
            server.setReuseAddress(true);
            final Socket client = server.accept();
            System.err.println(ANSI_CYAN + "[NEW CONNECTION]: " + ANSI_RESET + client.getPort());

            readRequest(client);
            respondToClient(client);

            server.close();
        } catch (final IOException e) {
            System.err.println(ANSI_RED + "[EXCEPTION]: " + ANSI_RESET + e.getMessage());
        }
    }

    /**
     * @param client
     * @throws IOException
     */
    private static void readRequest(final Socket client) throws IOException {
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(client.getInputStream()));
        String request = null;
        while ((request = reader.readLine()) != null && !request.isBlank()) {
            System.err.println(ANSI_PURPLE + "[INPUT]: " + ANSI_RESET + request);
        }
    }

    /**
     * @param client
     * @throws IOException
     */
    private static void respondToClient(final Socket client) throws IOException {
        final BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        writer.write("HTTP/1.1 200 OK\r\n\r\n");
        writer.flush();
        System.err.println(ANSI_CYAN + "[RESPONSE]: " + ANSI_RESET + "200 OK");
    }
}
