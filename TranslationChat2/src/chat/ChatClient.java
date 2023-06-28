package chat;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

import javax.swing.*;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.v3.Translation;

public class ChatClient extends JFrame {
    private static int WIDTH = 400;
    private static int HEIGHT = 300;
    private JTextField textField;
    private JTextArea messageArea;
    private PrintWriter out;
    private JComboBox<String> languageSelector;
    private JPanel bottomPanel;
    private JLabel sendingLabel;
    private JLabel receivingLabel;
    private JList<String> clientList;


    public ChatClient() {
    	super("Chat Client");
        this.setSize(ChatClient.WIDTH, ChatClient.HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(new Color(176, 224, 230)); 
        clientList = new JList<>();
        this.add(new JScrollPane(clientList), BorderLayout.EAST);

        
        textField = new JTextField(30);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14)); 
        textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
        textField.setBackground(new Color(240, 248, 255)); 
        
        languageSelector = new JComboBox<>(new String[]{"en", "fr", "de", "es", "it", "zh", "tr"});
        languageSelector.setBackground(new Color(135, 206, 235)); 

        messageArea = new JTextArea(8, 40);
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); 
        messageArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
        messageArea.setBackground(new Color(240, 248, 255)); 

        sendingLabel = new JLabel("Sending Language:");
        receivingLabel = new JLabel("Receiving Language:");
        
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(textField);
        bottomPanel.add(receivingLabel);
        bottomPanel.add(languageSelector);
        bottomPanel.setBackground(new Color(176, 224, 230)); 


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        mainPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        mainPanel.setBackground(new Color(176, 224, 230)); 



        this.setContentPane(mainPanel);
        this.pack();

        createMenu();
        
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (out != null) {
                    String message = textField.getText();
                    out.println(message);
                    textField.setText("");
                } else {
                    messageArea.append("Please connect to the server first.\n");
                }
            }
        });

        messageArea.setEditable(false);
        this.setVisible(true);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem connectItem = new JMenuItem("Connect");
        connectItem.addActionListener((e) -> {
            if (out == null) {
                connectToServer();
            }
        });
        menu.add(connectItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private void connectToServer() {
        String serverAddress = "localhost";
        try {
            Socket socket = new Socket(serverAddress, ChatServer.getPORT());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Thread receiveMessagesThread = new Thread(() -> {
                try {
                    while (true) {
                        String line = in.readLine();
                        if (line == null) {
                            break;
                        }
                        else if (line.startsWith("MESSAGE")) {
                            String originalMessage = line.substring(8);
                            
                            Translate translate = TranslateOptions.getDefaultInstance().getService();
                            com.google.cloud.translate.Translation translation = translate.translate(originalMessage, Translate.TranslateOption.targetLanguage((String)languageSelector.getSelectedItem()));

                            String translatedMessage = translation.getTranslatedText();
                            Scanner scanner = new Scanner(translatedMessage);
                            
                            messageArea.append(translatedMessage + "\n");
                            
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            });

            receiveMessagesThread.start();
        } catch (IOException e) {
            messageArea.append("Error connecting to server: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
    }
}