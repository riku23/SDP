/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import java.util.Date;

/**
 *
 * @author Tozio23
 */
public class NodoInfo {

    private String id;
    private String type;
    private Date uptime;
    private String address;
    private String port;

    public NodoInfo() {

    }

    public NodoInfo(String id, String type, Date uptime, String address, String port) {
        this.id = id;
        this.type = type;
        this.uptime = uptime;
        this.address = address;
        this.port = port;
    }

    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public Date getUptime() {
        return this.uptime;
    }

    public String getAddress() {
        return this.address;
    }

    public String getPort() {
        return this.port;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.id = type;
    }

    public void setUptime(Date uptime) {
        this.uptime = uptime;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(String port) {
        this.port = port;
    }
    
    @Override
    public String toString(){
        return this.id+"-"+this.type+"-"+this.address+"-"+this.port;
    }
}
