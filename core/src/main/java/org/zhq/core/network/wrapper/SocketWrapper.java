package org.zhq.core.network.wrapper;

import java.io.IOException;

/**
 * @author sinjinsong
 * @date 2018/5/4
 */
public interface SocketWrapper {
    void close() throws IOException;
    boolean isClosed();
}
