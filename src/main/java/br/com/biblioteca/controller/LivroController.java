package br.com.biblioteca.controller;

import br.com.biblioteca.util.Paginacao;
import br.com.biblioteca.dto.LivroRequest;
import br.com.biblioteca.dto.LivroV2Response;
import br.com.biblioteca.exception.VersaoNaoSuportadaException;
import br.com.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.biblioteca.model.Autor;
import br.com.biblioteca.model.Categoria;
import br.com.biblioteca.model.Livro;
import br.com.biblioteca.model.StatusLivro;
import br.com.biblioteca.repository.AutorRepository;
import br.com.biblioteca.repository.CategoriaRepository;
import br.com.biblioteca.repository.LivroRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/livros")
@Tag(name = "Livros", description = "Gerenciamento do acervo de livros (relaciona autor e categorias)")
public class LivroController {

    private final LivroRepository repository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final PagedResourcesAssembler<Livro> pagedAssembler;

    public LivroController(LivroRepository repository, AutorRepository autorRepository,
                           CategoriaRepository categoriaRepository, PagedResourcesAssembler<Livro> pagedAssembler) {
        this.repository = repository;
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.pagedAssembler = pagedAssembler;
    }

    @Operation(summary = "Listar livros (paginado)",
            description = "Retorna uma página de livros. Parâmetros: page, size e sort (ex.: sort=titulo,asc).")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping
    public PagedModel<EntityModel<Livro>> listar(@ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findAll(pageable)), this::toModel);
    }

    @Operation(summary = "Buscar livros por título",
            description = "Consulta personalizada: filtra por trecho do título (case-insensitive). Ex.: /livros/buscar?titulo=casmurro")
    @ApiResponse(responseCode = "200", description = "Página de resultados (pode estar vazia)")
    @GetMapping("/buscar")
    public PagedModel<EntityModel<Livro>> buscarPorTitulo(@RequestParam String titulo, @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findByTituloContainingIgnoreCase(titulo, pageable)), this::toModel);
    }

    @Operation(summary = "Listar livros por status",
            description = "Consulta personalizada: filtra pelo enum StatusLivro (DISPONIVEL, EMPRESTADO, RESERVADO, EM_MANUTENCAO).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados"),
            @ApiResponse(responseCode = "400", description = "Status inválido")})
    @GetMapping("/status/{status}")
    public PagedModel<EntityModel<Livro>> buscarPorStatus(@PathVariable StatusLivro status, @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findByStatus(status, pageable)), this::toModel);
    }

    @Operation(summary = "Listar livros de um autor", description = "Retorna os livros escritos pelo autor informado.")
    @ApiResponse(responseCode = "200", description = "Página de resultados")
    @GetMapping("/autor/{autorId}")
    public PagedModel<EntityModel<Livro>> buscarPorAutor(@PathVariable Long autorId, @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findByAutorId(autorId, pageable)), this::toModel);
    }

    @Operation(summary = "Listar livros de uma categoria", description = "Retorna os livros que pertencem à categoria informada.")
    @ApiResponse(responseCode = "200", description = "Página de resultados")
    @GetMapping("/categoria/{categoriaId}")
    public PagedModel<EntityModel<Livro>> buscarPorCategoria(@PathVariable Long categoriaId, @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(Paginacao.validar(repository.findByCategoriasId(categoriaId, pageable)), this::toModel);
    }

    @Operation(summary = "Buscar livro por id (versionado)",
            description = "Endpoint com versionamento via cabeçalho **X-API-Version**.\n\n"
                    + "- **v1**: entidade completa, com autor e categorias aninhados.\n"
                    + "- **v2** (padrão, quando o cabeçalho é omitido): representação resumida, com `disponivel` (boolean) e autor/categorias reduzidos a id e nome.\n\n"
                    + "Qualquer outro valor retorna 400. A versão aplicada é devolvida no cabeçalho de resposta X-API-Version.")
    @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, required = false,
            description = "Versão do contrato da resposta (1 ou 2). Padrão: 2.",
            schema = @Schema(type = "string", allowableValues = {"1", "2"}, defaultValue = "2"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado (formato depende da versão)",
                    headers = @Header(name = "X-API-Version", description = "Versão aplicada na resposta"),
                    content = @Content(mediaType = "application/hal+json",
                            schema = @Schema(oneOf = {Livro.class, LivroV2Response.class}))),
            @ApiResponse(responseCode = "400", description = "Versão não suportada"),
            @ApiResponse(responseCode = "404", description = "Não encontrado")})
    @GetMapping("/{id}")
    public ResponseEntity<RepresentationModel<?>> buscar(@PathVariable Long id,
            @RequestHeader(value = "X-API-Version", defaultValue = "2") String versao) {
        String v = versao.trim();
        if ("1".equals(v)) {
            return ResponseEntity.ok().header("X-API-Version", "1").body(toModel(obter(id)));
        }
        if ("2".equals(v)) {
            return ResponseEntity.ok().header("X-API-Version", "2").body(toModelV2(obter(id)));
        }
        throw new VersaoNaoSuportadaException(versao, List.of("1", "2"));
    }

    @Operation(summary = "Criar livro",
            description = "Cadastra um livro informando o id do autor (autorId) e os ids das categorias (categoriaIds).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado com sucesso (header Location)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Autor ou categoria inexistente"),
            @ApiResponse(responseCode = "409", description = "ISBN duplicado")})
    @PostMapping
    public ResponseEntity<EntityModel<Livro>> criar(@Valid @RequestBody LivroRequest request) {
        EntityModel<Livro> model = toModel(repository.save(preencher(new Livro(), request)));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @Operation(summary = "Atualizar livro", description = "Substitui os dados de um livro existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Livro, autor ou categoria inexistente"),
            @ApiResponse(responseCode = "409", description = "ISBN duplicado")})
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Livro>> atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequest request) {
        return ResponseEntity.ok(toModel(repository.save(preencher(obter(id), request))));
    }

    @Operation(summary = "Excluir livro", description = "Remove um livro e suas associações com categorias.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrado")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.delete(obter(id));
        return ResponseEntity.noContent().build();
    }

    private Livro obter(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Livro", id));
    }

    private Livro preencher(Livro livro, LivroRequest r) {
        Autor autor = autorRepository.findById(r.autorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Autor", r.autorId()));
        Set<Categoria> categorias = new HashSet<>(categoriaRepository.findAllById(r.categoriaIds()));
        if (categorias.size() != r.categoriaIds().size()) {
            Set<Long> encontrados = new HashSet<>();
            categorias.forEach(c -> encontrados.add(c.getId()));
            Long faltante = r.categoriaIds().stream().filter(i -> !encontrados.contains(i)).findFirst().orElse(null);
            throw new RecursoNaoEncontradoException("Categoria", faltante);
        }
        livro.setTitulo(r.titulo());
        livro.setIsbn(r.isbn());
        livro.setPaginas(r.paginas());
        livro.setAnoPublicacao(r.anoPublicacao());
        livro.setStatus(r.status());
        livro.setAutor(autor);
        livro.setCategorias(categorias);
        return livro;
    }

    private EntityModel<Livro> toModel(Livro l) {
        return EntityModel.of(l,
                linkTo(methodOn(LivroController.class).buscar(l.getId(), "1")).withSelfRel(),
                linkTo(methodOn(LivroController.class).atualizar(l.getId(), null)).withRel("update"),
                linkTo(methodOn(LivroController.class).excluir(l.getId())).withRel("delete"),
                linkTo(LivroController.class).withRel("livros"),
                linkTo(methodOn(AutorController.class).buscar(l.getAutor().getId())).withRel("autor"));
    }

    private EntityModel<LivroV2Response> toModelV2(Livro l) {
        return EntityModel.of(LivroV2Response.de(l),
                linkTo(methodOn(LivroController.class).buscar(l.getId(), "2")).withSelfRel(),
                linkTo(LivroController.class).withRel("livros"),
                linkTo(methodOn(AutorController.class).buscar(l.getAutor().getId())).withRel("autor"));
    }
}
