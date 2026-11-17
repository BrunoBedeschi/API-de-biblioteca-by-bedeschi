package br.com.biblioteca.dto;

import br.com.biblioteca.model.ChaveApi;
import br.com.biblioteca.model.NivelAcesso;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Chave de API. O campo 'chave' só aparece na criação e na rotação.")
public record ChaveApiResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Chave do balcão de atendimento") String nome,
        @Schema(example = "bib_a1B2c3D") String prefixo,
        @Schema(example = "ESCRITA") NivelAcesso nivel,
        @Schema(example = "true") boolean ativa,
        LocalDateTime criadaEm,
        @Schema(example = "1") Long usuarioId,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "Chave em texto puro. Exibida UMA única vez; guarde-a com segurança.",
                example = "bib_a1B2c3D4e5F6...") String chave
) {
    public static ChaveApiResponse de(ChaveApi c, String chavePlana) {
        return new ChaveApiResponse(c.getId(), c.getNome(), c.getPrefixo(), c.getNivel(), c.isAtiva(),
                c.getCriadaEm(), c.getUsuario() == null ? null : c.getUsuario().getId(), chavePlana);
    }
}
