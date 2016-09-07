/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sisdisper2016;

import java.util.Comparator;

/**
 *
 * @author Tozio23
 */
public class MeasurementComparator implements Comparator<Measurement>{

    @Override
    public int compare(Measurement o1, Measurement o2) {
        return o1.compareTo(o2);
    }
    
}
