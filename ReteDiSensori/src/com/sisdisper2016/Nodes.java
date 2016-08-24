/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import java.util.HashMap;

/**
 *
 * @author Tozio23
 */

public class Nodes {
   private static Nodes instance = null;
   private final  HashMap<String,Nodo> nodi;
   private Nodes() {
       nodi = new HashMap<>();

      // Exists only to defeat instantiation.
   }
   public static Nodes getInstance() {
      if(instance == null) {
         instance = new Nodes();
      }
      return instance;
   }

 public synchronized void addNode(Nodo nodo){
            
            nodi.put(nodo.getId(), nodo);
    }
    
 public HashMap<String,Nodo> getHashMap(){
     return this.nodi;
 }
 
   @Override
 public String toString(){
     return nodi.toString();
 }
 
 
}