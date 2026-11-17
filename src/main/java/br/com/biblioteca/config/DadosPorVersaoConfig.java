package br.com.biblioteca.config;

import br.com.biblioteca.filter.VersaoApi;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Separa os dados da v1 e da v2: cada registro recebe a "versao" da requisição que o criou
 * e toda consulta é filtrada automaticamente por ela (discriminator multi-tenancy do Hibernate).
 */
@Configuration
public class DadosPorVersaoConfig {

    @Bean
    public HibernatePropertiesCustomizer resolvedorDeVersao(
            @Value("${app.versionamento.dados-separados:true}") boolean separados) {
        CurrentTenantIdentifierResolver<String> resolvedor = new CurrentTenantIdentifierResolver<>() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                if (!separados) return "v2";
                if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                    return "1".equals(VersaoApi.da(attrs.getRequest())) ? "v1" : "v2";
                }
                return "v2"; // fora de requisição (ex.: carga inicial)
            }

            @Override
            public boolean validateExistingCurrentSessions() {
                return false;
            }
        };
        return props -> props.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolvedor);
    }
}
