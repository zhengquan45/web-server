package org.zhq.sample.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.filter.Filter;
import org.zhq.core.filter.FilterChain;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;

/**
 * @author sinjinsong
 * @date 2018/5/3
 */
@Slf4j
public class LogFilter implements Filter {
    @Override
    public void init() {
        log.info("LogFilter init...");
    }

    @Override
    public void doFilter(Request request, Response response, FilterChain filterChain) {
        log.info("{} before accessed, method is {}", request.getUrl(), request.getMethod());
        filterChain.doFilter(request, response);
        log.info("{} after accessed, method is {}", request.getUrl(), request.getMethod());
    }

    @Override
    public void destroy() {
        log.info("LogFilter destroy...");
    }
}
