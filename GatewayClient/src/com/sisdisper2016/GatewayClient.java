package com.sisdisper2016;

import com.google.common.reflect.TypeToken;
import java.net.URI;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.ProcessingException;
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
        Response answer = null;
        WebTarget target = null;
        ServerSocket serverSocket = new ServerSocket(0);;
        String gatewayAddress = "";
        String gatewayPort;
        String userAddress = InetAddress.getLocalHost().getHostAddress();
        String userPort = "" + serverSocket.getLocalPort();
        UserInfo userInfo = new UserInfo(userAddress, userPort);
        boolean logged = false;
        boolean validate = false;

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("LOGIN");
        while (!logged) {

            System.out.print("INSERISCI NOME UTENTE: ");
            String user = stdin.readLine();
            System.out.print("INSERISCI INDIRIZZO GATEWAY: ");
            gatewayAddress = stdin.readLine();
            System.out.print("INSERISCI PORTA GATEWAY: ");
            gatewayPort = stdin.readLine();
            

            userInfo.setId(user);
            ClientConfig config = new ClientConfig();
            Client client = ClientBuilder.newClient(config);
            target = client.target(getBaseURI(gatewayAddress + ":" + gatewayPort));
            Gson gson = new Gson();
            if (validateGatewayAddress(gatewayAddress, gatewayPort)) {
                try {
                    answer = target.path("rest").path("users").path("login").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(userInfo), MediaType.APPLICATION_JSON));
                } catch (ProcessingException e) {
                    System.out.println("ERRORE NELLA CONNESSIONE");
                }
            }

            if (answer != null) {
                if (answer.getStatus() == 202) {
                    System.out.println("LOGIN EFFETTUATO");
                    break;
                } else {
                    System.out.println("NOME UTENTE GIA' IN USO SCEGLIERNE UN ALTRO");
                }

            }

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
                case "creazioneNodo":
                    System.out.print("ID: ");
                    String enterId = stdin.readLine();
                    System.out.print("TIPO: ");
                    String enterType = stdin.readLine();
                    System.out.print("PORTA: ");
                    String enterPort = stdin.readLine();
                    if (validateCreationInput(enterType, gatewayAddress, enterPort)) {
                        queryCreazioneNodo(target, enterId, enterType, gatewayAddress, enterPort);
                    } else {
                        System.out.println("DATI NON VALIDI");
                    }
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

    private static boolean validateCreationInput(String type, String address, String port) {
        if (!type.equals("accelerometer") && !type.equals("light") && !type.equals("temperature")) {
            return false;
        }
        try {
            int portInt = Integer.parseInt(port);
            if (portInt <= 0 || portInt > 65535) {
                return false;
            }
        } catch (NumberFormatException ex) {
            return false;
        }

        return true;

    }

    private static long computeMidnightMilliseconds() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static Date millisToDate(long millis) {
        Calendar c = Calendar.getInstance();
        long midnight = computeMidnightMilliseconds();
        c.setTimeInMillis(midnight + millis);
        return c.getTime();
    }

    private static void queryUltimaMisurazioneID(WebTarget target, String id) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("ultimaMisurazioneID").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(id), MediaType.APPLICATION_JSON));
        if (answer.getStatus() == 202) {
            Measurement m = gson.fromJson(answer.readEntity(String.class), Measurement.class);
            if (m == null) {
                System.out.println("NESSUNA MISURAZIONE");
            } else {
                System.out.println("id=" + m.getId() + ", " + "type=" + m.getType() + ", " + "value=" + m.getValue() + ", " + "timestamp=" + millisToDate(m.getTimestamp()));
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
            System.out.println("id=" + max.getId() + ", " + "type=" + max.getType() + ", " + "value=" + max.getValue() + ", " + "timestamp=" + millisToDate(max.getTimestamp()));
            System.out.print("MISURAZIONE MIN: ");
            System.out.println("id=" + min.getId() + ", " + "type=" + min.getType() + ", " + "value=" + min.getValue() + ", " + "timestamp=" + millisToDate(min.getTimestamp()));
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
            System.out.println("id=" + max.getId() + ", " + "type=" + max.getType() + ", " + "value=" + max.getValue() + ", " + "timestamp=" + millisToDate(max.getTimestamp()));
            System.out.print("MISURAZIONE MIN: ");
            System.out.println("id=" + min.getId() + ", " + "type=" + min.getType() + ", " + "value=" + min.getValue() + ", " + "timestamp=" + millisToDate(min.getTimestamp()));
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

    private static void queryCreazioneNodo(WebTarget target, String id, String type, String address, String port) {
        NodoInfo nodo = new NodoInfo(id, type, new Date(), address, port);
        Gson gson = new Gson();
        Response answer = target.path("rest").path("nodes").path("create").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(nodo), MediaType.APPLICATION_JSON));
        String log = gson.fromJson(answer.readEntity(String.class), String.class);
        System.out.println(log);
    }

    private static void queryUscitaNodo(WebTarget target, String id) throws IOException {
        Gson gson = new Gson();
        String log;
        Response answer = target.path("rest").path("nodes").path("delete").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(id), MediaType.APPLICATION_JSON));

        if (answer.getStatus() == 202) {
            log = gson.fromJson(answer.readEntity(String.class), String.class);
        } else {
            log = gson.fromJson(answer.readEntity(String.class), String.class);

        }
        System.out.println(log);
    }

    private static void logout(WebTarget target, UserInfo user) {
        Gson gson = new Gson();
        Response answer = target.path("rest").path("users").path("logout").request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(user), MediaType.APPLICATION_JSON));
    }

    private static URI getBaseURI(String address) {
        return UriBuilder.fromUri("http://" + address + "/Gateway").build();
    }

    private static boolean validateGatewayAddress(String gatewayAddress, String gatewayPort) {
        try {
                InetAddress.getByName(gatewayAddress);

            } catch (UnknownHostException e1) {
                try {
                    InetAddress.getByAddress(gatewayAddress.getBytes());
                } catch (UnknownHostException e2) {
                    System.out.println("INDIRIZZO GATEWAY NON VALIDO");
                    return false;

                }
            }
            try {
                int portInt = Integer.parseInt(gatewayPort);
                if (portInt <= 0 || portInt > 65535) {
                    System.out.println("PORTA GATEWAY NON VALIDA");
                    return false;

                }
            } catch (NumberFormatException ex) {
                System.out.println("PORTA GATEWAY NON VALIDA");
                return false;

            }
            return true;
    }

}
