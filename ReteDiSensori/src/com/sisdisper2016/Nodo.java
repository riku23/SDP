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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        private static boolean exiting;
    private List<String> pending;
    static Token token;
    private String next;
    private String nodeType;
    private String id;
    private String address;
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
                case "Accelerometer":
                    bufferImpl = new BufferImplementation(true);
                    simulatorInstance = new AccelerometerSimulator(id, bufferImpl);
                    new Thread(simulatorInstance).start();
                    break;
                case "Light":
                    bufferImpl = new BufferImplementation(false);
                    simulatorInstance = new LightSimulator(id, bufferImpl);
                    new Thread(simulatorInstance).start();
                    break;
                case "Temperature":
                    bufferImpl = new BufferImplementation(false);
                    simulatorInstance = new TemperatureSimulator(id, bufferImpl);
                    new Thread(simulatorInstance).start();
                    break;

                default:
                    System.out.println("TIPOLOGIA DI NODO ERRATA");
                    break;
            }

        } catch (NumberFormatException e) {
            System.out.println("PORTA NON VALIDA");
        }
        this.pending = new ArrayList<>();
        this.threads = new ArrayList<>();
        this.exiting = false;
    }

    public static void main(String[] args) throws IOException, InterruptedException {


        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        Nodo n = new Nodo(args[0], args[1], args[2], args[3]);
        System.out.println("ID NODO: " + n.getId());
        System.out.println("TIPOLOGIA NODO: " + n.getType());
        System.out.println("INDIRIZZO NODO: " + n.getAddress());
        ServerSocket serverSocket = new ServerSocket(n.getListeningPort());
        System.out.println("PORTA DI ASCOLTO: "+serverSocket.getLocalPort());
        ThreadServer threadNodoServer = new ThreadServer(serverSocket, "server", n);
        n.addThread(threadNodoServer);
        threadNodoServer.start();
        
        registraNodo(n);

        

        
        
        //String command = stdin.readLine();
        //if (command.equals("START")){

          while(true){
              String command = stdin.readLine();
              if(command.equals("exit")){
                  System.out.println("KILL ME");
                  exiting = true;
                  n.getSimulator().stopMeGently();
                  /*for(Thread t: threads){
                      t.join();
                  }
                  System.out.println("ESCO");
                  System.exit(0);*/
              }
              
          }
        //}
    }
    
    
    public static void registraNodo(Nodo n) throws IOException{
        Response answer;
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        Gson gson = new Gson();
        answer = target.path("rest").path("nodes").path("register").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n.getId()+"-"+n.getAddress()+"-"+n.getListeningPort()), MediaType.APPLICATION_JSON));
        if (answer.getStatus() == 202) {
            ArrayList nodesList = gson.fromJson(answer.readEntity(String.class), ArrayList.class);
            System.out.println(nodesList);
            if(nodesList.isEmpty()){
                n.SetNeighbour(n.getAddress()+"-"+n.getListeningPort());
                System.out.println("CREO IL TOKEN");
                token = Token.getInstance();
                answer = target.path("rest").path("nodes").path("enter").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n.getId()+"-"+n.getAddress()+"-"+n.getListeningPort()), MediaType.APPLICATION_JSON));
                String tokenString = gson.toJson(token);
                String[] neighbourData = n.getNeighbour().split("-");
                Message message = new Message("token",n.getAddress(),""+n.getListeningPort(),tokenString);
                n.inviaMessaggio(message, neighbourData[0],neighbourData[1]);
                //n.inviaMessaggio("token:::"+n.getAddress()+":::"+n.getListeningPort()+":::"+tokenString, neighbourData[0], neighbourData[1]);
            }else{
                
                int randPick = (int)(Math.random() * (nodesList.size()-1));
                System.out.println("RANDOM PICK: "+ randPick);
                System.out.println(nodesList.get(randPick));
                String[] nodeInfo = nodesList.get(randPick).toString().split("-");
                System.out.println(nodeInfo[2]);
                Message message = new Message("insert", n.getAddress(), ""+n.getListeningPort(), n.getId()+"-"+n.getAddress()+"-"+n.getListeningPort());
                n.inviaMessaggio(message, nodeInfo[1], nodeInfo[2]);
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
    

    
    public String getNeighbour(){
        return this.next;
    }
    
    public BufferImplementation getBuffer(){
        return this.bufferImpl;
    }
    
    public synchronized void SetNeighbour(String neighbour){
        this.next = neighbour;
    }
    
    public synchronized void addPending(String s){
        pending.add(s);
    }
    
    public synchronized void removePending(String s){
        pending.remove(s);
    }
    public List<String> getPending(){
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
}
