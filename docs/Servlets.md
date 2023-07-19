Servlets are set via the ServletSet class.

```java
List<ServletSet> servlets = new ArrayList<>();
servlets.add(new ServletSet("/*", new WebController()));

JettyEmbedded.builder()
   .setServlets(servlets)
   .build()
   .waitForInterrupt();
```

# Path Handling

The path string can contain any pattern that Jetty normally uses.

## Sample WebController

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

