package org.zhq.core.servlet.impl;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.enumeration.RequestMethod;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;

import java.io.IOException;

/**
 * @author sinjinsong
 * @date 2018/5/3
 */
@Slf4j
public class DefaultServlet extends HttpServlet {

    @Override
    public void service(Request request, Response response) throws ServletException, IOException {
        if (request.getMethod() == RequestMethod.GET) {
            //首页
            if (request.getUrl().equals("/")) {
                request.setUrl("/index.html");
            }
            request.getRequestDispatcher(request.getUrl()).forward(request, response);
        }
    }
}
