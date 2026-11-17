package br.com.biblioteca.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"versao", "numero"})})
@Getter @Setter @NoArgsConstructor
@Schema(description = "Carteirinha de acesso do usuário")
public class Carteirinha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    /** Versão da API dona do registro (v1/v2). Preenchido pelo Hibernate; nunca exposto. */
    @TenantId
    @JsonIgnore
    @Schema(hidden = true)
    private String versao;

    @NotBlank(message = "O número é obrigatório")
    @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
    private String numero;

    @NotNull(message = "A data de emissão é obrigatória")
    private LocalDate dataEmissao;

    @NotNull(message = "A data de validade é obrigatória")
    private LocalDate dataValidade;

    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    private StatusCarteirinha status;

    // One-to-One (lado dono)
    @NotNull(message = "O usuário é obrigatório")
    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;
}
