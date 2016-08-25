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
     * Retrieves representation of an instance of com.sisdisper2016.gateway.NodesResource
     * @return an instance of java.lang.String
     */

    
    
    @Path("enter")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postTest(String nodoString){
        Gson gson = new Gson();
           String nodo = gson.fromJson(nodoString, String.class);
            System.out.println(nodo);
            String[] nodoInfo = nodo.split("-");
        if(Nodes.getInstance().getList().contains(nodo)){
            
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        //return Response.ok(new UserInfo("OK")).build();
        }else{
            
            Nodes.getInstance().addNode(nodoInfo);
            
            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(Nodes.getInstance().getList())).build();
        }
    }
    
    @Path("token")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response tokenTest(String tokenString){
        Gson gson = new Gson();
        Token token = gson.fromJson(tokenString, Token.class);
        if(!GatewayBuffer.getInstance().addMisurazioni(token.getBuffer())){
            
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        
        }else{
            
            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(GatewayBuffer.getInstance().getMisurazioni())).build();
        
        }
    }
    
    @Path("misurazioni")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response clientTest(){
        Gson gson = new Gson();
        return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(GatewayBuffer.getInstance().getList())).build();
    }
    
  
    /**
     * PUT method for updating or creating an instance of NodesResource
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    public void putXml(String content) {
    }
    
    
  
}