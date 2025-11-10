##CHAT SERVER.JAVA.

// ChatServer.java
// Multithreaded chat server using Java sockets.
// Author: [Your Name]
// Date: [Insert Date]

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private static final int DEFAULT_PORT = 5000;
    // Thread-safe list to hold writers for broadcasting
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        new ChatServer().start(port);
    }

    public void start(int port) {
        System.out.println("Starting ChatServer on port " + port + " ...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ChatServer started. Waiting for clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                handler.start(); // start thread for this client
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Broadcast a message to all connected clients
    public void broadcast(String message, ClientHandler from) {
        String out = message;
        for (ClientHandler client : clients) {
            if (!client.isClosed()) {
                client.send(out);
            }
        }
        System.out.println("Broadcast from " + (from != null ? from.getClientName() : "server") + ": " + message);
    }

    // Remove a client handler when a client disconnects
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected: " + client.getClientName());
    }

    // Inner class that handles each client connection
    private static class ClientHandler extends Thread {
        private final Socket socket;
        private final ChatServer server;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName = "Unknown";
        private volatile boolean closed = false;

        public ClientHandler(Socket socket, ChatServer server) {
            this.socket = socket;
            this.server = server;
        }

        public String getClientName() {
            return clientName;
        }

        public boolean isClosed() {
            return closed || socket.isClosed();
        }

        public void send(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Ask for username
                out.println("Welcome! Please enter your name:");
                String name = in.readLine();
                if (name == null || name.trim().isEmpty()) name = "Guest-" + socket.getPort();
                clientName = name.trim();
                out.println("Hi " + clientName + "! Type messages to chat. Type /quit to exit.");

                server.broadcast(clientName + " has joined the chat.", this);

                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    if (!line.isEmpty()) {
                        String formatted = clientName + ": " + line;
                        server.broadcast(formatted, this);
                    }
                }
            } catch (IOException e) {
                System.err.println("Connection error with " + clientName + ": " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void cleanup() {
            closed = true;
            try {
                server.broadcast(clientName + " has left the chat.", this);
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
            server.removeClient(this);
        }
    }
}
