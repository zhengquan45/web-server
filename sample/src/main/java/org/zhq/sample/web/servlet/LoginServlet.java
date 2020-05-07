package org.zhq.sample.web.servlet;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;
import org.zhq.core.servlet.impl.HttpServlet;
import org.zhq.sample.web.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by SinjinSong on 2017/7/21.
 */
@Slf4j
public class LoginServlet extends HttpServlet {
    
    private UserService userService;

    public LoginServlet() {
        userService = UserService.getInstance();
    }

    @Override
    public void init() {
        log.info("LoginServlet init...");
    }

    @Override
    public void destroy() {
        log.info("LoginServlet destroy...");
    }

    @Override
    public void doGet(Request request, Response response) throws ServletException, IOException {
        String username = (String) request.getSession().getAttribute("username");
        if (username != null) {
            log.info("已经登录，跳转至success页面");
            response.sendRedirect("/views/success.html");
        } else {
            request.getRequestDispatcher("/views/login.html").forward(request,response);
        }
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (userService.login(username, password)) {
            log.info("{} 登录成功", username);
            request.getSession().setAttribute("username", username);
            response.sendRedirect("/views/success.html");
        } else {
            log.info("登录失败");
            response.sendRedirect("/views/errors/400.html");
        }
    }
}
