# JettyEmbedded

A small class to create an Embedded Jetty that can serve jakarta.servlet classes.

Documentation: [Table Of Contents](docs/Table%20Of%20Contents.md)

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

