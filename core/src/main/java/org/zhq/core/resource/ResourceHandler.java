package org.zhq.core.resource;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.constant.CharsetProperties;
import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.ResourceNotFoundException;
import org.zhq.core.exception.ServletErrorException;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.exception.handler.ExceptionHandler;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;
import org.zhq.core.template.TemplateResolver;
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

    public void handle(Request request, Response response, Socket socket) {
        String url = request.getUrl();
        try {
            URL resource = ResourceHandler.this.getClass().getResource(url);
            if (resource == null) {
                log.error("找不到该资源:{}", url);
                throw new ResourceNotFoundException();
            }
            byte [] body = IOUtil.getBytesFromFile(url);
            if(url.endsWith(".html")) {
                // 对html文件模版参数替换
                body = TemplateResolver.resolve(new String(IOUtil.getBytesFromFile(url), CharsetProperties.UTF_8_CHARSET), request).getBytes(CharsetProperties.UTF_8_CHARSET);
            }
            response.header(HTTPStatus.OK, MimeTypeUtil.getTypes(url)).body(body).write();
            log.info("{}已经写入输出流", url);
        } catch (IOException e) {
            exceptionHandler.handle(new ServletErrorException(), response, socket);
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, socket);
        }

    }

}
