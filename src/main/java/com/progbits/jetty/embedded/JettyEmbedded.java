package com.progbits.jetty.embedded;

import com.progbits.jetty.embedded.logging.JettyLogHandler;
import com.progbits.jetty.embedded.routing.RouteController;
import com.progbits.jetty.embedded.routing.ServletRoutes;
import jakarta.servlet.Servlet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.VirtualThreads;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.WebSocketContainer;
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

    private List<FilterSet> _filters = null;
    private Map<String, Servlet> _servlets;
    private List<ServletSet> _servletSet;
    private int _port = 8080;
    private String _contextPath;
    private Integer _maxSessionTimeout = 14400;
    private Long _idleTimeout = Duration.ofSeconds(30).toMillis();
    private String _keyStoreFile;
    private String _keyStorePassword;
    private boolean _sniValidate = true;

    private String[] gzipIncludedMethods = null;
    private String[] gzipIncludedMimeTypes = null;
    private Integer gzipMinimumSize = null;
    private String ignoreRequestLogRegEx = null;

    private Map<String, Class> webSockets = null;

    private Long webSocket_MessageSize = 65535L;
    private Long webSocket_IdleTimeout = 10000L;

    private WebSocketContainer webSocketContainer = null;
    private QueuedThreadPool queuedThreadPool = null;

    public JettyEmbedded() {
    }

    public static JettyEmbedded builder() {
        return new JettyEmbedded();
    }

    public WebSocketContainer getWebSocketContainer() {
        return webSocketContainer;
    }

    public Server getJettyServer() {
        return _server;
    }

    public JettyEmbedded useVirtualThreads() {
        queuedThreadPool = new QueuedThreadPool();

        Executor executor = VirtualThreads.getDefaultVirtualThreadsExecutor();

        if (executor == null) {
            log.info("Virtual Threads Not Available");
        } else {
            log.info("Virtual Threads Used");
            queuedThreadPool.setVirtualThreadsExecutor(executor);
        }

        return this;
    }

    public JettyEmbedded setContextPath(String contextPath) {
        _contextPath = contextPath;

        return this;
    }

    public JettyEmbedded setPort(int iPort) {
        _port = iPort;
        return this;
    }

    public JettyEmbedded setFilters(List<FilterSet> filters) {
        _filters = filters;

        return this;
    }

    @Deprecated
    /**
     * Use setServlets(List<ServletSet>) instead.
     */
    public JettyEmbedded setServlets(Map<String, Servlet> servlets) {
        _servlets = servlets;

        return this;
    }

    public JettyEmbedded setServlets(List<ServletSet> servletSet) {
        _servletSet = servletSet;

        return this;
    }

    public JettyEmbedded setWebSockets(Map<String, Class> sockets) {
        webSockets = sockets;

        return this;
    }

    public JettyEmbedded setWebSocketIdle(Long idleTimeout) {
        webSocket_IdleTimeout = idleTimeout;

        return this;
    }

    public JettyEmbedded setIdleTimeout(Long idleTimeout) {
        _idleTimeout = idleTimeout;

        return this;
    }

    public JettyEmbedded setWebSocketMessageSize(Long messageSize) {
        webSocket_MessageSize = messageSize;

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

    public JettyEmbedded setGZipIncludedMethods(String... methods) {
        this.gzipIncludedMethods = methods;

        return this;
    }

    public JettyEmbedded setGZipIncludedMimeTypes(String... methods) {
        this.gzipIncludedMimeTypes = methods;

        return this;
    }

    public JettyEmbedded setGZipMimumumSize(int minSize) {
        this.gzipMinimumSize = minSize;

        return this;
    }

    public JettyEmbedded setIgnoreRequestLogRegEx(String ignoreRegEx) {
        this.ignoreRequestLogRegEx = ignoreRegEx;

        return this;
    }

    public JettyEmbedded setQueuedThreadPool(QueuedThreadPool queuedThreadPool) {
        this.queuedThreadPool = queuedThreadPool;

        return this;
    }

    /**
     * Setup a Router Servlet that processes Routes.
     *
     * @param routes Routes to process
     * @return JettyEmbedded this instance
     */
    public JettyEmbedded useServletRoutes(ServletRoutes... routes) {
        if (_servletSet == null) {
            _servletSet = new ArrayList<>();
        }

        _servletSet.add(new ServletSet("/*", new RouteController(routes)));

        return this;
    }

    public JettyEmbedded build() {
        setupServer();

        setupContext();

        startServer();

        return this;
    }

    public JettyEmbedded buildWebApp(SecurityHandler securityHandler, SessionHandler sessionHandler) {
        setupServer();

        if (securityHandler != null) {
            _context.setSecurityHandler(securityHandler);
        }

        if (sessionHandler != null) {
            _context.setSessionHandler(sessionHandler);
        } else {
            _context.setSessionHandler(new SessionHandler());
        }

        _context.getSessionHandler().setMaxInactiveInterval(_maxSessionTimeout);

        setupContext();

        startServer();

        return this;
    }

    public void waitForInterrupt() throws InterruptedException {
        _server.join();
    }

    private ServerConnector setupHttpConnector(Server server, Integer iPort) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setIdleTimeout(_idleTimeout);

        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);

        ServerConnector connector = new ServerConnector(server, http11);

        connector.setPort(iPort);

        return connector;
    }

    private ServerConnector setupSslConnector(Server server, Integer iPort) {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.addCustomizer(new SecureRequestCustomizer(_sniValidate));

        httpConfig.setIdleTimeout(_idleTimeout);

        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(_keyStoreFile);
        sslContextFactory.setKeyStorePassword(_keyStorePassword);

        SslConnectionFactory tls = new SslConnectionFactory(sslContextFactory, http11.getProtocol());

        ServerConnector connector = new ServerConnector(server, tls, http11);

        connector.setPort(iPort);

        return connector;
    }

    private GzipHandler setupGzipHandler() {
        if (gzipIncludedMethods != null || gzipIncludedMimeTypes != null || gzipMinimumSize != null) {
            GzipHandler gzipHandler = new GzipHandler();

            if (gzipIncludedMethods != null) {
                gzipHandler.setIncludedMethods(gzipIncludedMethods);
            }

            if (gzipIncludedMimeTypes != null) {
                gzipHandler.setIncludedMimeTypes(gzipIncludedMimeTypes);
            }

            if (gzipMinimumSize != null) {
                gzipHandler.setMinGzipSize(gzipMinimumSize);
            }

            return gzipHandler;
        } else {
            return null;
        }
    }

    private void setupServer() {
        if (queuedThreadPool != null) {
            _server = new Server(queuedThreadPool);
        } else {
            _server = new Server();
        }

        if (_keyStoreFile != null) {
            _server.addConnector(setupSslConnector(_server, _port));
        } else {
            _server.addConnector(setupHttpConnector(_server, _port));
        }

        _context = new ServletContextHandler();
        _context.setContextPath(_contextPath);
    }

    private void setupContext() {
        if (_filters != null) {
            _filters.forEach((filter) -> {
                FilterHolder fh = new FilterHolder(filter.getFilter());

                if (filter.getInitParams() != null) {
                    fh.setInitParameters(filter.getInitParams());
                }

                _context.addFilter(fh, filter.getPath(), filter.getTypes());
            });
        }

        if (_servlets != null) {
            if (_servletSet == null) {
                _servletSet = new ArrayList<>();
            }
            _servlets.forEach((k, v) -> {
                _servletSet.add(new ServletSet(v, k, null));
            });
        }

        if (_servletSet != null) {
            _servletSet.forEach((srvlet) -> {
                ServletHolder sh = new ServletHolder(srvlet.getServlet());

                if (srvlet.getInitParams() != null) {
                    sh.setInitParameters(srvlet.getInitParams());
                }

                _context.addServlet(sh, srvlet.getPath());
            });
        }

        if (webSockets != null) {
            final Map<String, Class> localWebSockets = webSockets;

            JettyWebSocketServletContainerInitializer.configure(_context, (serlvetContext, wsContainer) -> {
                webSocketContainer = wsContainer;

                wsContainer.setIdleTimeout(Duration.ofMillis(webSocket_IdleTimeout));
                wsContainer.setMaxTextMessageSize(webSocket_MessageSize);

                for (var entry : localWebSockets.entrySet()) {
                    wsContainer.addMapping(entry.getKey(), entry.getValue());
                }
            });
        }
    }

    private void startServer() {
        GzipHandler gzipHandler = setupGzipHandler();

        if (gzipHandler == null) {
            _server.setHandler(_context);
        } else {
            gzipHandler.setHandler(_context);

            _server.setHandler(gzipHandler);
        }

        _server.setRequestLog(new JettyLogHandler(ignoreRequestLogRegEx));

        try {
            _server.start();
        } catch (Exception ex) {
            log.error("Server Configuration Failed", ex);
        }
    }
}
