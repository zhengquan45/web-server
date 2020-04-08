package org.zhq.core.exception;

import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.base.ServletException;

public class RequestInvalidException extends ServletException {
    public RequestInvalidException(HTTPStatus status) {
        super(status);
    }
}
