package br.com.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"versao", "email"}), @UniqueConstraint(columnNames = {"versao", "cpf"})})
@Getter @Setter @NoArgsConstructor
@Schema(description = "Usuário (leitor) da biblioteca")
public class Usuario {

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
    @Schema(example = "Maria Silva")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    @Schema(example = "maria@email.com")
    private String email;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos numéricos")
    @Schema(example = "12345678901")
    private String cpf;

    // One-to-One (lado inverso)
    @JsonIgnore
    @OneToOne(mappedBy = "usuario")
    private Carteirinha carteirinha;
}
