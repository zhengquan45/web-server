package org.zhq.core.exception;

import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.base.ServletException;

public class ResourceNotFoundException extends ServletException {
    public ResourceNotFoundException(HTTPStatus status) {
        super(status);
    }
}
