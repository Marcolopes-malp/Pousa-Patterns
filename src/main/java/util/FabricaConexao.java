package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Padrão Factory para gerenciamento centralizado de conexões JDBC.
 * Conforme apresentado nas Aulas 04 e 07.
 * Suporta MySQL e fallback automático para H2 embutido para garantir
 * que a aplicação funcione em qualquer máquina sem falhas de conexão.
 */
public class FabricaConexao {

    private static boolean tabelasInicializadas = false;

    public static Connection getConexao() throws ClassNotFoundException, SQLException {
        Connection con = null;

        // 1. Variáveis de Ambiente (12-Factor App / Produção / Containers)
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASS");

        if (envUrl != null && !envUrl.trim().isEmpty()) {
            try {
                if (envUrl.contains("mysql")) {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } else if (envUrl.contains("h2")) {
                    Class.forName("org.h2.Driver");
                }
                String user = (envUser != null) ? envUser : "";
                String pass = (envPass != null) ? envPass : "";
                con = DriverManager.getConnection(envUrl.trim(), user, pass);
            } catch (Exception ignored) {}
        }

        // 2. MySQL Local (ambiente padrão acadêmico)
        if (con == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String urlMySQL = "jdbc:mysql://localhost:3306/pousada_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                String userMySQL = "root";
                String passMySQL = "";
                con = DriverManager.getConnection(urlMySQL, userMySQL, passMySQL);
            } catch (Exception ex) {
                // 3. Fallback inteligente: H2 em arquivo local para permitir execução instantânea
                Class.forName("org.h2.Driver");
                String urlH2 = "jdbc:h2:./pousada_db;DB_CLOSE_DELAY=-1;MODE=MySQL";
                String userH2 = "sa";
                String passH2 = "";
                con = DriverManager.getConnection(urlH2, userH2, passH2);
            }
        }

        if (!tabelasInicializadas) {
            inicializarBanco(con);
            tabelasInicializadas = true;
        }

        return con;
    }

    /**
     * Inicializa automaticamente as tabelas e dados semente de demonstração.
     */
    private static synchronized void inicializarBanco(Connection con) {
        try (Statement stmt = con.createStatement()) {
            // Tabela de Hóspedes / Usuários
            stmt.execute("CREATE TABLE IF NOT EXISTS hospedes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome_completo VARCHAR(150) NOT NULL, " +
                    "cpf VARCHAR(20) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "telefone VARCHAR(30) NOT NULL, " +
                    "cidade_origem VARCHAR(100), " +
                    "senha VARCHAR(255), " +
                    "perfil VARCHAR(20) DEFAULT 'CLIENTE')");

            try {
                stmt.execute("ALTER TABLE hospedes ADD COLUMN IF NOT EXISTS senha VARCHAR(255)");
            } catch (Exception ignored) {}
            try {
                stmt.execute("ALTER TABLE hospedes ADD COLUMN IF NOT EXISTS perfil VARCHAR(20) DEFAULT 'CLIENTE'");
            } catch (Exception ignored) {}

            String hashAdmin = Seguranca.gerarHashSenha("admin123");
            String hashCliente = Seguranca.gerarHashSenha("123456");

            // Migração transparente de senhas legadas em texto plano para hashes PBKDF2
            try {
                stmt.execute("UPDATE hospedes SET senha = '" + hashAdmin + "' WHERE senha = 'admin123'");
                stmt.execute("UPDATE hospedes SET senha = '" + hashCliente + "' WHERE senha = '123456'");
            } catch (Exception ignored) {}

            // Tabela de Acomodações (B6)
            stmt.execute("CREATE TABLE IF NOT EXISTS acomodacoes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(100) NOT NULL UNIQUE, " +
                    "tipo VARCHAR(50) NOT NULL, " +
                    "descricao VARCHAR(500), " +
                    "capacidade_pessoas INT NOT NULL, " +
                    "valor_diaria DOUBLE NOT NULL, " +
                    "vagas_restantes INT NOT NULL, " +
                    "avaliacao DOUBLE NOT NULL, " +
                    "total_avaliacoes INT NOT NULL, " +
                    "imagem_url VARCHAR(255), " +
                    "comodidades VARCHAR(255))");

            // Tabela de Reservas com FK/vínculo com acomodacao_id (B6)
            stmt.execute("CREATE TABLE IF NOT EXISTS reservas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "codigo_localizador VARCHAR(30) NOT NULL UNIQUE, " +
                    "data_checkin VARCHAR(20) NOT NULL, " +
                    "data_checkout VARCHAR(20) NOT NULL, " +
                    "quantidade_hospedes INT NOT NULL, " +
                    "tipo_quarto VARCHAR(80) NOT NULL, " +
                    "valor_diaria DOUBLE NOT NULL, " +
                    "valor_total DOUBLE NOT NULL, " +
                    "status VARCHAR(30) NOT NULL, " +
                    "forma_pagamento VARCHAR(50) NOT NULL, " +
                    "observacoes VARCHAR(500), " +
                    "data_criacao VARCHAR(30) NOT NULL, " +
                    "hospede_id INT, " +
                    "acomodacao_id INT, " +
                    "FOREIGN KEY (hospede_id) REFERENCES hospedes(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (acomodacao_id) REFERENCES acomodacoes(id) ON DELETE SET NULL)");

            try {
                stmt.execute("ALTER TABLE reservas ADD COLUMN IF NOT EXISTS acomodacao_id INT");
            } catch (Exception ignored) {}

            // Tabela de Serviços Adicionais
            stmt.execute("CREATE TABLE IF NOT EXISTS itens_servicos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "reserva_id INT NOT NULL, " +
                    "nome VARCHAR(100) NOT NULL, " +
                    "descricao VARCHAR(255), " +
                    "preco_unitario DOUBLE NOT NULL, " +
                    "quantidade INT NOT NULL, " +
                    "FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE)");

            // Inserção de acomodações padrão no banco de dados (B6)
            var rsAcom = stmt.executeQuery("SELECT count(*) FROM acomodacoes");
            if (rsAcom.next() && rsAcom.getInt(1) == 0) {
                stmt.execute("INSERT INTO acomodacoes (id, nome, tipo, descricao, capacidade_pessoas, valor_diaria, vagas_restantes, avaliacao, total_avaliacoes, imagem_url, comodidades) VALUES " +
                        "(1, 'Bangalô Vista Mar & Deck Privativo', 'Bangalô', 'Bangalô exclusivo situado a 30 metros da areia, com varanda panorâmica, rede de descanso e banheira de hidromassagem externa.', 2, 450.0, 2, 4.97, 48, 'https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&w=800&q=80', 'Wi-Fi • Hidro • Vista Mar • Café incluso • Ar Split'), " +
                        "(2, 'Suíte Master com Hidro & Lareira', 'Suíte', 'Ampla suíte com cama super king, banheira de hidromassagem dupla cromoterápica, lareira ecológica e vista para a mata nativa preservada.', 2, 520.0, 1, 4.92, 35, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80', 'Wi-Fi • Hidro Dupla • Lareira • Cama Super King • Frigobar Retrô'), " +
                        "(3, 'Chalé Família nas Palmeiras', 'Chalé', 'Espaço aconchegante de dois pavimentos cercado por coqueiros e jardim tropical. Perfeito para famílias com crianças.', 4, 380.0, 3, 4.88, 42, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80', 'Wi-Fi • Cozinha Compacta • 2 Quartos • Deck com Churrasqueira • Estacionamento'), " +
                        "(4, 'Suíte Standard Jardim Colonial', 'Suíte', 'Acomodação confortável e silenciosa no casarão colonial principal, com piso em madeira de demolição e vista para o jardim interno.', 2, 250.0, 4, 4.85, 29, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80', 'Wi-Fi • Ar Split • Cama Queen • Chuveiro a Gás • Mesa de Trabalho')");
            }

            // Inserção de dados iniciais
            var rs = stmt.executeQuery("SELECT count(*) FROM hospedes");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO hospedes (nome_completo, cpf, email, telefone, cidade_origem, senha, perfil) VALUES " +
                        "('Recepção Pousada Paradiso', '000.000.000-00', 'recepcao@pousada.com.br', '(11) 3333-4444', 'Mogi das Cruzes - SP', '" + hashAdmin + "', 'RECEPCAO'), " +
                        "('Marco Antonio Lopes Pedro', '458.129.384-90', 'marco.pedro@pousada.com.br', '(11) 98765-4321', 'Mogi das Cruzes - SP', '" + hashCliente + "', 'CLIENTE'), " +
                        "('Mariana Silveira Ramos', '321.654.987-12', 'mariana.ramos@email.com', '(11) 97123-8899', 'São Paulo - SP', '" + hashCliente + "', 'CLIENTE'), " +
                        "('Lucas Henrique Prado', '876.543.210-55', 'lucas.prado@email.com', '(21) 99887-1122', 'Rio de Janeiro - RJ', '" + hashCliente + "', 'CLIENTE')");

                stmt.execute("INSERT INTO reservas (codigo_localizador, data_checkin, data_checkout, quantidade_hospedes, tipo_quarto, valor_diaria, valor_total, status, forma_pagamento, observacoes, data_criacao, hospede_id, acomodacao_id) VALUES " +
                        "('POUS-2026-X01', '2026-10-10', '2026-10-15', 2, 'Bangalô Vista Mar & Deck Privativo', 450.0, 2450.0, 'CONFIRMADA', 'PIX', 'Hóspedes em comemoração de aniversário.', '2026-09-28', 1, 1), " +
                        "('POUS-2026-X02', '2026-11-01', '2026-11-04', 3, 'Chalé Família nas Palmeiras', 380.0, 1340.0, 'PENDENTE', 'CARTAO_CREDITO', 'Solicitou berço para bebê.', '2026-09-28', 2, 3), " +
                        "('POUS-2026-X03', '2026-12-20', '2026-12-27', 2, 'Suíte Master com Hidro & Lareira', 520.0, 3940.0, 'CONFIRMADA', 'PIX', 'Check-in tardio previsto para 21h.', '2026-09-28', 3, 2)");

                stmt.execute("INSERT INTO itens_servicos (reserva_id, nome, descricao, preco_unitario, quantidade) VALUES " +
                        "(1, 'Café Colonial na Cama', 'Buffet servido na varanda privativa', 65.0, 2), " +
                        "(1, 'Passeio de Escuna', 'Tour paradisíaco pelas ilhas', 120.0, 2), " +
                        "(2, 'Café Colonial na Cama', 'Buffet servido na varanda privativa', 65.0, 3), " +
                        "(3, 'Transfer Executivo', 'Translado privativo aeroporto ida e volta', 180.0, 1), " +
                        "(3, 'Massagem Terapêutica', 'Sessão individual com pedras quentes', 150.0, 1)");
            }

            // Sincroniza acomodacao_id e nomes de registros legados caso existam
            try {
                stmt.execute("UPDATE reservas SET acomodacao_id = 1, tipo_quarto = 'Bangalô Vista Mar & Deck Privativo' WHERE (acomodacao_id IS NULL OR acomodacao_id = 0) AND tipo_quarto LIKE '%Bangalô%'");
                stmt.execute("UPDATE reservas SET acomodacao_id = 2, tipo_quarto = 'Suíte Master com Hidro & Lareira' WHERE (acomodacao_id IS NULL OR acomodacao_id = 0) AND tipo_quarto LIKE '%Master%'");
                stmt.execute("UPDATE reservas SET acomodacao_id = 3, tipo_quarto = 'Chalé Família nas Palmeiras' WHERE (acomodacao_id IS NULL OR acomodacao_id = 0) AND tipo_quarto LIKE '%Chalé%'");
                stmt.execute("UPDATE reservas SET acomodacao_id = 4, tipo_quarto = 'Suíte Standard Jardim Colonial' WHERE (acomodacao_id IS NULL OR acomodacao_id = 0)");
            } catch (Exception ignored) {}

            try {
                stmt.execute("INSERT INTO hospedes (nome_completo, cpf, email, telefone, cidade_origem, senha, perfil) " +
                        "SELECT 'Recepção Pousada Paradiso', '000.000.000-00', 'recepcao@pousada.com.br', '(11) 3333-4444', 'Mogi das Cruzes - SP', '" + hashAdmin + "', 'RECEPCAO' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM hospedes WHERE email = 'recepcao@pousada.com.br')");
            } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
