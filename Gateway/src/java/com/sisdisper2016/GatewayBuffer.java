/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Tozio23
 */
public class GatewayBuffer {

    private static GatewayBuffer instance = null;
    private final List<Measurement> buffer;

    private GatewayBuffer() {
        buffer = new ArrayList<>();

        // Exists only to defeat instantiation.
    }

    public static GatewayBuffer getInstance() {
        if (instance == null) {
            instance = new GatewayBuffer();
        }
        return instance;
    }

    public synchronized boolean addMisurazione(Measurement m) {

        return buffer.add(m);
    }

    public synchronized boolean addMisurazioni(List<Measurement> lm) {

        return buffer.addAll(lm);
    }

    public List<Measurement> getList() {
        return this.buffer;
    }

    public List<Measurement> getListByID(String id) {
        List<Measurement> temp = new ArrayList<>();
        for (Measurement m : buffer) {
            if (m.getId().equals(id)) {
                temp.add(m);
            }

        }
        return temp;
    }

    public List<String> getTypes() {
        List<String> temp = new ArrayList<>();
        for (Measurement m : buffer) {
            temp.add(m.getType());
        }
        return temp;
    }
    

    public List<Measurement> getListByType(String type) {
        List<Measurement> temp = new ArrayList<>();
        for (Measurement m : buffer) {
            if (m.getType().equals(type)) {
                temp.add(m);
            } else {
            }

        }
        return temp;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    public int getMisurazioni() {
        return buffer.size();
        //return buffer.size();
    }


}
