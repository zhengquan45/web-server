package org.zhq.sample.web.servlet;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;
import org.zhq.sample.web.service.UserService;
import org.zhq.core.servlet.base.HttpServlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class LoginServlet extends HttpServlet {

    private UserService userService;

    public LoginServlet() {
        userService = new UserService();
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException, IOException {
        Map<String, List<String>> params = request.getParams();
        String username = params.get("username").get(0);
        String password = params.get("password").get(0);
        if(userService.login(username,password)){
            log.info("{} 登录成功",username);
            request.getSession().setAttributes("username",username);
            response.sendRedirect("/views/success.html");
        }else{
            log.info("登录失败");
            response.sendRedirect("/errors/400.html");
        }
    }

    @Override
    public void doGet(Request request, Response response) throws ServletException, IOException {
        String username = (String) request.getSession().getAttribute("username");
        if (username != null) {
            log.info("已经登录，跳转至success页面");
            response.sendRedirect("/success.html");
        } else {
            request.getRequestDispatcher("/views/login.html").forward(request,response);
        }
    }
}
