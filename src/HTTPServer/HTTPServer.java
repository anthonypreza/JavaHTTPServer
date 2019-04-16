package HTTPServer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

// Each client connection will be managed in a dedicated Thread
public class HTTPServer implements Runnable {
    static final File ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    // Port to listen for connection
    static final int PORT = 8000;

    // Verbose mode
    static final boolean verbose = true;

    // Client Connection via Socket Class
    private Socket connect;

    public HTTPServer(Socket c) {
        connect = c;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port: " + PORT + " ...\n");

            // Listen until user halts server execution
            while (true) {
                HTTPServer myServer = new HTTPServer(serverConnect.accept());

                if (verbose) {
                    System.out.println("Connection opened. (" + new Date() + ")");
                }

                // Create dedicated thread to manage the client connectioon
                Thread thread = new Thread(myServer);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server connection error: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // We manage a particular client connnection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            // We read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // We get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // Get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            // Get first line of the request from the client
            String input = in.readLine();
            // Parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // Get the HTTP method of the client
            // Get the file requested
            fileRequested = parse.nextToken().toLowerCase();

            // Support only GET and HEAD methods
            if (!method.equals("GET") && !method.equals("HEAD")) {
                if (verbose) {
                    System.out.println("501 Not Implemented: " + method + " method.");
                }
                // Return the not supported file to the client
                File file = new File(ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String ContentMimeType = "text/html";
                // Read content to return to client
                byte[] fileData = readFileData(file, fileLength);
                
                // Send HTTP Headers with data to client
                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: Java HTTP Server from ap : 1.0");
                out.println("Date: " + new Date());
                out.println("Conttent-type: " + ContentMimeType);
                out.println("Content length: " + fileLength);
                out.println(); // Blank line between headers and content
                out.flush(); // Flush character output stream buffer
                // File
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();

                return;
            }
            else {

            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if(fileIn != null) {
                fileIn.close();
            }
        }
        return fileData;
    }

}