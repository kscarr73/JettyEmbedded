package com.progbits.jetty.embedded;

import com.progbits.jetty.embedded.logging.JettyLogHandler;
import jakarta.servlet.Servlet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
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
    private Integer _maxSessionTimeout = 14400;
    private String _keyStoreFile;
    private String _keyStorePassword;
    private boolean _sniValidate = true;
    
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

    public JettyEmbedded setMaxSessionTimeout(Integer timeoutInSeconds) {
        this._maxSessionTimeout = timeoutInSeconds;

        return this;
    }

    public JettyEmbedded setKeyStore(String keyStore) {
        _keyStoreFile = keyStore;
        
        return this;
    }
    
    public JettyEmbedded setKeyStorePassword(String keyStorePassword) {
        _keyStorePassword = keyStorePassword;
        
        return this;
    }
    
    public JettyEmbedded setSNIValidate(boolean sniValidate) {
        _sniValidate = sniValidate;
        
        return this;
    }
    
    public JettyEmbedded build() {
        if (_keyStoreFile != null) {
            _server = new Server();
            
            _server.addConnector(setupSslConnector(_server, _port));
        } else {
            _server = new Server(_port);
        }
        
        _context = new ServletContextHandler();
        _context.setContextPath(_contextPath);

        AtomicBoolean bWebsocket = new AtomicBoolean(false);

        if (_servlets != null) {
            _servlets.forEach((k, v) -> {
                if (v instanceof JettyWebSocketServlet) {
                    bWebsocket.set(true);
                }

                ServletHolder sh = new ServletHolder(v);

                _context.addServlet(sh, k);
            });
        }

        if (bWebsocket.get()) {
            JettyWebSocketServletContainerInitializer.configure(_context, null);
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

    public JettyEmbedded buildWebApp(SecurityHandler securityHandler, SessionHandler sessionHandler) {
        if (_keyStoreFile != null) {
            _server = new Server();
            
            _server.addConnector(setupSslConnector(_server, _port));
        } else {
            _server = new Server(_port);
        }
        
        _context = new ServletContextHandler();
        _context.setContextPath(_contextPath);

        if (securityHandler != null) {
            _context.setSecurityHandler(securityHandler);
        }

        if (sessionHandler != null) {
            _context.setSessionHandler(sessionHandler);
        } else {
            _context.setSessionHandler(new SessionHandler());
        }

        _context.getSessionHandler().setMaxInactiveInterval(_maxSessionTimeout);
        
        AtomicBoolean bWebsocket = new AtomicBoolean(false);

        if (_servlets != null) {
            _servlets.forEach((k, v) -> {
                if (v instanceof JettyWebSocketServlet) {
                    bWebsocket.set(true);
                }

                ServletHolder sh = new ServletHolder(v);

                _context.addServlet(sh, k);
            });
        }

        if (bWebsocket.get()) {
            JettyWebSocketServletContainerInitializer.configure(_context, null);
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
    
    private ServerConnector setupSslConnector(Server server, Integer iPort) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer(_sniValidate));
        
        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);
        
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(_keyStoreFile);
        sslContextFactory.setKeyStorePassword(_keyStorePassword);
        
        SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, http11.getProtocol());
        
        ServerConnector connector = new ServerConnector(server, tls, http11);
        
        connector.setPort(iPort);
        
        return connector;
    }
}
