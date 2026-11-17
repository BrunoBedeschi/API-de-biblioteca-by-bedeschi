package br.com.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"versao", "isbn"})})
@Getter @Setter @NoArgsConstructor
@Schema(description = "Livro do acervo")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    /** Versão da API dona do registro (v1/v2). Preenchido pelo Hibernate; nunca exposto. */
    @TenantId
    @JsonIgnore
    @Schema(hidden = true)
    private String versao;

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
    private String titulo;

    @NotBlank(message = "O ISBN é obrigatório")
    @Pattern(regexp = "\\d{10}|\\d{13}", message = "O ISBN deve ter 10 ou 13 dígitos")
    private String isbn;

    @NotNull(message = "O número de páginas é obrigatório")
    @Min(value = 1, message = "O livro deve ter ao menos 1 página")
    @Max(value = 10000, message = "O livro deve ter no máximo 10000 páginas")
    private Integer paginas;

    @NotNull(message = "O ano de publicação é obrigatório")
    @Min(value = 1400, message = "Ano mínimo: 1400")
    @Max(value = 2100, message = "Ano máximo: 2100")
    private Integer anoPublicacao;

    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    private StatusLivro status;

    // Many-to-One: vários livros para um autor
    @NotNull(message = "O autor é obrigatório")
    @ManyToOne(optional = false)
    @JoinColumn(name = "autor_id")
    private Autor autor;

    // Many-to-Many (lado dono)
    @ManyToMany
    @JoinTable(name = "livro_categoria",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();
}
