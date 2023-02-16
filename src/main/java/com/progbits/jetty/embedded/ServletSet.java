package com.progbits.jetty.embedded;

import jakarta.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author scarr
 */
public class ServletSet {
    private Servlet servlet;
    private String path;
    
    private Map<String, String> initParams;

    public ServletSet() {
    }

    public ServletSet(String path, Servlet servlet) {
        this.servlet = servlet;
        this.path = path;
    }
    
    public ServletSet(Servlet servlet, String path, Map<String, String> initParams) {
        this.servlet = servlet;
        this.path = path;
        this.initParams = initParams;
    }

    public ServletSet setServlet(Servlet servlet) {
        this.servlet = servlet;
        
        return this;
    }
    
    public ServletSet setPath(String path) {
        this.path = path;
        
        return this;
    }
    
    public ServletSet setInitParams(Map<String, String> initParams) {
        this.initParams = initParams;
        
        return this;
    }

    public ServletSet addInitParam(String key, String value) {
        if (initParams == null) {
            initParams = new HashMap<>();
        }
        
        initParams.put(key, value);
        
        return this;
    }
    
    public Servlet getServlet() {
        return servlet;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getInitParams() {
        return initParams;
    }
    
}
