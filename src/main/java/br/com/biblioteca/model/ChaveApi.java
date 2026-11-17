package br.com.biblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
public class ChaveApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    /** Somente o hash SHA-256 é armazenado; a chave em texto puro é exibida uma única vez. */
    @Column(nullable = false, unique = true, length = 64)
    private String chaveHash;

    /** Primeiros caracteres da chave, apenas para identificação visual. */
    @Column(nullable = false, length = 12)
    private String prefixo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAcesso nivel;

    private boolean ativa = true;

    @Column(nullable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();

    /** Usuário dono da chave (opcional). */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
