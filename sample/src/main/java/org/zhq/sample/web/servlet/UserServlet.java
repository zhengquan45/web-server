package org.zhq.sample.web.servlet;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;
import org.zhq.core.servlet.impl.HttpServlet;
import org.zhq.sample.web.domain.User;
import org.zhq.sample.web.service.UserService;

import java.io.IOException;

/**
 * Created by SinjinSong on 2017/7/21.
 */
@Slf4j
public class UserServlet extends HttpServlet {
    private UserService userService;

    public UserServlet() {
        userService = UserService.getInstance();
    }
    
    @Override
    public void doGet(Request request, Response response) throws ServletException, IOException {
        String username = (String) request.getSession().getAttribute("username");
        if(username!=null) {
            User user = userService.findByUsername(username);
            request.setAttribute("user", user);
        }
        request.getRequestDispatcher("/views/user.html").forward(request, response);
    }
}
