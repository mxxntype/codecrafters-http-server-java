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
            log(LogLevel.INFO, "Server started on port " + server.getLocalPort());

            while (true) {
                final var client = server.accept();
                log(LogLevel.INFO, "New connection on port " + client.getPort());
                new ClientHandler(client).start();
            }
        } catch (final IOException e) {
            log(LogLevel.ERROR, e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private final BufferedWriter outputStream;
        private final BufferedReader inputStream;

        public ClientHandler(final Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            this.outputStream =
                    new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.inputStream =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        private static final String HTTP_200_OK = "HTTP/1.1 200 OK\r\n\r\n";
        private static final String HTTP_404_NOT_FOUND = "HTTP/1.1 404 Not Found\r\n\r\n";

        @Override
        public void run() {
            try {
                handleClient();
                this.outputStream.flush();
                this.clientSocket.close();
            } catch (final IOException e) {
                log(LogLevel.ERROR, "Error handling client: " + e.getMessage());
            }
        }

        /**
         * @param clientSocket
         * @throws IOException
         */
        private void handleClient() throws IOException {
            String request = null;
            while ((request = this.inputStream.readLine()) != null && !request.isBlank()) {
                log(LogLevel.DEBUG, "Read: " + request);
                final var tokens = request.split(" ");
                final var requestType = tokens[0].trim();
                if (requestType.equals("GET")) {
                    final var path = tokens[1];
                    if (path.equals("/")) writeResponse(HTTP_200_OK);
                    else if (path.startsWith("/echo/")) {
                        final var content = path.substring("/echo/".length());
                        writeResponse(HTTP_200_OK);
                        writeResponse("Content-Type: text/plain\r\n");
                        writeResponse("Content-Length: " + content.length() + "\r\n");
                        writeResponse("\r\n");
                        writeResponse(content);
                        log(LogLevel.INFO, "Echoed \"" + content + "\"");
                    } else {
                        log(LogLevel.WARN, "Path " + path + " is invalid");
                        writeResponse(HTTP_404_NOT_FOUND);
                    }
                }
            }
        }

        /**
         * @param clientSocket
         * @param response
         * @throws IOException
         */
        private void writeResponse(final String response) throws IOException {
            this.outputStream.write(response);
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
