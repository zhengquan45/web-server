package org.zhq.core.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.exception.RequestInvalidException;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.response.Response;
import org.zhq.core.util.IOUtil;

import java.io.IOException;
import java.net.Socket;

import static org.zhq.core.context.Context.ERROR_PAGE;

@Slf4j
public class ExceptionHandler {
    public void handle(ServletException e, Response response, Socket socket) {
        try {
            if (e instanceof RequestInvalidException) {
                log.info("请求无法读取,丢弃");
            } else {
                log.error("抛出异常:{}", e.getClass().getName());
                response.header(e.getStatus())
                        .body(IOUtil.getBytesFromFile(String.format(ERROR_PAGE, e.getStatus().getCode())))
                        .write();
                log.info("异常页面已写入输入流");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
