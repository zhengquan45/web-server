package org.zhq.core.exception.base;

import lombok.Getter;
import org.zhq.core.enumeration.HTTPStatus;

@Getter
public class ServletException extends Exception{
    private HTTPStatus status;

    public ServletException(HTTPStatus status) {
        this.status = status;
    }
}
