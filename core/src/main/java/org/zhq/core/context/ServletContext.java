package org.zhq.core.context;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.zhq.core.context.holder.ServletHolder;
import org.zhq.core.cookie.Cookie;
import org.zhq.core.response.Response;
import org.zhq.core.servlet.Servlet;
import org.zhq.core.servlet.impl.DefaultServlet;
import org.zhq.core.session.HttpSession;
import org.zhq.core.util.XMLUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.zhq.core.constant.ContextConstant.JSESSIONID;

/**
 * Servlet上下文
 * 包含:
 * 1、servlet和servletName映射
 * 2、path和servletName映射
 * 3、全局属性表
 * 4、全局session表
 */
@Slf4j
public class ServletContext {

    private Map<String, ServletHolder> servletMap;
    private Map<String, String> mapping;
    private Map<String, Object> attributes;
    private Map<String, HttpSession> sessions;

     public ServletContext() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        servletMap = new HashMap<>();
        mapping = new HashMap<>();
        attributes = new ConcurrentHashMap<>();
        sessions = new ConcurrentHashMap<>();
        loadServletMap();
    }

    private void loadServletMap() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Document document = XMLUtil.getDocument(ServletContext.class.getResource("/WEB-INF/web.xml").getFile());
        if (document == null) {
            throw new IllegalStateException("/WEB-INF/web.xml 文件不存在");
        }
        Element root = document.getRootElement();
        if (root == null) {
            throw new IllegalStateException("/WEB-INF/web.xml 文件没有根结点");
        }
        List<Element> servlets = root.elements("servlet");
        if (servlets == null || servlets.size() == 0) {
            throw new IllegalStateException("/WEB-INF/web.xml 文件没有servlet结点");
        }
        for (Element servlet : servlets) {
            String key = servlet.element("servlet-name").getText();
            String value = servlet.element("servlet-class").getText();
            servletMap.put(key, new ServletHolder(value));
        }

        List<Element> mappings = root.elements("servlet-mapping");
        if (mappings == null || mappings.size() == 0) {
            throw new IllegalStateException("/WEB-INF/web.xml 文件没有servlet-mapping结点");
        }
        for (Element mapping : mappings) {
            String key = mapping.element("url-pattern").getText();
            String value = mapping.element("servlet-name").getText();
            log.info("Loaded Servlet:" + value + ". Mapped Url:" + key);
            this.mapping.put(key,value);
        }
    }

    public HttpSession getSession(String id) {
        return sessions.get(id);
    }

    public HttpSession createSession(Response response) {
        HttpSession session = new HttpSession(UUID.randomUUID().toString().toUpperCase());
        sessions.put(session.getId(),session);
        response.addCookie(new Cookie(JSESSIONID,session.getId()));
        return session;
    }

    public Object getAttribute(String key){
        return attributes.get(key);
    }

    public void setAttributes(String key,Object value){
        attributes.put(key,value);
    }

    public Servlet mapServlet(String url) {
        ServletHolder servletHolder = this.servletMap.get(mapping.get(url));
        if (servletHolder == null) {
            return new DefaultServlet();
        }
        if(servletHolder.getServlet()!=null){
            return servletHolder.getServlet();
        }
        String servletClass = servletHolder.getServletClass();
        try {
            Servlet servlet = (Servlet) Class.forName(servletClass).newInstance();
            servletHolder.setServlet(servlet);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return servletHolder.getServlet();
    }
}