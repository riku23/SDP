/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tozio23
 */
public class ThreadConsole extends Thread {

    private Nodo nodo;

    public ThreadConsole(Nodo n) {
        this.nodo = n;
    }

    @Override
    public void run() {
        while (!nodo.isExiting()) {
            try {
                String command = nodo.getReader().readLine();
                if (command.equals("exit")) {
                    System.out.println("NODO IN ELIMINAZIONE");
                    nodo.setExiting(true);
                    nodo.getSimulator().stopMeGently();
                }
            } catch (IOException ex) {
                Logger.getLogger(ThreadConsole.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }

}
