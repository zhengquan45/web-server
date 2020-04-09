package org.zhq.core.request.dispatcher;

import org.zhq.core.exception.base.ServletException;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;

import java.io.IOException;

public interface RequestDispatcher {
    void forward(Request request, Response response) throws IOException, ServletException;
}
