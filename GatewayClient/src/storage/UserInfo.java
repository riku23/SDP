/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storage;

public class UserInfo {
    public String info;
    public UserInfo(){};
    public UserInfo(String infoInput){
        info = infoInput;
    }
    
    public String toString(){
        return this.info;
    }
}
