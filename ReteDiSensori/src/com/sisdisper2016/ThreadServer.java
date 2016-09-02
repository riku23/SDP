/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tozio23
 */
public class ThreadServer extends Thread {

    private Nodo nodo;
    private String threadType;
    private ServerSocket serverSocket;

    public ThreadServer(ServerSocket socket, String type, Nodo n) {
        this.serverSocket = socket;
        this.threadType = type;
        this.nodo = n;
    }

    @Override
    public void run() {

        while (true) {
            try {
                Socket estabSocket = serverSocket.accept();
                ThreadNodo threadNodo = new ThreadNodo(estabSocket, "server", nodo);
                threadNodo.start();

            } catch (IOException ex) {
                Logger.getLogger(ThreadServer.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }

        }
    }

}
