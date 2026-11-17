package br.com.biblioteca.config;

import br.com.biblioteca.filter.PoliticaAcesso;
import br.com.biblioteca.model.NivelAcesso;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_CHAVE = "ApiKeyAuth";
    private static final String SCHEMA_ERRO = "ErroResponse";
    private static final String TIPO_ERRO = "application/problem+json";

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Biblioteca By Bedeschi")
                        .version("2.0.0")
                        .description(DescricaoApi.V2)
                        .contact(new Contact().name("Bedeschi")))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_CHAVE, esquemaChave())
                        .addSchemas(SCHEMA_ERRO, schemaErro()));
    }

    /** Documentação da v1 (X-API-Version: 1): somente os requisitos da Parte 1. Sem /api-keys. */
    @Bean
    public GroupedOpenApi documentacaoV1() {
        return GroupedOpenApi.builder().group("v1").pathsToMatch("/**")
                .pathsToExclude("/api-keys", "/api-keys/**")
                .addOpenApiCustomizer(padronizarRespostas(false))
                .addOpenApiCustomizer(personalizarVersao("1")).build();
    }

    /** Documentação da v2 (padrão): tudo da v1 + recursos avançados da Parte 2. */
    @Bean
    public GroupedOpenApi documentacaoV2() {
        return GroupedOpenApi.builder().group("v2").pathsToMatch("/**")
                .addOpenApiCustomizer(padronizarRespostas(true))
                .addOpenApiCustomizer(personalizarVersao("2")).build();
    }

    /** Ajusta título, descrição e o endpoint versionado (GET /livros/{id}) para a versão escolhida. */
    private OpenApiCustomizer personalizarVersao(String versao) {
        return openApi -> {
            String aviso = "1".equals(versao)
                    ? "> 📌 Você está vendo a documentação da **v1**: somente os requisitos da Parte 1 (CRUD, paginação, validação, HATEOAS e Swagger), "
                      + "**sem** chave de API, rate limit, idempotência e CORS. Use o header `X-API-Version: 1`. "
                      + "`GET /livros/{id}` devolve a **entidade completa** do livro.\n\n"
                    : "> 📌 Você está vendo a documentação da **v2** (versão padrão): **tudo da v1** + chave de API, idempotência, rate limit, CORS e versionamento. "
                      + "`GET /livros/{id}` devolve o livro **resumido**, com o campo `disponivel`.\n\n";
            if (openApi.getInfo() != null) {
                openApi.getInfo().setTitle("API de Biblioteca By Bedeschi – v" + versao);
                openApi.getInfo().setVersion(versao + ".0.0");
                openApi.getInfo().setDescription(aviso + DescricaoApi.da(versao));
            }
            if (openApi.getPaths() == null) {
                return;
            }
            PathItem item = openApi.getPaths().get("/livros/{id}");
            if (item == null || item.getGet() == null) {
                return;
            }
            Operation get = item.getGet();
            get.setSummary("Buscar livro por id (v" + versao + ")");
            if (get.getParameters() != null) {
                for (Parameter p : get.getParameters()) {
                    if ("X-API-Version".equals(p.getName())) {
                        StringSchema fixo = new StringSchema();
                        fixo.setDefault(versao);
                        fixo.addEnumItem(versao);
                        p.setSchema(fixo);
                        p.setExample(versao);
                        p.setDescription("Versão do contrato da resposta. Nesta documentação: " + versao + ".");
                    }
                }
            }
            ApiResponse ok = get.getResponses() == null ? null : get.getResponses().get("200");
            if (ok != null) {
                ok.setDescription("Encontrado (formato da v" + versao + ")");
                ok.setContent(new Content().addMediaType("application/hal+json", new MediaType()
                        .schema(new Schema<>().$ref("1".equals(versao) ? "Livro" : "LivroV2Response"))));
            }
        };
    }

    /**
     * Padroniza a documentação de TODOS os endpoints:
     *  - respostas 4xx/5xx documentam o corpo de erro (e não o schema do recurso);
     *  - 204 não tem corpo;
     *  - marca endpoints protegidos (cadeado + 401/403);
     *  - documenta 429, 500 e o cabeçalho X-Idempotency-Key nos POST.
     */
    private OpenApiCustomizer padronizarRespostas(boolean v2) {
        return openApi -> {
            // O Springdoc descarta schemas não referenciados na hora de montar o documento;
            // por isso o schema de erro é (re)registrado aqui, junto com as referências a ele.
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            openApi.getComponents().addSchemas(SCHEMA_ERRO, schemaErro());
            // Define o esquema de segurança de forma explícita em cada versão (v1 sem, v2 com),
            // para que um documento nunca herde o estado do outro.
            if (v2) {
                openApi.getComponents().addSecuritySchemes(ESQUEMA_CHAVE, esquemaChave());
            } else {
                openApi.getComponents().setSecuritySchemes(null);
            }
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((caminho, item) -> item.readOperationsMap().forEach((metodo, op) -> {
                String m = metodo.name();
                if (op.getResponses() == null) {
                    op.setResponses(new ApiResponses());
                }

                NivelAcesso exigido = v2 ? PoliticaAcesso.exigido(m, caminho) : null;
                if (exigido != null) {
                    op.addSecurityItem(new SecurityRequirement().addList(ESQUEMA_CHAVE));
                    op.setDescription(acrescentar(op.getDescription(),
                            "🔒 **Exige chave de API** (`X-API-Key`) com nível mínimo **" + exigido + "**."));
                    garantir(op, "401", "Chave de API ausente, inválida ou revogada");
                    garantir(op, "403", "Nível de acesso da chave insuficiente");
                }

                if (v2 && "POST".equals(m) && !caminho.startsWith("/api-keys")) {
                    HeaderParameter idem = new HeaderParameter();
                    idem.setName("X-Idempotency-Key");
                    idem.setRequired(false);
                    idem.setDescription("Chave de idempotência (opcional, até 255 caracteres). Repetir a mesma chave com o mesmo corpo "
                            + "devolve a resposta original sem criar outro registro (cabeçalho de resposta Idempotency-Replayed: true).");
                    idem.setSchema(new StringSchema());
                    idem.setExample("pedido-0001");
                    op.addParametersItem(idem);
                    ApiResponse c409 = op.getResponses().get("409");
                    if (c409 == null) {
                        garantir(op, "409", "Requisição com a mesma X-Idempotency-Key ainda em processamento");
                    } else {
                        c409.setDescription(c409.getDescription()
                                + "; ou requisição com a mesma X-Idempotency-Key ainda em processamento");
                    }
                    garantir(op, "413", "Corpo maior que 100 KB (limite quando X-Idempotency-Key é usado)");
                    garantir(op, "422", "Chave de idempotência reutilizada com um corpo diferente");
                }

                documentarPaginacao(op);
                garantir(op, "400", "Requisição inválida: dados, parâmetros ou valores de enum fora do formato esperado");
                if (op.getRequestBody() != null) {
                    garantir(op, "415", "Tipo de conteúdo não suportado (envie application/json)");
                }

                garantir(op, "500", "Erro interno do servidor");
                if (v2) {
                    garantir(op, "429", "Limite de requisições excedido (veja o cabeçalho Retry-After)");
                    ApiResponse r429 = op.getResponses().get("429");
                    if (r429 != null && (r429.getHeaders() == null || !r429.getHeaders().containsKey("Retry-After"))) {
                        Header retry = new Header();
                        retry.setDescription("Segundos até poder tentar novamente");
                        retry.setSchema(new IntegerSchema());
                        r429.addHeaderObject("Retry-After", retry);
                    }
                }

                documentarCabecalhos(op, m, caminho, v2);
                normalizarConteudo(op);
                ordenarRespostas(op);
            }));
        };
    }

    private SecurityScheme esquemaChave() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("Chave de API. Níveis: LEITURA < ESCRITA < ADMIN.");
    }

    private void normalizarConteudo(Operation op) {
        op.getResponses().forEach((codigo, resposta) -> {
            if ("204".equals(codigo)) {
                resposta.setContent(null);
                return;
            }
            Integer status = parse(codigo);
            if (status != null && status >= 400) {
                MediaType tipo = new MediaType().schema(new Schema<>().$ref(SCHEMA_ERRO));
                tipo.setExample(exemploErro(status));
                resposta.setContent(new Content().addMediaType(TIPO_ERRO, tipo));
            }
        });
    }

    /** Cabeçalhos que a API realmente devolve: rate limit nas respostas 2xx, Location no 201, replay de idempotência. */
    private void documentarCabecalhos(Operation op, String metodo, String caminho, boolean v2) {
        op.getResponses().forEach((codigo, resposta) -> {
            Integer status = parse(codigo);
            if (status == null || status < 200 || status >= 300) {
                return;
            }
            if (v2) {
                cabecalho(resposta, "X-RateLimit-Limit", "Máximo de requisições na janela atual", new IntegerSchema());
                cabecalho(resposta, "X-RateLimit-Remaining", "Requisições restantes na janela atual", new IntegerSchema());
                cabecalho(resposta, "X-RateLimit-Reset", "Segundos até a janela reiniciar", new IntegerSchema());
            }
            if (status == 201) {
                cabecalho(resposta, "Location", "URL do recurso criado", new StringSchema());
            }
            if (v2 && "POST".equals(metodo) && !caminho.startsWith("/api-keys")) {
                cabecalho(resposta, "Idempotency-Replayed",
                        "\"true\" quando a resposta é a original reaproveitada (repetição da mesma X-Idempotency-Key)",
                        new StringSchema());
            }
        });
    }

    private void cabecalho(ApiResponse resposta, String nome, String descricao, Schema<?> schema) {
        if (resposta.getHeaders() == null || !resposta.getHeaders().containsKey(nome)) {
            Header h = new Header();
            h.setDescription(descricao);
            h.setSchema(schema);
            resposta.addHeaderObject(nome, h);
        }
    }

    /** Exibe os códigos em ordem crescente (200, 201, 400, 401...), independentemente de como foram adicionados. */
    private void ordenarRespostas(Operation op) {
        ApiResponses ordenadas = new ApiResponses();
        op.getResponses().entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, ApiResponse> e) -> {
                    Integer c = parse(e.getKey());
                    return c == null ? 999 : c;
                }))
                .forEach(e -> ordenadas.addApiResponse(e.getKey(), e.getValue()));
        op.setResponses(ordenadas);
    }

    /** Deixa explícitos no Swagger os limites reais da paginação (defaults e máximo configurados na aplicação). */
    private void documentarPaginacao(Operation op) {
        if (op.getParameters() == null) {
            return;
        }
        boolean paginado = false;
        for (Parameter p : op.getParameters()) {
            if (p.getSchema() == null) {
                continue;
            }
            if ("page".equals(p.getName())) {
                paginado = true;
                p.setDescription("Número da página, começando em 0. Valor negativo é tratado como 0. "
                        + "Página além da última retorna 404.");
                p.getSchema().setMinimum(BigDecimal.ZERO);
                p.getSchema().setDefault(0);
            } else if ("size".equals(p.getName())) {
                p.setDescription("Itens por página (padrão 10, máximo 100). Valores acima de 100 são limitados a 100.");
                p.getSchema().setMinimum(BigDecimal.ONE);
                p.getSchema().setMaximum(BigDecimal.valueOf(100));
                p.getSchema().setDefault(10);
            }
        }
        if (paginado) {
            garantir(op, "404", "Página inexistente (número maior que a última página)");
        }
    }

    private void garantir(Operation op, String codigo, String descricao) {
        if (op.getResponses().get(codigo) == null) {
            op.getResponses().addApiResponse(codigo, new ApiResponse().description(descricao));
        }
    }

    private String acrescentar(String atual, String texto) {
        return (atual == null || atual.isBlank()) ? texto : atual + "\n\n" + texto;
    }

    private Integer parse(String codigo) {
        try {
            return Integer.valueOf(codigo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, Object> exemploErro(int status) {
        Map<String, Object> ex = new LinkedHashMap<>();
        ex.put("type", "about:blank");
        switch (status) {
            case 400 -> {
                ex.put("title", "Erro de validação");
                ex.put("status", 400);
                ex.put("detail", "Dados inválidos");
                ex.put("instance", "/autores");
                Map<String, String> erros = new LinkedHashMap<>();
                erros.put("nome", "O nome é obrigatório");
                ex.put("erros", erros);
            }
            case 401 -> campos(ex, "Chave de API ausente", 401, "Informe uma chave de API válida no cabeçalho X-API-Key.", "/autores");
            case 403 -> campos(ex, "Acesso negado", 403, "Esta operação exige nível ADMIN; sua chave tem nível ESCRITA.", "/autores/1");
            case 404 -> campos(ex, "Recurso não encontrado", 404, "Livro não encontrado(a) com id 999", "/livros/999");
            case 409 -> campos(ex, "Conflito de dados", 409, "Violação de integridade: valor duplicado (campo único) ou registro possui vínculos com outros recursos.", "/categorias");
            case 422 -> campos(ex, "Chave de idempotência reutilizada", 422, "A chave X-Idempotency-Key já foi usada com uma requisição diferente. Gere uma nova chave para uma nova operação.", "/autores");
            case 413 -> campos(ex, "Corpo da requisição muito grande", 413, "Requisições com X-Idempotency-Key aceitam corpo de até 100 KB.", "/autores");
            case 429 -> campos(ex, "Limite de requisições excedido", 429, "Você excedeu o limite de 30 requisições por 60 segundos. Tente novamente em 42 segundos.", "/livros");
            case 500 -> campos(ex, "Erro interno do servidor", 500, "Ocorreu um erro interno. Tente novamente mais tarde.", "/livros");
            default -> campos(ex, "Falha ao processar a requisição", status, "A requisição não pôde ser concluída.", "/");
        }
        return ex;
    }

    private void campos(Map<String, Object> ex, String titulo, int status, String detalhe, String instancia) {
        ex.put("title", titulo);
        ex.put("status", status);
        ex.put("detail", detalhe);
        ex.put("instance", instancia);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Schema schemaErro() {
        Schema<Object> s = new ObjectSchema();
        s.setDescription("Corpo padrão de erro (Problem Details, RFC 9457). Não contém dados do recurso.");
        Map<String, Schema> p = new LinkedHashMap<>();
        p.put("type", propriedade(new StringSchema(), "about:blank", "URI que identifica o tipo do problema"));
        p.put("title", propriedade(new StringSchema(), "Recurso não encontrado", "Resumo curto do erro"));
        p.put("status", propriedade(new IntegerSchema(), 404, "Código de status HTTP"));
        p.put("detail", propriedade(new StringSchema(), "Livro não encontrado(a) com id 999", "Explicação detalhada do erro"));
        p.put("instance", propriedade(new StringSchema(), "/livros/999", "Caminho da requisição que gerou o erro"));
        ObjectSchema erros = new ObjectSchema();
        erros.setAdditionalProperties(new StringSchema());
        erros.setDescription("Somente em erros de validação (400): campo -> mensagem");
        p.put("erros", erros);
        s.setProperties(p);
        return s;
    }

    private Schema<?> propriedade(Schema<?> schema, Object exemplo, String descricao) {
        schema.setExample(exemplo);
        schema.setDescription(descricao);
        return schema;
    }
}
