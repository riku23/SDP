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
            Thread.sleep(3000);
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader((estabSocket.getInputStream())));
            clientSentence = inFromClient.readLine();
            Gson gson = new Gson();
            Message messageIn = gson.fromJson(clientSentence, Message.class);

            String header = messageIn.getHeader();
            String senderAddr = messageIn.getSenderAddr();
            String senderPort = messageIn.getSenderPort();
            String body = messageIn.getBody();

            List<NodoInfo> temp;

            //Seleziono le azioni da intraprendere dipendentemente all'header del messaggio
            if (header.equals("token")) {

                //Controllo se dei nodi hanno fatto richiesta di entrare nella rete in modo sincronizzato
                synchronized (nodo.getPending()) {
                    System.out.println(nodo.getPending());
                    temp = new ArrayList<>(nodo.getPending());
                    nodo.getPending().clear();
                }

                //Se sono presenti nodi nella lista pending mi occupo di farli entrare nella rete
                if (!temp.isEmpty()) {
                    System.out.println("INSERISCO NODI");
                    for (int i = 0; i < temp.size(); i++) {
                        InserisciNodo(temp.get(i));

                    }
                }
                //Estraggo il token dal corpo del messaggio
                Token token = gson.fromJson(body, Token.class);
                //Se il mio buffer di misurazioni le aggiungo al token (se c'è spazio nel token) e le invio al gateway
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
                    //Passo avanti
                    System.out.println("BUFFER NODO VUOTO");
                }
                //Ricavo le informazioni (indirizzo e porta) del mio successivo nella rete
                String[] neighbourData = nodo.getNeighbour().split("-");
                System.out.println("PROSSIMO NODO: " + neighbourData[0] + " " + neighbourData[1]);
                //Se mi trovo in stato di uscita setto il mio precedente o a chi mi ha mandato il messaggio oppure al primo nodo che ho servito nel caso avessi richieste di ingresso
                //Questa distinzione è necessaria per gestire il caso in cui ho ricevuto il messaggio da me stesso (ero da solo) ma ho inserito nuovi nodi nella rete
                if (nodo.isExiting()) {
                    System.out.println("ESCO DALLA RETE");
                    String prevAddr;
                    String prevPort;
                    if (temp.isEmpty()) {
                        prevAddr = senderAddr;
                        prevPort = senderPort;

                    } else if (senderAddr.equals(nodo.getAddress()) && senderPort.equals(""+nodo.getListeningPort())) {
                        prevAddr = temp.get(0).getAddress();
                        prevPort = temp.get(0).getPort();
                    } else {
                        prevAddr = senderAddr;
                        prevPort = senderPort;
                    }
                    esciRete(prevAddr, prevPort);
                    //Nel caso dello stato di uscita mando il messagio token al mio successivo segnando come mittente non me stesso ma il mio precedente
                    //per gestire la situazione in cui più nodi chiedono di uscire in cascata
                    Message messageOut = new Message("token", prevAddr, "" + prevPort, gson.toJson(token));
                    nodo.inviaMessaggio(messageOut, neighbourData[0], neighbourData[1]);
                    //Thread.sleep(5000);
                    System.exit(0);
                }
                //Altrimenti invio il messaggio token al mio successivo segnando me stesso come mittente
                Message messageOut = new Message("token", nodo.getAddress(), "" + nodo.getListeningPort(), gson.toJson(token));
                nodo.inviaMessaggio(messageOut, neighbourData[0], neighbourData[1]);
            }

            if (header.equals("insert")) {
                //Nel caso di messaggio di inserimento mi limito ad aggiungere in modo sincronizzato il nodo che ha mandato il messaggio all'elenco pending
                NodoInfo nodoInfo = gson.fromJson(body, NodoInfo.class);
                synchronized (nodo.getPending()) {
                    if (!nodo.getPending().contains(nodoInfo)) {
                        nodo.addPending(nodoInfo);
                    }
                }

            }

            if (header.equals("changeNext")) {
                //Nel caso di ricezione di cambio di successivo aggiorno il campo relativo al successivo e mando un messaggio di ACK al mittente per notificargli il corretto aggiornamento
                System.out.println("CHANGE NEXT");
                nodo.SetNeighbour(body);
                synchronized (nodo.getConnected()) {
                    nodo.setConnected(true);
                    nodo.getConnected().notify();
                }
                Message message = new Message("ack", nodo.getAddress(), "" + nodo.getListeningPort(), "");
                nodo.inviaMessaggio(message, senderAddr, senderPort);
            }

            if (header.equals("ack")) {
                //Ricevuto un messaggio di ack decremento di 1 il numero di ack che sto attendendo nel caso questo valga 0 libero la risorsa e notifico
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
                //La ricezione di un messaggio di uscita setta una variabile booleana dichiarata volatile (per garantirne la correttezza) a true portando il nodo in stato di uscita
                nodo.setExiting(true);

            }

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ThreadNodo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void esciRete(String prevAddr, String prevPort) throws IOException, InterruptedException {
        //System.out.println(prevAddr + "-" + prevPort);
        //Verifico se il mio successivo ha lo stesso indirizzo e porta di me stesso condizione che mi dice se sono l'unico nodo nella rete
        if (!(nodo.getNeighbour()).equals(nodo.getAddress() + "-" + nodo.getListeningPort())) {
            System.out.println("ALTRI NODI PRESENTI NELLA RETE");
            synchronized (nodo.getAck()) {
                //Se non sono da solo invio un messaggio di changeNext sincronizzandomi sugli ack e aspetto l'arrivo dei messaggi relativi e comunico l'uscita al gateway
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
            //Se sono solo devo solo occuparmi di comunicare al gateway la mia uscita e terminare l'esecuzione
            System.out.println("UNICO NODO PRESENTE NELLA RETE");
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
        //Comunico al gateway l'inserimento di un nuovo nodo
        answer = target.path("rest").path("nodes").path("enter").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(newNodo), MediaType.APPLICATION_JSON));

        String addr = newNodo.getAddress();
        String port = newNodo.getPort();
        //mi sincronizzo sugli ack attendendo le risposte che mi garantiscono la buona formazione della rete
        synchronized (nodo.getAck()) {
            Message message = new Message("changeNext", nodo.getAddress(), "" + nodo.getListeningPort(), nodo.getNeighbour());
            nodo.inviaMessaggio(message, addr, port);
            nodo.getAck()[0]++;
            nodo.getAck().wait();
        }
        //Aggiorno il valore del mio successivo
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
