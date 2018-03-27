package com.fuckmyclassic;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Main driver class for the program. Loads up the shim MainApplication
 * class which handles all the instantiation using Spring.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
@Component
public class Main {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(ApplicationConfiguration.class);
        ctx.refresh();
        MainApplication app = ctx.getBean(MainApplication.class);
        app.start(args);
    }
}
