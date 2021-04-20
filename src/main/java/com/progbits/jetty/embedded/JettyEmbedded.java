package com.progbits.jetty.embedded;

import com.progbits.jetty.embedded.logging.JettyLogHandler;
import jakarta.servlet.Servlet;
import java.util.Map;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
public class JettyEmbedded {
	private static final Logger log = LoggerFactory.getLogger(JettyEmbedded.class);
	
	private Server _server;
	private ServletContextHandler _context;
	
	private Map<String, Servlet> _servlets;
	private int _port = 8080;
	private String _contextPath;
	
	public JettyEmbedded() {
	}
	
	public static JettyEmbedded builder() {
		return new JettyEmbedded();
	}
	
	public JettyEmbedded setContextPath(String contextPath) {
		_contextPath = contextPath;
		
		return this;
	}
	
	public JettyEmbedded setPort(int iPort) {
		_port = iPort;
		return this;
	}
	
	public JettyEmbedded setServlets(Map<String, Servlet> servlets) {
		_servlets = servlets;
		
		return this;
	}
	
	public JettyEmbedded build() {
		_server = new Server(_port);
		
		_context = new ServletContextHandler();
		_context.setContextPath(_contextPath);
		
		if (_servlets != null) {
			_servlets.forEach((k, v) -> { 
				ServletHolder sh = new ServletHolder(v);
				
				_context.addServlet(sh, k);
			});
		}
		
		_server.setHandler(_context);
		
		_server.setRequestLog(new JettyLogHandler());
		
		try {
			_server.start();
		} catch (Exception ex) {
			log.error("Server Configuration Failed", ex);
		}
		
		return this;
	}
	
	public void waitForInterrupt() throws InterruptedException {
		_server.join();
	}
}
