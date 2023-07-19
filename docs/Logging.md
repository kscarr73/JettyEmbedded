A logger based on Log4j2 is setup.  

# Configuration

You can configure to run using JSON by placing the following files in your ```src/main/resources``` folder.

Sometimes you have access logs for endpoints that are hit more for internal information.  These could include healthcheck, and metrics endpoints.  You can add a pattern in JettyEmbedded, that removes these calls from the logs.

```java
	JettyEmbedded.builder()
	    .setIgnoreRequestLogRegEx(".*[\\/healthcheck.*|.*\\/metric].*")
	    .build()
	    ..waitForInterrupt();
```

All calls with `/healthcheck` or `/metric` will be ignored by the Log4j Access Logger.

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

## log4j Layouts

We are using log4j layouts to handle json processing in Log4j.  These should reside in the following folder:

`/resources/layouts`

## JSONEventLayoutV1.json  (ROOT Logger)
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

## AccessEvents.json (Access Events)
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