package br.com.biblioteca.filter;

import br.com.biblioteca.model.RegistroIdempotencia;
import br.com.biblioteca.repository.RegistroIdempotenciaRepository;
import br.com.biblioteca.util.Hash;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Idempotência em POST via cabeçalho X-Idempotency-Key (opcional).
 *  - 1ª requisição com a chave: processa normalmente e guarda a resposta (apenas 2xx);
 *  - repetição (mesma chave e mesmo payload): devolve a resposta guardada, sem processar de novo,
 *    com o cabeçalho Idempotency-Replayed: true;
 *  - mesma chave com payload diferente: 422;
 *  - mesma chave enquanto a 1ª ainda processa: 409.
 * Respostas de erro (não 2xx) não são guardadas, permitindo corrigir o payload e tentar de novo.
 * O endpoint /api-keys fica de fora para não guardar chaves em texto puro no banco.
 */
@Component
@Order(30)
public class FiltroIdempotencia extends FiltroBase {

    public static final String HEADER = "X-Idempotency-Key";
    private static final int LIMITE_CORPO = 20000;
    private static final long LIMITE_REQUISICAO = 100_000;
    private static final Logger log = LoggerFactory.getLogger(FiltroIdempotencia.class);

    private final RegistroIdempotenciaRepository repository;
    private final ObjectMapper mapper;
    private final long ttlHoras;

    public FiltroIdempotencia(@Lazy RegistroIdempotenciaRepository repository, ObjectMapper mapper,
                              @Value("${app.idempotency.ttl-hours:24}") long ttlHoras) {
        this.repository = repository;
        this.mapper = mapper;
        this.ttlHoras = ttlHoras;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return super.shouldNotFilter(req)
                || VersaoApi.ehV1(req)
                || !"POST".equalsIgnoreCase(req.getMethod())
                || caminho(req).startsWith("/api-keys");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String chave = req.getHeader(HEADER);
        if (chave == null) {
            chain.doFilter(req, res);
            return;
        }
        chave = chave.trim();
        String caminho = caminho(req);
        if (chave.isEmpty() || chave.length() > 255) {
            RespostaErro.escrever(res, mapper, HttpStatus.BAD_REQUEST, "Chave de idempotência inválida",
                    "O cabeçalho " + HEADER + " deve ter entre 1 e 255 caracteres.", caminho);
            return;
        }

        if (req.getContentLengthLong() > LIMITE_REQUISICAO) {
            RespostaErro.escrever(res, mapper, HttpStatus.PAYLOAD_TOO_LARGE, "Corpo da requisição muito grande",
                    "Requisições com " + HEADER + " aceitam corpo de até 100 KB.", caminho);
            return;
        }
        CorpoEmCacheRequest requisicao = new CorpoEmCacheRequest(req);
        String query = req.getQueryString() == null ? "" : req.getQueryString();
        String hashRequisicao = Hash.sha256Hex((caminho + "?" + query).getBytes(StandardCharsets.UTF_8))
                + Hash.sha256Hex(requisicao.corpo());

        Object chaveApiId = req.getAttribute(FiltroChaveApi.ATRIBUTO_CHAVE_ID);
        String cliente = chaveApiId != null ? "chave:" + chaveApiId : "ip:" + req.getRemoteAddr();
        String escopo = cliente + "|" + chave;

        Optional<RegistroIdempotencia> existente = repository.findByEscopo(escopo);
        if (existente.isPresent() && expirado(existente.get())) {
            repository.delete(existente.get());
            existente = Optional.empty();
        }

        if (existente.isPresent()) {
            RegistroIdempotencia r = existente.get();
            if (!r.getHashRequisicao().equals(hashRequisicao)) {
                RespostaErro.escrever(res, mapper, HttpStatus.UNPROCESSABLE_ENTITY, "Chave de idempotência reutilizada",
                        "A chave " + HEADER + " já foi usada com uma requisição diferente. Gere uma nova chave para uma nova operação.",
                        caminho);
                return;
            }
            if (!r.isConcluido()) {
                emProcessamento(req, res, caminho);
                return;
            }
            reproduzir(r, res);
            return;
        }

        RegistroIdempotencia novo = new RegistroIdempotencia();
        novo.setEscopo(escopo);
        novo.setHashRequisicao(hashRequisicao);
        try {
            novo = repository.saveAndFlush(novo);
        } catch (DataIntegrityViolationException concorrente) {
            emProcessamento(req, res, caminho);
            return;
        }

        ContentCachingResponseWrapper resposta = new ContentCachingResponseWrapper(res);
        boolean guardado = false;
        try {
            chain.doFilter(requisicao, resposta);
            guardado = guardar(novo, resposta);
        } finally {
            if (!guardado) {
                try {
                    repository.deleteById(novo.getId());
                } catch (RuntimeException e) {
                    log.warn("Não foi possível remover o registro de idempotência {}", novo.getId(), e);
                }
            }
            resposta.copyBodyToResponse();
        }
    }

    private boolean guardar(RegistroIdempotencia registro, ContentCachingResponseWrapper resposta) {
        int status = resposta.getStatus();
        byte[] corpo = resposta.getContentAsByteArray();
        if (status < 200 || status >= 300 || corpo.length > LIMITE_CORPO) {
            return false;
        }
        try {
            registro.setStatus(status);
            registro.setCorpo(new String(corpo, StandardCharsets.UTF_8));
            registro.setContentType(resposta.getContentType());
            registro.setLocation(resposta.getHeader("Location"));
            registro.setConcluido(true);
            repository.save(registro);
            return true;
        } catch (RuntimeException e) {
            log.warn("Falha ao guardar resposta idempotente", e);
            return false;
        }
    }

    private void reproduzir(RegistroIdempotencia r, HttpServletResponse res) throws IOException {
        res.setStatus(r.getStatus());
        if (r.getContentType() != null) {
            res.setContentType(r.getContentType());
        }
        if (r.getLocation() != null) {
            res.setHeader("Location", r.getLocation());
        }
        res.setHeader("Idempotency-Replayed", "true");
        byte[] corpo = r.getCorpo() == null ? new byte[0] : r.getCorpo().getBytes(StandardCharsets.UTF_8);
        res.setContentLength(corpo.length);
        res.getOutputStream().write(corpo);
    }

    private void emProcessamento(HttpServletRequest req, HttpServletResponse res, String caminho) throws IOException {
        res.setHeader("Retry-After", "1");
        RespostaErro.escrever(res, mapper, HttpStatus.CONFLICT, "Requisição em processamento",
                "Uma requisição com esta chave ainda está sendo processada. Aguarde e tente novamente.", caminho);
    }

    private boolean expirado(RegistroIdempotencia r) {
        return r.getCriadoEm().plusHours(ttlHoras).isBefore(LocalDateTime.now());
    }
}
