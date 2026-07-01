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
@Configuration
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
        sender.setUsername(cfg.getUsername());
        sender.setPassword(cfg.getPassword());
        sender.setProtocol(cfg.getProtocol());
        sender.setDefaultEncoding(EmailConstants.DEFAULT_CHARSET);

        Properties p = new Properties();
        p.put("mail.smtp.auth", String.valueOf(cfg.isAuth()));
        p.put("mail.smtp.starttls.enable", String.valueOf(cfg.isStarttls()));
        p.put("mail.smtp.ssl.enable", String.valueOf(cfg.isSsl()));
        p.put("mail.smtp.connectiontimeout", cfg.getConnectionTimeoutMs());
        p.put("mail.smtp.timeout", cfg.getTimeoutMs());
        p.put("mail.smtp.writetimeout", cfg.getWriteTimeoutMs());
        sender.setJavaMailProperties(p);

        log.info("SMTP mail sender configured: host={} port={}", cfg.getHost(), cfg.getPort());
        return sender;
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