package org.zhq.core.servlet.base;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zhq.core.enumeration.RequestMethod;
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

import static org.zhq.core.context.Context.*;

@Data
@Slf4j
public class DispatcherServlet {
    private ResourceHandler resourceHandler;
    private ExceptionHandler exceptionHandler;
    private ThreadPoolExecutor pool;
    private ServletContext servletContext;

    public DispatcherServlet() {
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
            String url = request.getUrl();
            RequestMethod method = request.getMethod();
            //首页逻辑 / -> /index.html
            if (isHome(url)) {
                request.setUrl(FIRST_PAGE);
                url = request.getUrl();
            }
            if (isStaticResource(url, method)) {
                //静态资源
                log.info("静态资源:{}", url);
                if (url.endsWith(VIEW_SUFFIX)) {
                    resourceHandler.handle(VIEW_PREFIX + url, response, client);
                } else {
                    resourceHandler.handle(VIEW_STATIC + url, response, client);
                }
            } else {
                //动态资源
                HttpServlet servlet = servletContext.dispatch(url);
                pool.execute(new RequestHandler(client, request, response, servlet, exceptionHandler));
            }
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, client);
        }
    }

    private boolean isStaticResource(String url, RequestMethod method) {
        return method == RequestMethod.GET && url.contains(".");
    }

    private boolean isHome(String url) {
        return url.equals("/");
    }
}
