package org.zhq.core.template;

import org.junit.Test;
import org.zhq.core.constant.CharsetProperties;
import org.zhq.core.util.IOUtil;

public class TemplateResolverTest {
    @Test
    public void resolve() throws Exception {
        byte[] rawBody = IOUtil.getBytesFromFile("/views/success.html");
        String body = new String(rawBody, CharsetProperties.charset);
        TemplateResolver.resolve(body,null);
    }
}
