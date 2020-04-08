package org.zhq.core.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private Map<String,String>users = new ConcurrentHashMap<>();
    private Set<String> online = Collections.synchronizedSet(new HashSet<>());

    public UserService(){
        users.put("admin","admin");
        users.put("admin1","admin");
        users.put("admin2","admin");
        users.put("admin3","admin");
        users.put("admin4","admin");
    }

    public boolean login(String username,String password){
        if(users.containsKey(username) && users.get(username).equals(password)){
            online.add(username);
            return true;
        }
        return false;
    }
}
