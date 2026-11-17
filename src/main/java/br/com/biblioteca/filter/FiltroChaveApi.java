package br.com.biblioteca.filter;

import br.com.biblioteca.model.ChaveApi;
import br.com.biblioteca.model.NivelAcesso;
import br.com.biblioteca.service.ChaveApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/** Autenticação por chave de API (cabeçalho X-API-Key) conforme {@link PoliticaAcesso}. */
@Component
@Order(20)
public class FiltroChaveApi extends FiltroBase {

    public static final String HEADER = "X-API-Key";
    public static final String ATRIBUTO_CHAVE_ID = "chaveApiId";

    private final ChaveApiService service;
    private final ObjectMapper mapper;

    public FiltroChaveApi(@Lazy ChaveApiService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return super.shouldNotFilter(req) || VersaoApi.ehV1(req);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String caminho = caminho(req);
        NivelAcesso exigido = PoliticaAcesso.exigido(req.getMethod(), caminho);
        if (exigido == null) {
            chain.doFilter(req, res);
            return;
        }

        String valor = req.getHeader(HEADER);
        if (valor == null || valor.isBlank()) {
            res.setHeader("WWW-Authenticate", "ApiKey header=\"" + HEADER + "\"");
            RespostaErro.escrever(res, mapper, HttpStatus.UNAUTHORIZED, "Chave de API ausente",
                    "Informe uma chave de API válida no cabeçalho " + HEADER + ".", caminho);
            return;
        }

        Optional<ChaveApi> chave = service.autenticar(valor.trim());
        if (chave.isEmpty()) {
            res.setHeader("WWW-Authenticate", "ApiKey header=\"" + HEADER + "\"");
            RespostaErro.escrever(res, mapper, HttpStatus.UNAUTHORIZED, "Chave de API inválida",
                    "A chave informada não existe ou foi revogada.", caminho);
            return;
        }

        if (!chave.get().getNivel().permite(exigido)) {
            RespostaErro.escrever(res, mapper, HttpStatus.FORBIDDEN, "Acesso negado",
                    "Esta operação exige nível " + exigido + "; sua chave tem nível " + chave.get().getNivel() + ".",
                    caminho);
            return;
        }

        req.setAttribute(ATRIBUTO_CHAVE_ID, chave.get().getId());
        chain.doFilter(req, res);
    }
}
