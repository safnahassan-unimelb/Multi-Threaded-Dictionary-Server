package unimelb.ds.assignment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Server {
    private static String QUERY = "QUERY";
    private static String REMOVE = "REMOVE";
    private static String ADD = "ADD";
    private static String UPDATE = "UPDATE";
    private JPanel rootPanel;
    private JPanel clientPanel;
    private JPanel logPanel;
    private JLabel clientInfo;
    private JLabel logInfo;

    public static void main(String[] args)
    {
        Integer port = Integer.parseInt(args[0]);
//        dictionary.json
        String fileName = args[1];
        JFrame frame = new JFrame("Dictionary Server GUI");
        Server form = new Server();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.pack();
        frame.setVisible(true);


        ServerSocket server = null;
        try {

            // server is listening on port specified
            server = new ServerSocket(port);
            server.setReuseAddress(true);
//          load dictionary json object to a map
            Map<String, String> dictionaryMap = null;
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("src/unimelb/ds/assignment/"+fileName);
            try {
                dictionaryMap = mapper.readValue(file, new TypeReference<Map<String, String>>() {});
            } catch (IOException e) {
                System.out.println("Dictionary file could not be read in the server.");
                e.printStackTrace();
            }
            Integer clientCount = 0;

            // running infinite loop for getting client request
            while (server != null) {

                // socket object to receive incoming client requests
                Socket client = server.accept();
                clientCount++;

                // Displaying that new client is connected to server
                String clientInfo = form.clientInfo.getText().replace("<html>","")
                        .replace("</html>","")
                        + "New client "+clientCount+" connected: " + client.getInetAddress().getHostAddress()
                        + "<br>";
                form.clientInfo.setText("<html>" + clientInfo + "</html>");
                System.out.println("New client "+clientCount+" connected : " + client.getInetAddress().getHostAddress());

                // Thread-per-request: create a new thread object for each client
                ClientHandler clientSocket = new ClientHandler(client, form, clientCount, dictionaryMap);
                new Thread(clientSocket).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private Integer clientCount;
        private Map<String,String> dictionaryMap;
        private Server form;

        // Constructor
        public ClientHandler(Socket socket, Server form, Integer clientCount, Map<String, String> dictionaryMap)
        {
            this.clientSocket = socket;
            this.clientCount = clientCount;
            this.dictionaryMap = dictionaryMap;
            this.form = form;

        }

        public void run()
        {
            List<String> operations = new ArrayList<>();
            operations.add(QUERY);
            operations.add(REMOVE);
            operations.add(ADD);
            operations.add(UPDATE);

            PrintWriter out = null;
            BufferedReader input = null;
            try {

                // get the outputstream of client
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // get the inputstream of client
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String line;
                while ((line = input.readLine()) != null) {

                    List<String> requestList = List.of(line.split(",", 2));
                    if ((requestList.size() == 2) && operations.contains(requestList.get(0))) {
                        String operation = requestList.get(0);
                        String arguments = requestList.get(1);
                        arguments.replace("{","");
                        arguments.replace("}","");
                        List<String> argumentList = List.of(arguments.split(",", 2));
                        String word = argumentList.get(0).replace("WORD=","")
                                .replace("{","")
                                .replace("}","")
                                .toLowerCase();

                        synchronized (dictionaryMap) {
                            switch (operation) {
                                case "QUERY":
                                    if (dictionaryMap.containsKey(word)) {
                                        String meaning = dictionaryMap.get(word);
//
                                        out.println("Meaning(s) of \""+word+"\" : "+meaning.substring(0, Math.min(meaning.length(), 150)));
                                    }
                                    else {
                                        out.println("The word: \""+word+"\" is not present in the Dictionary Server's database.");
                                    }
                                    break;
                                case "REMOVE":
                                    if (dictionaryMap.containsKey(word)) {
                                        dictionaryMap.remove(word);
                                        out.println("The word \""+word+"\" has been removed from the Dictionary Server's database.");
                                        String logInfo = form.logInfo.getText()
                                                .replace("<html>","")
                                                .replace("</html>","")
                                                + "Client "+clientCount+" : The word "+word+" has been removed from the Dictionary Server's database."
                                                + "<br>";
                                        form.logInfo.setText("<html>" + logInfo + "</html>");
                                    }
                                    else {
                                        out.println("The word \""+word+"\" is not present in the Dictionary Server's database.");
                                    }
                                    break;
                                case "ADD":
                                    if (argumentList.size() != 2) {
                                        out.println("Please provide the meaning(s) of the word to add in the dictionary.");
                                    }
                                    else {
                                        if (dictionaryMap.containsKey(word)) {
                                            out.println("The word \""+word+"\" already exists in the dictionary.");
                                        }
                                        else {
//                                            get all the meanings provided by the client
                                            String meanings = argumentList.get(1).replace("MEANING=", "")
                                                    .replace("{","")
                                                    .replace("}","")
                                                    .replace(",",";");
                                            dictionaryMap.put(word,meanings);
                                            out.println("Word: \""+word+"\" has been added to the Dictionary Server's database successfully!");
                                            String logInfo = form.logInfo.getText()
                                                    .replace("<html>","")
                                                    .replace("</html>","")
                                                    + "Client "+clientCount+" : The word \""+word+"\" has been added to the Dictionary Server's database."
                                                    + "<br>";
                                            form.logInfo.setText("<html>" + logInfo + "</html>");
                                        }
                                    }
                                    break;
                                case "UPDATE":
                                    if (argumentList.size() != 2) {
                                        out.println("Please provide the meaning(s) of the word to update in the dictionary.");
                                    }
                                    if (!dictionaryMap.containsKey(word)) {
                                        out.println("The word \""+word+"\" does not exist in the dictionary.");
                                    }
                                    else {
//                                            get all the meanings provided by the client
                                        String meanings = argumentList.get(1).replace("MEANING=", "")
                                                .replaceAll("\\s","")
                                                .replace("{","")
                                                .replace("}","");
                                        dictionaryMap.put(word,meanings);
                                        out.println("Meaning(s) of word: \""+word+"\" updated in the dictionary successfully!");
                                        String logInfo = form.logInfo.getText()
                                                .replace("<html>","")
                                                .replace("</html>","")
                                                + "Client "+clientCount+" : The word "+word+" has been updated in the Dictionary Server's database."
                                                + "<br>";
                                        form.logInfo.setText("<html>" + logInfo + "</html>");
                                    }
                                    break;

                                default:
                                    break;
                            }
                        }

                    }
                    else {
                        System.out.printf(" Sent from the client "+ clientCount +": %s\n", line);
                        out.println("Incorrect operation/parameters sent to the server");
                    }

                }
            }
            catch (SocketException e){
                String clientInfo = form.clientInfo.getText().replace("<html>","")
                        .replace("</html>","")
                        + "Client "+clientCount+" closed"
                        + "<br>";
                this.form.clientInfo.setText("<html>" + clientInfo + "</html>");
                System.out.printf(" Client "+ clientCount +" closed\n");
            }
            catch (Exception e) {
                out.println("The server could not complete the operation. Please try later.");
                e.printStackTrace();
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (input != null) {
                        input.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
