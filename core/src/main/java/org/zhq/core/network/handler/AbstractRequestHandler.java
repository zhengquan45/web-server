package org.zhq.core.network.handler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.zhq.core.context.WebApplication;
import org.zhq.core.exception.ServletErrorException;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.exception.handler.ExceptionHandler;
import org.zhq.core.network.wrapper.SocketWrapper;
import org.zhq.core.request.Request;
import org.zhq.core.resource.ResourceHandler;
import org.zhq.core.response.Response;
import org.zhq.core.servlet.Servlet;
import org.zhq.core.context.ServletContext;

/**
 * @author sinjinsong
 * @date 2018/5/4
 */
@Slf4j
@Getter
public abstract class AbstractRequestHandler implements Runnable {

    protected Request request;
    protected Response response;
    protected SocketWrapper socketWrapper;
    protected ServletContext servletContext;
    protected ExceptionHandler exceptionHandler;
    protected ResourceHandler resourceHandler;
    protected boolean isFinished;
    protected Servlet servlet;

    public AbstractRequestHandler(SocketWrapper socketWrapper,ExceptionHandler exceptionHandler, ResourceHandler resourceHandler, Request request, Response response) {
        this.servletContext = WebApplication.getServletContext();
        this.socketWrapper = socketWrapper;
        this.exceptionHandler = exceptionHandler;
        this.resourceHandler = resourceHandler;
        this.isFinished = false;
        this.request = request;
        this.request.setRequestHandler(this);
        this.response = response;
        this.servlet = servletContext.mapServlet(request.getUrl());
    }

    @Override
    public void run() {
        service();
    }

    private void service() {
        log.info("socket isClosed: {}", this.socketWrapper.isClosed());
        try {
            //处理动态资源，交由某个Servlet执行
            //Servlet是单例多线程
            //Servlet在RequestHandler中执行
            servlet.service(request, response);
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, socketWrapper);
        } catch (Exception e) {
            //其他未知异常
            e.printStackTrace();
            exceptionHandler.handle(new ServletErrorException(), response, socketWrapper);
        } finally {
            if (!isFinished) {
                flushResponse();
            }
        }
        log.info("请求处理完毕");
    }

    public abstract void flushResponse();
}
