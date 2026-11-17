package br.com.biblioteca.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Valida X-API-Version, informa a versão aplicada e isola os recursos que só existem na v2. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class FiltroVersao extends FiltroBase {

    private final boolean v1Habilitada;
    private final ObjectMapper mapper;

    public FiltroVersao(@Value("${app.versionamento.v1-habilitada:true}") boolean v1Habilitada, ObjectMapper mapper) {
        this.v1Habilitada = v1Habilitada;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String versao = VersaoApi.da(req);
        String caminho = caminho(req);

        if (!VersaoApi.valida(versao)) {
            RespostaErro.escrever(res, mapper, HttpStatus.BAD_REQUEST, "Versão da API não suportada",
                    "Versão '" + versao + "' não suportada. Versões disponíveis no cabeçalho X-API-Version: 1, 2.", caminho);
            return;
        }
        if ("1".equals(versao)) {
            if (!v1Habilitada) {
                RespostaErro.escrever(res, mapper, HttpStatus.BAD_REQUEST, "Versão da API desativada",
                        "A versão 1 está desativada neste ambiente. Envie X-API-Version: 2.", caminho);
                return;
            }
            if (caminho.startsWith("/api-keys")) {
                RespostaErro.escrever(res, mapper, HttpStatus.NOT_FOUND, "Recurso indisponível nesta versão",
                        "O gerenciamento de chaves de API existe apenas na v2. Envie X-API-Version: 2.", caminho);
                return;
            }
        }
        res.setHeader(VersaoApi.HEADER, versao);
        chain.doFilter(req, res);
    }
}
