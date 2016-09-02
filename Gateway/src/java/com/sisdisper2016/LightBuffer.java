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
public class LightBuffer {

    private static LightBuffer instance = null;
    private Map<String, List<Measurement>> bufferMap;
    private List<Measurement> buffer;

    private LightBuffer() {
        buffer = new ArrayList<>();
        bufferMap = new HashMap<>();

        // Exists only to defeat instantiation.
    }

    public static LightBuffer getInstance() {
        if (instance == null) {
            instance = new LightBuffer();
        }
        return instance;
    }

    public void addMisurazione(Measurement m) {
        if (bufferMap.containsKey(m.getId())) {
            bufferMap.get(m.getId()).add(m);
        } else {
            ArrayList<Measurement> list = new ArrayList<>();
            list.add(m);
            bufferMap.put(m.getId(), list);
        }

    }

    public Map<String, List<Measurement>> getBuffer() {
        return this.bufferMap;
    }

    public List<Measurement> getListByID(String id) {
        return bufferMap.get(id);

    }

    @Override
    public String toString() {
        return bufferMap.toString();
    }

    public int getMisurazioni() {
       int size=0;
       for(String id : bufferMap.keySet()){
           size+= bufferMap.get(id).size();
       }
       
        return size;
    }

}
