package br.com.biblioteca.dto;

import br.com.biblioteca.model.Categoria;
import br.com.biblioteca.model.Livro;
import br.com.biblioteca.model.StatusLivro;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Comparator;
import java.util.List;

@Schema(description = "Representação v2 do livro (X-API-Version: 2): resumida, com campo 'disponivel'")
public record LivroV2Response(
        @Schema(example = "1") Long id,
        @Schema(example = "Dom Casmurro") String titulo,
        @Schema(example = "9788535914849") String isbn,
        @Schema(example = "256") Integer paginas,
        @Schema(example = "1899") Integer anoPublicacao,
        @Schema(example = "DISPONIVEL") StatusLivro status,
        @Schema(example = "true", description = "true quando o status é DISPONIVEL") boolean disponivel,
        AutorResumo autor,
        List<CategoriaResumo> categorias
) {
    public record AutorResumo(Long id, String nome) {}

    public record CategoriaResumo(Long id, String nome) {}

    public static LivroV2Response de(Livro l) {
        List<CategoriaResumo> categorias = l.getCategorias().stream()
                .sorted(Comparator.comparing(Categoria::getNome))
                .map(c -> new CategoriaResumo(c.getId(), c.getNome()))
                .toList();
        return new LivroV2Response(l.getId(), l.getTitulo(), l.getIsbn(), l.getPaginas(), l.getAnoPublicacao(),
                l.getStatus(), l.getStatus() == StatusLivro.DISPONIVEL,
                new AutorResumo(l.getAutor().getId(), l.getAutor().getNome()), categorias);
    }
}
