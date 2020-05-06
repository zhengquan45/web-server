package org.zhq.core.network.handler.bio;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.zhq.core.exception.handler.ExceptionHandler;
import org.zhq.core.network.handler.AbstractRequestHandler;
import org.zhq.core.network.wrapper.SocketWrapper;
import org.zhq.core.network.wrapper.bio.BioSocketWrapper;
import org.zhq.core.request.Request;
import org.zhq.core.resource.ResourceHandler;
import org.zhq.core.response.Response;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by SinjinSong on 2017/7/21.
 * Servlet运行容器
 */
@Setter
@Getter
@Slf4j
public class BioRequestHandler extends AbstractRequestHandler {

    public BioRequestHandler(SocketWrapper socketWrapper, ExceptionHandler exceptionHandler, ResourceHandler resourceHandler, Request request, Response response) {
        super(socketWrapper, exceptionHandler, resourceHandler, request, response);
    }

    @Override
    public void flushResponse() {
        isFinished = true;
        BioSocketWrapper bioSocketWrapper = (BioSocketWrapper) socketWrapper;
        byte[] bytes = response.getResponseBytes();
        OutputStream os = null;
        try {
            os = bioSocketWrapper.getSocket().getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("socket closed");
        } finally {
            try {
                os.close();
                bioSocketWrapper.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
