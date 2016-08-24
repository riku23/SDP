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
public class MeasurementBuffer implements Buffer {
    private static MeasurementBuffer instance = null;
    private List<Measurement> measurementList;
    private MeasurementBuffer()
  {
      measurementList = new ArrayList<Measurement>();
  }
    
    public static MeasurementBuffer getInstance()
  {
    if (instance == null)
    {
      instance = new MeasurementBuffer();
    }

    return instance;
  }
    
    
    public List<Measurement> readAllAndClean(){
     List<Measurement> returnList;
     returnList = this.measurementList;
     measurementList.clear();
     return returnList;
     
    };

    @Override
    public void add(Object t) {
        	if(measurementList.size()>=15){
			measurementList.clear();
		}
		measurementList.add((Measurement) t);
		
	}
        public int getSize(){
        return measurementList.size();

}
    }

  

