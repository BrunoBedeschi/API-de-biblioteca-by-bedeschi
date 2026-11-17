package br.com.biblioteca.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/** Escreve erros no mesmo formato (Problem Details) usado pelo GlobalExceptionHandler. */
final class RespostaErro {

    private RespostaErro() {}

    static void escrever(HttpServletResponse res, ObjectMapper mapper, HttpStatus status,
                         String titulo, String detalhe, String instancia) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalhe);
        pd.setTitle(titulo);
        try {
            pd.setInstance(new URI(null, null, instancia, null));
        } catch (URISyntaxException ignorada) {
            // instance é opcional
        }
        res.setStatus(status.value());
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/problem+json");
        mapper.writeValue(res.getWriter(), pd);
    }
}
