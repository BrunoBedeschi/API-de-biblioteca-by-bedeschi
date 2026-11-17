package br.com.biblioteca.filter;

import br.com.biblioteca.model.NivelAcesso;

/**
 * Regras de proteção por chave de API (usadas pelo filtro e pela documentação Swagger).
 * Retorna o nível mínimo exigido ou null quando o endpoint é público.
 *
 *  - /api-keys/**                 -> ADMIN
 *  - /usuarios/**, /carteirinhas/** (dados pessoais): GET = LEITURA
 *  - demais recursos: GET público
 *  - POST / PUT / PATCH           -> ESCRITA
 *  - DELETE                       -> ADMIN
 */
public final class PoliticaAcesso {

    private PoliticaAcesso() {}

    public static NivelAcesso exigido(String metodo, String caminho) {
        if (caminho.startsWith("/api-keys")) {
            return NivelAcesso.ADMIN;
        }
        boolean sensivel = caminho.startsWith("/usuarios") || caminho.startsWith("/carteirinhas");
        switch (metodo.toUpperCase()) {
            case "GET":
            case "HEAD":
                return sensivel ? NivelAcesso.LEITURA : null;
            case "POST":
            case "PUT":
            case "PATCH":
                return NivelAcesso.ESCRITA;
            case "DELETE":
                return NivelAcesso.ADMIN;
            default:
                return null;
        }
    }
}
