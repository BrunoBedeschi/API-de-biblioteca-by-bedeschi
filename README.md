# API de Biblioteca By Bedeschi – Spring Boot (Parte 2)

API RESTful (Spring Boot 3.5 / Java 17+ / Maven / H2 / Spring Data JPA / Bean Validation / Springdoc OpenAPI / Spring HATEOAS).

## Executar
Use **JDK 17 ou 21** (o JDK 27 quebra o Lombok).
```
mvn spring-boot:run
```
- Swagger UI (tema vinho + cinza escuro): http://localhost:8080/swagger-ui.html
- Swagger UI padrão do Springdoc: http://localhost:8080/swagger-ui-padrao.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Console H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:bibliotecadb`, user `sa`, senha vazia)

## Entidades e relacionamentos
| Entidade | Relacionamento |
|---|---|
| Autor | **One-to-Many** com Livro |
| Livro | Many-to-One com Autor; **Many-to-Many** com Categoria; enum `StatusLivro` |
| Categoria | Many-to-Many com Livro |
| Usuario | **One-to-One** com Carteirinha |
| Carteirinha | One-to-One com Usuario; enum `StatusCarteirinha` |
| ChaveApi | Many-to-One (opcional) com Usuario; enum `NivelAcesso` |
| RegistroIdempotencia | guarda respostas de POST já processados |

## Recursos avançados (Parte 2)

### 4.1 Idempotência (`X-Idempotency-Key`)
- Vale para **POST** (exceto `/api-keys`, para não guardar chaves em texto puro). O cabeçalho é opcional.
- 1ª requisição: processa e guarda a resposta (somente 2xx) na tabela `registro_idempotencia`.
- Repetição (mesma chave e mesmo corpo): devolve a resposta original, **sem criar de novo**, com `Idempotency-Replayed: true`.
- Mesma chave com corpo diferente: **422**. Mesma chave ainda em processamento: **409**.
- Respostas de erro não são guardadas (dá para corrigir o corpo e reenviar com a mesma chave).
- A chave é isolada por cliente (chave de API ou IP) e expira em 24 h (`app.idempotency.ttl-hours`).

### 4.2 Autenticação por chave de API (`X-API-Key`)
Implementada com filtro próprio (sem Spring Security). Só o **hash SHA-256** da chave fica no banco; o valor em texto puro aparece uma única vez, ao gerar/rotacionar.

| Nível | Pode |
|---|---|
| (sem chave) | `GET` em livros, autores e categorias |
| `LEITURA` | + `GET` em usuários e carteirinhas (dados pessoais) |
| `ESCRITA` | + `POST` / `PUT` em todos os recursos |
| `ADMIN` | + `DELETE` e gerenciar chaves em `/api-keys` |

Sem chave ou chave inválida/revogada: **401**. Nível insuficiente: **403**.

**Chaves de teste** (criadas na inicialização, definidas em `application.properties`):
`admin-key-123456` (ADMIN) · `escrita-key-123456` (ESCRITA) · `leitura-key-123456` (LEITURA)

Gerenciamento (nível ADMIN): `POST /api-keys` (gerar) · `GET /api-keys` · `GET /api-keys/{id}` · `POST /api-keys/{id}/rotacionar` · `DELETE /api-keys/{id}` (revogar).

### 4.3 Rate limiting
- Por IP, janela fixa: **30 requisições / 60 s** (`app.rate-limit.max-requests` e `app.rate-limit.window-seconds`).
- Todas as respostas trazem `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`.
- Excedeu: **429** com `Retry-After` (segundos).
- Swagger UI e console H2 não entram na contagem.

### 4.4 CORS
- Origens permitidas: `http://localhost:3000`, `http://localhost:4200`, `http://localhost:5173` (`app.cors.allowed-origins`).
- Métodos: `GET, POST, PUT, DELETE, OPTIONS` (`app.cors.allowed-methods`).
- Cabeçalhos aceitos: `Content-Type, Accept, X-API-Key, X-Idempotency-Key, X-API-Version`; expostos: `Location, Retry-After, X-RateLimit-*, X-API-Version, Idempotency-Replayed`.
- O preflight (`OPTIONS`) não exige chave de API.

### 4.5 Versionamento (`X-API-Version`)
O header escolhe o **modo da API inteira**. Sem o header, vale a **v2**; valor diferente de `1` ou `2` retorna **400**. A versão aplicada volta no header de resposta `X-API-Version`.

| | **v1** (`X-API-Version: 1`) | **v2** (padrão) |
|---|---|---|
| Parte 1: CRUD, paginação, validação, HATEOAS, Swagger | ✅ | ✅ |
| Chave de API (`X-API-Key`) e `/api-keys` | ❌ (rotas abertas; `/api-keys` retorna 404) | ✅ |
| Rate limit (429, `Retry-After`) | ❌ | ✅ |
| Idempotência (`X-Idempotency-Key`) | ❌ | ✅ |
| CORS por origens específicas | ❌ | ✅ |
| `GET /livros/{id}` | entidade completa | resumido, com `disponivel` |
| Dados | próprios da v1 | próprios da v2 (independentes) |

No Swagger, o seletor **v1 / v2** no topo troca a documentação (`/v3/api-docs/v1` e `/v3/api-docs/v2`) e envia o `X-API-Version` correspondente em todo "Try it out".

### Dados independentes por versão
Cada registro (autor, categoria, livro, usuário, carteirinha) pertence à versão que o criou (coluna oculta `versao`, via `@TenantId` do Hibernate) e toda consulta é filtrada por ela. Alterar algo na v1 **não** muda a v2 e vice-versa. As duas começam com os mesmos dados de exemplo, mas os **ids são diferentes** entre elas (ex.: v2 → ids 1 e 2; v1 → ids 3 e 4 em cada tabela) e restrições como ISBN/e-mail únicos valem por versão. Para voltar ao banco compartilhado: `DADOS_SEPARADOS_POR_VERSAO=false`.

> ⚠️ Como a v1 não tem autenticação, qualquer cliente que enviar `X-API-Version: 1` escreve sem chave. Em produção, defina `V1_HABILITADA=false` para desativar o modo v1 (retorna 400).

## Erros (Problem Details, RFC 9457)
Todos os erros (4xx/5xx) saem no mesmo formato e **sem dados do recurso**:
```json
{ "type": "about:blank", "title": "Recurso não encontrado", "status": 404,
  "detail": "Livro não encontrado(a) com id 999", "instance": "/livros/999" }
```
Validação (400) inclui `"erros": { "campo": "mensagem" }`. O Swagger documenta esse corpo em todos os códigos 4xx/5xx e deixa o `204` sem corpo.

## Testes rápidos (curl)
```bash
# público
curl -i http://localhost:8080/livros

# 404 (corpo de erro, sem dados)
curl -i http://localhost:8080/livros/999

# 401 (sem chave) e 403 (chave LEITURA tentando escrever)
curl -i -X POST http://localhost:8080/autores -H "Content-Type: application/json" -d '{"nome":"Clarice Lispector"}'
curl -i -X POST http://localhost:8080/autores -H "X-API-Key: leitura-key-123456" -H "Content-Type: application/json" -d '{"nome":"Clarice Lispector"}'

# criar com idempotência: rode 2x -> 2ª volta 201 com Idempotency-Replayed: true e o MESMO id
curl -i -X POST http://localhost:8080/autores -H "X-API-Key: escrita-key-123456" \
  -H "X-Idempotency-Key: abc-001" -H "Content-Type: application/json" -d '{"nome":"Lima Barreto"}'

# mesma chave, corpo diferente -> 422
curl -i -X POST http://localhost:8080/autores -H "X-API-Key: escrita-key-123456" \
  -H "X-Idempotency-Key: abc-001" -H "Content-Type: application/json" -d '{"nome":"Outro Nome"}'

# versionamento (ids da v1 começam em 3 por causa da separação de dados)
curl -s http://localhost:8080/livros/3 -H "X-API-Version: 1"
curl -s http://localhost:8080/livros/1 -H "X-API-Version: 2"
curl -i http://localhost:8080/livros/1 -H "X-API-Version: 9"      # 400

# gerar chave (ADMIN)
curl -i -X POST http://localhost:8080/api-keys -H "X-API-Key: admin-key-123456" \
  -H "Content-Type: application/json" -d '{"nome":"Balcão de atendimento","nivel":"ESCRITA"}'

# CORS (preflight)
curl -i -X OPTIONS http://localhost:8080/livros -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" -H "Access-Control-Request-Headers: x-api-key,content-type"

# rate limit: passe de 30 chamadas em 1 minuto -> 429 com Retry-After
for i in $(seq 1 35); do curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/livros; done
```
No Swagger: clique em **Authorize**, informe uma chave e use *Try it out*.

## Endpoints (por entidade)
`GET /{recurso}` (paginado) · `GET /{recurso}/{id}` · `POST /{recurso}` · `PUT /{recurso}/{id}` · `DELETE /{recurso}/{id}` + consultas personalizadas:

- `/autores/buscar?nome=` · `/categorias/buscar?nome=` · `/usuarios/buscar?nome=`
- `/livros/buscar?titulo=` · `/livros/status/{status}` · `/livros/autor/{id}` · `/livros/categoria/{id}`
- `/carteirinhas/status/{status}`

Recursos: `autores`, `categorias`, `livros`, `usuarios`, `carteirinhas`, `api-keys`.

## Exemplo de POST /livros
```json
{
  "titulo": "Memórias Póstumas de Brás Cubas",
  "isbn": "9788535911664",
  "paginas": 368,
  "anoPublicacao": 1881,
  "status": "DISPONIVEL",
  "autorId": 1,
  "categoriaIds": [1, 2]
}
```
Dados de exemplo são carregados de `data.sql` na inicialização.

## Deploy no Render
O projeto já traz `Dockerfile`, `render.yaml`, `.dockerignore` e `.gitignore`.

1. Suba a pasta do projeto (com o `Dockerfile` na raiz) para um repositório no GitHub.
2. No [Render](https://render.com): **New + → Web Service** (ou **Blueprint**, que lê o `render.yaml`) e conecte o repositório.
3. Se for Web Service manual: **Language = Docker**, **Instance Type = Free**, **Health Check Path = `/v3/api-docs`** e a variável `SPRING_PROFILES_ACTIVE=prod`.
4. Clique em **Deploy**. Quando terminar, acesse `https://SEU-SERVICO.onrender.com/swagger-ui.html`.

Variáveis de ambiente opcionais: `V1_HABILITADA`, `DADOS_SEPARADOS_POR_VERSAO`, `ADMIN_API_KEY`, `ESCRITA_API_KEY`, `LEITURA_API_KEY`, `CORS_ALLOWED_ORIGINS`, `RATE_LIMIT_MAX_REQUESTS`, `RATE_LIMIT_WINDOW_SECONDS`.

Observações do plano gratuito: o serviço "dorme" após ~15 min sem acesso (a primeira chamada seguinte leva cerca de 1 minuto) e, como o H2 é em memória, **os dados voltam ao estado inicial do `data.sql` a cada reinício**. Com `prod` ativo, o console H2 fica desligado.

## Página inexistente
Pedir uma página maior que a última (ex.: `/livros?page=257`) retorna **404** com a mensagem "A página 257 não existe...". A página 0 sempre responde 200, mesmo sem registros.
