package br.com.biblioteca.repository;

import br.com.biblioteca.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    Page<Livro> findByStatus(StatusLivro status, Pageable pageable);
    Page<Livro> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);
    Page<Livro> findByAutorId(Long autorId, Pageable pageable);
    Page<Livro> findByCategoriasId(Long categoriaId, Pageable pageable);
}
