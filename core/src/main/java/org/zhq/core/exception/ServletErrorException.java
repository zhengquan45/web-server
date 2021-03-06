package org.zhq.core.exception;

import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.base.ServletException;

public class ServletErrorException extends ServletException {
    public static final HTTPStatus status = HTTPStatus.INTERNAL_SERVER_ERROR;
    public ServletErrorException() {
        super(status);
    }
}
