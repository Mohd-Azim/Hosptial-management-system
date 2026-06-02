package com.hospital.hms.config;

import java.net.ServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * If {@code server.port} is in use, binds to the next free port (increment until free or cap).
 * {@code server.port=0} is left unchanged (Spring assigns a random free port).
 */
@Component
public class AvailablePortWebServerCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private static final Logger log = LoggerFactory.getLogger(AvailablePortWebServerCustomizer.class);
    private static final int DEFAULT_START = 8080;

    private final Environment environment;

    public AvailablePortWebServerCustomizer(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        Integer configured = environment.getProperty("server.port", Integer.class);
        if (configured != null && configured == 0) {
            return;
        }
        int start = configured != null && configured > 0 ? configured : DEFAULT_START;
        int maxAttempts = environment.getProperty("hms.server.port-max-attempts", Integer.class, 64);

        for (int i = 0; i < maxAttempts; i++) {
            int port = start + i;
            if (port > 65535) {
                break;
            }
            if (isAvailable(port)) {
                factory.setPort(port);
                if (port != start) {
                    log.warn("Preferred port {} was in use; starting on {}", start, port);
                }
                return;
            }
        }
        log.warn("No free port in range starting at {} ({} attempts); using random port", start, maxAttempts);
        factory.setPort(0);
    }

    private static boolean isAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
