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
            final ServerSocket server = new ServerSocket(4221);
            server.setReuseAddress(true);
            log(LogLevel.INFO, "Server started on port " + server.getLocalPort());

            while (true) {
                final Socket client = server.accept();
                log(LogLevel.INFO, "New connection on port " + client.getPort());
                new ClientHandler(client).start();
            }
        } catch (final IOException e) {
            log(LogLevel.ERROR, e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket client;

        public ClientHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                handleClient(client);
            } catch (IOException e) {
                log(LogLevel.ERROR, "Error handling client: " + e.getMessage());
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    log(LogLevel.ERROR, "Error closing client connection: " + e.getMessage());
                }
            }
        }

        /**
         * @param client
         * @throws IOException
         */
        private static void handleClient(final Socket client) throws IOException {
            final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(client.getInputStream()));
            String request = null;
            while ((request = reader.readLine()) != null && !request.isBlank()) {
                log(LogLevel.DEBUG, "Read: " + request);
                final String[] tokens = request.split(" ");
                final String requestType = tokens[0].trim();
                if (requestType.equals("GET")) {
                    String path = tokens[1];
                    if (path.equals("/")) {
                        writeResponse(client, "HTTP/1.1 200 OK\r\n\r\n");
                    } else {
                        log(LogLevel.WARN, "Path " + path + " is invalid");
                        writeResponse(client, "HTTP/1.1 404 Not Found\r\n\r\n");
                    }
                }
            }
        }

        /**
         * @param client
         * @param response
         * @throws IOException
         */
        private static void writeResponse(final Socket client, String response) throws IOException {
            final BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            writer.write(response);
            writer.flush();
            log(LogLevel.DEBUG, "Wrote: " + response.trim());
        }
    }

    private enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
    }

    /**
     * @param logLevel
     * @param message
     */
    private static void log(final LogLevel logLevel, final String message) {
        String ansiPrefix = null;
        switch (logLevel) {
            case TRACE:
                ansiPrefix = ANSI_BLUE + "[TRACE] ";
                break;
            case DEBUG:
                ansiPrefix = ANSI_CYAN + "[DEBUG] ";
                break;
            case INFO:
                ansiPrefix = ANSI_GREEN + "[INFO] ";
                break;
            case WARN:
                ansiPrefix = ANSI_YELLOW + "[WARN] ";
                break;
            case ERROR:
                ansiPrefix = ANSI_RED + "[ERROR] ";
                break;
        }
        System.err.println(ansiPrefix + ANSI_RESET + message);
    }
}
