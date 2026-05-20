package com.goosebeoms.tickets.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT_SCHEME = "bearerAuth";
    private static final String QUEUE_TOKEN_SCHEME = "queueToken";

    @Bean
    public OpenAPI ticketsOpenAPI() {
        SecurityScheme jwt = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("로그인 API의 응답 토큰을 'Bearer ...' 형식으로 입력");

        SecurityScheme queueToken = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Queue-Token")
                .description("대기열 승격 시 발급된 토큰. 좌석 hold 시점에만 필요");

        return new OpenAPI()
                .info(new Info()
                        .title("Goosebeoms Tickets API")
                        .version("v1")
                        .description("공연 티켓팅 서버 — 대기열, 좌석 예매, 결제, 쿠폰"))
                .components(new Components()
                        .addSecuritySchemes(JWT_SCHEME, jwt)
                        .addSecuritySchemes(QUEUE_TOKEN_SCHEME, queueToken))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME));
    }
}
