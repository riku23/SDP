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
   private final List<String> nodiRegistrati;
   private final List<String> nodiInseriti;
   //private final  HashMap<String,String> nodi;
   private Nodes() {
       nodiRegistrati = new ArrayList<>();
       nodiInseriti = new ArrayList<>();
      // Exists only to defeat instantiation.
   }
   public static Nodes getInstance() {
      if(instance == null) {
         instance = new Nodes();
      }
      return instance;
   }

 public synchronized void registraNodo(String[] nodo){
            
            nodiRegistrati.add(nodo[0]+"-"+nodo[1]+"-"+nodo[2]);
    }
 
  public synchronized void inserisciNodo(String[] nodo){
            
            nodiInseriti.add(nodo[0]+"-"+nodo[1]+"-"+nodo[2]);
    }
    
    public synchronized void rimuoviNodo(String[] nodo){
            
            nodiInseriti.remove(nodo[0]+"-"+nodo[1]+"-"+nodo[2]);
            nodiRegistrati.remove(nodo[0]+"-"+nodo[1]+"-"+nodo[2]);
    }
 public List<String> nodiRegistrati(){
     return this.nodiRegistrati;
 }
 
  public List<String> nodiInseriti(){
     return this.nodiInseriti;
 }
 

 public String toStringRegistrati(){
     return nodiRegistrati.toString();
 }
 
  public String toStringInseriti(){
     return nodiInseriti.toString();
 }
 
 
}