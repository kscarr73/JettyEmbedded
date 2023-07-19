# Purpose

JettyEmbedded sets up a web environment using Jetty.  It is designed to take care of the internal settings for Jetty.  It uses a builder style settings process.

# Functionality

## JettyEmbedded Class

The JettyEmbedded class is the main class that creates the object, and processes the settings.

### Properties

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

### Building the Server and Wait

When you are done adding settings, you should add `.build().waitForInterrupt();`.  This will pause execution in the main function until the service gets an interrupt signal, or the process is stopped.

```java
JettyEmbedded.builder()
	.build()
	.waitForInterrupt();
```

# Samples

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

