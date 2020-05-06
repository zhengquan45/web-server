package org.zhq.core;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.servlet.base.RequestDispatcher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by SinjinSong on 2017/7/20.
 */
@Slf4j
public class Server {
    private static final int PORT = 8080;
    private ServerSocket server;
    private RequestDispatcher requestDispatcher;

    private Acceptor acceptor;

    public Server() {
        try {
            server = new ServerSocket(PORT);
            acceptor = new Acceptor();
            acceptor.start();
            requestDispatcher = new RequestDispatcher();
            log.info("服务器启动,端口:{}",PORT);
        } catch (Exception e) {
            log.error("服务器初始化异常:{}",e);
            close();
        }
    }

    public void close() {
        acceptor.shutdown();
        requestDispatcher.shutdown();
    }

    /**
     * socket接收监听器
     */
    private class Acceptor extends Thread {
        public Acceptor() {
            super("Socket Acceptor");
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = server.accept();
                    log.info("客户端:{}",client);
                    //把接收的socket交给请求调度器调度
                    requestDispatcher.doDispatch(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void shutdown(){
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                this.interrupt();
            }
        }


    }

}
