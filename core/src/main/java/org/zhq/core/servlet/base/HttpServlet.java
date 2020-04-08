package org.zhq.core.servlet.base;

import org.zhq.core.enumeration.RequestMethod;
import org.zhq.core.exception.ResourceNotFoundException;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;

import java.io.IOException;

public class HttpServlet {
    public void service(Request request, Response response) throws ServletException, IOException {
        if (request.getMethod() == RequestMethod.GET) {
            doGet(request, response);
        } else if (request.getMethod() == RequestMethod.POST) {
            doPost(request, response);
        } else if (request.getMethod() == RequestMethod.PUT) {
            doPut(request, response);
        } else if (request.getMethod() == RequestMethod.DELETE) {
            doDelete(request, response);
        }
    }

    public void doGet(Request request, Response response) throws ServletException, IOException {

    }

    public void doPost(Request request, Response response) throws ServletException, IOException {

    }

    public void doPut(Request request, Response response) {

    }

    public void doDelete(Request request, Response response) {

    }
}
