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
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
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
@Path("users")
public class UsersResource {

    @Context
    private UriInfo context;
    private Users users;

    /**
     * Creates a new instance of GenericResource
     */
    public UsersResource() {
        users = Users.getInstance();
    }

    /**
     * Retrieves representation of an instance of
     * com.sisdisper2016.gateway.NodesResource
     *
     * @return an instance of java.lang.String
     */
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postLogin(String userString) {
        //Chiamata REST per il login di un utente
        Gson gson = new Gson();
        UserInfo user = gson.fromJson(userString, UserInfo.class);
        
        //Sincronizzo sulla struttura dati degli utenti registrati per aggiungerci l'utente se l'ID scelto non è già in uso
        synchronized (users.getUsers()) {
            if (users.getUsers().containsKey(user.getId())) {

                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
                //return Response.ok(new UserInfo("OK")).build();
            } else {

                users.registraUtente(user);
                System.out.println("LOGIN UTENTE: " + user);
                return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(users.getUsers())).build();
            }
        }
    }

    @Path("logout")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postExit(String userString) {
        //Chiamata REST per il logout dell'utente
        Gson gson = new Gson();
        UserInfo user = gson.fromJson(userString, UserInfo.class);
        //Sincronizzo sulla struttura dati degli utenti e rimuovo l'id selezionato
        synchronized (users.getUsers()) {
            users.logout(user);
            System.out.println("LOGOUT UTENTE: " + user);
            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(users.getUsers())).build();
        }
    }

    @Path("users")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(){
        //Chiamata REST che mi restituisce tutti gli utenti registrati sul gateway
        Gson gson = new Gson();
        synchronized(users.getUsers()){
            
            return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(users.getUsers())).build();
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
