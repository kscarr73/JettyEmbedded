package com.progbits.jetty.embedded.logging;

import java.util.Enumeration;
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
				.with("reqhost", req.getLocalName())
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

		log.info(msg);
	}

}
