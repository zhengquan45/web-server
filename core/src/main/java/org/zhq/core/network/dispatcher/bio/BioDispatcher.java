package org.zhq.core.network.dispatcher.bio;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.exception.RequestInvalidException;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.network.dispatcher.AbstractDispatcher;
import org.zhq.core.network.handler.bio.BioRequestHandler;
import org.zhq.core.network.wrapper.SocketWrapper;
import org.zhq.core.network.wrapper.bio.BioSocketWrapper;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author sinjinsong
 * @date 2018/5/4
 */
@Slf4j
public class BioDispatcher extends AbstractDispatcher {
    @Override
    public void doDispatch(SocketWrapper socketWrapper) {
        BioSocketWrapper bioSocketWrapper = (BioSocketWrapper) socketWrapper;
        Socket socket = bioSocketWrapper.getSocket();
        Request request;
        Response response = null;
        log.info("开始读取Request");
        try {
            BufferedInputStream bin = new BufferedInputStream(socket.getInputStream());
            byte[] buf = null;
            try {
                buf = new byte[bin.available()];
                int len = bin.read(buf);
                if (len <= 0) {
                    throw new RequestInvalidException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //解析请求
            response = new Response();
            request = new Request(buf);
            pool.execute(new BioRequestHandler(socketWrapper, exceptionHandler, resourceHandler, request, response));
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, socketWrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
