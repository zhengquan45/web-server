package org.zhq.core.request;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zhq.core.constant.CharsetProperties;
import org.zhq.core.context.WebApplication;
import org.zhq.core.cookie.Cookie;
import org.zhq.core.enumeration.RequestMethod;
import org.zhq.core.network.handler.AbstractRequestHandler;
import org.zhq.core.request.dispatcher.RequestDispatcher;
import org.zhq.core.request.dispatcher.impl.ApplicationRequestDispatcher;
import org.zhq.core.context.ServletContext;
import org.zhq.core.session.HttpSession;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.zhq.core.constant.CharConstant.BLANK;
import static org.zhq.core.constant.CharConstant.CRLF;
import static org.zhq.core.constant.ContextConstant.CONTENT_LENGTH;
import static org.zhq.core.constant.ContextConstant.COOKIE;

/**
 * Created by SinjinSong on 2017/7/20.
 * <p>
 * GET /search?hl=zh-CN&source=hp&q=domety&aq=f&oq= HTTP/1.1
 * Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/vnd.ms-excel, application/vnd.ms-powerpoint,
 * application/msword, application/x-silverlight
 * Referer: <a href="http://www.google.cn/">http://www.google.cn/</a>
 * Accept-Language: zh-cn
 * Accept-Encoding: gzip, deflate
 * User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; TheWorld)
 * Host: <a href="http://www.google.cn">www.google.cn</a>
 * Connection: Keep-Alive
 * Cookie: PREF=ID=80a06da87be9ae3c:U=f7167333e2c3b714:NW=1:TM=1261551909:LM=1261551917:S=ybYcq2wpfefs4V9g;
 * NID=31=ojj8d-IygaEtSxLgaJmqSjVhCspkviJrB6omjamNrSm8lZhKy_yMfO2M4QMRKcH1g0iQv9u-2hfBW7bUFwVh7pGaRUb0RnHcJU37y-
 * FxlRugatx63JLv7CWMD6UB_O_r
 * <p>
 * <p>
 * <p>
 * POST /search HTTP/1.1
 * Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/vnd.ms-excel, application/vnd.ms-powerpoint,
 * application/msword, application/x-silverlight
 * Referer: <a href="http://www.google.cn/">http://www.google.cn/</a>
 * Accept-Language: zh-cn
 * Accept-Encoding: gzip,deflate
 * User-Agent: Mozilla/4.0(compatible;MSIE6.0;Windows NT5.1;SV1;.NET CLR2.0.50727;TheWorld)
 * Host: <a href="http://www.google.cn">www.google.cn</a>
 * Connection: Keep-Alive
 * Cookie: PREF=ID=80a06da87be9ae3c:U=f7167333e2c3b714:NW=1:TM=1261551909:LM=1261551917:S=ybYcq2wpfefs4V9g;
 * NID=31=ojj8d-IygaEtSxLgaJmqSjVhCspkviJrB6omjamNrSm8lZhKy_yMfO2M4QMRKcH1g0iQv9u-2hfBW7bUFwVh7pGaRUb0RnHcJU37y-
 * FxlRugatx63JLv7CWMD6UB_O_r
 * <p>
 * hl=zh-CN&source=hp&q=domety
 */

@Data
@Slf4j
public class Request {
    private AbstractRequestHandler requestHandler;
    private RequestMethod method;
    private String url;
    private Map<String, List<String>> params;
    private Map<String, List<String>> headers;
    private Map<String, Object> attributes;
    private ServletContext servletContext;
    private List<Cookie> cookies;
    private HttpSession session;

    public Request(byte[] data){
        servletContext = WebApplication.getServletContext();
        attributes = new HashMap<>();
        String requestMsg = null;
        try {
            requestMsg = URLDecoder.decode(new String(data, CharsetProperties.UTF_8_CHARSET), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<String> lines = Arrays.asList(requestMsg.split(CRLF));
        log.info("Request读取完毕\r\n{}", Arrays.toString(lines.toArray()));
        parseHeader(lines);
        if (headers.containsKey(CONTENT_LENGTH) && !headers.get(CONTENT_LENGTH).get(0).equals("0")) {
            parseBody(lines.get(lines.size() - 1));
        }
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public RequestDispatcher getRequestDispatcher(String url) {
        return new ApplicationRequestDispatcher(url);
    }

    public HttpSession getSession() {
        if (session != null) {
            return session;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getKey().equals("JSESSIONID")) {
                log.info("servletContext:{}", servletContext);
                HttpSession currentSession = servletContext.getSession(cookie.getValue());
                if (currentSession != null) {
                    this.session = currentSession;
                    return session;
                }
            }
        }
        session = servletContext.createSession(requestHandler.getResponse());
        return session;
    }


    private void parseHeader(List<String> lines) {
        log.info("解析请求头");
        String firstLine = lines.get(0);
        //解析方法
        String[] firstLineSlices = firstLine.split(BLANK);
        this.method = RequestMethod.valueOf(firstLineSlices[0]);
        log.debug("method:{}", this.method);

        //解析URL
        String rawURL = firstLineSlices[1];
        String[] urlSlices = rawURL.split("\\?");
        this.url = urlSlices[0];
        log.debug("url:{}", this.url);

        //解析GET参数
        if (urlSlices.length > 1) {
            parseParams(urlSlices[1]);
        }
        log.debug("params:{}", this.params);

        parseHead(lines);
        log.debug("headers:{}", this.headers);
        parseCookie();
    }

    private void parseCookie() {
        if (headers.containsKey(COOKIE)) {
            String[] rawCookies = headers.get(COOKIE).get(0).split("; ");
            this.cookies = Stream.of(rawCookies).map(s -> {
                String[] kv = s.split("=");
                return new Cookie(kv[0], kv[1]);
            }).collect(Collectors.toList());
            headers.remove(COOKIE);
        } else {
            this.cookies = Collections.emptyList();
        }
        log.info("Cookies:{}", cookies);
    }

    private void parseHead(List<String> lines) {
        String header;
        this.headers = new HashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            header = lines.get(i);
            if (header.equals("")) {
                break;
            }
            int colonIndex = header.indexOf(':');
            String key = header.substring(0, colonIndex);
            String[] values = header.substring(colonIndex + 2).split(",");
            headers.put(key, Arrays.asList(values));
        }
    }

    private void parseBody(String body) {
        log.info("解析请求体");
        //解析请求体
        parseParams(body);
        log.debug("params:{}", this.params);
    }

    private void parseParams(String params) {
        String[] urlParams = params.split("&");
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        for (String param : urlParams) {
            String[] kv = param.split("=");
            String key = kv[0];
            String[] values = kv[1].split(",");
            this.params.put(key, Arrays.asList(values));
        }
    }
}
