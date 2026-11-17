package br.com.biblioteca.exception;

import java.util.List;

public class VersaoNaoSuportadaException extends RuntimeException {
    public VersaoNaoSuportadaException(String versao, List<String> suportadas) {
        super("Versão '" + versao + "' não suportada. Versões disponíveis no cabeçalho X-API-Version: "
                + String.join(", ", suportadas) + ".");
    }
}
