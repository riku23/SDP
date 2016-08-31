package com.sisdisper2016;

import java.net.URI;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import storage.UserInfo;

public class GatewayClient {

  public static void main(String[] args) throws IOException {
    ClientConfig config = new ClientConfig();
    Client client = ClientBuilder.newClient(config);
    WebTarget target = client.target(getBaseURI());
    Gson gson = new Gson();
    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("INSERISCI UN COMANDO:");
    while(true){
        String command = stdin.readLine();
        switch(command){
            case "misurazioni":
            Response answer = target.path("rest").path("nodes").path("misurazioni").request(MediaType.APPLICATION_JSON).get();
            ArrayList list = gson.fromJson(answer.readEntity(String.class), ArrayList.class);
            System.out.println(list);
            break;
            
            
            default:
                System.out.println("COMANDO NON RICONOSCIUTO");
                break;
        }
        
    }
    
    //String loginAnswer = target.path("rest").path("users").path("login").path(id).request().accept(MediaType.TEXT_PLAIN).get(String.class);
    //System.out.println(response);
    //System.out.println(storageAnswer.CountConnections());
    //System.out.println(storageAnswer.PrintMessage());
    //System.out.println(loginAnswer);

  }

  private static URI getBaseURI() {
    return UriBuilder.fromUri("http://localhost:8084/Gateway").build();
  }
} 

