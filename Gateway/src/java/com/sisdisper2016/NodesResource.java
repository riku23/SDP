/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
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
    public Response postRegister(String nodoString) {
        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);
        System.out.println(nodo.toString());
        //String[] nodoInfo = nodo.split("-");
        synchronized (nodes.nodiRegistrati()) {

            if (nodes.nodiRegistrati().containsKey(nodo.getId())) {

                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
                //return Response.ok(new UserInfo("OK")).build();
            } else {

                nodes.registraNodo(nodo);
                synchronized (nodes.nodiInseriti()) {
                    if (nodes.nodiInseriti().isEmpty()) {
                        nodes.inserisciNodo(nodo);
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
    public Response postEnter(String nodoString) {
        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);
        //String[] nodoInfo = nodo.split("-");
        synchronized (nodes.nodiInseriti()) {
            nodes.inserisciNodo(nodo);
            System.out.println("NODI INSERITI: " + nodes.nodiInseriti());
            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(nodes.nodiInseriti())).build();
        }
    }

    @Path("exit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postExit(String nodoString) {
        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);
        //String[] nodoInfo = nodo.split("-");
        synchronized (nodes.nodiInseriti()) {
            nodes.rimuoviNodo(nodo);
            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(Nodes.getInstance().nodiInseriti())).build();

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
        String idString = gson.fromJson(id, String.class);
        synchronized (nodes.nodiInseriti()) {
            NodoInfo nodo = nodes.nodiInseriti().get(idString);
            if (nodo == null) {
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NODO NON PRESENTE NELLA RETE")).build();
            } else {
                return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(nodo)).build();
            }
        }
    }

    @Path("misurazioniID")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryID(String id) {
        accelerometerBuffer = AccelerometerBuffer.getInstance();
        lightBuffer = LightBuffer.getInstance();
        temperatureBuffer = TemperatureBuffer.getInstance();
        nodes = Nodes.getInstance();
        Gson gson = new Gson();
        String idString = gson.fromJson(id, String.class);
        System.out.println(idString);
        NodoInfo nodo = nodes.nodiRegistrati().get(idString);
        if (nodo == null) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NODO NON PRESENTE NELLA RETE")).build();
        } else {
            String type = nodo.getType();
            switch (type) {
                case "accelerometer":
                    synchronized (accelerometerBuffer.getBuffer()) {
                        List<Measurement> list = accelerometerBuffer.getListByID(idString);
                        if (list != null) {
                            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list)).build();
                        } else {
                            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE")).build();

                        }
                    }
                case "light":
                    synchronized (lightBuffer.getBuffer()) {
                        List<Measurement> list = lightBuffer.getListByID(idString);
                        if (list != null) {
                            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list)).build();
                        } else {
                            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE")).build();

                        }
                    }
                case "temperature":
                    synchronized (temperatureBuffer.getBuffer()) {
                        List<Measurement> list = temperatureBuffer.getListByID(idString);
                        if (list != null) {
                            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list)).build();
                        } else {
                            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE")).build();

                        }
                    }
                default:
                    return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
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
        System.out.println(idString);
        NodoInfo nodo = nodes.nodiRegistrati().get(idString);
        if (nodo != null) {
            String type = nodo.getType();
            switch (type) {
                case "accelerometer":
                    synchronized (accelerometerBuffer.getBuffer()) {
                        List<Measurement> list = accelerometerBuffer.getListByID(idString);
                        if (list != null) {
                            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list.get(list.size() - 1))).build();
                        } else {
                            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE")).build();

                        }
                    }
                case "light":
                    synchronized (lightBuffer.getBuffer()) {
                        List<Measurement> list = lightBuffer.getListByID(idString);
                        if (list != null) {
                            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list.get(list.size() - 1))).build();
                        } else {
                            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE")).build();

                        }
                    }
                case "temperature":
                    synchronized (temperatureBuffer.getBuffer()) {
                        List<Measurement> list = temperatureBuffer.getListByID(idString);
                        if (list != null) {
                            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(list.get(list.size() - 1))).build();
                        } else {
                            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NESSUNA MISURAZIONE")).build();

                        }
                    }
                default:
                    return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
        } else {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson("NODO NON PRESENTE NELLA RETE")).build();
        }
    }

    @Path("misurazioniType")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryType(String type) {
        accelerometerBuffer = AccelerometerBuffer.getInstance();
        lightBuffer = LightBuffer.getInstance();
        temperatureBuffer = TemperatureBuffer.getInstance();

        Gson gson = new Gson();
        String typeString = gson.fromJson(type, String.class);
        System.out.println(typeString);
        switch (typeString) {
            case "accelerometer":
                synchronized (accelerometerBuffer.getBuffer()) {
                    return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(accelerometerBuffer.getBuffer())).build();
                }
            case "light":
                synchronized (lightBuffer.getBuffer()) {
                    return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(lightBuffer.getBuffer())).build();
                }
            case "temperature":
                synchronized (temperatureBuffer.getBuffer()) {
                    return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(temperatureBuffer.getBuffer())).build();
                }
            default:
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

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
