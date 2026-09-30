-- ========================================================
-- Schema DDL e Seeds Iniciais - Pousada Paradiso (Pousa-Patterns)
-- Compatível com H2 (MODE=MySQL) e MySQL 8.x
-- ========================================================

CREATE TABLE IF NOT EXISTS hospedes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefone VARCHAR(30) NOT NULL,
    cidade_origem VARCHAR(100),
    senha VARCHAR(255),
    perfil VARCHAR(20) DEFAULT 'CLIENTE'
);

CREATE TABLE IF NOT EXISTS acomodacoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    tipo VARCHAR(50) NOT NULL,
    descricao VARCHAR(500),
    capacidade_pessoas INT NOT NULL,
    valor_diaria DOUBLE NOT NULL,
    vagas_restantes INT NOT NULL,
    avaliacao DOUBLE NOT NULL,
    total_avaliacoes INT NOT NULL,
    imagem_url VARCHAR(255),
    comodidades VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS reservas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_localizador VARCHAR(30) NOT NULL UNIQUE,
    data_checkin VARCHAR(20) NOT NULL,
    data_checkout VARCHAR(20) NOT NULL,
    quantidade_hospedes INT NOT NULL,
    tipo_quarto VARCHAR(80) NOT NULL,
    valor_diaria DOUBLE NOT NULL,
    valor_total DOUBLE NOT NULL,
    status VARCHAR(30) NOT NULL,
    forma_pagamento VARCHAR(50) NOT NULL,
    observacoes VARCHAR(500),
    data_criacao VARCHAR(30) NOT NULL,
    hospede_id INT,
    acomodacao_id INT,
    FOREIGN KEY (hospede_id) REFERENCES hospedes(id) ON DELETE SET NULL,
    FOREIGN KEY (acomodacao_id) REFERENCES acomodacoes(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS itens_servicos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reserva_id INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    preco_unitario DOUBLE NOT NULL,
    quantidade INT NOT NULL,
    FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE
);
