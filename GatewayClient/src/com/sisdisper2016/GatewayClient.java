package com.sisdisper2016;

import com.google.common.reflect.TypeToken;
import java.net.URI;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
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

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        String address = args[0];
        String port = args[1];
        UserInfo userInfo = new UserInfo(args[0], args[1]);
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
            userInfo.setId(user);
            Response answer = target.path("rest").path("users").path("login").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(userInfo), MediaType.APPLICATION_JSON));
            if (answer.getStatus() == 202) {
                System.out.println("LOGIN EFFETTUATO");
                break;
            } else {
                System.out.println("NOME UTENTE GIA' IN USO SCEGLIERNE UN ALTRO");
            }

        }
        try {
            int portInt = Integer.parseInt(port);
            serverSocket = new ServerSocket(portInt);
        } catch (IOException ex) {
            System.out.println("PORTA GIA' IN USO");
            System.exit(0);
        } catch (NumberFormatException e) {
            System.out.println("PORTA NON VALIDA");
            System.exit(0);
        }
        ThreadUserServer server = new ThreadUserServer(serverSocket);
        server.start();
        System.out.println("INSERISCI UN COMANDO:");
        System.out.println("DIGITA 'help' PER VISUALIZZARE L'ELENCO DEI COMANDI DISPONIBILI");
        while (true) {
            String command = stdin.readLine();
            switch (command) {
                case "help":
                    System.out.println(
                            "ultimaMisurazioneID - visualizza l'ultima misurazione di uno specifico nodo.\n"
                            + "nodi - visualizza l'elenco dei nodi presenti nella rete.\n"
                            + "uscitaNodo - ferma e rimuole dalla rete il nodo con l'ID specificato\n"
                            + "logout - effettua il logout");
                    break;

                case "nodi":
                    queryNodi(target);
                    break;

                case "uscitaNodo":
                    System.out.print("ID: ");
                    String exitId = stdin.readLine();
                    queryUscitaNodo(target, exitId);
                    break;
                case "misurazioniTempoID":
                    System.out.print("ID: ");

                    String nodeId = stdin.readLine();
                    System.out.println("TEMPO T1:");
                    System.out.print("ORA: ");
                    String h1 = stdin.readLine();
                    System.out.print("MINUTI: ");
                    String m1 = stdin.readLine();
                    System.out.print("SECONDI: ");
                    String s1 = stdin.readLine();
                    System.out.println("TEMPO T2:");
                    System.out.print("ORA: ");
                    String h2 = stdin.readLine();
                    System.out.print("MINUTI: ");
                    String m2 = stdin.readLine();
                    System.out.print("SECONDI: ");
                    String s2 = stdin.readLine();
                    queryTimeID(target, nodeId + "-" + h1 + ":" + m1 + ":" + s1 + "-" + h2 + ":" + m2 + ":" + s2);
                    break;
                case "misurazioniTempoType":
                    System.out.print("TIPO: ");

                    String nodeType = stdin.readLine();
                    System.out.println("TEMPO T1:");
                    System.out.print("ORA: ");
                    h1 = stdin.readLine();
                    System.out.print("MINUTI: ");
                    m1 = stdin.readLine();
                    System.out.print("SECONDI: ");
                    s1 = stdin.readLine();
                    System.out.println("TEMPO T2:");
                    System.out.print("ORA: ");
                    h2 = stdin.readLine();
                    System.out.print("MINUTI: ");
                    m2 = stdin.readLine();
                    System.out.print("SECONDI: ");
                    s2 = stdin.readLine();
                    queryTimeType(target, nodeType + "-" + h1 + ":" + m1 + ":" + s1 + "-" + h2 + ":" + m2 + ":" + s2);
                    break;
                case "ultimaMisurazioneID":
                    System.out.print("ID: ");
                    String lastId = stdin.readLine();
                    queryUltimaMisurazioneID(target, lastId);
                    break;
                case "logout":
                    logout(target, userInfo);
                    System.out.println("LOGOUT EFFETTUATO, ARRIVEDERCI " + userInfo.getId());
                    System.exit(0);
                    break;

                default:
                    System.out.println("COMANDO NON RICONOSCIUTO");
                    break;
            }

        }

    }

    private static void queryUltimaMisurazioneID(WebTarget target, String id) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("ultimaMisurazioneID").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(id), MediaType.APPLICATION_JSON));
        if (answer.getStatus() == 202) {
            Measurement m = gson.fromJson(answer.readEntity(String.class), Measurement.class);
            if (m == null) {
                System.out.println("NESSUNA MISURAZIONE");
            } else {
                System.out.println("id=" + m.getId() + ", " + "type=" + m.getType() + ", " + "value=" + m.getValue() + ", " + "timestamp=" + m.getTimestamp());
            }
        } else {
            String errorLog = gson.fromJson(answer.readEntity(String.class), String.class);
            System.out.println(errorLog);
        }
    }

    public static void queryNodi(WebTarget target) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("nodi").request(MediaType.APPLICATION_JSON).get();
        Type t = new TypeToken<HashMap<String, NodoInfo>>() {
        }.getType();
        Map map = gson.fromJson(answer.readEntity(String.class), t);
        if (map.isEmpty()) {
            System.out.println("NESSUN NODO NELLA RETE");
        } else {
            for (Object o : map.keySet()) {
                System.out.println(map.get(o));
            }
        }
    }

    public static void queryTimeID(WebTarget target, String idTime) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("misurazioniTempoID").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(idTime), MediaType.APPLICATION_JSON));
        if (answer.getStatus() == 202) {
            Type t = new TypeToken<ArrayList<Measurement>>() {
            }.getType();
            List<Measurement> list = gson.fromJson(answer.readEntity(String.class), t);
            MeasurementValueComparator c = new MeasurementValueComparator();
            list.sort(c);
            Measurement max = list.get(list.size() - 1);
            Measurement min = list.get(0);
            System.out.print("MISURAZIONE MAX: ");
            System.out.println("id=" + max.getId() + ", " + "type=" + max.getType() + ", " + "value=" + max.getValue() + ", " + "timestamp=" + max.getTimestamp());
            System.out.print("MISURAZIONE MIN: ");
            System.out.println("id=" + min.getId() + ", " + "type=" + min.getType() + ", " + "value=" + min.getValue() + ", " + "timestamp=" + min.getTimestamp());
            System.out.println("MEDIA MISURAZIONI: " + mediaMisurazioni(list));
        } else {
            String errorLog = gson.fromJson(answer.readEntity(String.class), String.class);
            System.out.println(errorLog);
        }
    }

    public static void queryTimeType(WebTarget target, String typeTime) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("misurazioniTempoType").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(typeTime), MediaType.APPLICATION_JSON));
        if (answer.getStatus() == 202) {
            Type t = new TypeToken<ArrayList<Measurement>>() {
            }.getType();
            List<Measurement> list = gson.fromJson(answer.readEntity(String.class), t);
            MeasurementValueComparator c = new MeasurementValueComparator();
            list.sort(c);
            Measurement max = list.get(list.size() - 1);
            Measurement min = list.get(0);
            System.out.print("MISURAZIONE MAX: ");
            System.out.println("id=" + max.getId() + ", " + "type=" + max.getType() + ", " + "value=" + max.getValue() + ", " + "timestamp=" + max.getTimestamp());
            System.out.print("MISURAZIONE MIN: ");
            System.out.println("id=" + min.getId() + ", " + "type=" + min.getType() + ", " + "value=" + min.getValue() + ", " + "timestamp=" + min.getTimestamp());
            System.out.println("MEDIA MISURAZIONI: " + mediaMisurazioni(list));
        } else {
            String errorLog = gson.fromJson(answer.readEntity(String.class), String.class);
            System.out.println(errorLog);
        }
    }

    private static double mediaMisurazioni(List<Measurement> list) {
        double media = 0.0;
        for (Measurement m : list) {
            media += Double.parseDouble(m.getValue());
        }
        media = media / list.size();

        return media;
    }

    private static void queryUscitaNodo(WebTarget target, String id) throws IOException {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("nodoID").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(id), MediaType.APPLICATION_JSON));

        if (answer.getStatus() == 202) {
            NodoInfo nodo = gson.fromJson(answer.readEntity(String.class
            ), NodoInfo.class
            );
            System.out.println(nodo);
            Message message = new Message("exit", "", "" + "", "");
            String messageString = gson.toJson(message);
            String portString = nodo.getPort();
            int port = Integer.parseInt(portString);
            Socket clientSocket = new Socket(nodo.getAddress(), port);
            DataOutputStream outToServer = new DataOutputStream((clientSocket.getOutputStream()));
            outToServer.writeBytes(messageString + '\n');
            clientSocket.close();
        } else {
            String errorLog = gson.fromJson(answer.readEntity(String.class), String.class);
            System.out.println(errorLog);
        }
    }

    private static void logout(WebTarget target, UserInfo user) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("users").path("logout").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(user), MediaType.APPLICATION_JSON));
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8084/Gateway").build();
    }

}
