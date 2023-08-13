package com.progbits.jetty.embedded.routing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author scarr
 */
public interface ServletRoutes {

    boolean processRoutes(HttpServletRequest req, HttpServletResponse resp);

}
