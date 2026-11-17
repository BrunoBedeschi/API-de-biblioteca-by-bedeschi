package br.com.biblioteca.repository;

import br.com.biblioteca.model.RegistroIdempotencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistroIdempotenciaRepository extends JpaRepository<RegistroIdempotencia, Long> {
    Optional<RegistroIdempotencia> findByEscopo(String escopo);
}
