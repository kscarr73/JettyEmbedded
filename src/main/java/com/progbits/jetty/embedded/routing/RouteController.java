package com.progbits.jetty.embedded.routing;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @author scarr
 */
public class RouteController extends HttpServlet {

    private ServletRoutes[] router;

    public RouteController(ServletRoutes... router) {
        this.router = router;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        for (var entry : router) {
            if (entry.processRoutes(req, resp)) {
                break;
            }
        }
    }
}
