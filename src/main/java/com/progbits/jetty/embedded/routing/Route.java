package com.progbits.jetty.embedded.routing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 *
 * @author scarr
 */
public class Route {
    private String method;
    private String path;
    private Handler handler;
    
    public Route(String method, String path, Handler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
    }
    
    public Handler getHandler() {
        return handler;
    }
    
    public boolean matches(HttpServletRequest req) {
        if (method.equals(req.getMethod()) && path.equals(req.getRequestURI())) {
            return true;
        } else {
            return false;
        }
    }
}
