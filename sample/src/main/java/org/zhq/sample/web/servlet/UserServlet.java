package org.zhq.sample.web.servlet;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;
import org.zhq.core.servlet.impl.HttpServlet;

import java.io.IOException;

@Slf4j
public class UserServlet extends HttpServlet {

    @Override
    public void doGet(Request request, Response response) throws ServletException, IOException {
        if (request.getSession().getAttribute("username")!=null) {
            request.getRequestDispatcher("/views/user.html").forward(request,response);
        }else{
            response.sendRedirect("http://localhost:8080/views/login.html");
        }
    }
}
