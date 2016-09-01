/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tozio23
 */
public class Nodes {

    private static Nodes instance = null;
    private final List<NodoInfo> nodiRegistrati;
    private final List<NodoInfo> nodiInseriti;
    //private final  HashMap<String,String> nodi;

    private Nodes() {
        nodiRegistrati = new ArrayList<>();
        nodiInseriti = new ArrayList<>();
        // Exists only to defeat instantiation.
    }

    public static Nodes getInstance() {
        if (instance == null) {
            instance = new Nodes();
        }
        return instance;
    }

    public synchronized void registraNodo(NodoInfo nodo) {

        nodiRegistrati.add(nodo);
    }

    public synchronized void inserisciNodo(NodoInfo nodo) {

        nodiInseriti.add(nodo);
    }

    public synchronized void rimuoviNodo(NodoInfo nodo) {
        for (NodoInfo n : nodiInseriti) {
            if (n.getId().equals(nodo.getId())) {
                nodiInseriti.remove(n);
            }
        }
        for (NodoInfo n : nodiRegistrati) {
            if (n.getId().equals(nodo.getId())) {
                nodiRegistrati.remove(n);
            }
        }

    }

    public List<NodoInfo> nodiRegistrati() {
        return this.nodiRegistrati;
    }

    public List<NodoInfo> nodiInseriti() {
        return this.nodiInseriti;
    }

    public String toStringRegistrati() {
        return nodiRegistrati.toString();
    }

    public String toStringInseriti() {
        return nodiInseriti.toString();
    }

}
