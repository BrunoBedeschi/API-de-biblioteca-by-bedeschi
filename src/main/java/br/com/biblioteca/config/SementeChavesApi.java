package br.com.biblioteca.config;

import br.com.biblioteca.model.NivelAcesso;
import br.com.biblioteca.service.ChaveApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Cria chaves de teste (definidas em application.properties) para testar a API. */
@Component
public class SementeChavesApi implements ApplicationRunner {

    private final ChaveApiService service;
    private final String admin;
    private final String escrita;
    private final String leitura;

    public SementeChavesApi(ChaveApiService service,
                            @Value("${app.security.seed-keys.admin:}") String admin,
                            @Value("${app.security.seed-keys.escrita:}") String escrita,
                            @Value("${app.security.seed-keys.leitura:}") String leitura) {
        this.service = service;
        this.admin = admin;
        this.escrita = escrita;
        this.leitura = leitura;
    }

    @Override
    public void run(ApplicationArguments args) {
        semear("Chave ADMIN (teste)", NivelAcesso.ADMIN, admin);
        semear("Chave ESCRITA (teste)", NivelAcesso.ESCRITA, escrita);
        semear("Chave LEITURA (teste)", NivelAcesso.LEITURA, leitura);
    }

    private void semear(String nome, NivelAcesso nivel, String chave) {
        if (chave != null && !chave.isBlank()) {
            service.semear(nome, nivel, chave);
        }
    }
}
