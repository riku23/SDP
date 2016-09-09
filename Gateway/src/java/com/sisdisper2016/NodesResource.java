/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import com.google.gson.Gson;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author Tozio23
 */
@Path("nodes")
public class NodesResource {

    @Context
    private UriInfo context;
    private AccelerometerBuffer accelerometerBuffer;
    private LightBuffer lightBuffer;
    private TemperatureBuffer temperatureBuffer;
    private Nodes nodes;
    private Users users;

    /**
     * Creates a new instance of GenericResource
     */
    public NodesResource() {
    }

    /**
     * Retrieves representation of an instance of
     * com.sisdisper2016.gateway.NodesResource
     *
     * @param nodoString
     * @return an instance of java.lang.String
     */
    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postRegister(String nodoString) throws IOException {
        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);

        //String[] nodoInfo = nodo.split("-");
        synchronized (nodes.nodiRegistrati()) {

            if (nodes.nodiRegistrati().containsKey(nodo.getId())) {

                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            } else {
                System.out.println("REGISTRZIONE NODO: " + nodo.toString());
                nodes.registraNodo(nodo);
                synchronized (nodes.nodiInseriti()) {
                    if (nodes.nodiInseriti().isEmpty()) {
                        nodes.inserisciNodo(nodo);
                        broadcastUtenti(nodo, "nodeEnter");
                        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(new HashMap<String, NodoInfo>())).build();
                    }
                }
                System.out.println("NODI REGISTRATI: " + nodes.nodiRegistrati());
                return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(nodes.nodiInseriti())).build();
            }
        }
    }

    @Path("retry")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postRegisterRetry(String nodoString) throws IOException {
        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);
        System.out.println("NUOVO TENTATIVO REGISTRZIONE NODO: " + nodo.toString());
        //String[] nodoInfo = nodo.split("-");
        synchronized (nodes.nodiRegistrati()) {

            if (!nodes.nodiRegistrati().containsKey(nodo.getId())) {

                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
                //return Response.ok(new UserInfo("OK")).build();
            } else {

                nodes.registraNodo(nodo);
                synchronized (nodes.nodiInseriti()) {
                    if (nodes.nodiInseriti().isEmpty()) {
                        nodes.inserisciNodo(nodo);
                        broadcastUtenti(nodo, "nodeEnter");
                        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(new HashMap<String, NodoInfo>())).build();
                    }
                }
                System.out.println("NODI REGISTRATI: " + nodes.nodiRegistrati());
                return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(nodes.nodiInseriti())).build();
            }
        }
    }

    @Path("enter")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postEnter(String nodoString) throws IOException {
        nodes = Nodes.getInstance();
        users = Users.getInstance();
        HashMap<String, UserInfo> temp;
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);
        //String[] nodoInfo = nodo.split("-");
        synchronized (nodes.nodiInseriti()) {
            nodes.inserisciNodo(nodo);
            System.out.println("NODI INSERITI: " + nodes.nodiInseriti());
        }
        broadcastUtenti(nodo, "nodeEnter");
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(nodes.nodiInseriti())).build();

    }

    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postCreate(String nodoString) throws IOException {
        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);

        synchronized (nodes.nodiRegistratiClient()) {
            if (nodes.nodiRegistratiClient().contains(nodo.getId())) {
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("ID NODO GIA' PRESENTE")).build();
            } else {

                nodes.nodiRegistratiClient().add(nodo.getId());
                if (available(nodo.getAddress(), Integer.parseInt(nodo.getPort()))) {
                    System.out.println("CREAZIONE NODO: " + nodo.toString());
                    Process proc = Runtime.getRuntime().exec("java -jar D:\\Documenti\\NetBeansProjects\\ReteDiSensori\\dist\\ReteDiSensori.jar "
                            + nodo.getId() + " " + nodo.getType() + " " + nodo.getPort() + " " + "localhost:8084");

                    return Response.status(Response.Status.ACCEPTED).entity(gson.toJson("NODO IN CREAZIONE")).build();
                } else {
                    synchronized (nodes.nodiRegistratiClient()) {
                        nodes.nodiRegistratiClient().remove(nodo.getId());
                    }
                    return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("PORTA GIA' IN USO")).build();
                }
            }
        }
    }

    private static boolean available(String address, int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            ignored.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @Path("delete")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postDelete(String nodoString) throws IOException {
        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        String nodoId = gson.fromJson(nodoString, String.class);
        synchronized (nodes.nodiInseriti()) {
            if (nodes.nodiInseriti().containsKey(nodoId)) {
                NodoInfo nodo = nodes.nodiInseriti().get(nodoId);
                Message message = new Message("exit", "", "" + "", "");
                String messageString = gson.toJson(message);
                String portString = nodo.getPort();
                int port = Integer.parseInt(portString);
                Socket clientSocket = new Socket(nodo.getAddress(), port);
                DataOutputStream outToServer = new DataOutputStream((clientSocket.getOutputStream()));
                outToServer.writeBytes(messageString + '\n');
                clientSocket.close();
                return Response.status(Response.Status.ACCEPTED).entity(gson.toJson("NODO IN ELIMINAZIONE")).build();
            } else {
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NODO NON PRESENTE NELLA RETE")).build();
            }
        }
    }

    @Path("exit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postExit(String nodoString) throws IOException {
        nodes = Nodes.getInstance();
        users = Users.getInstance();
        HashMap<String, UserInfo> temp;
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);
        //String[] nodoInfo = nodo.split("-");
        synchronized (nodes.nodiInseriti()) {
            nodes.rimuoviNodo(nodo);
        }
        System.out.println("ELIMINAZIONE NODO: " + nodo.toString());
        broadcastUtenti(nodo, "nodeExit");
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(Nodes.getInstance().nodiInseriti())).build();
    }

    public void broadcastUtenti(NodoInfo nodo, String header) throws IOException {
        users = Users.getInstance();
        HashMap<String, UserInfo> temp;
        Gson gson = new Gson();
        if (!users.getUsers().isEmpty()) {
            synchronized (users.getUsers()) {
                temp = new HashMap<>(users.getUsers());
            }
            for (String s : temp.keySet()) {
                Message message = new Message(header, "", "", gson.toJson(nodo));
                String messageString = gson.toJson(message);
                String address = users.getUsers().get(s).getAddress();
                String portString = users.getUsers().get(s).getPort();
                int port = Integer.parseInt(portString);
                Socket clientSocket = new Socket(address, port);
                DataOutputStream outToServer = new DataOutputStream((clientSocket.getOutputStream()));
                outToServer.writeBytes(messageString + '\n');
                clientSocket.close();
            }
        }
    }

    @Path("token")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postToken(String tokenString) {
        accelerometerBuffer = AccelerometerBuffer.getInstance();
        lightBuffer = LightBuffer.getInstance();
        temperatureBuffer = TemperatureBuffer.getInstance();
        Gson gson = new Gson();
        Token token = gson.fromJson(tokenString, Token.class);
        for (Measurement m : token.getBuffer()) {
            switch (m.getType()) {

                case "accelerometer":
                    synchronized (accelerometerBuffer.getBuffer()) {
                        accelerometerBuffer.addMisurazione(m);
                        break;
                    }
                case "light":
                    synchronized (lightBuffer.getBuffer()) {
                        lightBuffer.addMisurazione(m);
                        break;
                    }
                case "temperature":
                    synchronized (temperatureBuffer.getBuffer()) {
                        temperatureBuffer.addMisurazione(m);
                        break;
                    }
            }
        }
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(accelerometerBuffer.getMisurazioni() + lightBuffer.getMisurazioni() + temperatureBuffer.getMisurazioni())).build();

    }

    @Path("misurazioni")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryMeasurement() {
        accelerometerBuffer = AccelerometerBuffer.getInstance();
        lightBuffer = LightBuffer.getInstance();
        temperatureBuffer = TemperatureBuffer.getInstance();
        Gson gson = new Gson();
        HashMap<String, List<Measurement>> measurementBuffer = new HashMap<>();
        synchronized (accelerometerBuffer.getBuffer()) {
            measurementBuffer.putAll(accelerometerBuffer.getBuffer());
        }
        synchronized (lightBuffer.getBuffer()) {
            measurementBuffer.putAll(lightBuffer.getBuffer());
        }
        synchronized (temperatureBuffer.getBuffer()) {
            measurementBuffer.putAll(temperatureBuffer.getBuffer());
        }
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(measurementBuffer)).build();
    }

    @Path("nodi")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryNodes() {

        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        synchronized (nodes.nodiInseriti()) {
            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(nodes.nodiInseriti())).build();
        }
    }

    @Path("nodoID")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryNode(String id) {

        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        NodoInfo nodo;
        String idString = gson.fromJson(id, String.class);
        synchronized (nodes.nodiInseriti()) {
            nodo = nodes.nodiInseriti().get(idString);
        }
        if (nodo == null) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NODO NON PRESENTE NELLA RETE")).build();
        } else {
            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(nodo)).build();
        }
    }

    @Path("ultimaMisurazioneID")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryLastID(String id) {
        accelerometerBuffer = AccelerometerBuffer.getInstance();
        lightBuffer = LightBuffer.getInstance();
        temperatureBuffer = TemperatureBuffer.getInstance();
        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        String idString = gson.fromJson(id, String.class);
        List<Measurement> list;
        MeasurementComparator c = new MeasurementComparator();
        System.out.println(idString);
        NodoInfo nodo = nodes.nodiRegistrati().get(idString);
        if (nodo != null) {
            String type = nodo.getType();
            switch (type) {
                case "accelerometer":
                    synchronized (accelerometerBuffer.getBuffer()) {
                        list = accelerometerBuffer.getListByID(idString);
                    }

                    if (list != null) {
                        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list.get(list.size() - 1))).build();
                    } else {
                        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE")).build();

                    }

                case "light":
                    synchronized (lightBuffer.getBuffer()) {
                        list = lightBuffer.getListByID(idString);
                    }

                    if (list != null) {
                        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list.get(list.size() - 1))).build();
                    } else {
                        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE")).build();

                    }

                case "temperature":
                    synchronized (temperatureBuffer.getBuffer()) {
                        list = temperatureBuffer.getListByID(idString);
                    }

                    if (list != null) {
                        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list.get(list.size() - 1))).build();
                    } else {
                        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE")).build();

                    }

                default:
                    return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
        } else {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NODO NON PRESENTE NELLA RETE")).build();
        }
    }

    @Path("misurazioniTempoID")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryTimeID(String message) {
        accelerometerBuffer = AccelerometerBuffer.getInstance();
        lightBuffer = LightBuffer.getInstance();
        temperatureBuffer = TemperatureBuffer.getInstance();
        nodes = Nodes.getInstance();
        List<Measurement> list = new ArrayList<>();
        List<Measurement> temp;
        Map<String, NodoInfo> tempMap;

        Gson gson = new Gson();
        String messageString = gson.fromJson(message, String.class);
        String[] messageSplit = messageString.split("-");
        String id = messageSplit[0];
        String t1 = messageSplit[1];
        System.out.println(t1);
        String t2 = messageSplit[2];
        System.out.println(t2);
        String h1 = t1.split(":")[0];
        String m1 = t1.split(":")[1];
        String s1 = t1.split(":")[2];
        String h2 = t2.split(":")[0];
        String m2 = t2.split(":")[1];
        String s2 = t2.split(":")[2];
        long timestamp1 = stringToMillis(h1, m1, s1);
        System.out.println(timestamp1);
        long timestamp2 = stringToMillis(h2, m2, s2);
        System.out.println(timestamp2);
        if (timestamp1 == -1 || timestamp2 == -1 || timestamp2 < timestamp1) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("INTERVALLO NON VALIDO")).build();
        }
        synchronized (nodes.nodiRegistrati()) {
            tempMap = nodes.nodiRegistrati();

        }
        if (tempMap.keySet().contains(id)) {
            switch (tempMap.get(id).getType()) {
                case "accelerometer":
                    if (accelerometerBuffer.getListByID(id) != null) {
                        synchronized (accelerometerBuffer.getListByID(id)) {
                            temp = new ArrayList<>(accelerometerBuffer.getListByID(id));
                        }
                        for (Measurement measurement : temp) {

                            int intTimestamp = (int) ((measurement.getTimestamp() / 1000) * 1000);

                            if ((long) intTimestamp > timestamp1 && (long) intTimestamp < timestamp2) {
                                list.add(measurement);
                            }
                        }

                        if (!list.isEmpty()) {
                            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list)).build();
                        } else {
                            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE NEL RANGE TEMPORALE")).build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE REGISTRATA")).build();
                    }

                case "light":
                    if (lightBuffer.getListByID(id) != null) {
                        synchronized (lightBuffer.getListByID(id)) {
                            temp = new ArrayList<>(lightBuffer.getListByID(id));
                        }
                        for (Measurement measurement : temp) {

                            int intTimestamp = (int) ((measurement.getTimestamp() / 1000) * 1000);

                            if ((long) intTimestamp > timestamp1 && (long) intTimestamp < timestamp2) {
                                list.add(measurement);
                            }
                        }

                        if (!list.isEmpty()) {
                            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list)).build();
                        } else {
                            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE NEL RANGE TEMPORALE")).build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE REGISTRATA")).build();
                    }

                case "temperature":
                    if (temperatureBuffer.getListByID(id) != null) {
                        synchronized (temperatureBuffer.getListByID(id)) {
                            temp = new ArrayList<>(temperatureBuffer.getListByID(id));
                        }
                        for (Measurement measurement : temp) {

                            int intTimestamp = (int) ((measurement.getTimestamp() / 1000) * 1000);

                            if ((long) intTimestamp > timestamp1 && (long) intTimestamp < timestamp2) {
                                list.add(measurement);
                            }
                        }

                        if (!list.isEmpty()) {
                            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list)).build();
                        } else {
                            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE NEL RANGE TEMPORALE")).build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE REGISTRATA")).build();
                    }
                default:
                    return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("ERRORE SUL TIPO")).build();

            }
        } else {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NODO NON PRESENTE NELLA RETE")).build();
        }

    }

    @Path("misurazioniTempoType")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryTimeType(String message) {
        accelerometerBuffer = AccelerometerBuffer.getInstance();
        lightBuffer = LightBuffer.getInstance();
        temperatureBuffer = TemperatureBuffer.getInstance();
        nodes = Nodes.getInstance();
        List<Measurement> list = new ArrayList<>();
        List<Measurement> temp;
        Gson gson = new Gson();
        String messageString = gson.fromJson(message, String.class);
        String[] messageSplit = messageString.split("-");
        String type = messageSplit[0];
        String t1 = messageSplit[1];
        System.out.println(t1);
        String t2 = messageSplit[2];
        System.out.println(t2);
        String h1 = t1.split(":")[0];
        String m1 = t1.split(":")[1];
        String s1 = t1.split(":")[2];
        String h2 = t2.split(":")[0];
        String m2 = t2.split(":")[1];
        String s2 = t2.split(":")[2];
        long timestamp1 = stringToMillis(h1, m1, s1);
        System.out.println(timestamp1);
        long timestamp2 = stringToMillis(h2, m2, s2);
        System.out.println(timestamp2);
        if (timestamp1 == -1 || timestamp2 == -1 || timestamp2 < timestamp1) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("INTERVALLO NON VALIDO")).build();
        }

        switch (type) {
            case "accelerometer":
                if (!accelerometerBuffer.getList().isEmpty()) {
                    synchronized (accelerometerBuffer.getList()) {
                        temp = new ArrayList<>(accelerometerBuffer.getList());
                    }
                    for (Measurement measurement : temp) {

                        int intTimestamp = (int) ((measurement.getTimestamp() / 1000) * 1000);

                        if ((long) intTimestamp > timestamp1 && (long) intTimestamp < timestamp2) {
                            list.add(measurement);
                        }
                    }

                    if (!list.isEmpty()) {
                        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list)).build();
                    } else {
                        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE NEL RANGE TEMPORALE")).build();
                    }
                } else {
                    return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE REGISTRATA")).build();
                }

            case "light":
                if (!lightBuffer.getList().isEmpty()) {
                    synchronized (lightBuffer.getList()) {
                        temp = new ArrayList<>(lightBuffer.getList());
                    }
                    for (Measurement measurement : temp) {

                        int intTimestamp = (int) ((measurement.getTimestamp() / 1000) * 1000);

                        if ((long) intTimestamp > timestamp1 && (long) intTimestamp < timestamp2) {
                            list.add(measurement);
                        }
                    }

                    if (!list.isEmpty()) {
                        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list)).build();
                    } else {
                        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE NEL RANGE TEMPORALE")).build();
                    }
                } else {
                    return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE REGISTRATA")).build();
                }

            case "temperature":
                if (!temperatureBuffer.getList().isEmpty()) {
                    synchronized (temperatureBuffer.getList()) {
                        temp = new ArrayList<>(temperatureBuffer.getList());
                    }
                    for (Measurement measurement : temp) {

                        int intTimestamp = (int) ((measurement.getTimestamp() / 1000) * 1000);

                        if ((long) intTimestamp > timestamp1 && (long) intTimestamp < timestamp2) {
                            list.add(measurement);
                        }
                    }

                    if (!list.isEmpty()) {
                        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list)).build();
                    } else {
                        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE NEL RANGE TEMPORALE")).build();
                    }
                } else {
                    return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE REGISTRATA")).build();
                }
            default:
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("ERRORE SUL TIPO")).build();

        }

    }

    private long computeMidnightMilliseconds() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long stringToMillis(String h, String m, String s) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        long timestamp = 0;
        int ora = 0;
        int min = 0;
        int sec = 0;
        try {
            ora = Integer.parseInt(h);
            min = Integer.parseInt(m);
            sec = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
        if (ora < 0 || ora >= 24) {
            return -1;
        }
        if (min < 0 || min >= 60) {
            return -1;
        }
        if (sec < 0 || sec >= 60) {
            return -1;
        }
        String date = c.get(Calendar.DAY_OF_MONTH) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR) + " " + h + ":" + m + ":" + s;

        try {
            Date d = df.parse(date);
            timestamp = d.getTime() - computeMidnightMilliseconds();

        } catch (ParseException ex) {
            System.out.println("DATA MAL FORMATA");
            return -1;
        }
        return timestamp;
    }

    private Date millisToDate(long millis) {
        Calendar c = Calendar.getInstance();
        long midnight = computeMidnightMilliseconds();
        c.setTimeInMillis(midnight + millis);
        return c.getTime();
    }

    /**
     * PUT method for updating or creating an instance of NodesResource
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    public void putXml(String content) {
    }

}
