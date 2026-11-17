
INSERT INTO autor (nome, nacionalidade, data_nascimento, versao) VALUES ('Machado de Assis', 'Brasileira', '1839-06-21', 'v2');
INSERT INTO autor (nome, nacionalidade, data_nascimento, versao) VALUES ('Clarice Lispector', 'Brasileira', '1920-12-10', 'v2');

INSERT INTO categoria (nome, descricao, versao) VALUES ('Romance', 'Obras de ficção narrativa', 'v2');
INSERT INTO categoria (nome, descricao, versao) VALUES ('Clássicos', 'Obras consagradas da literatura', 'v2');

INSERT INTO livro (titulo, isbn, paginas, ano_publicacao, status, autor_id, versao)
VALUES ('Dom Casmurro', '9788535914849', 256, 1899, 'DISPONIVEL', (SELECT id FROM autor WHERE nome = 'Machado de Assis' AND versao = 'v2'), 'v2');
INSERT INTO livro (titulo, isbn, paginas, ano_publicacao, status, autor_id, versao)
VALUES ('A Hora da Estrela', '9788532508126', 88, 1977, 'EMPRESTADO', (SELECT id FROM autor WHERE nome = 'Clarice Lispector' AND versao = 'v2'), 'v2');

INSERT INTO livro_categoria (livro_id, categoria_id)
VALUES ((SELECT id FROM livro WHERE isbn = '9788535914849' AND versao = 'v2'), (SELECT id FROM categoria WHERE nome = 'Romance' AND versao = 'v2'));
INSERT INTO livro_categoria (livro_id, categoria_id)
VALUES ((SELECT id FROM livro WHERE isbn = '9788535914849' AND versao = 'v2'), (SELECT id FROM categoria WHERE nome = 'Clássicos' AND versao = 'v2'));
INSERT INTO livro_categoria (livro_id, categoria_id)
VALUES ((SELECT id FROM livro WHERE isbn = '9788532508126' AND versao = 'v2'), (SELECT id FROM categoria WHERE nome = 'Romance' AND versao = 'v2'));

INSERT INTO usuario (nome, email, cpf, versao) VALUES ('Maria Silva', 'maria@email.com', '12345678901', 'v2');

INSERT INTO carteirinha (numero, data_emissao, data_validade, status, usuario_id, versao)
VALUES ('CART-0001', '2026-01-10', '2028-01-10', 'ATIVA', (SELECT id FROM usuario WHERE email = 'maria@email.com' AND versao = 'v2'), 'v2');

INSERT INTO autor (nome, nacionalidade, data_nascimento, versao) VALUES ('Machado de Assis', 'Brasileira', '1839-06-21', 'v1');
INSERT INTO autor (nome, nacionalidade, data_nascimento, versao) VALUES ('Clarice Lispector', 'Brasileira', '1920-12-10', 'v1');

INSERT INTO categoria (nome, descricao, versao) VALUES ('Romance', 'Obras de ficção narrativa', 'v1');
INSERT INTO categoria (nome, descricao, versao) VALUES ('Clássicos', 'Obras consagradas da literatura', 'v1');

INSERT INTO livro (titulo, isbn, paginas, ano_publicacao, status, autor_id, versao)
VALUES ('Dom Casmurro', '9788535914849', 256, 1899, 'DISPONIVEL', (SELECT id FROM autor WHERE nome = 'Machado de Assis' AND versao = 'v1'), 'v1');
INSERT INTO livro (titulo, isbn, paginas, ano_publicacao, status, autor_id, versao)
VALUES ('A Hora da Estrela', '9788532508126', 88, 1977, 'EMPRESTADO', (SELECT id FROM autor WHERE nome = 'Clarice Lispector' AND versao = 'v1'), 'v1');

INSERT INTO livro_categoria (livro_id, categoria_id)
VALUES ((SELECT id FROM livro WHERE isbn = '9788535914849' AND versao = 'v1'), (SELECT id FROM categoria WHERE nome = 'Romance' AND versao = 'v1'));
INSERT INTO livro_categoria (livro_id, categoria_id)
VALUES ((SELECT id FROM livro WHERE isbn = '9788535914849' AND versao = 'v1'), (SELECT id FROM categoria WHERE nome = 'Clássicos' AND versao = 'v1'));
INSERT INTO livro_categoria (livro_id, categoria_id)
VALUES ((SELECT id FROM livro WHERE isbn = '9788532508126' AND versao = 'v1'), (SELECT id FROM categoria WHERE nome = 'Romance' AND versao = 'v1'));

INSERT INTO usuario (nome, email, cpf, versao) VALUES ('Maria Silva', 'maria@email.com', '12345678901', 'v1');

INSERT INTO carteirinha (numero, data_emissao, data_validade, status, usuario_id, versao)
VALUES ('CART-0001', '2026-01-10', '2028-01-10', 'ATIVA', (SELECT id FROM usuario WHERE email = 'maria@email.com' AND versao = 'v1'), 'v1');
