/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;
import com.google.gson.Gson;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;

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
/*
public class MainRete {

    public static void main(String[] args) {
        // TODO code application logic here
        Response answer;
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        
        Nodo n1 = new Nodo("1" /*,nodeType);
        Nodo n2 = new Nodo("2" /*,nodeType);
        Nodo n3 = new Nodo("3" /*,nodeType);
        Nodo n4 = new Nodo("1" /*,nodeType);
        
        
        
               Gson gson = new Gson();
               answer = target.path("rest").path("nodes").path("enter").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n1),MediaType.APPLICATION_JSON));
               if(answer.getStatus() == 202){
               System.out.println(gson.fromJson(answer.readEntity(String.class), HashMap.class));
               }else{
                   System.out.println("ERRORE");
               }
               
               answer = target.path("rest").path("nodes").path("enter").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n2),MediaType.APPLICATION_JSON));
               if(answer.getStatus() == 202){
               System.out.println(gson.fromJson(answer.readEntity(String.class), HashMap.class));
               }else{
                   System.out.println("ERRORE");
               }
               
               answer = target.path("rest").path("nodes").path("enter").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n3),MediaType.APPLICATION_JSON));
               if(answer.getStatus() == 202){
               System.out.println(gson.fromJson(answer.readEntity(String.class), HashMap.class));
               }else{
                   System.out.println("ERRORE");
               }
               
               answer = target.path("rest").path("nodes").path("enter").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(n4),MediaType.APPLICATION_JSON));
               if(answer.getStatus() == 202){
               System.out.println(gson.fromJson(answer.readEntity(String.class), HashMap.class));
               }else{
                   System.out.println("ERRORE");
               }
               
               }
               
               
               
               
    
          private static URI getBaseURI() {
    return UriBuilder.fromUri("http://localhost:8084/Gateway").build();
  }
}
*/