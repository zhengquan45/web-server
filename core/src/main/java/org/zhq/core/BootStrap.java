package org.zhq.core;

import lombok.extern.slf4j.Slf4j;
import org.zhq.core.network.endpoint.Endpoint;

import java.util.Scanner;

@Slf4j
public class BootStrap {
    private static final int PORT = 8080;
    public static void run(){
        Endpoint server = Endpoint.getInstance("bio");
        server.start(PORT);
        log.info("myServer started on port(s): {} (http)",PORT);
        Scanner scanner = new Scanner(System.in);
        String order;
        while (scanner.hasNext()) {
            order = scanner.next();
            if (order.equals("EXIT")) {
                server.close();
            }
        }
    }

    public static void main(String[] args) {
        run();
    }
}
