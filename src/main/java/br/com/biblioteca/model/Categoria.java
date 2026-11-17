package br.com.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"versao", "nome"})})
@Getter @Setter @NoArgsConstructor
@Schema(description = "Categoria/gênero literário")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    /** Versão da API dona do registro (v1/v2). Preenchido pelo Hibernate; nunca exposto. */
    @TenantId
    @JsonIgnore
    @Schema(hidden = true)
    private String versao;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres")
    @Schema(example = "Romance")
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    @Schema(example = "Obras de ficção narrativa")
    private String descricao;

    // Many-to-Many (lado inverso)
    @JsonIgnore
    @ManyToMany(mappedBy = "categorias")
    private Set<Livro> livros = new HashSet<>();
}
