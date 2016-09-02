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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
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
public class Nodo {
    private ServerSocket serverSocket;
    private int[] ackCounter;
    private static boolean exiting;
    private List<NodoInfo> pending;
    static Token token;
    private String next;
    private String nodeType;
    private String id;
    private String address;
    private NodoInfo nodoInfo;
    private int listeningPort;
    private Simulator simulatorInstance;
    private BufferImplementation bufferImpl;
    private List<Thread> threads;
    //private static MeasurementBuffer buffer;
    public Nodo() {
    }

    public Nodo(String id, String nodeType,String address, String listeningPort) {
        this.id = id;
        this.nodeType = nodeType;
        this.address = address;
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
        this.threads = new ArrayList<>();
        this.exiting = false;
        this.nodoInfo = new NodoInfo(id, nodeType, new Date(), address, listeningPort);
        this.ackCounter = new int[1];
        this.ackCounter[0] = 0;
        try {
            this.serverSocket = new ServerSocket(this.listeningPort);
        } catch (IOException ex) {
            Logger.getLogger(Nodo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {


        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        Nodo n = new Nodo(args[0], args[1], args[2], args[3]);
        System.out.println("ID NODO: " + n.getId());
        System.out.println("TIPOLOGIA NODO: " + n.getType());
        System.out.println("INDIRIZZO NODO: " + n.getAddress());
        
        System.out.println("PORTA DI ASCOLTO: "+n.getServerSocket().getLocalPort());
        ThreadServer threadNodoServer = new ThreadServer(n.getServerSocket(), "server", n);
        threadNodoServer.start();
        registraNodo(n);

        

        
        
        //String command = stdin.readLine();
        //if (command.equals("START")){

          while(!exiting){
              String command = stdin.readLine();
              if(command.equals("exit")){
                  System.out.println("KILL ME");
                  exiting = true;
                  n.getSimulator().stopMeGently();
     
              }
              
          }
          

    }
    
    
    public static void registraNodo(Nodo n) throws IOException{
        Response answer;
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        
        Gson gson = new Gson();
        answer = target.path("rest").path("nodes").path("register").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n.getNodoInfo()), MediaType.APPLICATION_JSON));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Nodo.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (answer.getStatus() == 202) {
            Type t = new TypeToken<HashMap<String,NodoInfo>>() {}.getType();
            HashMap<String,NodoInfo> nodesList = gson.fromJson(answer.readEntity(String.class),t);
            System.out.println(nodesList);
            if(nodesList.isEmpty()){
                n.SetNeighbour(n.getAddress()+"-"+n.getListeningPort());
                System.out.println("CREO IL TOKEN");
                token = Token.getInstance();
                String tokenString = gson.toJson(token);
                String[] neighbourData = n.getNeighbour().split("-");
                Message message = new Message("token",n.getAddress(),""+n.getListeningPort(),tokenString);
                n.inviaMessaggio(message, neighbourData[0],neighbourData[1]);
                //n.inviaMessaggio("token:::"+n.getAddress()+":::"+n.getListeningPort()+":::"+tokenString, neighbourData[0], neighbourData[1]);
            }else{
                List<String> keysAsArray = new ArrayList<>(nodesList.keySet());
                Random r = new Random();
                NodoInfo nodeInfo = (NodoInfo) nodesList.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
                System.out.println(nodeInfo);
                String nodoInfoString = gson.toJson(n.getNodoInfo());
                Message message = new Message("insert", n.getAddress(), ""+n.getListeningPort(), nodoInfoString);
                n.inviaMessaggio(message, nodeInfo.getAddress(), nodeInfo.getPort());
                //n.inviaMessaggio("insert:::"+n.getAddress()+":::"+n.getListeningPort()+":::"+n.getId()+"-"+n.getAddress()+"-"+n.getListeningPort(),nodeInfo[1], nodeInfo[2]);
                
                }
            
        } else {
            System.out.println("REGISTRAZIONE FALLITA");
        }
    }
    public void inviaMessaggio(Message message,String address, String toPort) throws IOException {
        Gson gson = new Gson();
        String messageString = gson.toJson(message);
        String portString = toPort;
        int port = Integer.parseInt(portString);
        Socket clientSocket = new Socket(address, port);
        DataOutputStream outToServer = new DataOutputStream((clientSocket.getOutputStream()));
        outToServer.writeBytes(messageString + '\n');
        clientSocket.close();
    }
    public NodoInfo getNodoInfo(){
        return this.nodoInfo;
    }    
    public String getId() {
        return this.id;
    }
    
    public Simulator getSimulator(){
        return this.simulatorInstance;
    }
    
    public String getType(){
        return this.nodeType;
    }
    public String getAddress(){
        return this.address;
    }
    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8084/Gateway").build();
    }

    public int getListeningPort() {
        return this.listeningPort;
    }
    
    public ServerSocket getServerSocket(){
        return this.serverSocket;
    }
    

    
    public String getNeighbour(){
        return this.next;
    }
    
    public BufferImplementation getBuffer(){
        return this.bufferImpl;
    }
    
    public synchronized void SetNeighbour(String neighbour){
        this.next = neighbour;
    }
    
    public synchronized void addPending(NodoInfo s){
        pending.add(s);
    }
    
    public synchronized void removePending(NodoInfo s){
        pending.remove(s);
    }
    public List<NodoInfo> getPending(){
        return this.pending;
    }
    
    public boolean isExiting(){
        return this.exiting;
    }
    
    public void setExiting(boolean bool){
        this.exiting = bool;
    }
    
    public void addThread(Thread thread){
        this.threads.add(thread);
    }
    
    public List<Thread> getThreads(){
        return this.threads;
    }
    
    public int[] getAck(){
        return this.ackCounter;
    }
}
