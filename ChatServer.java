import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server started...");

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler client = new ClientHandler(socket);
            clients.add(client);
            new Thread(client).start();
        }
    }

    static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void run() {
            try {
                // Ask username
                out.println("Enter your username:");
                username = in.readLine();

                broadcast("🔵 " + username + " joined the chat", this);

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(username + ": " + message, this);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected");
            } finally {
                clients.remove(this);
                broadcast("🔴 " + username + " left the chat", this);
                try { socket.close(); } catch (IOException e) {}
            }
        }

        void sendMessage(String msg) {
            out.println(msg);
        }
    }
}