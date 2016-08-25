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
   private final List<String> nodi;
   //private final  HashMap<String,String> nodi;
   private Nodes() {
       nodi = new ArrayList<>();

      // Exists only to defeat instantiation.
   }
   public static Nodes getInstance() {
      if(instance == null) {
         instance = new Nodes();
      }
      return instance;
   }

 public synchronized void addNode(String[] nodo){
            
            nodi.add(nodo[0]+"-"+nodo[1]+"-"+nodo[2]);
    }
    
 public List<String> getList(){
     return this.nodi;
 }
 
   @Override
 public String toString(){
     return nodi.toString();
 }
 
 
}