package br.com.biblioteca.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Serve a página personalizada do Swagger UI (tema vinho + cinza escuro).
 * O conteúdo fica no próprio código para não depender de arquivos estáticos no classpath.
 */
@Hidden
@Controller
public class DocumentacaoController {

    @GetMapping(value = "/swagger-ui.html", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String pagina() {
        return HTML;
    }

    @GetMapping(value = "/swagger-ui-custom.css", produces = "text/css;charset=UTF-8")
    @ResponseBody
    public String css() {
        return CSS;
    }

    private static final String HTML = """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>API de Biblioteca By Bedeschi – Documentação</title>
  <link rel="stylesheet" href="/swagger-ui/swagger-ui.css">
  <link rel="stylesheet" href="/swagger-ui-custom.css">
</head>
<body>
  <header class="cabecalho">
    <div class="cabecalho__marca">
      <span class="cabecalho__icone">📚</span>
      <div>
        <h1>API de Biblioteca By Bedeschi</h1>
        <p>Acervo, leitores e carteirinhas · desenvolvido por Bedeschi</p>
      </div>
    </div>
    <div class="cabecalho__acoes">
      <label class="seletor" for="versao">Versão da API
        <select id="versao">
          <option value="v1">v1</option>
          <option value="v2" selected>v2</option>
        </select>
      </label>
      <span id="modo" class="modo"></span>
      <a id="linkJson" class="cabecalho__link" href="/v3/api-docs/v2" target="_blank" rel="noopener">OpenAPI JSON</a>
    </div>
  </header>

  <div id="swagger-ui"></div>

  <script src="/swagger-ui/swagger-ui-bundle.js"></script>
  <script>
    var versaoAtual = "v2";

    function carregar(versao) {
      versaoAtual = versao;
      var modo = document.getElementById("modo");
      if (versao === "v1") {
        modo.textContent = "🔓 v1 · rotas abertas (sem chave de API)";
        modo.className = "modo modo--v1";
      } else {
        modo.textContent = "🔒 v2 · chave de API exigida para escrever";
        modo.className = "modo modo--v2";
      }
      document.getElementById("linkJson").href = "/v3/api-docs/" + versao;
      window.ui = SwaggerUIBundle({
        url: "/v3/api-docs/" + versao,
        dom_id: "#swagger-ui",
        presets: [SwaggerUIBundle.presets.apis],
        layout: "BaseLayout",
        deepLinking: false,
        persistAuthorization: false,
        displayRequestDuration: true,
        filter: true,
        docExpansion: "list",
        defaultModelsExpandDepth: 0,
        tryItOutEnabled: true,
        requestInterceptor: function (req) {
          req.headers["X-API-Version"] = versaoAtual.replace("v", "");
          return req;
        }
      });
    }

    window.onload = function () {
      var seletor = document.getElementById("versao");
      seletor.value = "v2";
      seletor.addEventListener("change", function () {
        carregar(seletor.value);
      });
      carregar(seletor.value);
    };
  </script>
</body>
</html>
""";

    private static final String CSS = """
/* Tema da API de Biblioteca By Bedeschi: vermelho vinho (destaque) sobre cinza escuro (fundo) */
:root {
  --vinho: #7b1e3a;
  --vinho-escuro: #5a1429;
  --vinho-claro: #a8344f;
  --vinho-brilho: #d4607d;
  --fundo: #2a2a2d;
  --painel: #333337;
  --painel-2: #3b3b40;
  --codigo: #1f1f22;
  --borda: #4b4b52;
  --texto: #ececee;
  --texto-suave: #b9b9c0;
  --get: #4a86c5;
  --post: #3f9b72;
  --put: #c8923a;
  --delete: #d65a4f;
}

html, body { margin: 0; background: var(--fundo); color: var(--texto); }
body { font-family: "Segoe UI", system-ui, -apple-system, Roboto, sans-serif; }

/* ---------- cabeçalho ---------- */
.cabecalho {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 32px;
  background: linear-gradient(90deg, var(--vinho-escuro), var(--vinho));
  border-bottom: 3px solid var(--vinho-claro);
  box-shadow: 0 2px 12px rgba(0, 0, 0, .45);
}
.cabecalho__marca { display: flex; align-items: center; gap: 14px; }
.cabecalho__icone { font-size: 32px; }
.cabecalho h1 { margin: 0; font-size: 22px; color: #fff; letter-spacing: .3px; }
.cabecalho p { margin: 2px 0 0; font-size: 13px; color: #f0d5dc; }
.cabecalho__link {
  color: #fff; text-decoration: none; font-size: 13px; font-weight: 600;
  padding: 7px 14px; border: 1px solid rgba(255, 255, 255, .55); border-radius: 4px;
}
.cabecalho__link:hover { background: rgba(255, 255, 255, .15); }

/* ---------- base ---------- */
.swagger-ui, .swagger-ui .info .title, .swagger-ui .info p, .swagger-ui .info li, .swagger-ui .info table,
.swagger-ui .opblock-tag, .swagger-ui .opblock .opblock-summary-description, .swagger-ui .opblock-description-wrapper p,
.swagger-ui .opblock-external-docs-wrapper p, .swagger-ui .opblock-title_normal p, .swagger-ui .response-col_description,
.swagger-ui .response-col_status, .swagger-ui .responses-inner h4, .swagger-ui .responses-inner h5,
.swagger-ui .parameter__name, .swagger-ui .parameter__type, .swagger-ui .parameter__in, .swagger-ui .parameter__deprecated,
.swagger-ui table thead tr th, .swagger-ui table thead tr td, .swagger-ui .tab li, .swagger-ui label,
.swagger-ui .model, .swagger-ui .model-title, .swagger-ui .prop-type, .swagger-ui .prop-format,
.swagger-ui section.models h4, .swagger-ui .opblock-section-header h4, .swagger-ui .opblock-section-header label,
.swagger-ui .markdown p, .swagger-ui .markdown li, .swagger-ui .renderedMarkdown p, .swagger-ui .renderedMarkdown li,
.swagger-ui .dialog-ux .modal-ux-header h3, .swagger-ui .dialog-ux .modal-ux-content p,
.swagger-ui .dialog-ux .modal-ux-content h4, .swagger-ui .auth-container h4, .swagger-ui .auth-container h6,
.swagger-ui .scheme-container .schemes > label, .swagger-ui .servers > label, .swagger-ui .response-control-media-type__title,
.swagger-ui .model-box, .swagger-ui .opblock-summary-path, .swagger-ui .opblock-summary-path__deprecated {
  color: var(--texto);
}
.swagger-ui .parameter__name.required span, .swagger-ui table.model tr.required td.property-type { color: var(--vinho-brilho); }
.swagger-ui .parameter__name.required::after { color: var(--vinho-brilho); }
.swagger-ui .markdown code, .swagger-ui .renderedMarkdown code {
  background: var(--codigo); color: #f2c2cf; padding: 1px 5px; border-radius: 3px;
}
.swagger-ui a, .swagger-ui .info a, .swagger-ui .info .base-url { color: var(--vinho-brilho); }
.swagger-ui .info .title small { background: var(--vinho); }
.swagger-ui .info .title small.version-stamp { background: var(--vinho-claro); }
.swagger-ui .wrapper { max-width: 1280px; }
.swagger-ui .info { margin: 36px 0 20px; }

/* ---------- barra de autorização ---------- */
.swagger-ui .scheme-container { background: var(--painel); box-shadow: none; border-bottom: 1px solid var(--borda); }
.swagger-ui .btn { border-radius: 4px; box-shadow: none; }
.swagger-ui .btn.authorize { color: var(--vinho-brilho); border-color: var(--vinho-brilho); background: transparent; }
.swagger-ui .btn.authorize svg { fill: var(--vinho-brilho); }
.swagger-ui .btn.authorize:hover { background: var(--vinho); color: #fff; border-color: var(--vinho); }
.swagger-ui .btn.authorize:hover svg { fill: #fff; }
.swagger-ui .btn.execute { background: var(--vinho); border-color: var(--vinho); color: #fff; }
.swagger-ui .btn.execute:hover { background: var(--vinho-claro); border-color: var(--vinho-claro); }
.swagger-ui .btn.try-out__btn { color: var(--texto); border-color: var(--vinho-brilho); background: transparent; }
.swagger-ui .btn.try-out__btn:hover { background: var(--vinho); border-color: var(--vinho); }
.swagger-ui .btn.cancel { color: var(--delete); border-color: var(--delete); background: transparent; }
.swagger-ui .btn.btn-clear { color: var(--texto); border-color: var(--borda); background: transparent; }
.swagger-ui .btn.modal-btn-auth { background: var(--vinho); border-color: var(--vinho); color: #fff; }
.swagger-ui .authorization__btn svg { fill: var(--vinho-brilho); }

/* ---------- tags e operações ---------- */
.swagger-ui .opblock-tag { border-bottom: 1px solid var(--borda); }
.swagger-ui .opblock-tag:hover { background: rgba(123, 30, 58, .25); }
.swagger-ui .opblock-tag small { color: var(--texto-suave); }
.swagger-ui .opblock-tag svg, .swagger-ui .expand-operation svg, .swagger-ui .model-toggle::after { fill: var(--texto); }

.swagger-ui .opblock { background: rgba(0, 0, 0, .18); border-width: 1px; border-radius: 6px; box-shadow: none; }
.swagger-ui .opblock .opblock-summary { border-bottom: 0; }
.swagger-ui .opblock .opblock-section-header { background: var(--painel); box-shadow: none; }
.swagger-ui .opblock-body pre.microlight { background: var(--codigo) !important; color: #e8e8ea !important; }
.swagger-ui .opblock .opblock-summary-method { border-radius: 4px; color: #fff; text-shadow: none; }

.swagger-ui .opblock.opblock-get { border-color: var(--get); background: rgba(74, 134, 197, .10); }
.swagger-ui .opblock.opblock-get .opblock-summary-method, .swagger-ui .tab li:first-of-type::after { background: var(--get); }
.swagger-ui .opblock.opblock-post { border-color: var(--post); background: rgba(63, 155, 114, .10); }
.swagger-ui .opblock.opblock-post .opblock-summary-method { background: var(--post); }
.swagger-ui .opblock.opblock-put { border-color: var(--put); background: rgba(200, 146, 58, .10); }
.swagger-ui .opblock.opblock-put .opblock-summary-method { background: var(--put); }
.swagger-ui .opblock.opblock-delete { border-color: var(--delete); background: rgba(214, 90, 79, .10); }
.swagger-ui .opblock.opblock-delete .opblock-summary-method { background: var(--delete); }
.swagger-ui .opblock.opblock-get .opblock-summary,
.swagger-ui .opblock.opblock-post .opblock-summary,
.swagger-ui .opblock.opblock-put .opblock-summary,
.swagger-ui .opblock.opblock-delete .opblock-summary { border-color: inherit; }

/* ---------- tabelas, campos, respostas ---------- */
.swagger-ui table thead tr th, .swagger-ui table thead tr td { border-bottom: 1px solid var(--borda); }
.swagger-ui .responses-table .response .response-col_status { color: var(--vinho-brilho); font-weight: 700; }
.swagger-ui .response-col_links { color: var(--texto-suave); }
.swagger-ui .responses-inner { background: transparent; }
.swagger-ui .parameters-col_description input[type=text], .swagger-ui input[type=text], .swagger-ui input[type=password],
.swagger-ui input[type=search], .swagger-ui input[type=email], .swagger-ui input[type=file], .swagger-ui textarea, .swagger-ui select {
  background: var(--fundo); color: var(--texto); border: 1px solid var(--borda); border-radius: 4px;
}
.swagger-ui input[type=text]:focus, .swagger-ui textarea:focus, .swagger-ui select:focus { outline: 2px solid var(--vinho-claro); }
.swagger-ui textarea.body-param__text { background: var(--codigo); color: #e8e8ea; }
.swagger-ui .highlight-code > .microlight, .swagger-ui .microlight { background: var(--codigo) !important; color: #e8e8ea !important; }
.swagger-ui .copy-to-clipboard { background: var(--vinho); }
.swagger-ui .tab li button.tablinks { color: var(--texto); }
.swagger-ui .tab li:first-of-type::after { background: var(--vinho-claro); }
.swagger-ui .filter .operation-filter-input { background: var(--painel); color: var(--texto); border: 2px solid var(--vinho); }
.swagger-ui .loading-container .loading::after { color: var(--texto); }

/* ---------- modelos (schemas) ---------- */
.swagger-ui section.models { border: 1px solid var(--borda); background: transparent; }
.swagger-ui section.models.is-open h4 { border-bottom: 1px solid var(--borda); }
.swagger-ui section.models .model-container { background: rgba(0, 0, 0, .22); }
.swagger-ui section.models .model-container:hover { background: rgba(123, 30, 58, .25); }
.swagger-ui .model-box { background: transparent; }
.swagger-ui .model .property.primitive { color: var(--texto-suave); }

/* ---------- janela de autorização ---------- */
.swagger-ui .dialog-ux .backdrop-ux { background: rgba(0, 0, 0, .65); }
.swagger-ui .dialog-ux .modal-ux { background: var(--painel); border: 1px solid var(--vinho); box-shadow: 0 10px 40px rgba(0, 0, 0, .6); }
.swagger-ui .dialog-ux .modal-ux-header { border-bottom: 1px solid var(--borda); }
.swagger-ui .dialog-ux .modal-ux-header .close-modal svg { fill: var(--texto); }

/* ---------- barra de rolagem ---------- */
* { scrollbar-color: var(--vinho) var(--fundo); }
::-webkit-scrollbar { width: 11px; height: 11px; }
::-webkit-scrollbar-track { background: var(--fundo); }
::-webkit-scrollbar-thumb { background: var(--vinho); border-radius: 6px; }
::-webkit-scrollbar-thumb:hover { background: var(--vinho-claro); }

@media (max-width: 700px) {
  .cabecalho { padding: 12px 16px; }
  .cabecalho p { display: none; }
}

/* ---------- descrição da API (markdown) ---------- */
.swagger-ui .info .renderedMarkdown h1, .swagger-ui .info .renderedMarkdown h2, .swagger-ui .info .renderedMarkdown h3,
.swagger-ui .info h1, .swagger-ui .info h2, .swagger-ui .info h3 { color: var(--vinho-brilho); font-family: inherit; margin: 26px 0 10px; }
.swagger-ui .info h1 { font-size: 24px; }
.swagger-ui .info h2 { font-size: 21px; border-bottom: 1px solid var(--borda); padding-bottom: 6px; }
.swagger-ui .info table { border-collapse: collapse; width: 100%; margin: 10px 0 18px; font-size: 14px; }
.swagger-ui .info table th { background: var(--vinho); color: #fff; text-align: left; padding: 8px 12px; border: 1px solid var(--vinho-escuro); }
.swagger-ui .info table td { padding: 8px 12px; border: 1px solid var(--borda); background: rgba(0, 0, 0, .15); vertical-align: top; }
.swagger-ui .info table tr:nth-child(even) td { background: rgba(255, 255, 255, .03); }
.swagger-ui .info ul, .swagger-ui .info ol { padding-left: 26px; line-height: 1.6; }

/* ---------- seletor de versão ---------- */
.cabecalho__acoes { display: flex; align-items: center; gap: 14px; }
.seletor { display: flex; align-items: center; gap: 8px; color: #f6dfe6; font-size: 13px; font-weight: 600; }
.seletor select {
  background: var(--fundo); color: #fff; border: 1px solid rgba(255, 255, 255, .55);
  border-radius: 4px; padding: 6px 10px; font-size: 14px; font-weight: 700; cursor: pointer;
}
.seletor select:hover, .seletor select:focus { border-color: #fff; outline: none; }

.modo { font-size: 12px; font-weight: 700; padding: 6px 10px; border-radius: 4px; white-space: nowrap; }
.modo--v1 { background: #c8923a; color: #1f1f22; }
.modo--v2 { background: rgba(255, 255, 255, .16); color: #fff; border: 1px solid rgba(255, 255, 255, .45); }
""";
}
