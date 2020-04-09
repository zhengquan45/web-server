package org.zhq.core.resource;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.RequestParseException;
import org.zhq.core.exception.ResourceNotFoundException;
import org.zhq.core.exception.handler.ExceptionHandler;
import org.zhq.core.response.Response;
import org.zhq.core.util.IOUtil;
import org.zhq.core.util.MimeTypeUtil;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

@Slf4j
public class ResourceHandler {
    private ExceptionHandler exceptionHandler;

    public ResourceHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void handle(String url, Response response, Socket socket) {
        try {
            URL resource = ResourceHandler.this.getClass().getResource(url);
            if (resource == null) {
                log.error("找不到该资源:{}", url);
                throw new ResourceNotFoundException();
            }
            response.header(HTTPStatus.OK, MimeTypeUtil.getTypes(url)).body(IOUtil.getBytesFromFile(url)).write();
            log.info("{}已经写入输出流", url);
        } catch (IOException e) {
            e.printStackTrace();
            exceptionHandler.handle(new RequestParseException(), response, socket);
        } catch (ResourceNotFoundException e) {
            exceptionHandler.handle(e, response, socket);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
