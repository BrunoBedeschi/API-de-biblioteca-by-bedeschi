package br.com.biblioteca.repository;

import br.com.biblioteca.model.ChaveApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChaveApiRepository extends JpaRepository<ChaveApi, Long> {
    Optional<ChaveApi> findByChaveHash(String chaveHash);
    boolean existsByChaveHash(String chaveHash);
}
