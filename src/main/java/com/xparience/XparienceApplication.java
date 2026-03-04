package com.xparience;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@Slf4j
public class XparienceApplication {

    private final Environment environment;

    public XparienceApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(XparienceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logResolvedFileAppender() {
        String logFileName = environment.getProperty("logging.file.name", "console-only");
        String[] profiles = environment.getActiveProfiles();
        String activeProfiles = profiles.length == 0 ? "default" : String.join(",", profiles);
        log.info("Logging initialized. activeProfiles={}, file={}", activeProfiles, logFileName);
    }
}