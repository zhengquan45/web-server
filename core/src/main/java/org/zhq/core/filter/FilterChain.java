package org.zhq.core.filter;

import org.zhq.core.request.Request;
import org.zhq.core.response.Response;

/**
 * @author sinjinsong
 * @date 2018/5/2
 */
public interface FilterChain {
    void doFilter(Request request, Response response) ;
}
