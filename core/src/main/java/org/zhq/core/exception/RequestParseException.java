package org.zhq.core.exception;

import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.base.ServletException;

/**
 * Created by SinjinSong on 2017/7/20.
 */

public class RequestParseException extends ServletException {
    public RequestParseException(HTTPStatus status) {
        super(status);
    }
}
