package br.com.biblioteca.controller;

import br.com.biblioteca.util.Paginacao;
import br.com.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.biblioteca.model.Categoria;
import br.com.biblioteca.repository.CategoriaRepository;
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
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Gerenciamento de categorias literárias")
public class CategoriaController {

    private final CategoriaRepository repository;
    private final PagedResourcesAssembler<Categoria> pagedAssembler;

    public CategoriaController(CategoriaRepository repository, PagedResourcesAssembler<Categoria> pagedAssembler) {
        this.repository = repository;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Listar categorias (paginado)",
            description = "Retorna uma página de categorias. Parâmetros: page (0-based), size e sort (ex.: sort=nome,asc).")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping
    public PagedModel<EntityModel<Categoria>> listar(@ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findAll(pageable)), this::toModel);
    }

    @Operation(summary = "Buscar categorias por nome",
            description = "Consulta personalizada: filtra por trecho do nome (case-insensitive), com paginação. Ex.: /categorias/buscar?nome=romance")
    @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)")
    @GetMapping("/buscar")
    public PagedModel<EntityModel<Categoria>> buscarPorNome(@RequestParam String nome, @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findByNomeContainingIgnoreCase(nome, pageable)), this::toModel);
    }

    @Operation(summary = "Buscar categoria por id", description = "Retorna um(a) categoria pelo identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado(a)"),
            @ApiResponse(responseCode = "404", description = "Não encontrado(a)")})
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Categoria>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(obter(id)));
    }

    @Operation(summary = "Criar categoria", description = "Cadastra um(a) novo(a) categoria. O id é gerado automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado(a) com sucesso (header Location com o novo recurso)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (Bean Validation)"),
            @ApiResponse(responseCode = "409", description = "Valor duplicado em campo único")})
    @PostMapping
    public ResponseEntity<EntityModel<Categoria>> criar(@Valid @RequestBody Categoria body) {
        body.setId(null);
        EntityModel<Categoria> model = toModel(repository.save(body));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Operation(summary = "Atualizar categoria", description = "Substitui os dados de um(a) categoria existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado(a) com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Não encontrado(a)"),
            @ApiResponse(responseCode = "409", description = "Valor duplicado em campo único")})
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Categoria>> atualizar(@PathVariable Long id, @Valid @RequestBody Categoria body) {
        body.setVersao(obter(id).getVersao());
        body.setId(id);
        return ResponseEntity.ok(toModel(repository.save(body)));
    }

    @Operation(summary = "Excluir categoria", description = "Remove um(a) categoria. Falha com 409 se houver vínculos com outros recursos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Excluído(a) com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrado(a)"),
            @ApiResponse(responseCode = "409", description = "Possui vínculos e não pode ser excluído(a)")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.delete(obter(id));
        return ResponseEntity.noContent().build();
    }

    private Categoria obter(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private EntityModel<Categoria> toModel(Categoria e) {
        return EntityModel.of(e,
                linkTo(methodOn(CategoriaController.class).buscar(e.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).atualizar(e.getId(), null)).withRel("update"),
                linkTo(methodOn(CategoriaController.class).excluir(e.getId())).withRel("delete"),
                linkTo(CategoriaController.class).withRel("categorias"), linkTo(LivroController.class).slash("categoria").slash(e.getId()).withRel("livros"));
    }
}
