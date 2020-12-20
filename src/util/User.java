package util;

import java.io.Serializable;

public class User implements Serializable {
    public static final int ADMIN = 'a';
    public static final int MANUFACTURER = 'm';
    public static final int VIEWER = 'v' ;

    public String username ;
    public String password ;
    public int type = VIEWER;

    public User(String uname, String pass, int type ){
        username = uname;
        password = pass;
        this.type = type;
    }

    public String toString(){
        return username+","+password+","+type ;
    }
}