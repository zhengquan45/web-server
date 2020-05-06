package org.zhq.core;

import org.zhq.core.network.endpoint.Endpoint;

import java.util.Scanner;

public class BootStrap {
    private static final int PORT = 8080;
    public static void run(){
        Endpoint server = Endpoint.getInstance("bio");
        server.start(PORT);
        Scanner scanner = new Scanner(System.in);
        String order;
        while (scanner.hasNext()) {
            order = scanner.next();
            if (order.equals("EXIT")) {
                server.close();
            }
        }
    }
}
