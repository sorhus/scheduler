package com.github.sorhus.scheduler.app;

import com.github.sorhus.scheduler.pipe.PipeService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {

        Server jettyServer = new Server(8080);

        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/pipe");
        PipeAPI pipeAPI = new PipeAPI(new PipeService(Executors.newFixedThreadPool(2)));
        ResourceHandler resourceHandler1 = new ResourceHandler();
        ServletContainer pipeAPIContainer = new ServletContainer(new ResourceConfig().register(pipeAPI));
        contextHandler.addServlet(new ServletHolder(pipeAPIContainer), "/*");

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase("src/main/webapp/");

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[] {contextHandler, resourceHandler, new DefaultHandler()});

        jettyServer.setHandler(handlerList);

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }
}