package br.com.biblioteca.controller;

import br.com.biblioteca.util.Paginacao;
import br.com.biblioteca.dto.CarteirinhaRequest;
import br.com.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.biblioteca.model.Carteirinha;
import br.com.biblioteca.model.StatusCarteirinha;
import br.com.biblioteca.model.Usuario;
import br.com.biblioteca.repository.CarteirinhaRepository;
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
@RequestMapping("/carteirinhas")
@Tag(name = "Carteirinhas", description = "Gerenciamento de carteirinhas (relacionamento One-to-One com Usuário)")
public class CarteirinhaController {

    private final CarteirinhaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PagedResourcesAssembler<Carteirinha> pagedAssembler;

    public CarteirinhaController(CarteirinhaRepository repository, UsuarioRepository usuarioRepository,
                                 PagedResourcesAssembler<Carteirinha> pagedAssembler) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Listar carteirinhas (paginado)",
            description = "Retorna uma página de carteirinhas. Parâmetros: page, size e sort (ex.: sort=dataValidade,desc).")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping
    public PagedModel<EntityModel<Carteirinha>> listar(@ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findAll(pageable)), this::toModel);
    }

    @Operation(summary = "Listar carteirinhas por status",
            description = "Consulta personalizada: filtra pelo enum StatusCarteirinha (ATIVA, SUSPENSA, CANCELADA, VENCIDA).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados"),
            @ApiResponse(responseCode = "400", description = "Status inválido")})
    @GetMapping("/status/{status}")
    public PagedModel<EntityModel<Carteirinha>> buscarPorStatus(@PathVariable StatusCarteirinha status,
                                                                @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findByStatus(status, pageable)), this::toModel);
    }

    @Operation(summary = "Buscar carteirinha por id", description = "Retorna uma carteirinha pelo identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "404", description = "Não encontrada")})
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Carteirinha>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(obter(id)));
    }

    @Operation(summary = "Criar carteirinha",
            description = "Emite uma carteirinha para um usuário (usuarioId). Cada usuário só pode ter uma carteirinha.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criada com sucesso (header Location)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário inexistente"),
            @ApiResponse(responseCode = "409", description = "Número duplicado ou usuário já possui carteirinha")})
    @PostMapping
    public ResponseEntity<EntityModel<Carteirinha>> criar(@Valid @RequestBody CarteirinhaRequest request) {
        EntityModel<Carteirinha> model = toModel(repository.save(preencher(new Carteirinha(), request)));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Operation(summary = "Atualizar carteirinha", description = "Substitui os dados de uma carteirinha existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Carteirinha ou usuário inexistente"),
            @ApiResponse(responseCode = "409", description = "Número duplicado ou usuário já possui carteirinha")})
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Carteirinha>> atualizar(@PathVariable Long id,
                                                              @Valid @RequestBody CarteirinhaRequest request) {
        return ResponseEntity.ok(toModel(repository.save(preencher(obter(id), request))));
    }

    @Operation(summary = "Excluir carteirinha", description = "Remove uma carteirinha (o usuário é mantido).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrada")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.delete(obter(id));
        return ResponseEntity.noContent().build();
    }

    private Carteirinha obter(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Carteirinha", id));
    }

    private Carteirinha preencher(Carteirinha c, CarteirinhaRequest r) {
        Usuario usuario = usuarioRepository.findById(r.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", r.usuarioId()));
        c.setNumero(r.numero());
        c.setDataEmissao(r.dataEmissao());
        c.setDataValidade(r.dataValidade());
        c.setStatus(r.status());
        c.setUsuario(usuario);
        return c;
    }

    private EntityModel<Carteirinha> toModel(Carteirinha c) {
        return EntityModel.of(c,
                linkTo(methodOn(CarteirinhaController.class).buscar(c.getId())).withSelfRel(),
                linkTo(methodOn(CarteirinhaController.class).atualizar(c.getId(), null)).withRel("update"),
                linkTo(methodOn(CarteirinhaController.class).excluir(c.getId())).withRel("delete"),
                linkTo(CarteirinhaController.class).withRel("carteirinhas"),
                linkTo(methodOn(UsuarioController.class).buscar(c.getUsuario().getId())).withRel("usuario"));
    }
}
