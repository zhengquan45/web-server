package org.zhq.core.servlet.base;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.exception.handler.ExceptionHandler;
import org.zhq.core.request.Request;
import org.zhq.core.resource.ResourceHandler;
import org.zhq.core.response.Response;
import org.zhq.core.servlet.context.ServletContext;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class RequestDispatcher {
    private ResourceHandler resourceHandler;
    private ExceptionHandler exceptionHandler;
    private ThreadPoolExecutor pool;
    private ServletContext servletContext;

    public RequestDispatcher() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        servletContext = ServletContext.getInstance();
        exceptionHandler = new ExceptionHandler();
        resourceHandler = new ResourceHandler(exceptionHandler);
        pool = new ThreadPoolExecutor(5, 8, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void shutdown() {
        pool.shutdown();
    }

    public void doDispatch(Socket client) throws IOException {
        Response response = new Response(client.getOutputStream());
        try {
            Request request = new Request(client.getInputStream());
            request.setServletContext(servletContext);
            HttpServlet servlet = servletContext.dispatch(request.getUrl());
            pool.execute(new RequestHandler(client, request, response, servlet, exceptionHandler, resourceHandler));
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, client);
        }
    }


}
