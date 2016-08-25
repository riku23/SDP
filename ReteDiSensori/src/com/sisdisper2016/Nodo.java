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
    private List<String> pending;
    static Token token;
    private String next;
    private String nodeType;
    private String id;
    private int listeningPort;
    private Simulator simulatorInstance;
    private BufferImplementation bufferImpl;
    //private static MeasurementBuffer buffer;
    public Nodo() {
    }

    public Nodo(String id, String nodeType, String listeningPort) {
        this.id = id;
        this.nodeType = nodeType;
        try {
            this.listeningPort = Integer.parseInt(listeningPort);
            switch (nodeType) {
                case "Accelerometer":
                    bufferImpl = new BufferImplementation();
                    simulatorInstance = new AccelerometerSimulator(id, bufferImpl);
                    new Thread(simulatorInstance).start();
                    break;
                case "Light":
                    bufferImpl = new BufferImplementation();
                    simulatorInstance = new LightSimulator(id, bufferImpl);
                    new Thread(simulatorInstance).start();
                    break;
                case "Temperature":
                    bufferImpl = new BufferImplementation();
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
    }

    public static void main(String[] args) throws IOException {


        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        Nodo n = new Nodo(args[0], args[1], args[2]);
        System.out.println("ID NODO: " + n.getId());
        System.out.println("TIPOLOGIA NODO:" + n.getType());
        ServerSocket serverSocket = new ServerSocket(n.getListeningPort());
        System.out.println("PORTA DI ASCOLTO: "+serverSocket.getLocalPort());
        ThreadServer threadNodoServer = new ThreadServer(serverSocket, "server", n);
        threadNodoServer.start();
        registraNodo(n);

        

        
        
        //String command = stdin.readLine();
        //if (command.equals("START")){

          while(true){
              String command = stdin.readLine();
              if(command.equals("EXIT")){
                  //KILL ME
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
        answer = target.path("rest").path("nodes").path("register").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n.getId()+"-localhost-"+n.getListeningPort()), MediaType.APPLICATION_JSON));
        if (answer.getStatus() == 202) {
            ArrayList nodesList = gson.fromJson(answer.readEntity(String.class), ArrayList.class);
            System.out.println(nodesList);
            if(nodesList.isEmpty()){
                n.SetNeighbour(""+n.getListeningPort());
                System.out.println("CREO IL TOKEN");
                token = Token.getInstance();
                answer = target.path("rest").path("nodes").path("enter").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n.getId()+"-localhost-"+n.getListeningPort()), MediaType.APPLICATION_JSON));
                String tokenString = gson.toJson(token);  
                n.inviaMessaggio("token:::"+tokenString, n.getNeighbour());
            }else{
                /*
                String[] firstNodeInfo = nodesList.get(0).toString().split("-");
                String[] lastNodeInfo = nodesList.get(nodesList.size()-2).toString().split("-");
                n.SetNeighbour(firstNodeInfo[2]);
                */
                int randPick = (int)(Math.random() * (nodesList.size()-1));
                System.out.println("RANDOM PICK: "+ randPick);
                System.out.println(nodesList.get(randPick));
                String[] nodeInfo = nodesList.get(randPick).toString().split("-");
                System.out.println(nodeInfo[2]);
                n.inviaMessaggio("insert-"+n.getListeningPort(), nodeInfo[2]);
                
                }
            
        } else {
            System.out.println("REGISTRAZIONE FALLITA");
        }
    }
    public void inviaMessaggio(String Message, String toPort) throws IOException {
        String address = "localhost";
        String portString = toPort;
        int port = Integer.parseInt(portString);
        Socket clientSocket = new Socket(address, port);
        DataOutputStream outToServer = new DataOutputStream((clientSocket.getOutputStream()));
        outToServer.writeBytes(Message + '\n');
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
}
