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
import java.util.ArrayList;
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
    private final Socket estabSocket;
    private String clientSentence;

    //COSTRUTTORE
    public ThreadNodo(Socket socket, Nodo n) {
        this.estabSocket = socket;
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

            List<NodoInfo> temp;
            Thread.sleep(2000);
            if (header.equals("token")) {

                System.out.println(nodo.getPending());

                synchronized (nodo.getPending()) {
                    temp = new ArrayList<>(nodo.getPending());
                    nodo.getPending().clear();
                }
                Thread.sleep(5000);
                if (!temp.isEmpty()) {
                    System.out.println("INSERISCO NODI");
                    for (int i = 0; i < temp.size(); i++) {
                        InserisciNodo(temp.get(i));

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
                System.out.println("PROSSIMO NODO: " + neighbourData[0] + " " + neighbourData[1]);

                if (nodo.isExiting()) {
                    System.out.println("ESCO DALLA RETE");
                    if(temp.isEmpty()){
                        esciRete(senderAddr, senderPort);
                    }else{
                        esciRete(temp.get(0).getAddress(), temp.get(0).getPort());
                    }
                }

                Message messageOut = new Message("token", nodo.getAddress(), "" + nodo.getListeningPort(), gson.toJson(token));
                nodo.inviaMessaggio(messageOut, neighbourData[0], neighbourData[1]);
                if (nodo.isExiting()) {
                    Thread.sleep(5000);
                    System.exit(0);
                }
            }
            if (header.equals("insert")) {
                NodoInfo nodoInfo = gson.fromJson(body, NodoInfo.class);
                synchronized (nodo.getPending()) {
                    nodo.addPending(nodoInfo);
                }

            }

            if (header.equals("changeNext")) {
                System.out.println("CHANGE NEXT");
                nodo.SetNeighbour(body);
                Message message = new Message("ack", nodo.getAddress(), "" + nodo.getListeningPort(), "");
                nodo.inviaMessaggio(message, senderAddr, senderPort);
            }

            if (header.equals("ack")) {

                System.out.println("ACK RICEVUTO");
                synchronized (nodo.getAck()) {
                    nodo.getAck()[0]--;
                    //System.out.println("ACK: "+ nodo.getAck()[0]);
                    if (nodo.getAck()[0] == 0) {
                        nodo.getAck().notify();

                    }
                }

            }

            if (header.equals("exit")) {
                nodo.setExiting(true);

            }

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ThreadNodo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void esciRete(String prevAddr, String prevPort) throws IOException, InterruptedException {
        //System.out.println(prevAddr + "-" + prevPort);
        if (!(nodo.getNeighbour()).equals(nodo.getAddress() + "-" + nodo.getListeningPort())) {
            System.out.println("non sono solo");
            synchronized (nodo.getAck()) {
                Message message = new Message("changeNext", nodo.getAddress(), "" + nodo.getListeningPort(), nodo.getNeighbour());
                nodo.inviaMessaggio(message, prevAddr, prevPort);
                nodo.getAck()[0]++;
                //System.out.println("ACK: "+ nodo.getAck()[0]);
                nodo.getAck().wait();
                Response answer;
                ClientConfig config = new ClientConfig();
                Client client = ClientBuilder.newClient(config);
                WebTarget target = client.target(getBaseURI());
                Gson gson = new Gson();
                answer = target.path("rest").path("nodes").path("exit").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(nodo.getNodoInfo()), MediaType.APPLICATION_JSON));
                nodo.getServerSocket().close();

            }

        } else {
            Response answer;
            ClientConfig config = new ClientConfig();
            Client client = ClientBuilder.newClient(config);
            WebTarget target = client.target(getBaseURI());
            Gson gson = new Gson();
            answer = target.path("rest").path("nodes").path("exit").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(nodo.getNodoInfo()), MediaType.APPLICATION_JSON));
            //nodo.getServerSocket().close();
            System.exit(0);
        }

    }

    public void InserisciNodo(NodoInfo newNodo) throws IOException, InterruptedException {
        Response answer;
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        Gson gson = new Gson();
        answer = target.path("rest").path("nodes").path("enter").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(newNodo), MediaType.APPLICATION_JSON));

        String addr = newNodo.getAddress();
        String port = newNodo.getPort();

        synchronized (nodo.getAck()) {
            Message message = new Message("changeNext", nodo.getAddress(), "" + nodo.getListeningPort(), nodo.getNeighbour());
            nodo.inviaMessaggio(message, addr, port);
            nodo.getAck()[0]++;
            nodo.getAck().wait();
        }
        nodo.SetNeighbour(addr + "-" + port);

    }

    public void InviaMisurazioniToken(Token t) {
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
