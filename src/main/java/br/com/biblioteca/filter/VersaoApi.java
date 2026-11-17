package br.com.biblioteca.filter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Versão da API pedida no cabeçalho X-API-Version.
 *  - v1: somente os requisitos da Parte 1 (sem chave de API, rate limit, idempotência e CORS);
 *  - v2 (padrão, quando o cabeçalho é omitido): v1 + recursos avançados da Parte 2.
 */
public final class VersaoApi {

    public static final String HEADER = "X-API-Version";
    public static final String PADRAO = "2";

    private VersaoApi() {}

    public static String da(HttpServletRequest req) {
        String v = req.getHeader(HEADER);
        return (v == null || v.isBlank()) ? PADRAO : v.trim();
    }

    public static boolean valida(String versao) {
        return "1".equals(versao) || "2".equals(versao);
    }

    public static boolean ehV1(HttpServletRequest req) {
        return "1".equals(da(req));
    }
}
