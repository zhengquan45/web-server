package org.zhq.core.context.holder;

import lombok.Data;
import org.zhq.core.servlet.Servlet;

@Data
public class ServletHolder {
    private String servletClass;
    private Servlet servlet;
    public ServletHolder(String servletClass) {
        this.servletClass = servletClass;
    }
}
