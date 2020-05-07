package org.zhq.core.filter.impl;

import org.zhq.core.filter.Filter;
import org.zhq.core.filter.FilterChain;
import org.zhq.core.request.Request;
import org.zhq.core.response.Response;

public abstract class HttpFilter implements Filter {
    protected String []excludePages;

    @Override
    public void doFilter(Request request, Response response, FilterChain filterChain) {
        boolean isExcludedPage = false;
        for (String excludePage : excludePages) {
            if(request.getUrl().equals(excludePage)){
                isExcludedPage = true;
                break;
            }
        }
        if(isExcludedPage){
            filterChain.doFilter(request, response);
        }

    }

}
