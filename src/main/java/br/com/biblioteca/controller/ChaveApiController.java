package br.com.biblioteca.controller;

import br.com.biblioteca.util.Paginacao;
import br.com.biblioteca.dto.ChaveApiRequest;
import br.com.biblioteca.dto.ChaveApiResponse;
import br.com.biblioteca.repository.ChaveApiRepository;
import br.com.biblioteca.service.ChaveApiService;
import br.com.biblioteca.service.ChaveApiService.ChaveCriada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api-keys")
@Tag(name = "Chaves de API", description = "Geração e gerenciamento de chaves de API (exige chave de nível ADMIN)")
public class ChaveApiController {

    private final ChaveApiService service;
    private final ChaveApiRepository repository;
    private final PagedResourcesAssembler<ChaveApiResponse> pagedAssembler;

    public ChaveApiController(ChaveApiService service, ChaveApiRepository repository,
                              PagedResourcesAssembler<ChaveApiResponse> pagedAssembler) {
        this.service = service;
        this.repository = repository;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Listar chaves de API (paginado)",
            description = "Lista as chaves cadastradas. A chave em texto puro nunca é retornada aqui.")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping
    public PagedModel<EntityModel<ChaveApiResponse>> listar(@ParameterObject Pageable pageable) {
        Page<ChaveApiResponse> pagina = repository.findAll(pageable).map(c -> ChaveApiResponse.de(c, null));
        return pagedAssembler.toModel(Paginacao.validar(pagina), this::toModel);
    }

    @Operation(summary = "Buscar chave de API por id", description = "Retorna os metadados de uma chave (sem o valor da chave).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada"),
            @ApiResponse(responseCode = "404", description = "Não encontrada")})
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ChaveApiResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(ChaveApiResponse.de(service.buscar(id), null)));
    }

    @Operation(summary = "Gerar chave de API",
            description = "Gera uma nova chave com o nível informado (LEITURA, ESCRITA ou ADMIN), opcionalmente vinculada a um usuário. "
                    + "O valor da chave aparece SOMENTE nesta resposta; no banco fica apenas o hash.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chave gerada (header Location)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário inexistente")})
    @PostMapping
    public ResponseEntity<EntityModel<ChaveApiResponse>> criar(@Valid @RequestBody ChaveApiRequest request) {
        ChaveCriada criada = service.criar(request);
        EntityModel<ChaveApiResponse> model = toModel(ChaveApiResponse.de(criada.chaveApi(), criada.chave()));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Operation(summary = "Rotacionar chave de API",
            description = "Gera um novo valor para a chave e invalida o anterior imediatamente. O novo valor aparece somente nesta resposta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chave rotacionada"),
            @ApiResponse(responseCode = "404", description = "Não encontrada"),
            @ApiResponse(responseCode = "409", description = "Chave já revogada")})
    @PostMapping("/{id}/rotacionar")
    public ResponseEntity<EntityModel<ChaveApiResponse>> rotacionar(@PathVariable Long id) {
        ChaveCriada criada = service.rotacionar(id);
        return ResponseEntity.ok(toModel(ChaveApiResponse.de(criada.chaveApi(), criada.chave())));
    }

    @Operation(summary = "Revogar chave de API",
            description = "Desativa a chave (ela deixa de autenticar). O registro é mantido para histórico.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Revogada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrada")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revogar(@PathVariable Long id) {
        service.revogar(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<ChaveApiResponse> toModel(ChaveApiResponse c) {
        return EntityModel.of(c,
                linkTo(methodOn(ChaveApiController.class).buscar(c.id())).withSelfRel(),
                linkTo(methodOn(ChaveApiController.class).rotacionar(c.id())).withRel("rotacionar"),
                linkTo(methodOn(ChaveApiController.class).revogar(c.id())).withRel("revogar"),
                linkTo(ChaveApiController.class).withRel("api-keys"));
    }
}
