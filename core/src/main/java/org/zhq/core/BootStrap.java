package org.zhq.core;

import java.util.Scanner;

public class BootStrap {
    public static void run(){
        Server server = new Server();
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
