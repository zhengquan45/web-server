package org.zhq.core.context;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.util.AntPathMatcher;
import org.zhq.core.context.holder.FilterHolder;
import org.zhq.core.context.holder.ServletHolder;
import org.zhq.core.cookie.Cookie;
import org.zhq.core.filter.Filter;
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
import java.util.stream.Collectors;

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
    private Map<String, FilterHolder> filterMap;
    private Map<String, String> servletMapping;
    private Map<String, List<String>> filterMapping;
    private Map<String, Object> attributes;
    private Map<String, HttpSession> sessions;
    private IdleSessionCleaner idleSessionCleaner;
    private AntPathMatcher matcher;

    public static final int DEFAULT_SESSION_TIMEOUT = 30;

     public ServletContext() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
         servletMap = new HashMap<>();
         filterMap = new HashMap<>();
         servletMapping = new HashMap<>();
         filterMapping = new HashMap<>();
         attributes = new ConcurrentHashMap<>();
         sessions = new ConcurrentHashMap<>();
         matcher = new AntPathMatcher();
         idleSessionCleaner = new IdleSessionCleaner();
         idleSessionCleaner.start();
         parseConfig();
    }

    private void parseConfig() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Document document = XMLUtil.getDocument(ServletContext.class.getResource("/WEB-INF/web.xml").getFile());
        if (document == null) {
            throw new IllegalStateException("/WEB-INF/web.xml 文件不存在");
        }
        Element root = document.getRootElement();
        if (root == null) {
            throw new IllegalStateException("/WEB-INF/web.xml 文件没有根结点");
        }
        parseServletConfig(root);
        parseFilterConfig(root);
    }

    private void parseFilterConfig(Element root) {
        List<Element> filters = root.elements("filter");
        if (filters == null || filters.size() == 0) {
            throw new IllegalStateException("/WEB-INF/web.xml 文件没有filter结点");
        }
        for (Element filter : filters) {
            String key = filter.element("filter-name").getText();
            String value = filter.element("filter-class").getText();
            filterMap.put(key, new FilterHolder(value));
        }

        List<Element> mappings = root.elements("filter-mapping");
        if (mappings == null || mappings.size() == 0) {
            throw new IllegalStateException("/WEB-INF/web.xml 文件没有filter-mapping结点");
        }
        for (Element mapping : mappings) {
            List<Element> urlPatterns = mapping.elements("url-pattern");
            String value = mapping.element("filter-name").getText();
            StringJoiner stringJoiner = new StringJoiner(",");
            for (Element urlPattern : urlPatterns) {
                List<String> values = this.filterMapping.get(urlPattern.getText());
                if (values == null) {
                    values = new ArrayList<>();
                    this.filterMapping.put(urlPattern.getText(), values);
                }
                values.add(value);
                stringJoiner.add(urlPattern.getText());
            }
            log.info("Loaded Filter:" + value + ". Mapped Url:" + stringJoiner.toString());
        }
    }

    private void parseServletConfig(Element root) {
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
            this.servletMapping.put(key, value);
        }
    }

    public Servlet mapServlet(String url) {
        ServletHolder servletHolder = this.servletMap.get(servletMapping.get(url));
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

    public List<Filter> mapFilter(String url) {
        List<String> matchingPatterns = new ArrayList<>();
        Set<String> patterns = filterMapping.keySet();
        for (String pattern : patterns) {
            if (matcher.match(pattern, url)) {
                matchingPatterns.add(pattern);
            }
        }

        Set<String> filterAliases = matchingPatterns.stream().flatMap(pattern -> this.filterMapping.get(pattern).stream()).collect(Collectors.toSet());
        List<Filter> result = new ArrayList<>();
        for (String alias : filterAliases) {
            Filter filter = initAndGetFilter(alias);
            if (filter != null) {
                result.add(filter);
            }
        }
        return result;
    }

    private Filter initAndGetFilter(String filterAlias) {
        FilterHolder filterHolder = filterMap.get(filterAlias);
        if (filterHolder == null) {
            return null;
        }
        if (filterHolder.getFilter() == null) {
            try {
                Filter filter = (Filter) Class.forName(filterHolder.getFilterClass()).newInstance();
                filter.init();
                filterHolder.setFilter(filter);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return filterHolder.getFilter();
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
