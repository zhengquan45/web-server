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
 * @author sinjinsong
 * @date 2018/5/3
 */
@Slf4j
public class UserEditServlet extends HttpServlet {
    private UserService userService;

    public UserEditServlet() {
        userService = UserService.getInstance();
    }

    
    @Override
    public void doGet(Request request, Response response) throws ServletException, IOException {
        User user = userService.findByUsername((String) request.getSession().getAttribute("username"));
        request.setAttribute("user",user);
        request.getRequestDispatcher("/views/userEdit.html").forward(request,response);
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException, IOException {
        log.info("{}",request.getParams());
        User user = new User();
        user.setUsername((String) request.getSession(false).getAttribute("username"));
        user.setRealName(request.getParameter("realName"));
        user.setAge(Integer.valueOf(request.getParameter("age")));
        userService.update(user);
        
        request.setAttribute("user",user);
        request.getRequestDispatcher("/views/user.html").forward(request, response);
    }
}
