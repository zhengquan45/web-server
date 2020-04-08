package org.zhq.core.exception;

import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.base.ServletException;

public class ServletNotFoundException extends ServletException {
    public ServletNotFoundException(HTTPStatus status) {
        super(status);
    }
}
