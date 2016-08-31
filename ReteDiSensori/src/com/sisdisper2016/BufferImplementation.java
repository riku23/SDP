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
public class BufferImplementation implements Buffer {
        
    private List<Measurement> BufferListSW;
    private List<Measurement> BufferList;
    private boolean sw;

    public BufferImplementation(boolean sw) {
        this.BufferList = new ArrayList<>();
        this.sw = sw;
        if(this.sw){
            BufferListSW = new ArrayList<>();
        }
    }

    @Override
    public void add(Object t) {
        if (sw) {
            if (!BufferList.isEmpty()) {
                if (BufferList.get(BufferList.size() - 1).compareTo((Measurement) t) < 1000) {
                    BufferListSW.add((Measurement) t);
                } else {
                    BufferList.add(mediaMisurazioni());
                }

            } else {
                BufferList.add((Measurement) t);
            }
        } else {
            BufferList.add((Measurement) t);
        }
    }

    @Override
    public List readAllAndClean() {
        List<Measurement> temp = new ArrayList<>(BufferList);
        BufferList.clear();
        return temp;
    }

    public boolean isEmpty() {
        return BufferList.isEmpty();
    }

    public int getSize() {
        return BufferList.size();
    }
    
    
    private Measurement mediaMisurazioni() {
        Measurement media;
        Measurement lastSW = BufferListSW.get(BufferListSW.size() - 1);
        Double value = 0.0;
        for(Measurement m: BufferListSW){
           value += Double.parseDouble(m.getValue());
        }
        Double valueMedia;
        valueMedia = value/BufferListSW.size();
        media = new Measurement(lastSW.getId(),lastSW.getType(),""+valueMedia,lastSW.getTimestamp());
        return media;
    }

}
