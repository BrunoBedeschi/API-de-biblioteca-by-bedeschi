package br.com.biblioteca.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Rate limiting por IP (janela fixa em memória). Excedeu o limite: 429 + Retry-After. */
@Component
@Order(10)
public class FiltroLimiteTaxa extends FiltroBase {

    private record Janela(long inicio, int usados) {}

    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();
    private final int limite;
    private final long janelaMs;
    private final long janelaSegundos;
    private final ObjectMapper mapper;

    public FiltroLimiteTaxa(@Value("${app.rate-limit.max-requests:30}") int limite,
                            @Value("${app.rate-limit.window-seconds:60}") long janelaSegundos,
                            ObjectMapper mapper) {
        this.limite = limite;
        this.janelaSegundos = janelaSegundos;
        this.janelaMs = janelaSegundos * 1000;
        this.mapper = mapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return super.shouldNotFilter(req) || VersaoApi.ehV1(req);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        final long agora = System.currentTimeMillis();
        Janela j = janelas.compute(req.getRemoteAddr(), (cliente, atual) ->
                (atual == null || agora - atual.inicio() >= janelaMs)
                        ? new Janela(agora, 1)
                        : new Janela(atual.inicio(), atual.usados() + 1));
        limparExpiradas(agora);

        long segundosAteReset = Math.max(1, (long) Math.ceil((j.inicio() + janelaMs - agora) / 1000.0));
        res.setHeader("X-RateLimit-Limit", String.valueOf(limite));
        res.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limite - j.usados())));
        res.setHeader("X-RateLimit-Reset", String.valueOf(segundosAteReset));

        if (j.usados() > limite) {
            res.setHeader("Retry-After", String.valueOf(segundosAteReset));
            RespostaErro.escrever(res, mapper, HttpStatus.TOO_MANY_REQUESTS, "Limite de requisições excedido",
                    "Você excedeu o limite de " + limite + " requisições por " + janelaSegundos
                            + " segundos. Tente novamente em " + segundosAteReset + " segundos.",
                    caminho(req));
            return;
        }
        chain.doFilter(req, res);
    }

    private void limparExpiradas(long agora) {
        if (janelas.size() > 10_000) {
            janelas.entrySet().removeIf(e -> agora - e.getValue().inicio() >= janelaMs);
        }
    }
}
