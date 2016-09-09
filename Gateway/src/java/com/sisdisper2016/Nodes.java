/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tozio23
 */
public class Nodes {

    private static Nodes instance = null;
    private Map<String, NodoInfo> nodiRegistratiMap;
    private Map<String, NodoInfo> nodiInseritiMap; 
    private List<String> nodiRegistratiList;

    //private final  HashMap<String,String> nodi;

    private Nodes() {
        nodiRegistratiMap = new HashMap<>();
        nodiInseritiMap = new HashMap<>();
        nodiRegistratiList = new ArrayList<>();
        // Exists only to defeat instantiation.
    }

    public static Nodes getInstance() {
        if (instance == null) {
            instance = new Nodes();
        }
        return instance;
    }

    public  void registraNodo(NodoInfo nodo) {
         nodiRegistratiMap.put(nodo.getId(), nodo);
         nodiRegistratiList.add(nodo.getId());
        
    }

    public  void inserisciNodo(NodoInfo nodo) {
        nodiInseritiMap.put(nodo.getId(), nodo);

    }

    public  void rimuoviNodo(NodoInfo nodo) {
        nodiInseritiMap.remove(nodo.getId());
        

    }

    public Map<String,NodoInfo> nodiRegistrati() {
        return this.nodiRegistratiMap;
    }

    public Map<String,NodoInfo> nodiInseriti() {
        return this.nodiInseritiMap;
    }
    
    public List<String> nodiRegistratiClient(){
        return this.nodiRegistratiList;
    }

    public String toStringRegistrati() {
        return nodiRegistratiMap.toString();
    }

    public String toStringInseriti() {
        return nodiInseritiMap.toString();
    }

}
