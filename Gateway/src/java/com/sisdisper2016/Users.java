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
public class Users {
    private static Users instance;
    private List<String> usersList;
    private Map<String, UserInfo> usersMap;

    private Users(){
        this.usersList = new ArrayList<>();
        this.usersMap = new HashMap<>();
    }
    
    public static Users getInstance() {
        if (instance == null) {
            instance = new Users();
        }
        return instance;
    }
    
    public void registraUtente(UserInfo user){
        usersMap.put(user.getId(), user);
    }
    public Map<String, UserInfo> getUsers(){
        return this.usersMap;
    }
    
    public void logout(UserInfo user){
        this.usersMap.remove(user.getId());
    }

}
