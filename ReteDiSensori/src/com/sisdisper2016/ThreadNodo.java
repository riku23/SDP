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
            String[] splitMessage = clientSentence.split(":::");
            Thread.sleep(3000);
            if (clientSentence.contains("token")) {
                if (nodo.isExiting()) {
                    System.out.println("NO MARIA IO ESCO: "+ splitMessage[1]);
                    esciRete(""+splitMessage[1]);
                }
                System.out.println(nodo.getPending());
                if (!nodo.getPending().isEmpty()) {
                    System.out.println("INSERISCO NODI");
                    for (int i = 0; i < nodo.getPending().size(); i++) {
                        InserisciNodo(nodo.getPending().get(i));
                        nodo.removePending(nodo.getPending().get(i));
                    }

                }
                
                Token token = gson.fromJson(splitMessage[2], Token.class);

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

                nodo.inviaMessaggio("token:::"+nodo.getListeningPort()+":::" + gson.toJson(token), nodo.getNeighbour());
            }
            if (clientSentence.contains("insert")) {
                String[] splitString = clientSentence.split(":::");
                nodo.addPending(splitString[2]);
            }
            if (clientSentence.contains("changeNext")) {
                String[] splitString = clientSentence.split(":::");

                nodo.SetNeighbour(splitString[2]);
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ThreadNodo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public synchronized void esciRete(String prev) throws IOException{
        nodo.inviaMessaggio("changeNext:::"+nodo.getListeningPort()+":::"+nodo.getNeighbour(), prev);
        nodo.setExiting(false);
    }
    public synchronized void InserisciNodo(String newNodo) throws IOException {
        nodo.inviaMessaggio("changeNext:::"+nodo.getListeningPort()+":::" + nodo.getNeighbour(), newNodo);
        nodo.SetNeighbour(newNodo);

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
