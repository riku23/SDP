/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Tozio23
 */
public class ThreadServer extends Thread {

    private Nodo nodo;

    private ServerSocket serverSocket;

    public ThreadServer(ServerSocket socket, Nodo n) {
        this.serverSocket = socket;

        this.nodo = n;
    }

    @Override
    public void run() {
        //Il thread si occupa di ricevere le richieste in arrivo sulla porta di ascolto e lanciare il relativo thread per il trattamento del messaggio
        while (true) {
            try {
                Socket estabSocket = serverSocket.accept();
                ThreadNodo threadNodo = new ThreadNodo(estabSocket, nodo);
                threadNodo.start();

            } catch (IOException ex) {
                //Logger.getLogger(ThreadServer.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("LISTENER CHIUSO");
                break;
            }

        }
    }

}
