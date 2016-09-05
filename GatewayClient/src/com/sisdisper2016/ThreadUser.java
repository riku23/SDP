/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tozio23
 */
public class ThreadUser extends Thread {
    
    private Socket estabSocket;
        private String clientSentence;
    public ThreadUser(Socket socket){
        this.estabSocket = socket;
    }
    
    @Override
    public void run(){
        try {
            
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader((estabSocket.getInputStream())));
            DataOutputStream outToClient = new DataOutputStream(estabSocket.getOutputStream());
            clientSentence = inFromClient.readLine();
            Gson gson = new Gson();
            Message messageIn = gson.fromJson(clientSentence, Message.class);

            String header = messageIn.getHeader();
            String senderAddr = messageIn.getSenderAddr();
            String senderPort = messageIn.getSenderPort();
            String body = messageIn.getBody();
           if(header.equals("nodeEnter")){
               NodoInfo nodo = gson.fromJson(body, NodoInfo.class);
               System.out.println("INSERITO NODO: "+ nodo);
           }
           if(header.equals("nodeExit")){
               NodoInfo nodo = gson.fromJson(body, NodoInfo.class);
               System.out.println("USCITO NODO: "+ nodo);
           }    
      
           
                

        } catch (IOException ex) {
            Logger.getLogger(ThreadUser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
