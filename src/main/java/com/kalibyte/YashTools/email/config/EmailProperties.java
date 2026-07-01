package com.kalibyte.YashTools.email.config;

import com.kalibyte.YashTools.email.constants.EmailConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    private boolean enabled = false;
    private String fromAddress = "noreply@yashtools.com";
    private String fromDisplayName = "Yash Tools";
    private int maxRetries = EmailConstants.MAX_RETRIES;
    private int threadPoolSize = 5;
    private String storagePath = "/tmp/yash-email";

    private Smtp smtp = new Smtp();

    @Data
    public static class Smtp {
        private String host = "smtp.gmail.com";
        private int port = 587;
        private String username;
        private String password;
        private String protocol = "smtp";
        private boolean auth = true;
        private boolean starttls = true;
        private boolean ssl = false;
        private int connectionTimeoutMs = EmailConstants.SMTP_CONNECTION_TIMEOUT_MS;
        private int timeoutMs = EmailConstants.SMTP_TIMEOUT_MS;
        private int writeTimeoutMs = EmailConstants.SMTP_WRITE_TIMEOUT_MS;
    }
}