package br.com.biblioteca.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.filter.OncePerRequestFilter;

/** Base dos filtros da API: ignora Swagger, console H2 e requisições OPTIONS (preflight CORS). */
abstract class FiltroBase extends OncePerRequestFilter {

    /** Caminho já decodificado/normalizado pelo container (evita burlar a proteção com %XX). */
    protected static String caminho(HttpServletRequest req) {
        String servlet = req.getServletPath();
        String info = req.getPathInfo();
        return info == null ? servlet : servlet + info;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String c = caminho(req);
        return c.startsWith("/swagger-ui")
                || c.startsWith("/v3/api-docs")
                || c.startsWith("/h2-console")
                || c.equals("/favicon.ico")
                || "OPTIONS".equalsIgnoreCase(req.getMethod());
    }
}
