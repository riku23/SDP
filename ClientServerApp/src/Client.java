

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by civi on 22/04/15.
 */
public class Client {
    public static void main(String[] args) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = "";

            System.out.println("Insert the string to capitalize: ");
            line = br.readLine()+"\n";

            System.out.print("Asking the server...");

            Socket connectionSocket = new Socket(args[0],Integer.parseInt(args[1]));
            DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
            outToServer.writeBytes(line);

            System.out.println("done!");

            System.out.println("Waiting for the answer..");

            BufferedReader fromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            String answer = fromServer.readLine();
            connectionSocket.close();

            System.out.println("The answer from the server is: ");
            System.out.println(answer);

        } catch (IOException e) {
            System.out.println("Problems occurred during the connection with the server.");
        }


    }
}
