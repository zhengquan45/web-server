package org.zhq.core.exception;

import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.base.ServletException;

public class VMResolveException extends ServletException {
    public VMResolveException(HTTPStatus status) {
        super(status);
    }
}
