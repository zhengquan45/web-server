package org.zhq.core.request.dispatcher.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zhq.core.constant.CharsetProperties;
import org.zhq.core.enumeration.HTTPStatus;
import org.zhq.core.exception.ResourceNotFoundException;
import org.zhq.core.exception.base.ServletException;
import org.zhq.core.request.Request;
import org.zhq.core.request.dispatcher.RequestDispatcher;
import org.zhq.core.resource.ResourceHandler;
import org.zhq.core.response.Response;
import org.zhq.core.template.TemplateResolver;
import org.zhq.core.util.IOUtil;
import org.zhq.core.util.MimeTypeUtil;

import java.io.IOException;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationRequestDispatcher implements RequestDispatcher {
    private String url;

    @Override
    public void forward(Request request, Response response) throws ServletException, IOException {

        if (ResourceHandler.class.getResource(url) == null) {
            throw new ResourceNotFoundException(HTTPStatus.NOT_FOUND);
        }
        String body = TemplateResolver.resolve(new String(IOUtil.getBytesFromFile(url), CharsetProperties.charset), request);
        response.header(HTTPStatus.OK, MimeTypeUtil.getTypes(url)).body(body.getBytes(CharsetProperties.charset));
    }
}
