package com.progbits.jetty.embedded.logging;

import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.slf4j.MDC;

/**
 *
 * @author scarr
 */
public class JettyLogHandler implements RequestLog {

    private static final Logger log = LogManager.getLogger();

    private Pattern _ignoreRegEx;

    private static final LinkedBlockingQueue<MapMessage> logEntries = new LinkedBlockingQueue<>();

    private Thread logProcessor;

    public JettyLogHandler() {
        configure();
    }

    public JettyLogHandler(String ignoreRegEx) {
        if (ignoreRegEx != null) {
            _ignoreRegEx = Pattern.compile(ignoreRegEx);
        }
        configure();
    }

    private void configure() {
        logProcessor = new Thread(() -> {
            while (true) {
                try {
                    MapMessage msg = logEntries.take();

                    log.info(msg);
                } catch (InterruptedException iex) {
                    // Nothing really to do here
                }
            }
        });

//        logProcessor.setDaemon(true);
//        logProcessor.setName("AccessLogProcessor-1");
//        logProcessor.start();
    }
    
    @Override
    public void log(Request req, Response resp) {
        MapMessage msg = new MapMessage()
                .with("status", resp.getStatus())
                .with("length", resp.getContentCount())
                .with("requestUri", req.getRequestURI())
                .with("speed", System.currentTimeMillis() - req.getTimeStamp())
                .with("timestamp", req.getTimeStamp())
                .with("method", req.getMethod())
                .with("clientip", req.getRemoteAddr())
                .with("reqhost", req.getLocalName() == null ? "" : req.getLocalName())
                .with("reqproto", req.getProtocol())
                .with("request", String.format("%s %s%s %s", req.getMethod(), req.getRequestURI(), req.getQueryString() == null ? "" : "?" + req.getQueryString(), req.getProtocol()))
                .with("sourceip", req.getLocalAddr());

        for (Enumeration<?> e = req.getHeaderNames(); e.hasMoreElements();) {
            String nextHeaderName = (String) e.nextElement();

            if (!"authorization".equals(nextHeaderName)) {
                msg.with("hdr_" + nextHeaderName, req.getHeader(nextHeaderName));
            }
        }

        String mdcFlowId = MDC.get("X-FlowId");

        if (mdcFlowId != null) {
            msg.with("flowid", mdcFlowId);
        }

        if (_ignoreRegEx == null || !_ignoreRegEx.matcher(req.getRequestURI()).matches()) {
            log.info(msg);
        }
    }

}
