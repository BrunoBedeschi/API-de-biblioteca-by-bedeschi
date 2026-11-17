package br.com.biblioteca.config;

/** Textos (Markdown) exibidos no topo do Swagger UI, um por versão da API. */
final class DescricaoApi {

    private DescricaoApi() {}

    static String da(String versao) {
        return "1".equals(versao) ? V1 : V2;
    }

    static final String V1 = """
# O que é esta API?

API RESTful para gerenciamento de uma **Biblioteca**. Ela permite cadastrar e consultar **autores, categorias, livros, usuários e carteirinhas** (5 entidades), mantendo todos os relacionamentos entre eles.

## Como funciona

| Recurso | O que representa | Relacionamento |
|---|---|---|
| Autores | Escritores das obras | 1:N com Livros |
| Categorias | Gêneros literários | N:N com Livros |
| Livros | Obras do acervo, com status (`DISPONIVEL`, `EMPRESTADO`, `RESERVADO`, `EM_MANUTENCAO`) | N:1 com Autor, N:N com Categorias |
| Usuários | Leitores da biblioteca | 1:1 com Carteirinha |
| Carteirinhas | Cartão de acesso do usuário, com validade e status (`ATIVA`, `SUSPENSA`, `CANCELADA`, `VENCIDA`) | 1:1 com Usuário |

Um **Livro** é cadastrado informando o `autorId` e a lista `categoriaIds`. A **Carteirinha** é emitida para um usuário já existente (`usuarioId`) e cada usuário pode ter apenas uma.

## O que esta versão permite

Nesta versão (v1) **todas as rotas são abertas**: não há autenticação nem controle de acesso.

| Operação | Acesso na v1 |
|---|---|
| Consultar (`GET`) qualquer recurso | aberto, sem chave |
| Criar e alterar (`POST`, `PUT`) | aberto, sem chave |
| Excluir (`DELETE`) | aberto, sem chave |
| Limite de requisições | sem limite |
| Chamadas de outras origens (CORS) | sem política configurada |
| Gerenciamento de chaves de API (`/api-keys`) | indisponível (apenas na v2) |

## Recursos da API

- **REST nível 3 (HATEOAS):** cada resposta traz o campo `_links` (formato HAL) com links para o próprio recurso (`self`), para as ações de edição e remoção (`update`, `delete`), para a coleção e para recursos relacionados, permitindo navegar pela API só seguindo os links.
- **Paginação:** todas as listagens são paginadas. Use `page` (começa em 0), `size` e `sort` (ex.: `sort=titulo,asc`).
- **Consultas personalizadas:** busca de autores, categorias e usuários por nome; livros por título, status, autor e categoria; carteirinhas por status.
- **Validação (Bean Validation):** os dados enviados em POST e PUT são validados; erros retornam `400` com a mensagem de cada campo inválido.
- **Erros padronizados:** todo erro (4xx/5xx) segue o formato Problem Details (RFC 9457) e nunca devolve dados do recurso.
- **Banco de dados:** H2 em memória (os dados são apagados quando a aplicação é reiniciada). Os dados da v1 são **independentes** dos da v2: o que você cria, altera ou exclui aqui não aparece na outra versão (cada uma começa com os mesmos dados de exemplo).

## Limites e regras dos campos

| Recurso | Campo | Regra |
|---|---|---|
| Autor | `nome` | obrigatório, até 100 caracteres |
| Autor | `nacionalidade` | opcional, até 60 caracteres |
| Autor | `dataNascimento` | opcional, deve estar no passado (`AAAA-MM-DD`) |
| Categoria | `nome` | obrigatório, até 60 caracteres, único |
| Categoria | `descricao` | opcional, até 255 caracteres |
| Livro | `titulo` | obrigatório, até 150 caracteres |
| Livro | `isbn` | obrigatório, 10 ou 13 dígitos (somente números), único |
| Livro | `paginas` | obrigatório, de 1 a 10000 |
| Livro | `anoPublicacao` | obrigatório, de 1400 a 2100 |
| Livro | `status` | obrigatório: `DISPONIVEL`, `EMPRESTADO`, `RESERVADO` ou `EM_MANUTENCAO` |
| Livro | `autorId` / `categoriaIds` | autor obrigatório; de 1 a 20 categorias, todas existentes |
| Usuário | `nome` | obrigatório, até 100 caracteres |
| Usuário | `email` | obrigatório, e-mail válido, até 150 caracteres, único |
| Usuário | `cpf` | obrigatório, 11 dígitos numéricos (apenas o formato é validado), único |
| Carteirinha | `numero` | obrigatório, até 20 caracteres, único |
| Carteirinha | `dataEmissao` | obrigatória, não pode ser futura |
| Carteirinha | `dataValidade` | obrigatória, futura e posterior à emissão |
| Carteirinha | `status` / `usuarioId` | obrigatórios (`ATIVA`, `SUSPENSA`, `CANCELADA`, `VENCIDA`); um usuário só pode ter uma carteirinha |

**Outros limites**

- **Paginação:** `page` começa em 0; `size` padrão 10 e **máximo 100** (valores maiores são reduzidos a 100). Pedir uma página além da última retorna `404`; a página 0 sempre responde `200`, mesmo sem registros.

## Códigos de resposta

| Código | Significado |
|---|---|
| `200 OK` | Consulta ou atualização realizada com sucesso |
| `201 Created` | Recurso criado (o cabeçalho `Location` aponta para ele) |
| `204 No Content` | Recurso removido com sucesso |
| `400 Bad Request` | Dados inválidos (validação, JSON malformado, parâmetro ou enum inválido) |
| `404 Not Found` | Recurso não encontrado (inclusive autor, categoria ou usuário informados em um cadastro) ou **página inexistente** (número maior que a última página) |
| `409 Conflict` | Valor duplicado (ISBN, e-mail, CPF...) ou registro com vínculos que impedem a exclusão |
| `415 Unsupported Media Type` | Corpo enviado em formato diferente de `application/json` |
| `500 Internal Server Error` | Erro inesperado no servidor |

## Versão 1: o que ela contém

Esta é a **v1** da API (`X-API-Version: 1`): estrutura do projeto, 5 entidades com relacionamentos (1:1, 1:N e N:N) e enums, CRUD completo com paginação e consultas personalizadas, validações (Bean Validation), tratamento de erros, documentação Swagger e HATEOAS.

Os recursos avançados (**chave de API, rate limit, CORS, idempotência e versionamento**) pertencem à **v2**. Para usá-los, envie `X-API-Version: 2` ou escolha **v2** no seletor, no topo da página.

## Por onde começar

1. Cadastre um **Autor** (`POST /autores`).
2. Cadastre uma ou mais **Categorias** (`POST /categorias`).
3. Cadastre um **Livro** (`POST /livros`) com o `autorId` e os `categoriaIds`.
4. Cadastre um **Usuário** (`POST /usuarios`) e emita sua **Carteirinha** (`POST /carteirinhas`) com o `usuarioId`.
5. Consulte as listagens paginadas (`GET /livros?page=0&size=10`) e as buscas (`GET /livros/buscar?titulo=`).
6. Siga os links `_links` das respostas (HATEOAS) para navegar entre autor, livro e categorias.
""";

    static final String V2 = """
# O que é esta API?

API RESTful para gerenciamento de uma **Biblioteca**. Ela permite cadastrar e consultar **autores, categorias, livros, usuários e carteirinhas** (5 entidades), mantendo todos os relacionamentos entre eles.

## Como funciona

| Recurso | O que representa | Relacionamento |
|---|---|---|
| Autores | Escritores das obras | 1:N com Livros |
| Categorias | Gêneros literários | N:N com Livros |
| Livros | Obras do acervo, com status (`DISPONIVEL`, `EMPRESTADO`, `RESERVADO`, `EM_MANUTENCAO`) | N:1 com Autor, N:N com Categorias |
| Usuários | Leitores da biblioteca | 1:1 com Carteirinha |
| Carteirinhas | Cartão de acesso do usuário, com validade e status (`ATIVA`, `SUSPENSA`, `CANCELADA`, `VENCIDA`) | 1:1 com Usuário |
| Chaves de API | Credenciais para acessar as rotas protegidas | N:1 (opcional) com Usuário |

Um **Livro** é cadastrado informando o `autorId` e a lista `categoriaIds`. A **Carteirinha** é emitida para um usuário já existente (`usuarioId`) e cada usuário pode ter apenas uma.

## O que esta versão permite

Na v2 o acesso é controlado pelo header `X-API-Key`:

| Operação | Quem pode |
|---|---|
| `GET` em livros, autores e categorias | qualquer cliente, sem chave |
| `GET` em usuários e carteirinhas (dados pessoais) | chave `LEITURA`, `ESCRITA` ou `ADMIN` |
| `POST` e `PUT` em todos os recursos | chave `ESCRITA` ou `ADMIN` |
| `DELETE` em todos os recursos | somente chave `ADMIN` |
| Gerenciar chaves de API (`/api-keys`) | somente chave `ADMIN` |
| Limite de requisições | 30 por minuto por IP (`429` ao exceder) |
| Chamadas de outras origens (CORS) | apenas origens permitidas |
| `POST` repetido | evitado com `X-Idempotency-Key` |

Sem chave: `401`. Chave sem nível suficiente: `403`.

## Recursos da API

- **REST nível 3 (HATEOAS):** cada resposta traz o campo `_links` (formato HAL) com links para o próprio recurso (`self`), para as ações de edição e remoção (`update`, `delete`), para a coleção e para recursos relacionados, permitindo navegar pela API só seguindo os links.
- **Paginação:** todas as listagens são paginadas. Use `page` (começa em 0), `size` e `sort` (ex.: `sort=titulo,asc`).
- **Consultas personalizadas:** busca de autores, categorias e usuários por nome; livros por título, status, autor e categoria; carteirinhas por status.
- **Validação (Bean Validation):** os dados enviados em POST e PUT são validados; erros retornam `400` com a mensagem de cada campo inválido.
- **Erros padronizados:** todo erro (4xx/5xx) segue o formato Problem Details (RFC 9457) e nunca devolve dados do recurso.
- **Banco de dados:** H2 em memória (os dados são apagados quando a aplicação é reiniciada). Os dados da v2 são **independentes** dos da v1: o que você cria, altera ou exclui aqui não aparece na outra versão (cada uma começa com os mesmos dados de exemplo).

## Limites e regras dos campos

| Recurso | Campo | Regra |
|---|---|---|
| Autor | `nome` | obrigatório, até 100 caracteres |
| Autor | `nacionalidade` | opcional, até 60 caracteres |
| Autor | `dataNascimento` | opcional, deve estar no passado (`AAAA-MM-DD`) |
| Categoria | `nome` | obrigatório, até 60 caracteres, único |
| Categoria | `descricao` | opcional, até 255 caracteres |
| Livro | `titulo` | obrigatório, até 150 caracteres |
| Livro | `isbn` | obrigatório, 10 ou 13 dígitos (somente números), único |
| Livro | `paginas` | obrigatório, de 1 a 10000 |
| Livro | `anoPublicacao` | obrigatório, de 1400 a 2100 |
| Livro | `status` | obrigatório: `DISPONIVEL`, `EMPRESTADO`, `RESERVADO` ou `EM_MANUTENCAO` |
| Livro | `autorId` / `categoriaIds` | autor obrigatório; de 1 a 20 categorias, todas existentes |
| Usuário | `nome` | obrigatório, até 100 caracteres |
| Usuário | `email` | obrigatório, e-mail válido, até 150 caracteres, único |
| Usuário | `cpf` | obrigatório, 11 dígitos numéricos (apenas o formato é validado), único |
| Carteirinha | `numero` | obrigatório, até 20 caracteres, único |
| Carteirinha | `dataEmissao` | obrigatória, não pode ser futura |
| Carteirinha | `dataValidade` | obrigatória, futura e posterior à emissão |
| Carteirinha | `status` / `usuarioId` | obrigatórios (`ATIVA`, `SUSPENSA`, `CANCELADA`, `VENCIDA`); um usuário só pode ter uma carteirinha |
| Chave de API | `nome` / `nivel` | nome obrigatório (até 100 caracteres); nível `LEITURA`, `ESCRITA` ou `ADMIN` |

**Outros limites**

- **Paginação:** `page` começa em 0; `size` padrão 10 e **máximo 100** (valores maiores são reduzidos a 100). Pedir uma página além da última retorna `404`; a página 0 sempre responde `200`, mesmo sem registros.
- **Rate limit:** 30 requisições por 60 segundos por IP.
- **Idempotência:** `X-Idempotency-Key` com até 255 caracteres, lembrada por 24 horas; corpo de até 100 KB.
- **Versionamento:** `X-API-Version` aceita apenas `1` ou `2`.

## Códigos de resposta

| Código | Significado |
|---|---|
| `200 OK` | Consulta ou atualização realizada com sucesso |
| `201 Created` | Recurso criado (o cabeçalho `Location` aponta para ele) |
| `204 No Content` | Recurso removido com sucesso |
| `400 Bad Request` | Dados inválidos (validação, JSON malformado, parâmetro ou enum inválido, versão não suportada) |
| `401 Unauthorized` | Chave de API ausente, inválida ou revogada (header `X-API-Key`) |
| `403 Forbidden` | Chave válida, mas com nível de acesso insuficiente |
| `404 Not Found` | Recurso não encontrado (inclusive autor, categoria ou usuário informados em um cadastro) ou **página inexistente** (número maior que a última página) |
| `409 Conflict` | Valor duplicado (ISBN, e-mail, CPF...), registro com vínculos que impedem a exclusão ou requisição idempotente ainda em processamento |
| `413 Payload Too Large` | POST com `X-Idempotency-Key` e corpo maior que 100 KB |
| `415 Unsupported Media Type` | Corpo enviado em formato diferente de `application/json` |
| `422 Unprocessable Entity` | `X-Idempotency-Key` reutilizada com outro corpo ou outra rota |
| `429 Too Many Requests` | Limite de requisições por minuto excedido (veja o header `Retry-After`) |
| `500 Internal Server Error` | Erro inesperado no servidor |

## Versão 2: o que foi acrescentado

Esta é a **v2** da API: ela tem **tudo da v1** (CRUD, paginação, validação, HATEOAS e Swagger) e adiciona:

- **Chave de API:** `GET` em livros, autores e categorias é público. `POST`, `PUT` e `DELETE`, os dados pessoais (usuários e carteirinhas) e o grupo **Chaves de API** exigem o header `X-API-Key`. Clique em **Authorize**, cole sua chave e use os endpoints protegidos (cadeado). Níveis: `LEITURA` (lê usuários e carteirinhas), `ESCRITA` (cria e altera) e `ADMIN` (exclui e gerencia chaves).
- **Rate limit:** limite de requisições por minuto por IP (`429` + `Retry-After`; headers `X-RateLimit-*`).
- **CORS:** só as origens permitidas podem chamar a API pelo navegador.
- **Idempotência:** o header `X-Idempotency-Key` em POST evita criar o mesmo recurso duas vezes.
- **Versionamento:** o header `X-API-Version` escolhe a versão (`1` ou `2`; **padrão `2`**; outro valor retorna `400`). Com `X-API-Version: 1` a API funciona no modo da v1 (sem chave de API, rate limit, idempotência e CORS, e sem `/api-keys`). Em `GET /livros/{id}`, a v2 devolve o livro resumido, com o campo `disponivel`. Use o seletor **Versão da API**, no topo da página, para alternar entre a documentação da v1 e da v2.

## Por onde começar

1. Clique em **Authorize** e informe uma chave de API. Chaves de teste: `escrita-key-123456` (ESCRITA), `admin-key-123456` (ADMIN) e `leitura-key-123456` (LEITURA).
2. Cadastre um **Autor** (`POST /autores`).
3. Cadastre uma ou mais **Categorias** (`POST /categorias`).
4. Cadastre um **Livro** (`POST /livros`) com o `autorId` e os `categoriaIds`.
5. Cadastre um **Usuário** (`POST /usuarios`) e emita sua **Carteirinha** (`POST /carteirinhas`) com o `usuarioId`.
6. Consulte o livro com `GET /livros/{id}` usando `X-API-Version` `1` e `2`, e busque com `GET /livros/buscar?titulo=`.
7. Repita um POST com o mesmo `X-Idempotency-Key` para ver a resposta original ser reaproveitada.
""";
}
