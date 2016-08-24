/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storage;

import java.util.ArrayList;

/**
 *
 * @author Tozio23
 */
public class Storage {
    int connections;
    public Storage(){
        this.connections = 1000;
    }
    
    
    public int CountConnections(){
        return this.connections;
    }
    
    public String PrintMessage(){
        return "Se vedi questo messaggio sei bello";
    }
   
}
