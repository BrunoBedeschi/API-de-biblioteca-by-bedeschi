package br.com.biblioteca.config;

import br.com.biblioteca.filter.FiltroCors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS como filtro de maior prioridade: assim o preflight (OPTIONS) é respondido sem exigir
 * chave de API e até as respostas 401/403/429 saem com os cabeçalhos CORS.
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<FiltroCors> corsFilter(
            @Value("${app.cors.allowed-origins}") List<String> origens,
            @Value("${app.cors.allowed-methods}") List<String> metodos) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(origens);
        cfg.setAllowedMethods(metodos);
        cfg.setAllowedHeaders(List.of("Content-Type", "Accept", "X-API-Key", "X-Idempotency-Key", "X-API-Version"));
        cfg.setExposedHeaders(List.of("Location", "Retry-After", "X-RateLimit-Limit", "X-RateLimit-Remaining",
                "X-RateLimit-Reset", "X-API-Version", "Idempotency-Replayed"));
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", cfg);

        FilterRegistrationBean<FiltroCors> registro = new FilterRegistrationBean<>(new FiltroCors(new CorsFilter(fonte)));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }
}
