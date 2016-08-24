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
    private List<Measurement> BufferList;
    
    public BufferImplementation(){
        this.BufferList = new ArrayList<>();
    }
    @Override
    public void add(Object t) {
        BufferList.add((Measurement) t);
    }

    @Override
    public List readAllAndClean() {
        List<Measurement> temp = new ArrayList<>(BufferList);
        BufferList.clear();
        return temp;
    }
    
    public boolean isEmpty(){
        return BufferList.isEmpty();
    }
    
    public int getSize(){
        return BufferList.size();
    }
    
}
