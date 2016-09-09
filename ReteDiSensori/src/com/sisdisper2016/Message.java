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
public class Message {
    //Classe per la definizione di un messaggio
    //Composta da header che descrive il tipo del messaggio dall'indirizzo e la porta del mittente e il corpo del messaggio
    private String header;
    private String senderAddr;
    private String senderPort;
    private String body;

    public Message(String header, String senderAddr, String senderPort, String body) {
        this.header = header;
        this.senderAddr = senderAddr;
        this.senderPort = senderPort;
        this.body = body;

    }

    public String getHeader() {
        return this.header;
    }

    public String getSenderAddr() {
        return this.senderAddr;
    }

    public String getSenderPort() {
        return this.senderPort;
    }

    public String getBody() {
        return this.body;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setSenderAddr(String addr) {
        this.senderAddr = addr;
    }

    public void setSenderPort(String port) {
        this.senderPort = port;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
