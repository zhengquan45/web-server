package org.zhq.core.servlet;

import org.zhq.core.exception.base.ServletException;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;

import java.io.IOException;

public interface Servlet {
    void init();

    void destroy();

    void service(Request request, Response response) throws ServletException, IOException;
}
