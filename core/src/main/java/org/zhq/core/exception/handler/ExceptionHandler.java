package org.zhq.core.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.exception.RequestInvalidException;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.network.wrapper.SocketWrapper;
import org.zhq.core.response.Response;
import org.zhq.core.util.IOUtil;

import java.io.IOException;

import static org.zhq.core.constant.ContextConstant.ERROR_PAGE;

@Slf4j
public class ExceptionHandler {
    public void handle(ServletException e, Response response, SocketWrapper socketWrapper) {
        try {
            if (e instanceof RequestInvalidException) {
                log.info("请求无法读取,丢弃");
            } else {
                log.error("抛出异常:{}", e.getClass().getName());
                response.header(e.getStatus())
                        .body(IOUtil.getBytesFromFile(String.format(ERROR_PAGE, e.getStatus().getCode())));
                log.info("异常页面已写入输入流");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }finally {
            try {
                socketWrapper.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
