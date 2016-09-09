/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ProcessingException;
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
public class Nodo {

    private ServerSocket serverSocket;
    private int[] ackCounter;
    private volatile boolean exiting;
    private List<NodoInfo> pending;
    static Token token;
    private String next;
    private String nodeType;
    private String id;
    private String gatewayAddress;
    private String address;
    private NodoInfo nodoInfo;
    private int listeningPort;
    private Simulator simulatorInstance;
    private BufferImplementation bufferImpl;
    private BufferedReader stdin;
    private ThreadConsole consoleThread;

    //private static MeasurementBuffer buffer;
    public Nodo() {
    }
    //Costruttore nodo
    public Nodo(String id, String nodeType, String listeningPort, String gatewayAddress) throws IOException {
        this.id = id;
        this.nodeType = nodeType;
        this.address = InetAddress.getLocalHost().getHostAddress();
        this.gatewayAddress = gatewayAddress;

        try {
            this.listeningPort = Integer.parseInt(listeningPort);
            switch (nodeType) {
                case "accelerometer":
                    bufferImpl = new BufferImplementation(true);
                    simulatorInstance = new AccelerometerSimulator(id, bufferImpl);
                    new Thread(simulatorInstance).start();
                    break;
                case "light":
                    bufferImpl = new BufferImplementation(false);
                    simulatorInstance = new LightSimulator(id, bufferImpl);
                    new Thread(simulatorInstance).start();
                    break;
                case "temperature":
                    bufferImpl = new BufferImplementation(false);
                    simulatorInstance = new TemperatureSimulator(id, bufferImpl);
                    new Thread(simulatorInstance).start();
                    break;

                default:
                    System.out.println("TIPOLOGIA DI NODO ERRATA");
                    System.exit(0);
                    break;
            }

        } catch (NumberFormatException e) {
            System.out.println("PORTA NON VALIDA");
            System.exit(0);
        }
        this.pending = new ArrayList<>();
        this.exiting = false;
        this.nodoInfo = new NodoInfo(id, nodeType, new Date(), address, listeningPort);
        this.ackCounter = new int[1];

        this.ackCounter[0] = 0;
        //Controllo la disponibilià della porta selezionata
        try {
            this.serverSocket = new ServerSocket(this.listeningPort);
        } catch (IOException ex) {
            System.out.println("PORTA GIA' IN USO");
            //this.serverSocket.close();
            System.exit(0);
        }

        this.stdin = new BufferedReader(new InputStreamReader(System.in));
    }
    //Verifica della correttezza e disponibilità dell'indirizzo del Gateway inserito in input
    private static boolean validateAddress(String address, String port) {
        try {
            int portInt = Integer.parseInt(port);
            if (portInt <= 0 || portInt > 65535) {
                return false;
            }
        } catch (NumberFormatException ex) {
            return false;
        }
        try {
            InetAddress.getByName(address);

        } catch (UnknownHostException e1) {
            try {
                InetAddress.getByAddress(address.getBytes());
            } catch (UnknownHostException e2) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        Gson gson = new Gson();
        System.out.println("VALIDAZIONE INPUT");
        //Validazione input
        if (!validateAddress(args[3].split(":")[0], args[3].split(":")[1])) {
            System.out.println("INDIRIZZO DI RETE NON VALIDO");
            System.exit(0);
        }
        //Creo il nodo e definisco le variabili per le chiamate REST
        Nodo n = new Nodo(args[0], args[1], args[2], args[3]);
        Response answer = null;
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI(n.getGatewayAddress()));
        System.out.println("ID NODO: " + n.getId());
        System.out.println("TIPOLOGIA NODO: " + n.getType());
        System.out.println("INDIRIZZO NODO: " + n.getAddress());
        System.out.println("PORTA DI ASCOLTO: " + n.getServerSocket().getLocalPort());
        //Avvio il thread di ascolto e quello per l'input
        ThreadConsole console = new ThreadConsole(n);
        console.start();
        n.SetConsole(console);
        ThreadServer threadNodoServer = new ThreadServer(n.getServerSocket(), n);
        threadNodoServer.start();
        //Effettuo la chiamata rest per la registrazione del nodo
        try {
            answer = target.path("rest").path("nodes").path("register").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n.getNodoInfo()), MediaType.APPLICATION_JSON));
        } catch (ProcessingException e) {
            System.out.println("ERRORE NELLA CONNESSIONE AL GATEWAY");
            System.exit(0);

        }
        registraNodo(n, answer);

    }

    public static void registraNodo(Nodo n, Response answer) throws IOException {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI(n.getGatewayAddress()));
        Gson gson = new Gson();
        //ricevo dal gateway l'elenco dei nodi della rete
        try {
            Thread.sleep(5000);

        } catch (InterruptedException ex) {
            Logger.getLogger(Nodo.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        if (answer.getStatus() == 202) {
            Type t = new TypeToken<HashMap<String, NodoInfo>>() {
            }.getType();
            HashMap<String, NodoInfo> nodesList = gson.fromJson(answer.readEntity(String.class
            ), t);
            System.out.println(nodesList);
            //Se sono il primo nodo ad entrare nella rete mi occupo di creare il token e lanciarlo per la prima volta
            if (nodesList.isEmpty()) {
                n.SetNeighbour(n.getAddress() + "-" + n.getListeningPort());
                System.out.println("CREO IL TOKEN");
                token = Token.getInstance();
                String tokenString = gson.toJson(token);
                String[] neighbourData = n.getNeighbour().split("-");
                Message message = new Message("token", n.getAddress(), "" + n.getListeningPort(), tokenString);
                n.inviaMessaggio(message, neighbourData[0], neighbourData[1]);
            } else {
                //Seleziono un nodo a caso tra quelli inseriti nella rete e lo contatto per essere inserito nella rete
                List<String> keysAsArray = new ArrayList<>(nodesList.keySet());
                Random r = new Random();
                NodoInfo nodeInfo = (NodoInfo) nodesList.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
                System.out.println(nodeInfo);
                String nodoInfoString = gson.toJson(n.getNodoInfo());

                try {
                    //Invio il messaggio di inserimento al nodo scelto
                    Message message = new Message("insert", n.getAddress(), "" + n.getListeningPort(), nodoInfoString);
                    n.inviaMessaggio(message, nodeInfo.getAddress(), nodeInfo.getPort());

                } catch (IOException e) {
                    //Catturo l'eccezione ConnectionRefused e riprovo a connettermi alla rete
                    System.out.println("RIPROVO CAUSA ERRORE");
                    answer = target.path("rest").path("nodes").path("nodi").request(MediaType.APPLICATION_JSON).get();

                    nodesList
                            = gson.fromJson(answer.readEntity(String.class
                            ), t);
                    if (!nodesList.containsKey(nodeInfo.getId())) {
                        System.out.println("IL VECCHIO NODO E' MORTO");
                        answer = target.path("rest").path("nodes").path("retry").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n.getNodoInfo()), MediaType.APPLICATION_JSON));
                        if (answer.getStatus() == 202) {
                            registraNodo(n, answer);
                        }
                    } else {
                        System.out.println("DEVI ASPETTARE");

                    }

                }
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException ex) {
                    Logger.getLogger(Nodo.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                //Se dopo un determinato tempo il campo neighbour è ancora null vuol dire che non sono stato inserito nella rete quindi riprovo
                if (n.getNeighbour() == null) {
                    System.out.println("RIPROVO CAUSA TIMEOUT");
                    answer = target.path("rest").path("nodes").path("nodi").request(MediaType.APPLICATION_JSON).get();

                    nodesList
                            = gson.fromJson(answer.readEntity(String.class
                            ), t);
                    if (!nodesList.containsKey(nodeInfo.getId())) {
                        System.out.println("IL VECCHIO NODO E' MORTO");
                        answer = target.path("rest").path("nodes").path("retry").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n.getNodoInfo()), MediaType.APPLICATION_JSON));
                        if (answer.getStatus() == 202) {
                            registraNodo(n, answer);
                        }
                    } else {
                        System.out.println("DEVI ASPETTARE");

                    }

                }

            }

        } else {
            System.out.println("REGISTRAZIONE FALLITA");
            System.exit(0);
        }
    }

    public void inviaMessaggio(Message message, String address, String toPort) throws IOException {
        Gson gson = new Gson();
        String messageString = gson.toJson(message);
        String portString = toPort;
        int port = Integer.parseInt(portString);
        Socket clientSocket = new Socket(address, port);
        DataOutputStream outToServer = new DataOutputStream((clientSocket.getOutputStream()));
        outToServer.writeBytes(messageString + '\n');
        clientSocket.close();
    }

    public NodoInfo getNodoInfo() {
        return this.nodoInfo;
    }

    public String getId() {
        return this.id;
    }

    public Simulator getSimulator() {
        return this.simulatorInstance;
    }

    public String getType() {
        return this.nodeType;
    }

    public String getAddress() {
        return this.address;
    }

    public String getGatewayAddress() {
        return this.gatewayAddress;
    }

    private static URI getBaseURI(String address) {
        return UriBuilder.fromUri("http://" + address + "/Gateway").build();
    }

    public int getListeningPort() {
        return this.listeningPort;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public String getNeighbour() {
        return this.next;
    }

    public BufferImplementation getBuffer() {
        return this.bufferImpl;
    }

    public synchronized void SetNeighbour(String neighbour) {
        this.next = neighbour;
    }

    public synchronized void addPending(NodoInfo s) {
        pending.add(s);
    }

    public synchronized void removePending(NodoInfo s) {
        pending.remove(s);
    }

    public List<NodoInfo> getPending() {
        return this.pending;
    }

    public boolean isExiting() {
        return this.exiting;
    }

    public void setExiting(boolean bool) {
        this.exiting = bool;
    }

    public int[] getAck() {
        return this.ackCounter;
    }

    public BufferedReader getReader() {
        return this.stdin;
    }

    public void setReader(BufferedReader reader) {
        this.stdin = reader;

    }

    public ThreadConsole getConsole() {
        return this.consoleThread;
    }

    public void SetConsole(ThreadConsole thread) {
        this.consoleThread = thread;
    }

}
