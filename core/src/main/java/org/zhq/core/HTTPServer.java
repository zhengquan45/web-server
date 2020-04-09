package org.zhq.core;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.enumeration.RequestMethod;
import org.zhq.core.request.Request;
import org.zhq.core.servlet.base.DispatcherServlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by SinjinSong on 2017/7/20.
 */
@Slf4j
public class HTTPServer {
    private static final int PORT = 8080;
    private ServerSocket server;
    private DispatcherServlet dispatcherServlet;

    private Listener listener;

    public HTTPServer() {
        try {
            server = new ServerSocket(PORT);
            listener = new Listener();
            listener.start();
            dispatcherServlet = new DispatcherServlet();
            log.info("服务器启动,端口:{}",PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        listener.shutdown();
        dispatcherServlet.shutdown();
    }

    private class Listener extends Thread {
        public void shutdown(){
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                this.interrupt();
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = server.accept();
                    log.info("客户端:{}",client);
                    dispatcherServlet.doDispatch(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

     public static void run(){
         HTTPServer server = new HTTPServer();
         Scanner scanner = new Scanner(System.in);
         String order;
         while (scanner.hasNext()) {
             order = scanner.next();
             if (order.equals("EXIT")) {
                 server.close();
             }
         }
     }
}
