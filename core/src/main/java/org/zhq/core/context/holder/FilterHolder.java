package org.zhq.core.context.holder;

import lombok.Data;
import org.zhq.core.filter.Filter;

@Data
public class FilterHolder {
    private Filter filter;
    private String filterClass;

    public FilterHolder(String filterClass) {
        this.filterClass = filterClass;
    }
}
