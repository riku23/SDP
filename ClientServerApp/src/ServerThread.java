

import java.io.*;
import java.net.*;

public class ServerThread extends Thread {
    private     Socket connectionSocket = null;
    private     BufferedReader inFromClient;
    private     DataOutputStream  outToClient;

    /* L'argomento del costruttore e' una established socket */
    public ServerThread(Socket s) {
        connectionSocket = s;

        try{
            inFromClient =
                    new BufferedReader(
                            new InputStreamReader(connectionSocket.getInputStream()));

            outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String clientSentence;
        String capitalizedSentence;

        try {
            System.out.println("I'm "+this.getName()+", starting right now!");
            System.out.println("Host: "+connectionSocket.getInetAddress());
            System.out.println("Port: "+connectionSocket.getPort());

            clientSentence = inFromClient.readLine();

            capitalizedSentence = clientSentence.toUpperCase() + '\n';

            outToClient.writeBytes(capitalizedSentence);

            connectionSocket.close();

            System.out.println("I'm "+this.getName()+", goodbye.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
