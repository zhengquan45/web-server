package org.zhq.core.network.dispatcher;

import org.zhq.core.context.WebApplication;
import org.zhq.core.exception.RequestInvalidException;
import org.zhq.core.exception.handler.ExceptionHandler;
import org.zhq.core.network.wrapper.SocketWrapper;
import org.zhq.core.resource.ResourceHandler;
import org.zhq.core.context.ServletContext;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author sinjinsong
 * @date 2018/5/4
 */
public abstract class AbstractDispatcher {
    protected ResourceHandler resourceHandler;
    protected ExceptionHandler exceptionHandler;
    protected ThreadPoolExecutor pool;
    protected ServletContext servletContext;
    
    public AbstractDispatcher() {
        this.servletContext = WebApplication.getServletContext();
        this.exceptionHandler = new ExceptionHandler();
        this.resourceHandler = new ResourceHandler(exceptionHandler);
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Worker Pool-" + count++);
            }
        };
        this.pool = new ThreadPoolExecutor(100, 100, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(200), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void shutdown() {
        pool.shutdown();
//        servletContext.destroy();
    }
    
    public abstract void doDispatch(SocketWrapper socketWrapper) throws RequestInvalidException;
}
