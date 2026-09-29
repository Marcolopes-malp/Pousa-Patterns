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

        // 1. Tenta conectar no MySQL (ambiente padrão ensinado na UMC)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String urlMySQL = "jdbc:mysql://localhost:3306/pousada_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            String userMySQL = "root";
            String passMySQL = "";
            con = DriverManager.getConnection(urlMySQL, userMySQL, passMySQL);
        } catch (Exception ex) {
            // 2. Fallback inteligente: H2 em arquivo local para permitir execução instantânea
            Class.forName("org.h2.Driver");
            String urlH2 = "jdbc:h2:./pousada_db;DB_CLOSE_DELAY=-1;MODE=MySQL";
            String userH2 = "sa";
            String passH2 = "";
            con = DriverManager.getConnection(urlH2, userH2, passH2);
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
                    "senha VARCHAR(100))");

            try {
                stmt.execute("ALTER TABLE hospedes ADD COLUMN IF NOT EXISTS senha VARCHAR(100) DEFAULT '123456'");
            } catch (Exception ignored) {}

            // Tabela de Reservas
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
                    "FOREIGN KEY (hospede_id) REFERENCES hospedes(id) ON DELETE SET NULL)");

            // Tabela de Serviços Adicionais
            stmt.execute("CREATE TABLE IF NOT EXISTS itens_servicos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "reserva_id INT NOT NULL, " +
                    "nome VARCHAR(100) NOT NULL, " +
                    "descricao VARCHAR(255), " +
                    "preco_unitario DOUBLE NOT NULL, " +
                    "quantidade INT NOT NULL, " +
                    "FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE)");

            // Inserção de dados iniciais
            var rs = stmt.executeQuery("SELECT count(*) FROM hospedes");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO hospedes (nome_completo, cpf, email, telefone, cidade_origem, senha) VALUES " +
                        "('Marco Antonio Lopes Pedro', '458.129.384-90', 'marco.pedro@pousada.com.br', '(11) 98765-4321', 'Mogi das Cruzes - SP', '123456'), " +
                        "('Mariana Silveira Ramos', '321.654.987-12', 'mariana.ramos@email.com', '(11) 97123-8899', 'São Paulo - SP', '123456'), " +
                        "('Lucas Henrique Prado', '876.543.210-55', 'lucas.prado@email.com', '(21) 99887-1122', 'Rio de Janeiro - RJ', '123456')");

                stmt.execute("INSERT INTO reservas (codigo_localizador, data_checkin, data_checkout, quantidade_hospedes, tipo_quarto, valor_diaria, valor_total, status, forma_pagamento, observacoes, data_criacao, hospede_id) VALUES " +
                        "('POUS-2026-X01', '2026-10-10', '2026-10-15', 2, 'Bangalô Vista Mar', 450.0, 2450.0, 'CONFIRMADA', 'PIX', 'Hóspedes em comemoração de aniversário.', '2026-09-28', 1), " +
                        "('POUS-2026-X02', '2026-11-01', '2026-11-04', 3, 'Chalé Família', 380.0, 1340.0, 'PENDENTE', 'CARTAO_CREDITO', 'Solicitou berço para bebê.', '2026-09-28', 2), " +
                        "('POUS-2026-X03', '2026-12-20', '2026-12-27', 2, 'Suíte Master', 520.0, 3940.0, 'CONFIRMADA', 'PIX', 'Check-in tardio previsto para 21h.', '2026-09-28', 3)");

                stmt.execute("INSERT INTO itens_servicos (reserva_id, nome, descricao, preco_unitario, quantidade) VALUES " +
                        "(1, 'Café Colonial na Cama', 'Buffet servido na varanda privativa', 65.0, 2), " +
                        "(1, 'Passeio de Escuna', 'Tour paradisíaco pelas ilhas', 120.0, 2), " +
                        "(2, 'Café Colonial na Cama', 'Buffet servido na varanda privativa', 65.0, 3), " +
                        "(3, 'Transfer Executivo', 'Translado privativo aeroporto ida e volta', 180.0, 1), " +
                        "(3, 'Massagem Terapêutica', 'Sessão individual com pedras quentes', 150.0, 1)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
