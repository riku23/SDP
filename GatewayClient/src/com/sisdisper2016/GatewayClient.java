package com.sisdisper2016;

import com.google.common.reflect.TypeToken;
import java.net.URI;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

public class GatewayClient {

    public static void main(String[] args) throws IOException{

        String userID = "";
        boolean logged = false;
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        Gson gson = new Gson();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("LOGIN");
        while (!logged) {
            
            System.out.print("INSERISCI NOME UTENTE: ");
            String user = stdin.readLine();
            Response answer = target.path("rest").path("users").path("login").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(user), MediaType.APPLICATION_JSON));
            if (answer.getStatus() == 202) {
                System.out.println("LOGIN EFFETTUATO");
                logged = true;
                userID = user;
                break;
            } else {
                System.out.println("NOME UTENTE GIA' IN USO SCEGLIERNE UN ALTRO");
            }
        
      
        }
        System.out.println("INSERISCI UN COMANDO:");
        System.out.println("DIGITA 'help' PER VISUALIZZARE L'ELENCO DEI COMANDI DISPONIBILI");
        while (true) {
            String command = stdin.readLine();
            switch (command) {
                case "help":
                    System.out.println("misurazioni - visualizza le misurazioni registrate.\n"
                            + "misurazioniID - visualizza le misurazioni di uno specifico sensore.\n"
                            + "misurazioniTipo - visualizza le misurazioni di una specifica tipoliga di sensore.\n"
                            + "nodi - visualizza l'elenco dei nodi presenti nella rete.\n"
                            + "logout - effettua il logout");
                    break;
                case "misurazioni":
                    queryMisurazioni(target);
                    break;

                case "nodi":
                    queryNodi(target);
                    break;
                case "misurazioniID":
                    System.out.print("ID: ");
                    String id = stdin.readLine();
                    queryMisurazioniID(target, id);
                    break;
                case "misurazioniTipo":
                    System.out.print("TIPO: ");
                    String type = stdin.readLine();
                    queryMisurazioniType(target, type);
                    break;
                case "logout":
                    logout(target, userID);
                    System.out.println("LOGOUT EFFETTUATO, ARRIVEDERCI " + userID);
                    System.exit(0);
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

    public static void queryMisurazioni(WebTarget target) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("misurazioni").request(MediaType.APPLICATION_JSON).get();
        Type t = new TypeToken<HashMap<String,List<Measurement>>>(){}.getType();
        Map map = gson.fromJson(answer.readEntity(String.class), t);
        if (map.isEmpty()) {
            System.out.println("NESSUNA MISURAZIONE");
        } else {
            for (Object id : map.keySet()) {
                System.out.println("ID NODO: " + id);
                System.out.println("MISURAZIONI:");
                for(Measurement m : (List<Measurement>)map.get(id)){
                System.out.println("id="+m.getId()+", "+"type="+m.getType()+", "+"value="+m.getValue()+", "+"timestamp="+m.getTimestamp());
                }
            }
        }
    }

    private static void queryMisurazioniID(WebTarget target, String id) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("misurazioniID").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(id), MediaType.APPLICATION_JSON));
        ArrayList list = gson.fromJson(answer.readEntity(String.class), ArrayList.class);
        if (list.isEmpty()) {
            System.out.println("NESSUNA MISURAZIONE");
        } else {
            for (Object o : list) {
                System.out.println(o);
            }
        }
    }

    public static void queryNodi(WebTarget target) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("nodi").request(MediaType.APPLICATION_JSON).get();
        Type t = new TypeToken<HashMap<String,NodoInfo>>(){}.getType();
        Map map = gson.fromJson(answer.readEntity(String.class), t);
        if (map.isEmpty()) {
            System.out.println("NESSUN NODO NELLA RETE");
        } else {
            for (Object o : map.keySet()) {
                System.out.println(map.get(o));
            }
        }
    }

    private static void queryMisurazioniType(WebTarget target, String type) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("misurazioniType").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(type), MediaType.APPLICATION_JSON));
        Type t = new TypeToken<HashMap<String,List<Measurement>>>(){}.getType();
        Map map = gson.fromJson(answer.readEntity(String.class), t);
        if (map.isEmpty()) {
            System.out.println("NESSUNA MISURAZIONE");
        } else {
            for (Object id : map.keySet()) {
                System.out.println("ID NODO: " + id);
                System.out.println("MISURAZIONI:");
                for(Measurement m : (List<Measurement>)map.get(id)){
                System.out.println("id="+m.getId()+", "+"type="+m.getType()+", "+"value="+m.getValue()+", "+"timestamp="+m.getTimestamp());
                }
            }
        }
    }

    private static void logout(WebTarget target, String user) {
            Gson gson = new Gson();
            Response answer = target.path("rest").path("users").path("logout").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(user), MediaType.APPLICATION_JSON));
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8084/Gateway").build();
    }

}
