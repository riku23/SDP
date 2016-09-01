/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import com.google.gson.Gson;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
    private String[] utenti = {"id5", "id6"};

    /**
     * Creates a new instance of GenericResource
     */
    public NodesResource() {
    }

    /**
     * Retrieves representation of an instance of
     * com.sisdisper2016.gateway.NodesResource
     *
     * @return an instance of java.lang.String
     */

    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response postRegister(String nodoString) {
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);
        System.out.println(nodo.toString());
        //String[] nodoInfo = nodo.split("-");
        if (Nodes.getInstance().nodiRegistrati().contains(nodo)) {

            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            //return Response.ok(new UserInfo("OK")).build();
        } else {

            Nodes.getInstance().registraNodo(nodo);

            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(Nodes.getInstance().nodiInseriti())).build();
        }
    }

    @Path("enter")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response postEnter(String nodoString) {
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);
        //String[] nodoInfo = nodo.split("-");
        Nodes.getInstance().inserisciNodo(nodo);
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(Nodes.getInstance().nodiInseriti())).build();
    }

    @Path("exit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postExit(String nodoString) {
        Gson gson = new Gson();
        NodoInfo nodo = gson.fromJson(nodoString, NodoInfo.class);
        //String[] nodoInfo = nodo.split("-");
        Nodes.getInstance().rimuoviNodo(nodo);
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(Nodes.getInstance().nodiInseriti())).build();
    }

    @Path("token")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postToken(String tokenString) {
        Gson gson = new Gson();
        Token token = gson.fromJson(tokenString, Token.class);
        if (!GatewayBuffer.getInstance().addMisurazioni(token.getBuffer())) {

            return Response.status(Response.Status.NOT_ACCEPTABLE).build();

        } else {

            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(GatewayBuffer.getInstance().getMisurazioni())).build();

        }
    }

    @Path("misurazioni")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryMeasurement() {
        Gson gson = new Gson();
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(GatewayBuffer.getInstance().getList())).build();
    }

    @Path("nodi")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryNodes() {
        Gson gson = new Gson();
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(Nodes.getInstance().nodiInseriti())).build();
    }

    @Path("misurazioniID")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryID(String id) {
        Gson gson = new Gson();
        String idString = gson.fromJson(id, String.class);
        System.out.println(idString);
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(GatewayBuffer.getInstance().getListByID(idString))).build();
    }

    @Path("misurazioniType")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientQueryType(String type) {

        Gson gson = new Gson();
        String typeString = gson.fromJson(type, String.class);
        System.out.println(typeString);

        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(GatewayBuffer.getInstance().getListByType(typeString))).build();
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
