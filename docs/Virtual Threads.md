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