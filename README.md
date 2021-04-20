# JettyEmbedded

A small class to create an Embedded Jetty that can serve jakarta.servlet classes.

# Example of Usage

```
import com.progbits.jetty.embedded.JettyEmbedded;
import jakarta.servlet.Servlet;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainApplication {
	private static Logger log = LogManager.getLogger(MainApplication.class);
	
	public static void main(String[] args) {
		Map<String, Servlet> servlets = new LinkedHashMap<>();

		servlets.put("/", new TestServlet());
		
		try {
			JettyEmbedded.builder()
					.setPort(8828)
					.setContextPath("/my-test")
					.setServlets(servlets)
					.build()
					.waitForInterrupt();
		} catch (InterruptedException iex) {
			log.error("Interruped Exception", iex);
		}
	}
}
```

# Logging

A logger based on Log4j2 is setup.  You can configure to run using JSON by placing the following files in your ```src/main/resources``` folder.

## log4j2.yaml
```
Configutation:
  name: Default
  Properties:
    Property:
      name: log-path
      value: "logs"
  Appenders:
    Console:
        - name: Console_Appender
          target: SYSTEM_OUT
          JsonTemplateLayout:
            eventTemplateUri: classpath:JSONEventLayoutV1.json
    
        - name: Access_Appender
          target: SYSTEM_OUT
          JsonTemplateLayout:
            eventTemplateUri: classpath:AccessEvents.json
  Loggers:
      Root:
        level: info
        AppenderRef:
          - ref: Console_Appender
      Logger:
        - name: com.progbits.jetty.embedded.logging.JettyLogHandler
          level: info
          additivity: false
          AppenderRef:
            - ref: Access_Appender
```

## JSONEventLayoutV1.json  (ROOT Logger)
```
{
	"mdc": {
		"$resolver": "mdc"
	},
	"exception": {
		"exception_class": {
			"$resolver": "exception",
			"field": "className"
		},
		"exception_message": {
			"$resolver": "exception",
			"field": "message"
		},
		"stacktrace": {
			"$resolver": "exception",
			"field": "stackTrace",
			"stackTrace": {
				"stringified": true
			}
		}
	},
	"line_number": {
		"$resolver": "source",
		"field": "lineNumber"
	},
	"class": {
		"$resolver": "source",
		"field": "className"
	},
	"@version": 1,
	"source_host": "${hostName}",
	"message": {
		"$resolver": "message",
		"stringified": true
	},
	"thread_name": {
		"$resolver": "thread",
		"field": "name"
	},
	"@timestamp": {
		"$resolver": "timestamp"
	},
	"level": {
		"$resolver": "level",
		"field": "name"
	},
	"file": {
		"$resolver": "source",
		"field": "fileName"
	},
	"method": {
		"$resolver": "source",
		"field": "methodName"
	},
	"logger_name": {
		"$resolver": "logger",
		"field": "name"
	}
}
```

## AccessEvents.json (Access Events)
```
{
	"clientip": "${clientip}",
	"status": "${status}",
	"length": "${length}",
	"requestUri": "${requestUri}",
	"speed": "${speed}",
	"timestamp": {
		"$resolver": "timestamp"
	},
	"method": "${method}",
	"reqhost": "${reqhost}",
	"reqproto": "${reqproto}",
	"request": "${request}",
	"sourceip": "${sourceip}",
	"useragent": "${useragent}",
	"flowid": "${flowid}"
}
```