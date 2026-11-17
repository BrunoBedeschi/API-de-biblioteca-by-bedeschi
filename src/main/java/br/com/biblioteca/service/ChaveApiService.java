package br.com.biblioteca.service;

import br.com.biblioteca.dto.ChaveApiRequest;
import br.com.biblioteca.exception.ConflitoException;
import br.com.biblioteca.exception.RecursoNaoEncontradoException;
import br.com.biblioteca.model.ChaveApi;
import br.com.biblioteca.model.NivelAcesso;
import br.com.biblioteca.model.Usuario;
import br.com.biblioteca.repository.ChaveApiRepository;
import br.com.biblioteca.repository.UsuarioRepository;
import br.com.biblioteca.util.Hash;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class ChaveApiService {

    /** Chave em texto puro + entidade salva. O texto puro só existe neste momento. */
    public record ChaveCriada(ChaveApi chaveApi, String chave) {}

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ChaveApiRepository repository;
    private final UsuarioRepository usuarioRepository;

    public ChaveApiService(ChaveApiRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    public static String gerarChave() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return "bib_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String chave) {
        return Hash.sha256Hex(chave.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public ChaveCriada criar(ChaveApiRequest request) {
        Usuario usuario = null;
        if (request.usuarioId() != null) {
            usuario = usuarioRepository.findById(request.usuarioId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", request.usuarioId()));
        }
        String chave = gerarChave();
        ChaveApi entidade = montar(request.nome(), request.nivel(), chave);
        entidade.setUsuario(usuario);
        return new ChaveCriada(repository.save(entidade), chave);
    }

    @Transactional(readOnly = true)
    public ChaveApi buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Chave de API", id));
    }

    @Transactional
    public ChaveCriada rotacionar(Long id) {
        ChaveApi entidade = buscar(id);
        if (!entidade.isAtiva()) {
            throw new ConflitoException("Chave revogada não pode ser rotacionada. Gere uma nova chave.");
        }
        String chave = gerarChave();
        entidade.setChaveHash(hash(chave));
        entidade.setPrefixo(chave.substring(0, 11));
        return new ChaveCriada(repository.save(entidade), chave);
    }

    @Transactional
    public void revogar(Long id) {
        ChaveApi entidade = buscar(id);
        entidade.setAtiva(false);
        repository.save(entidade);
    }

    /** Retorna a chave correspondente somente se existir e estiver ativa. */
    @Transactional(readOnly = true)
    public Optional<ChaveApi> autenticar(String chavePlana) {
        return repository.findByChaveHash(hash(chavePlana)).filter(ChaveApi::isAtiva);
    }

    /** Usado na inicialização para criar chaves de teste. */
    @Transactional
    public void semear(String nome, NivelAcesso nivel, String chavePlana) {
        if (!repository.existsByChaveHash(hash(chavePlana))) {
            repository.save(montar(nome, nivel, chavePlana));
        }
    }

    private ChaveApi montar(String nome, NivelAcesso nivel, String chave) {
        ChaveApi c = new ChaveApi();
        c.setNome(nome);
        c.setNivel(nivel);
        c.setChaveHash(hash(chave));
        c.setPrefixo(chave.length() > 11 ? chave.substring(0, 11) : chave);
        return c;
    }
}
