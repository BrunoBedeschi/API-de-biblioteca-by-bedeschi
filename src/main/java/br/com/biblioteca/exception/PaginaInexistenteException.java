package br.com.biblioteca.exception;

public class PaginaInexistenteException extends RuntimeException {
    public PaginaInexistenteException(int pagina, int totalPaginas) {
        super(totalPaginas == 0
                ? "A página " + pagina + " não existe: não há registros. Apenas a página 0 está disponível (vazia)."
                : "A página " + pagina + " não existe. A numeração começa em 0 e a última página é a "
                        + (totalPaginas - 1) + " (total de " + totalPaginas + " página(s)).");
    }
}
