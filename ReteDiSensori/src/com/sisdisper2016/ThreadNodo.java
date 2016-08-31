/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

/**
 *
 * @author Tozio23
 */
public class ThreadNodo extends Thread {

    private boolean stopped = false;
    private final Nodo nodo;
    private final String threadType;
    private final Socket estabSocket;
    private String clientSentence;
    private String capitalizedSentence;
    private final static String MESSAGE1 = "ciao";
    private final static String MESSAGE2 = "chain";

    //COSTRUTTORE
    public ThreadNodo(Socket socket, String type, Nodo n) {
        this.estabSocket = socket;
        this.threadType = type;
        this.nodo = n;
    }

    @Override
    public void run() {

        try {

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader((estabSocket.getInputStream())));
            DataOutputStream outToClient = new DataOutputStream(estabSocket.getOutputStream());
            clientSentence = inFromClient.readLine();
            Gson gson = new Gson();
            Message messageIn = gson.fromJson(clientSentence, Message.class);

            String header = messageIn.getHeader();
            String senderAddr = messageIn.getSenderAddr();
            String senderPort = messageIn.getSenderPort();
            String body = messageIn.getBody();

            Thread.sleep(2000);
            if (header.equals("token")) {
                System.out.println(nodo.getPending());
                if (!nodo.getPending().isEmpty()) {
                    System.out.println("INSERISCO NODI");
                    for (int i = 0; i < nodo.getPending().size(); i++) {
                        InserisciNodo(nodo.getPending().get(i));
                        nodo.removePending(nodo.getPending().get(i));
                    }
                }

                Token token = gson.fromJson(body, Token.class);

                if (!nodo.getBuffer().isEmpty()) {
                    if ((nodo.getBuffer().getSize() + token.getMisurazioni()) > 15) {
                        InviaMisurazioniToken(token);
                        token.clearBuffer();
                    }
                    List l = nodo.getBuffer().readAllAndClean();
                    System.out.println("NUMERO MISURAZIONI NODO: " + l.size());
                    token.addMisurazioni(l);
                    System.out.println("NUMERO MISURAZIONI TOKEN: " + token.getMisurazioni());
                } else {
                    System.out.println("BUFFER NODO VUOTO");
                }

                String[] neighbourData = nodo.getNeighbour().split("-");
                System.out.println("PROSSIMO NODO: "+neighbourData[0] + " " + neighbourData[1]);
                Message messageOut = new Message("token", nodo.getAddress(), "" + nodo.getListeningPort(), gson.toJson(token));
                nodo.inviaMessaggio(messageOut, neighbourData[0], neighbourData[1]);
                if (nodo.isExiting()) {
                    System.out.println("NO MARIA IO ESCO!");
                    esciRete(senderAddr, senderPort);

                }

            }
            if (header.equals("insert")) {
                nodo.addPending(body);
            }
            if (header.equals("changeNext")) {

                nodo.SetNeighbour(body);
            }

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ThreadNodo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void esciRete(String prevAddr, String prevPort) throws IOException {
        if (!(prevAddr + "-" + prevPort).equals(nodo.getAddress() + "-" + nodo.getListeningPort())) {
            Message message = new Message("changeNext", nodo.getAddress(), "" + nodo.getListeningPort(), nodo.getNeighbour());
            nodo.inviaMessaggio(message, prevAddr, prevPort);
        }
        Response answer;
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        Gson gson = new Gson();
        answer = target.path("rest").path("nodes").path("exit").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(nodo.getId() + "-" + nodo.getAddress() + "-" + nodo.getListeningPort()), MediaType.APPLICATION_JSON));

        nodo.setExiting(false);
        System.exit(0);
    }

    public synchronized void InserisciNodo(String newNodo) throws IOException {
        Response answer;
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        Gson gson = new Gson();
        answer = target.path("rest").path("nodes").path("enter").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(newNodo), MediaType.APPLICATION_JSON));
        String[] splitString = newNodo.split("-");
        String addr = splitString[1];
        String port = splitString[2];
        Message message = new Message("changeNext", nodo.getAddress(), "" + nodo.getListeningPort(), nodo.getNeighbour());
        nodo.inviaMessaggio(message, addr, port);
        nodo.SetNeighbour(addr + "-" + port);

    }

    public synchronized void InviaMisurazioniToken(Token t) {
        System.out.println("INVIO MISURAZIONI TOKEN AL GATEWAY");
        Response answer;
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        Gson gson = new Gson();
        answer = target.path("rest").path("nodes").path("token").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(t), MediaType.APPLICATION_JSON));
        String answerString = gson.fromJson(answer.readEntity(String.class), String.class);
        System.out.println("NUMERO MISURAZIONI SUL GATEWAY: " + answerString);
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8084/Gateway").build();
    }
}
