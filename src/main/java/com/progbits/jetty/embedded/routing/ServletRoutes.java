package com.progbits.jetty.embedded.routing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.http.HttpMethod;

/**
 *
 * @author scarr
 */
public class ServletRoutes {
    private final List<Route> routes = new ArrayList<>();
    private final List<ExceptionHandler> errorHandlers = new ArrayList<>();
    
    public ServletRoutes setErrorHandler(ExceptionHandler exceptionHandler) {
        errorHandlers.add(exceptionHandler);
        
        return this;
    }
    
    public ServletRoutes get(String path, Handler handler) {
        routes.add(new Route(HttpMethod.GET.asString(), path, handler));
        
        return this;
    }
    
    public ServletRoutes post(String path, Handler handler) {
        routes.add(new Route(HttpMethod.POST.asString(), path, handler));
        
        return this;
    }
    
    public ServletRoutes search(String path, Handler handler) {
        routes.add(new Route(HttpMethod.SEARCH.asString(), path, handler));
        
        return this;
    }
    
    public ServletRoutes put(String path, Handler handler) {
        routes.add(new Route(HttpMethod.PUT.asString(), path, handler));
        
        return this;
    }
    
    public ServletRoutes custom(String method, String path, Handler handler) {
        routes.add(new Route(method, path, handler));
        
        return this;
    }
    
    public void processRoutes(HttpServletRequest req, HttpServletResponse resp) {
        for (var entry : routes) {
            if (entry.matches(req)) {
                try {
                    entry.getHandler().handle(req, resp);
                } catch (Exception ex) {
                    processExceptionHandlers(ex, resp);
                }
                break;
            }
        }
    }
    
    private void processExceptionHandlers(Throwable thr, HttpServletResponse resp) {
        for (var entry : errorHandlers) {
            if (entry.handle(thr, resp)) {
                break;
            }
        }
    }
}
