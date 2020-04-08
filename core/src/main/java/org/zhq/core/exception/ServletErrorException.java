package org.zhq.core.exception;

import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.base.ServletException;

public class ServletErrorException extends ServletException {
    public ServletErrorException(HTTPStatus status) {
        super(status);
    }
}
