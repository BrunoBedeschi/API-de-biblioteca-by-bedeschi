package br.com.biblioteca.dto;

import br.com.biblioteca.model.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para gerar uma chave de API")
public record ChaveApiRequest(
        @NotBlank(message = "O nome da chave é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        @Schema(example = "Chave do balcão de atendimento") String nome,

        @NotNull(message = "O nível de acesso é obrigatório")
        @Schema(example = "ESCRITA", description = "LEITURA < ESCRITA < ADMIN") NivelAcesso nivel,

        @Schema(example = "1", description = "Id do usuário dono da chave (opcional)") Long usuarioId
) {}
