package br.com.biblioteca.controller;

import br.com.biblioteca.util.Paginacao;
import br.com.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.biblioteca.model.Usuario;
import br.com.biblioteca.repository.UsuarioRepository;
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
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Gerenciamento de usuários (leitores) da biblioteca")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PagedResourcesAssembler<Usuario> pagedAssembler;

    public UsuarioController(UsuarioRepository repository, PagedResourcesAssembler<Usuario> pagedAssembler) {
        this.repository = repository;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Listar usuarios (paginado)",
            description = "Retorna uma página de usuarios. Parâmetros: page (0-based), size e sort (ex.: sort=nome,asc).")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping
    public PagedModel<EntityModel<Usuario>> listar(@ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findAll(pageable)), this::toModel);
    }

    @Operation(summary = "Buscar usuarios por nome",
            description = "Consulta personalizada: filtra por trecho do nome (case-insensitive), com paginação. Ex.: /usuarios/buscar?nome=maria")
    @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)")
    @GetMapping("/buscar")
    public PagedModel<EntityModel<Usuario>> buscarPorNome(@RequestParam String nome, @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findByNomeContainingIgnoreCase(nome, pageable)), this::toModel);
    }

    @Operation(summary = "Buscar usuário por id", description = "Retorna um(a) usuário pelo identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado(a)"),
            @ApiResponse(responseCode = "404", description = "Não encontrado(a)")})
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(obter(id)));
    }

    @Operation(summary = "Criar usuário", description = "Cadastra um(a) novo(a) usuário. O id é gerado automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado(a) com sucesso (header Location com o novo recurso)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (Bean Validation)"),
            @ApiResponse(responseCode = "409", description = "Valor duplicado em campo único")})
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> criar(@Valid @RequestBody Usuario body) {
        body.setId(null);
        EntityModel<Usuario> model = toModel(repository.save(body));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Operation(summary = "Atualizar usuário", description = "Substitui os dados de um(a) usuário existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado(a) com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Não encontrado(a)"),
            @ApiResponse(responseCode = "409", description = "Valor duplicado em campo único")})
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> atualizar(@PathVariable Long id, @Valid @RequestBody Usuario body) {
        body.setVersao(obter(id).getVersao());
        body.setId(id);
        return ResponseEntity.ok(toModel(repository.save(body)));
    }

    @Operation(summary = "Excluir usuário", description = "Remove um(a) usuário. Falha com 409 se houver vínculos com outros recursos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Excluído(a) com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrado(a)"),
            @ApiResponse(responseCode = "409", description = "Possui vínculos e não pode ser excluído(a)")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.delete(obter(id));
        return ResponseEntity.noContent().build();
    }

    private Usuario obter(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", id));
    }

    private EntityModel<Usuario> toModel(Usuario e) {
        return EntityModel.of(e,
                linkTo(methodOn(UsuarioController.class).buscar(e.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).atualizar(e.getId(), null)).withRel("update"),
                linkTo(methodOn(UsuarioController.class).excluir(e.getId())).withRel("delete"),
                linkTo(UsuarioController.class).withRel("usuarios"));
    }
}
