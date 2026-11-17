package br.com.biblioteca.dto;

import br.com.biblioteca.model.StatusLivro;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Set;

@Schema(description = "Dados para criação/atualização de um livro")
public record LivroRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        @Schema(example = "Memórias Póstumas de Brás Cubas") String titulo,

        @NotBlank(message = "O ISBN é obrigatório")
        @Pattern(regexp = "\\d{10}|\\d{13}", message = "O ISBN deve ter 10 ou 13 dígitos")
        @Schema(example = "9788535911664") String isbn,

        @NotNull(message = "O número de páginas é obrigatório")
        @Min(value = 1, message = "O livro deve ter ao menos 1 página")
        @Max(value = 10000, message = "O livro deve ter no máximo 10000 páginas")
        @Schema(example = "368") Integer paginas,

        @NotNull(message = "O ano de publicação é obrigatório")
        @Min(value = 1400, message = "Ano mínimo: 1400")
        @Max(value = 2100, message = "Ano máximo: 2100")
        @Schema(example = "1881") Integer anoPublicacao,

        @NotNull(message = "O status é obrigatório")
        @Schema(example = "DISPONIVEL") StatusLivro status,

        @NotNull(message = "O id do autor é obrigatório")
        @Schema(example = "1") Long autorId,

        @NotEmpty(message = "Informe ao menos uma categoria")
        @Size(max = 20, message = "Informe no máximo 20 categorias")
        @Schema(example = "[1, 2]") Set<Long> categoriaIds
) {}
