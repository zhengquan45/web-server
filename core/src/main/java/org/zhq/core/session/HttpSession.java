package org.zhq.core.session;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSession {
    private String id;
    private Map<String,Object> attributes;
    private boolean isValid;
    private Instant lastAccessed;

    public HttpSession(String id) {
        this.id = id;
        attributes = new ConcurrentHashMap<>();
        this.isValid = true;
        this.lastAccessed = Instant.now();
    }

    public void invalidate() {
        this.isValid = false;
        this.attributes.clear();
    }

    public Object getAttribute(String key){
        if (isValid) {
            lastAccessed = Instant.now();
            return attributes.get(key);
        }
        throw new IllegalStateException("session [" + id + "] is invalidate");
    }

    public void setAttributes(String key,Object value){
        if (isValid) {
            lastAccessed = Instant.now();
            attributes.put(key, value);
            return;
        }
        throw new IllegalStateException("session [" + id + "] is invalidate");
    }

    public String getId() {
        return id;
    }

    public Instant getLastAccessed() {
        return lastAccessed;
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
}
