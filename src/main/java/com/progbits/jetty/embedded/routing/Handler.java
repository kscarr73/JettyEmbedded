package com.progbits.jetty.embedded.routing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author scarr
 */
@FunctionalInterface
public interface Handler {
    void handle(HttpServletRequest req, HttpServletResponse resp) throws Exception;
}
