package br.com.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
@Schema(description = "Autor de livros")
public class Autor {

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
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    @Schema(example = "Machado de Assis")
    private String nome;

    @Size(max = 60, message = "A nacionalidade deve ter no máximo 60 caracteres")
    @Schema(example = "Brasileira")
    private String nacionalidade;

    @Past(message = "A data de nascimento deve estar no passado")
    @Schema(example = "1839-06-21")
    private LocalDate dataNascimento;

    // One-to-Many: um autor possui vários livros
    @JsonIgnore
    @OneToMany(mappedBy = "autor")
    private List<Livro> livros = new ArrayList<>();
}
