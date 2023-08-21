- [Basic Usage](#basic-usage)
	- [Purpose](#purpose)
	- [Functionality](#functionality)
		- [JettyEmbedded Class](#jettyembedded-class)
			- [Properties](#properties)
			- [Building the Server and Wait](#building-the-server-and-wait)
	- [Samples](#samples)
	- [Sample MainApplication](#sample-mainapplication)
- [Logging](#logging)
	- [Configuration](#configuration)
		- [log4j2.yaml](#log4j2yaml)
		- [log4j Layouts](#log4j-layouts)
		- [JSONEventLayoutV1.json  (ROOT Logger)](#jsoneventlayoutv1json--root-logger)
		- [AccessEvents.json (Access Events)](#accesseventsjson-access-events)
- [Servlets](#servlets)
	- [Path Handling](#path-handling)
		- [Simple WebController](#simple-webcontroller)
	- [ServletRoutes WebController](#servletroutes-webcontroller)
- [Filters](#filters)
- [Secure HTTP](#secure-http)
- [Web Socket Handling](#web-socket-handling)
- [Virtual Threads](#virtual-threads)

# Basic Usage

## Purpose

JettyEmbedded sets up a web environment using Jetty.  It is designed to take care of the internal settings for Jetty.  It uses a builder style settings process.

## Functionality

### JettyEmbedded Class

The JettyEmbedded class is the main class that creates the object, and processes the settings.

#### Properties

| Property | Default | Description |
| --------- | ------- | ------------ |
| setContextPath |  | **Required** The default context of the Server |
| setFilters | | List of Filters to assign |
| setIdleTimeout | 30s | HTTP Timeout for Idle |
| setGZipIncludedMethods | | HTTP Methods to use for GZIP |
| setGZipIncludedMimeTypes | | Mime Types to GZip on send |
| setGZipMinimumSize | | The minimum content size, before GZip is used |
| setIgnoreRequestLogRegEx | | Request Log Pattern to IGNORE logging |
| setKeyStore | | File Name of a JKS to use for Secure HTTP |
| setKeyStorePassword | | Password to use keys in the JKS store |
| setMaxSessionTimeout | | HTTP Session Timeout |
| setPort | 8080 | The port to connect the HTTP Server to |
| setSNIValidate | true | Used to turn off HOst Name validation |
| setServlets | | List of ServletSet for mapping controllers to paths |
| setWebSocketIdle | 10m | Auto Disconnect of Socket after milliseconds |
| setWebSocketMessageSize | 65535 | Max Size of Web Socket messages |
| setWebSockets | | Map of Web Socket Controllers with path |

#### Building the Server and Wait

When you are done adding settings, you should add `.build().waitForInterrupt();`.  This will pause execution in the main function until the service gets an interrupt signal, or the process is stopped.

```java
JettyEmbedded.builder()
	.build()
	.waitForInterrupt();
```

## Samples

## Sample MainApplication

```java
public class MainApplication {

	public static final String CONTEXT_PATH = "/testapp";
	
	public static void main(String[] args) {
		String webPort = System.getenv("PORT");
		
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8080";
		}

		try {
			List<FilterSet> filters = new ArrayList<>();

			List<ServletSet> servlets = new ArrayList<>();

			servlets.add(new ServletSet("/*", new WebController()));

			JettyEmbedded.builder()
					.setPort(Integer.parseInt(webPort))
					.setContextPath(CONTEXT_PATH)
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

A logger based on Log4j2 is setup.  

## Configuration

You can configure to run using JSON by placing the following files in your ```src/main/resources``` folder.

Sometimes you have access logs for endpoints that are hit more for internal information.  These could include healthcheck, and metrics endpoints.  You can add a pattern in JettyEmbedded, that removes these calls from the logs.

```java
	JettyEmbedded.builder()
	    .setIgnoreRequestLogRegEx(".*[\\/healthcheck.*|.*\\/metric].*")
	    .build()
	    ..waitForInterrupt();
```

All calls with `/healthcheck` or `/metric` will be ignored by the Log4j Access Logger.

### log4j2.yaml
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

### log4j Layouts

We are using log4j layouts to handle json processing in Log4j.  These should reside in the following folder:

`/resources/layouts`

### JSONEventLayoutV1.json  (ROOT Logger)
```
{
	"flowid": {
		"$resolver": "mdc",
		"key": "flowid"
	},
	"exception": {
		"$resolver": "exception",
		"field": "stackTrace",
		"stackTrace": {
			"stringified": true
		}
	},
	"class": {
		"$resolver": "source",
		"field": "className"
	},
	"message": {
		"$resolver": "message",
		"stringified": true
	},
	"thread": {
		"$resolver": "thread",
		"field": "name"
	},
	"timestamp": {
		"$resolver": "timestamp",
		"epoch": {
			"unit": "secs",
			"rounded": true
		}
	},
	"level": {
		"$resolver": "level",
		"field": "name"
	},
	"logger": {
		"$resolver": "logger",
		"field": "name"
	}
}
```

### AccessEvents.json (Access Events)
```
{
    "clientip": {
        "$resolver": "map",
        "key": "clientip"
    },
    "status": {
        "$resolver": "map",
        "key": "status"
    },
    "length": {
        "$resolver": "map",
        "key": "length"
    },
    "requestUri": {
        "$resolver": "map",
        "key": "requestUri"
    },
    "speed": {
        "$resolver": "map",
        "key": "speed"
    },
    "timestamp": {
		"$resolver": "timestamp",
		"epoch": {
			"unit": "secs",
			"rounded": true
		}
	},
    "reqhost": {
        "$resolver": "map",
        "key": "reqhost"
    },
    "reqproto": {
        "$resolver": "map",
        "key": "reqproto"
    },
    "request": {
        "$resolver": "map",
        "key": "request"
    },
    "sourceip": {
        "$resolver": "map",
        "key": "sourceip"
    },
    "useragent": {
        "$resolver": "map",
        "key": "hdr_User-Agent"
    },
    "flowid": {
        "$resolver": "mdc",
        "key": "flowid"
    }
}
```

# Servlets

Servlets are set via the ServletSet class.

```java
List<ServletSet> servlets = new ArrayList<>();
servlets.add(new ServletSet("/*", new WebController()));

JettyEmbedded.builder()
   .setServlets(servlets)
   .build()
   .waitForInterrupt();
```

## Path Handling

The path string can contain any pattern that Jetty normally uses.

### Simple WebController

```java
public class WebController extends HttpServlet {
    private static final String ALIAS = MainApplication.CONTEXT_PATH;
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            switch (req.getMethod() + " " + req.getRequestURI()) {
                case "GET " + ALIAS + "/healthcheck" -> {
                    resp.setStatus(200);
                    resp.getWriter().append("Ok");
                }
            }
        } catch (Exception ex) {
            resp.setStatus(500);
            resp.getWriter().append(ex.getMessage);
        }
    }
}
```

## ServletRoutes WebController

You can also use a ServletRoutes class to manage your routes inside the controller.  This can help make routes easier to code.

[ServletRouter Project](https://github.com/kscarr73/JettyServletRouter)

# Filters

# Secure HTTP

# Web Socket Handling

# Virtual Threads

If you are using JDK19 or higher, and you are using `--enable-preview`, 
then you can use Virtual Threads.  If these requirements are NOT met, then the following 
will fall back to using regular Thread Executors.

> [!note]
> Once JDK21 is released, you will no longer need the `--enable-preview` flag

```java
List<ServletSet> servlets = new ArrayList<>();
servlets.add(new ServletSet("/*", new WebController()));

JettyEmbedded.builder()
   .setServlets(servlets)
   .useVirtualThreads()
   .build()
   .waitForInterrupt();
```