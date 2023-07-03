package unimelb.ds.assignment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientUiForm {

    public static Socket socket;
    private JPanel rootPanel;
    private JLabel welcome;
    private JPanel taskPanel;
    private JButton queryMeaning;
    private JButton removeExistingWordButton;
    private JButton addNewWordButton;
    private JButton updateMeaningButton;
    private JButton exitButton;
    private JPanel exitPanel;
    private JPanel outputPanel;
    private JLabel outputLabel;
    private JPanel queryMeaningPanel;
    private JTextArea querytextArea;
    private JPanel removeWordPanel;
    private JTextArea removeWordTextArea;
    private JPanel addWordPanel;
    private JPanel updateWordPanel;
    private JTextArea addNewWord;
    private JTextArea addNewWordMeaning;
    private JTextArea updateWord;
    private JTextArea updateWordMeaning;
    private JLabel addWordlabel;
    private JLabel addNewMeaningLabel;
    private JButton performTask;

    public ClientUiForm() {
        this.queryMeaningPanel.setVisible(false);
        this.removeWordPanel.setVisible(false);
        this.addWordPanel.setVisible(false);
        this.updateWordPanel.setVisible(false);

        queryMeaning.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryMeaning.setForeground(Color.red);
                removeExistingWordButton.setForeground(Color.black);
                addNewWordButton.setForeground(Color.black);
                updateMeaningButton.setForeground(Color.black);

                removeWordPanel.setVisible(false);
                addWordPanel.setVisible(false);
                updateWordPanel.setVisible(false);
                queryMeaningPanel.setVisible(true);
            }
        });
        addNewWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryMeaning.setForeground(Color.black);
                removeExistingWordButton.setForeground(Color.black);
                addNewWordButton.setForeground(Color.red);
                updateMeaningButton.setForeground(Color.black);

                removeWordPanel.setVisible(false);
                addWordPanel.setVisible(true);
                updateWordPanel.setVisible(false);
                queryMeaningPanel.setVisible(false);
            }
        });
        removeExistingWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryMeaning.setForeground(Color.black);
                removeExistingWordButton.setForeground(Color.red);
                addNewWordButton.setForeground(Color.black);
                updateMeaningButton.setForeground(Color.black);

                removeWordPanel.setVisible(true);
                addWordPanel.setVisible(false);
                updateWordPanel.setVisible(false);
                queryMeaningPanel.setVisible(false);
            }
        });
        updateMeaningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryMeaning.setForeground(Color.black);
                removeExistingWordButton.setForeground(Color.black);
                addNewWordButton.setForeground(Color.black);
                updateMeaningButton.setForeground(Color.red);

                removeWordPanel.setVisible(false);
                addWordPanel.setVisible(false);
                updateWordPanel.setVisible(true);
                queryMeaningPanel.setVisible(false);
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        performTask.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (queryMeaningPanel.isVisible() ) {
                    if (querytextArea.getText().matches("[a-zA-Z\n]+")) {

                        Map<String, String> arguments = new HashMap<>();
                        arguments.put("WORD", querytextArea.getText().replace("\n",""));
//                        send to server as client request
                        sendRequestToServer("QUERY", arguments);
                    }
                    else {
                        outputLabel.setText("Invalid word! Please enter a valid word (with English alphabet only).");
                    }
                }
                else if (removeWordPanel.isVisible() ) {
                    if (removeWordTextArea.getText().matches("[a-zA-Z\n]+")) {
                        Map<String, String> arguments = new HashMap<>();
                        arguments.put("WORD", removeWordTextArea.getText().replace("\n",""));
//                        send to server as client request
                        sendRequestToServer("REMOVE", arguments);
                    }
                    else {
                        outputLabel.setText("Invalid word! Please enter a valid word (with English alphabets only).");
                    }

                } else if (addWordPanel.isVisible()) {
                    if (addNewWord.getText().matches("[a-zA-Z\n]+") && !addNewWordMeaning.getText().isBlank()) {
                        Map<String, String> arguments = new HashMap<>();
                        arguments.put("WORD", addNewWord.getText().replace("\n",""));
                        arguments.put("MEANING", addNewWordMeaning.getText());
//                        send to server as client request
                        sendRequestToServer("ADD", arguments);
                    }
                    else if (!addNewWord.getText().matches("[a-zA-Z\n]+")){
                        outputLabel.setText("Invalid word(s)! Please enter a valid word (with English alphabets only.");
                    }
                    else {
                        outputLabel.setText("Enter the meaning(s) of the word!");
                    }

                } else if (updateWordPanel.isVisible()) {
                    if (updateWord.getText().matches("[a-zA-Z\n]+") && updateWordMeaning.getText().matches("[A-Za-z]+(,\\s?([A-Za-z]+))*")) {
                        Map<String, String> arguments = new HashMap<>();
                        arguments.put("WORD", updateWord.getText().replace("\n",""));
                        arguments.put("MEANING", updateWordMeaning.getText());
//                        send to server as client request
                        sendRequestToServer("UPDATE", arguments);
                    }
                    else {
                        outputLabel.setText("Invalid word(s)! Please enter a valid word and comma separated meanings.");
                    }

                }

                else {
                    outputLabel.setText("Choose a task first!");
                }
//                Clear all text fields after each request is sent to server
                querytextArea.setText("");
                removeWordTextArea.setText("");
                addNewWord.setText("");
                addNewWordMeaning.setText("");
                updateWord.setText("");
                updateWordMeaning.setText("");
            }
        });
    }

    public void sendRequestToServer(String action, Map<String, String> arguments) {
        // writing to server
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // reading from server
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(action + "," + arguments.toString());
            out.flush();

            String acknowledgment = null;
            acknowledgment = input.readLine()
                    .replace(";",";<br>");
            System.out.println(acknowledgment);

            if (acknowledgment != null) {
                outputLabel.setText("<html>" + acknowledgment + "</html>");
            }

        } catch (IOException e) {
            outputLabel.setText("ERROR: Server is facing issues. try again later.");
        }


    }
    public static void main(String[] args) {
        String serverAddress = (args[0]);
        Integer port = Integer.parseInt(args[1]);
        JFrame frame = new JFrame("Client GUI");
        ClientUiForm form = new ClientUiForm();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        socket = null;
        // establish a connection by providing host and port number
        try {
            socket = new Socket(serverAddress,port);
        }
        catch (ConnectException e) {
            form.outputLabel.setText("Could not connect to Dictionary Server. please check the server address details.");
        }
        catch (IOException e) {
            form.outputLabel.setText("ERROR: Server is facing issues. try again later.");
        }

        if (socket != null) {
            form.outputLabel.setText("Connection established to Dictionary Server.");
        }
    }

}
