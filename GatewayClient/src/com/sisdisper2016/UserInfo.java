/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

/**
 *
 * @author Tozio23
 */
public class UserInfo {
    private String id;
    private String address;
    private String port;
    
    public UserInfo(String id, String address, String port){
        this.id = id;
        this.address = address;
        this.port = port;
    }
    
    public UserInfo(String address, String port){
         this.id = "";
        this.address = address;
        this.port = port;
    }
    
    
    
    public String getId(){
        return this.id;
    }
     public void setId(String id){
        this.id = id;
    }
    public String getAddress(){
        return this.address;
    }
    
    public String getPort(){
        return this.port;
    }
    @Override
    public String toString(){
        return this.id+"-"+this.address+"-"+this.port;
    }
}
