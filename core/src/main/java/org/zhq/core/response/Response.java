package org.zhq.core.response;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.zhq.core.constant.CharsetProperties;
import org.zhq.core.cookie.Cookie;
import org.zhq.core.enumeration.HTTPStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static org.zhq.core.constant.CharConstant.BLANK;
import static org.zhq.core.constant.CharConstant.CRLF;
import static org.zhq.core.context.Context.*;


/**
 * Created by SinjinSong on 2017/7/20.
 * <p>
 * HTTP/1.1 200 OK
 * Date: Sat, 31 Dec 2005 23:59:59 GMT
 * Content-Type: text/html;constant=ISO-8859-1
 * Content-Length: 122
 * <p>
 * ＜html＞
 * ＜head＞
 * ＜title＞Wrox Homepage＜/title＞
 * ＜/head＞
 * ＜body＞
 * ＜!-- body goes here --＞
 * ＜/body＞
 * ＜/html＞
 */
@Data
@Slf4j
public class Response {

    private StringBuilder headerAppender;
    private StringBuilder bodyAppender;
    private List<Cookie> cookies;
    private byte[] body;
    private OutputStream os;

    public Response(OutputStream os) {
        this.os = os;
        this.headerAppender = new StringBuilder();
        this.bodyAppender = new StringBuilder();
        this.cookies = new ArrayList<>();
    }

    public Response header(HTTPStatus status, String contentType, Map<String, String> headers) {
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        //HTTP/1.1 200 OK
        headerAppender.append("HTTP/1.1").append(BLANK).append(status.getCode()).append(BLANK).append(status).append(CRLF);
        //Date: Sat, 31 Dec 2005 23:59:59 GMT
        headerAppender.append("Date:").append(BLANK).append(new Date()).append(CRLF);
        headerAppender.append(CONTENT_TYPE).append(":").append(BLANK).append(contentType).append(CRLF);
        headerAppender.append(CONTENT_LENGTH).append(":").append(BLANK).append(CRLF);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                headerAppender.append(entry.getKey()).append(":").append(BLANK).append(entry.getValue()).append(CRLF);
            }
        }
        if (cookies.size() > 0) {
            for (Cookie cookie : cookies) {
                headerAppender.append(SET_COOKIE).append(":").append(BLANK).append(cookie.getKey()).append("=").append(cookie.getValue()).append(CRLF);
            }
        }
        return this;
    }

    public Response header(HTTPStatus status) {
        return header(status, DEFAULT_CONTENT_TYPE, null);
    }

    public Response header(HTTPStatus status, String contentType) {
        return header(status, contentType, null);
    }

    public Response header(HTTPStatus status, Map<String, String> headers) {
        return header(status, DEFAULT_CONTENT_TYPE, headers);
    }


    public Response body(byte[] body) {
        headerAppender.append(body.length).append(CRLF).append(CRLF);
        this.body = body;
        return this;
    }

    public void write() {
        //默认返回OK
        if (this.headerAppender.toString().length() == 0) {
            header(HTTPStatus.OK);
        }

        if (body == null) {
            log.info("多次使用print或println构建的响应体");
            body(bodyAppender.toString().getBytes(CharsetProperties.charset));
        }

        byte[] header = headerAppender.toString().getBytes(CharsetProperties.charset);
        byte[] response = new byte[header.length + body.length];

        System.arraycopy(header, 0, response, 0, header.length);
        System.arraycopy(body, 0, response, header.length, body.length);

        try {
            os.write(response);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendRedirect(String url) {
        log.info("重定向至{}", url);
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", url);
        header(HTTPStatus.MOVED_TEMPORARILY, headers);
        body(bodyAppender.toString().getBytes(CharsetProperties.charset));
    }

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }
}
