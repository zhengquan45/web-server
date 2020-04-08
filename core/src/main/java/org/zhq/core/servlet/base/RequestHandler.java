package org.zhq.core.servlet.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.ServletErrorException;
import org.zhq.core.exception.ServletNotFoundException;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.exception.handler.ExceptionHandler;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;

import java.io.IOException;
import java.net.Socket;

@Data
@AllArgsConstructor
@Slf4j
public class RequestHandler implements Runnable {
    private Socket client;
    private Request request;
    private Response response;
    private HttpServlet servlet;
    private ExceptionHandler exceptionHandler;

    @Override
    public void run() {
        try {
            if (servlet == null) {
                throw new ServletNotFoundException(HTTPStatus.NOT_FOUND);
            }
            request.setRequestHandler(this);
            servlet.service(request, response);
            response.write();
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, client);
        } catch (IOException e) {
            exceptionHandler.handle(new ServletErrorException(HTTPStatus.INTERNAL_SERVER_ERROR), response, client);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
