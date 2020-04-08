package org.zhq.core.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSession {
    private String id;
    private Map<String,Object> attributes;

    public HttpSession(String id) {
        this.id = id;
        attributes = new ConcurrentHashMap<>();
    }

    public Object getAttribute(String key){
        return attributes.get(key);
    }

    public void setAttributes(String key,Object value){
        attributes.put(key, value);
    }

    public String getId() {
        return id;
    }
}
