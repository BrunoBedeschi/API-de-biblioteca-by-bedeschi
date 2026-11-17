package br.com.biblioteca.dto;

import br.com.biblioteca.model.StatusCarteirinha;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Dados para criação/atualização de uma carteirinha")
public record CarteirinhaRequest(
        @NotBlank(message = "O número é obrigatório")
        @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
        @Schema(example = "CART-0002") String numero,

        @NotNull(message = "A data de emissão é obrigatória")
        @PastOrPresent(message = "A data de emissão não pode ser futura")
        @Schema(example = "2026-02-01") LocalDate dataEmissao,

        @NotNull(message = "A data de validade é obrigatória")
        @Future(message = "A data de validade deve estar no futuro")
        @Schema(example = "2028-02-01") LocalDate dataValidade,

        @NotNull(message = "O status é obrigatório")
        @Schema(example = "ATIVA") StatusCarteirinha status,

        @NotNull(message = "O id do usuário é obrigatório")
        @Schema(example = "1") Long usuarioId
) {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "A data de validade deve ser posterior à data de emissão")
    public boolean isValidadePosteriorEmissao() {
        return dataEmissao == null || dataValidade == null || dataValidade.isAfter(dataEmissao);
    }
}
