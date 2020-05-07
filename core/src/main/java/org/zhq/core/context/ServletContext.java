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
import org.zhq.core.session.IdleSessionCleaner;
import org.zhq.core.util.XMLUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
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
    private IdleSessionCleaner idleSessionCleaner;

    public static final int DEFAULT_SESSION_TIMEOUT = 30;

     public ServletContext() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
         servletMap = new HashMap<>();
         mapping = new HashMap<>();
         attributes = new ConcurrentHashMap<>();
         sessions = new ConcurrentHashMap<>();
         idleSessionCleaner = new IdleSessionCleaner();
         idleSessionCleaner.start();
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

    public HttpSession getSession(String id) {
        return sessions.get(id);
    }

    public HttpSession createSession(Response response) {
        HttpSession session = new HttpSession(UUID.randomUUID().toString().toUpperCase());
        sessions.put(session.getId(), session);
        response.addCookie(new Cookie(JSESSIONID, session.getId()));
        return session;
    }

    public void invalidateSession(HttpSession session) {
        sessions.remove(session.getId());
    }

    //这里想了想不加锁了 同时操作同一个session的机会比较小,一个用户只有一个session,如果出现多个用户同时操作session的情况大概率也是攻击。
    //且session这个数据如果操作过程中被过期机制删除就删除了 不影响业务
    public void cleanIdleSessions() {
        Set<String> keySet = sessions.keySet();
        Iterator itr = keySet.iterator();
        while (itr.hasNext()) {
            HttpSession session = sessions.get(itr.next());
            //按照默认的Session过期时间来进行清理闲置session
            if (Duration.between(session.getLastAccessed(), Instant.now()).getSeconds() >= DEFAULT_SESSION_TIMEOUT) {
                log.warn("session[" + session.getId() + "] 过期了");
                itr.remove();
            }
        }
    }


    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttributes(String key, Object value) {
        attributes.put(key, value);
    }

}
