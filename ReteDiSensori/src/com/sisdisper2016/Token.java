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

public class Token {

   private int misurazioni;
   private List<Measurement> tokenBuffer;
   
   public Token() {
       tokenBuffer = new ArrayList<Measurement>();

      // Exists only to defeat instantiation.
   }


 public synchronized void addMisurazione(Measurement e){
            
            this.tokenBuffer.add(e);
    }
 public synchronized void  addMisurazioni(List<Measurement> le){
     for(int i=0; i<le.size(); i++){
         tokenBuffer.add(le.get(i));
     }
 }
 public int getMisurazioni(){
     return this.tokenBuffer.size();
 }
 
 public List<Measurement> getBuffer(){
     return this.tokenBuffer;
 }
 
 public void clearBuffer(){
     this.tokenBuffer.clear();
 }

}