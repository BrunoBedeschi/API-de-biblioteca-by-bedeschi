package br.com.biblioteca.controller;

import br.com.biblioteca.util.Paginacao;
import br.com.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.biblioteca.model.Autor;
import br.com.biblioteca.repository.AutorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/autores")
@Tag(name = "Autores", description = "Gerenciamento de autores de livros")
public class AutorController {

    private final AutorRepository repository;
    private final PagedResourcesAssembler<Autor> pagedAssembler;

    public AutorController(AutorRepository repository, PagedResourcesAssembler<Autor> pagedAssembler) {
        this.repository = repository;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Listar autores (paginado)",
            description = "Retorna uma página de autores. Parâmetros: page (0-based), size e sort (ex.: sort=nome,asc).")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping
    public PagedModel<EntityModel<Autor>> listar(@ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findAll(pageable)), this::toModel);
    }

    @Operation(summary = "Buscar autores por nome",
            description = "Consulta personalizada: filtra por trecho do nome (case-insensitive), com paginação. Ex.: /autores/buscar?nome=machado")
    @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)")
    @GetMapping("/buscar")
    public PagedModel<EntityModel<Autor>> buscarPorNome(@RequestParam String nome, @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findByNomeContainingIgnoreCase(nome, pageable)), this::toModel);
    }

    @Operation(summary = "Buscar autor por id", description = "Retorna um(a) autor pelo identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado(a)"),
            @ApiResponse(responseCode = "404", description = "Não encontrado(a)")})
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Autor>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(obter(id)));
    }

    @Operation(summary = "Criar autor", description = "Cadastra um(a) novo(a) autor. O id é gerado automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado(a) com sucesso (header Location com o novo recurso)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (Bean Validation)"),
            @ApiResponse(responseCode = "409", description = "Valor duplicado em campo único")})
    @PostMapping
    public ResponseEntity<EntityModel<Autor>> criar(@Valid @RequestBody Autor body) {
        body.setId(null);
        EntityModel<Autor> model = toModel(repository.save(body));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Operation(summary = "Atualizar autor", description = "Substitui os dados de um(a) autor existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado(a) com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Não encontrado(a)"),
            @ApiResponse(responseCode = "409", description = "Valor duplicado em campo único")})
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Autor>> atualizar(@PathVariable Long id, @Valid @RequestBody Autor body) {
        body.setVersao(obter(id).getVersao());
        body.setId(id);
        return ResponseEntity.ok(toModel(repository.save(body)));
    }

    @Operation(summary = "Excluir autor", description = "Remove um(a) autor. Falha com 409 se houver vínculos com outros recursos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Excluído(a) com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrado(a)"),
            @ApiResponse(responseCode = "409", description = "Possui vínculos e não pode ser excluído(a)")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.delete(obter(id));
        return ResponseEntity.noContent().build();
    }

    private Autor obter(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Autor", id));
    }

    private EntityModel<Autor> toModel(Autor e) {
        return EntityModel.of(e,
                linkTo(methodOn(AutorController.class).buscar(e.getId())).withSelfRel(),
                linkTo(methodOn(AutorController.class).atualizar(e.getId(), null)).withRel("update"),
                linkTo(methodOn(AutorController.class).excluir(e.getId())).withRel("delete"),
                linkTo(AutorController.class).withRel("autores"), linkTo(LivroController.class).slash("autor").slash(e.getId()).withRel("livros"));
    }
}
