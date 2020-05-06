package org.zhq.core.servlet.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.enumeration.RequestMethod;
import org.zhq.core.exception.ServletErrorException;
import org.zhq.core.exception.ServletNotFoundException;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.exception.handler.ExceptionHandler;
import org.zhq.core.request.Request;
import org.zhq.core.resource.ResourceHandler;
import org.zhq.core.response.Response;
import org.zhq.core.servlet.context.ServletContext;

import java.io.IOException;
import java.net.Socket;

/**
 * 请求逻辑处理器
 */
@Data
@AllArgsConstructor
@Slf4j
public class RequestHandler implements Runnable {
    private Socket client;
    private Request request;
    private Response response;
    private HttpServlet servlet;
    private ExceptionHandler exceptionHandler;
    private ResourceHandler resourceHandler;

    @Override
    public void run() {
        try {
            String url = request.getUrl();
            RequestMethod method = request.getMethod();
            request.setRequestHandler(this);
            if (isStaticResource(url,method)) {
                log.info("静态资源:{}", url);
                //首页
                if (url.equals("/")) {
                    request.setUrl("/index.html");
                }
                resourceHandler.handle(request, response, client);
            }else {
                if (servlet == null) {
                    throw new ServletNotFoundException();
                }
                servlet.service(request, response);
                response.write();
            }
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, client);
        } catch (IOException e) {
            exceptionHandler.handle(new ServletErrorException(), response, client);
        }
    }

    private boolean isStaticResource(String url, RequestMethod method) {
        return method == RequestMethod.GET && (url.contains(".") || url.equals("/"));
    }
}
