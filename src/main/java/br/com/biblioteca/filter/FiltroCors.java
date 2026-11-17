package br.com.biblioteca.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Aplica a política de CORS somente na v2 (a v1 não configura CORS). */
public class FiltroCors extends OncePerRequestFilter {

    private final CorsFilter delegado;

    public FiltroCors(CorsFilter delegado) {
        this.delegado = delegado;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (VersaoApi.ehV1(req)) {
            chain.doFilter(req, res);
        } else {
            delegado.doFilter(req, res, chain);
        }
    }
}
