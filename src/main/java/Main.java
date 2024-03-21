import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(final String[] args) {
        try {
            final var server = new ServerSocket(4221);
            server.setReuseAddress(true);
            log(LogLevel.INFO, "HTTP server is listening on port " + server.getLocalPort());

            while (true) {
                final var client = server.accept();
                log(LogLevel.INFO, "Accepted connection from port " + client.getPort());
                new ClientHandler(client).start();
            }
        } catch (final IOException e) {
            log(LogLevel.ERROR, e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private final BufferedWriter output;
        private final BufferedReader input;

        public ClientHandler(final Socket socket) throws IOException {
            this.socket = socket;
            this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        private static final String HTTP_200_OK = "HTTP/1.1 200 OK\r\n";
        private static final String HTTP_404_NOT_FOUND = "HTTP/1.1 404 Not Found\r\n\r\n";

        @Override
        public void run() {
            try {
                handleClient();
                this.output.flush();
                this.socket.close();
            } catch (final IOException e) {
                log(LogLevel.ERROR, "Error handling client: " + e.getMessage());
            }
        }

        /**
         * @param socket
         * @throws IOException
         */
        private void handleClient() throws IOException {
            String request = null;
            while ((request = this.input.readLine()) != null && !request.isBlank()) {
                log(LogLevel.DEBUG, "Read: " + request);
                final var tokens = request.split(" ");
                final var requestType = tokens[0].trim();
                if (requestType.equals("GET")) {
                    final var path = tokens[1];
                    if (path.equals("/")) {
                        this.output.write(HTTP_200_OK + "\r\n");
                    } else if (path.startsWith("/echo/")) {
                        final var content = path.substring("/echo/".length());
                        this.output.write(HTTP_200_OK);
                        this.output.write("Content-Type: text/plain\r\n");
                        this.output.write("Content-Length: " + content.length() + "\r\n");
                        this.output.write("\r\n");
                        this.output.write(content);
                        log(LogLevel.INFO, "Echoed: " + content);
                    } else {
                        log(LogLevel.WARN, "Path " + path + " is invalid");
                        this.output.write(HTTP_404_NOT_FOUND);
                    }
                }
            }
        }
    }

    private enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

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
