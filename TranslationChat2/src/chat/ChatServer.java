package chat;


import java.awt.BorderLayout;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.cloud.translate.*;

import javax.swing.*;

public class ChatServer extends JFrame implements Runnable {
    private static final int PORT = 9898;
    private static ConcurrentHashMap<Integer, PrintWriter> writers = new ConcurrentHashMap<>();
    private static AtomicInteger clientIdCounter = new AtomicInteger(0);

    private JTextArea serverLog;
    public static int getPORT() {
    	return PORT;
    }
    public ChatServer() {
        super("Chat Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createMenu();

        serverLog = new JTextArea(20, 40);
        serverLog.setEditable(false);
        getContentPane().add(new JScrollPane(serverLog), BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener((e) -> System.exit(0));
        menu.add(exitItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    public void run() {
        serverLog.append("Chat Server is running...\n");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                int clientId = clientIdCounter.incrementAndGet();
                serverLog.append("Client connected: " + clientId + " - " + socket.getInetAddress() + "\n");
                new Handler(socket, serverLog, clientId).start();
            }
        } catch (IOException e) {
            serverLog.append("Error: " + e.getMessage() + "\n");
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private JTextArea serverLog;
        private BufferedReader in;
        private PrintWriter out;
        private int clientId;

        // Add a Translate object
        private Translate translate;

        public Handler(Socket socket, JTextArea serverLog, int clientId) {
            this.socket = socket;
            this.serverLog = serverLog;
            this.clientId = clientId;
            
            // Initialize the Translate object
            translate = TranslateOptions.getDefaultInstance().getService();
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                //out.println("CLIENTID " + clientId);
                writers.put(clientId, out);

                for (String message; (message = in.readLine()) != null;) {
                    for (Map.Entry<Integer, PrintWriter> entry : writers.entrySet()) {
                        String translatedText;
                        
                        if (entry.getKey() == clientId) {
                            translatedText = message;
                        } else {
                            // Translate the message
                            Translation translation = translate.translate(message, Translate.TranslateOption.targetLanguage("en"));
                            translatedText = translation.getTranslatedText();
                        }
                        entry.getValue().println("MESSAGE: " + "Client " + clientId + ": " + translatedText);
                    }
                }
            } catch (IOException e) {
                serverLog.append("Error: " + e.getMessage() + "\n");
            } finally {
                if (out != null) {
                    writers.remove(clientId);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    serverLog.append("Error: " + e.getMessage() + "\n");
                }
            }
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        new Thread(chatServer).start();
    }
}
