package br.com.biblioteca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Guarda a resposta de um POST já processado, identificado por X-Idempotency-Key. */
@Entity
@Getter @Setter @NoArgsConstructor
public class RegistroIdempotencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** cliente (chave de API ou IP) + "|" + valor do cabeçalho X-Idempotency-Key. */
    @Column(nullable = false, unique = true, length = 400)
    private String escopo;

    /** Hash do caminho, query string e corpo, para detectar reutilização da chave com outro payload. */
    @Column(nullable = false, length = 128)
    private String hashRequisicao;

    private Integer status;

    @Column(length = 20000)
    private String corpo;

    private String contentType;

    @Column(length = 500)
    private String location;

    private boolean concluido;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}
