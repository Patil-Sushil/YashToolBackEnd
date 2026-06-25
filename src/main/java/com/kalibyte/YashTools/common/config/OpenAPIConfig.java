package com.kalibyte.YashTools.common.config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration
 *
 * @author YashTools Dev Team
 * @version 2.0
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "YashTools Enquiry Management API",
                version = "2.0",
                description = "RESTful API for managing customer enquiries, quotations, and orders",
                contact = @Contact(
                        name = "YashTools Support",
                        email = "support@yashtools.com",
                        url = "https://yashtools.com"
                ),
                license = @License(
                        name = "Proprietary",
                        url = "https://yashtools.com/license"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Staging Environment",
                        url = "https://staging.yashtools.com"
                ),
                @Server(
                        description = "Production Environment",
                        url = "https://api.yashtools.com"
                )
        }
)
@SecurityScheme(
        name = "bearer-jwt",
        description = "JWT authentication token",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenAPIConfig {
    // Configuration is done via annotations
}