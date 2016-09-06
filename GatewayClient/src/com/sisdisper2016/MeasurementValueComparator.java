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
public class MeasurementValueComparator implements Comparator<Measurement> {

    @Override
    public int compare(Measurement m1, Measurement m2) {
        double value1 = Double.parseDouble(m1.getValue());
        double value2 = Double.parseDouble(m2.getValue());
        return Double.compare(value1, value2);
    }
    
}
