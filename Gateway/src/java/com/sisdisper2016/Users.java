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
public class Users {
    private static Users instance;
    private List<String> usersList;

    private Users(){
        usersList = new ArrayList<>();
    }
    
    public static Users getInstance() {
        if (instance == null) {
            instance = new Users();
        }
        return instance;
    }
    
    public void registraUtente(String user){
        usersList.add(user);
    }
    public List<String> getUsers(){
        return this.usersList;
    }
    
    public void logout(String user){
        this.usersList.remove(user);
    }

}
