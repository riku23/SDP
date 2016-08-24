

import java.net.*;

class MultiServer {

    public static void main(String args[]) throws Exception
    {
        int port = Integer.parseInt(args[0]);
        ServerSocket welcomeSocket = new ServerSocket(port);

        while(true) {
            Socket connectionSocket = welcomeSocket.accept();

            System.out.println("New connection!");
            System.out.println("Host: "+connectionSocket.getInetAddress());
            System.out.println("Port: "+connectionSocket.getPort());

			/* Creazione di un thread e passaggio della established socket */
            ServerThread theThread =
                    new ServerThread(connectionSocket);

			/* Avvio del thread */
            theThread.start();
        }
    }
}
