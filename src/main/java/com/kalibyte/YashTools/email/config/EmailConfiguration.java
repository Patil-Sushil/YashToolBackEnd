package com.kalibyte.YashTools.email.config;

import com.kalibyte.YashTools.email.constants.EmailConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableAsync
@RequiredArgsConstructor
public class EmailConfiguration {

    private final EmailProperties emailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        EmailProperties.Smtp cfg = emailProperties.getSmtp();
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(cfg.getHost());
        sender.setPort(cfg.getPort());
        String username = resolveUsername(cfg.getUsername());
        String password = resolvePassword(cfg.getPassword());

        sender.setUsername(username);
        sender.setPassword(password);
        sender.setProtocol(cfg.getProtocol());
        sender.setDefaultEncoding(EmailConstants.DEFAULT_CHARSET);

        Properties p = new Properties();
        p.put("mail.smtp.auth", String.valueOf(cfg.isAuth()));
        p.put("mail.smtp.starttls.enable", String.valueOf(cfg.isStarttls()));
        p.put("mail.smtp.starttls.required", "true");
        p.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        p.put("mail.smtp.ssl.enable", String.valueOf(cfg.isSsl()));
        p.put("mail.smtp.connectiontimeout", cfg.getConnectionTimeoutMs());
        p.put("mail.smtp.timeout", cfg.getTimeoutMs());
        p.put("mail.smtp.writetimeout", cfg.getWriteTimeoutMs());
        sender.setJavaMailProperties(p);

        log.info("SMTP mail sender configured: host={} port={} username={} passwordConfigured={}",
                cfg.getHost(), cfg.getPort(), username, (password != null && !password.isBlank()));
        return sender;
    }

    private String resolveUsername(String configUser) {
        if (configUser != null && !configUser.isBlank()) return configUser.trim();
        String u = System.getProperty("SPRING_MAIL_USERNAME");
        if (u != null && !u.isBlank()) return u.trim();
        u = System.getenv("SPRING_MAIL_USERNAME");
        if (u != null && !u.isBlank()) return u.trim();
        u = System.getProperty("SMTP_USERNAME");
        if (u != null && !u.isBlank()) return u.trim();
        u = System.getenv("SMTP_USERNAME");
        if (u != null && !u.isBlank()) return u.trim();

        java.io.File envFile = new java.io.File(".env");
        if (envFile.exists()) {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("SPRING_MAIL_USERNAME=") || line.startsWith("SMTP_USERNAME=")) {
                        int eq = line.indexOf('=');
                        String val = line.substring(eq + 1).trim();
                        if (!val.isBlank()) return val.trim();
                    }
                }
            } catch (Exception ignored) {}
        }
        return "kalibyte.official@gmail.com";
    }

    private String resolvePassword(String configPass) {
        if (configPass != null && !configPass.isBlank()) return cleanPassword(configPass);
        String p = System.getProperty("SPRING_MAIL_PASSWORD");
        if (p != null && !p.isBlank()) return cleanPassword(p);
        p = System.getenv("SPRING_MAIL_PASSWORD");
        if (p != null && !p.isBlank()) return cleanPassword(p);
        p = System.getProperty("SMTP_PASSWORD");
        if (p != null && !p.isBlank()) return cleanPassword(p);
        p = System.getenv("SMTP_PASSWORD");
        if (p != null && !p.isBlank()) return cleanPassword(p);

        java.io.File envFile = new java.io.File(".env");
        if (envFile.exists()) {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("SPRING_MAIL_PASSWORD=") || line.startsWith("SMTP_PASSWORD=")) {
                        int eq = line.indexOf('=');
                        String val = line.substring(eq + 1).trim();
                        if (!val.isBlank()) return cleanPassword(val);
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String cleanPassword(String raw) {
        if (raw == null) return null;
        String p = raw.trim();
        if ((p.startsWith("\"") && p.endsWith("\"")) || (p.startsWith("'") && p.endsWith("'"))) {
            p = p.substring(1, p.length() - 1).trim();
        }
        return p.replace(" ", "");
    }

    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(emailProperties.getThreadPoolSize());
        ex.setMaxPoolSize(emailProperties.getThreadPoolSize() * 2);
        ex.setQueueCapacity(500);
        ex.setThreadNamePrefix("email-");
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(30);
        ex.initialize();
        return ex;
    }
}