package br.com.biblioteca.model;

/** Níveis de acesso de uma chave de API (cada nível inclui os anteriores). */
public enum NivelAcesso {
    LEITURA(1), ESCRITA(2), ADMIN(3);

    private final int peso;

    NivelAcesso(int peso) {
        this.peso = peso;
    }

    public boolean permite(NivelAcesso exigido) {
        return this.peso >= exigido.peso;
    }
}
