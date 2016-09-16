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
public class ThreadUserServer extends Thread {

    private ServerSocket serverSocket;

    public ThreadUserServer(ServerSocket socket) {
        this.serverSocket = socket;

    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket estabSocket = serverSocket.accept();
                ThreadUser threadUser = new ThreadUser(estabSocket);
                threadUser.start();

            } catch (IOException ex) {
                //Logger.getLogger(ThreadServer.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("LISTENER CHIUSO");
                break;
            }

        }
    }

}
