package com.kompu.api.infrastructure.config.web.startup;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener {

    public ApplicationStartupListener() {
        // noop
    }

    @EventListener(classes = ApplicationReadyEvent.class)
    public void handleApplicationStartup(ApplicationReadyEvent event) {
        // Application startup logic can be added here
    }

}
