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

## Simple WebController

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

# ServletRoutes WebController

You can also use a ServletRoutes class to manage your routes inside the controller.  This can help make routes easier to code.

```java
public class WebController extends HttpServlet {
    private static final String ALIAS = MainApplication.CONTEXT_PATH;
    private static ServletRoutes servletRoutes = new ServletRoutes();

    @Override
    public void init() throws ServletException {
        super.init();

        addRoutes();
    }

    private void addRoutes() {
        servletRoutes.get(ALIAS + "/api", this::handleApi);
        servletRoutes.get(ALIAS + "/api/", this::handleApi);
        servletRoutes.get(ALIAS + "/api/index.html", this::handleApi);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        servletRoutes.processRoutes(req, resp);
    }

    private void handleApi(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setStatus(200);
        resp.getWriter().append("Hello World");
    }

}
```
