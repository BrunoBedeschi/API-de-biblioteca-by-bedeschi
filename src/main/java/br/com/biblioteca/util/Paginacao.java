package br.com.biblioteca.util;

import br.com.biblioteca.exception.PaginaInexistenteException;
import org.springframework.data.domain.Page;

public final class Paginacao {

    private Paginacao() {}

    /** Página 0 sempre é válida (mesmo vazia); páginas além da última geram 404. */
    public static <T> Page<T> validar(Page<T> pagina) {
        if (pagina.getNumber() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {
            throw new PaginaInexistenteException(pagina.getNumber(), pagina.getTotalPages());
        }
        return pagina;
    }
}
