package org.zhq.core.network.connector.bio;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.network.endpoint.bio.BioEndpoint;
import org.zhq.core.servlet.base.RequestDispatcher;

import java.io.IOException;
import java.net.Socket;

/**
 * BIO socket监听器
 */
@Slf4j
public class BioAcceptor implements Runnable {
    private BioEndpoint server;
    private RequestDispatcher dispatcher;
    
    public BioAcceptor(BioEndpoint server, RequestDispatcher dispatcher) {
        this.server = server;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        log.info("开始监听");
        while (server.isRunning()) {
            Socket client;
            try {
                //TCP的短连接，请求处理完即关闭
                client = server.accept();
                log.info("client:{}", client);
                dispatcher.doDispatch(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
