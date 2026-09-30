package util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Listener de ciclo de vida da aplicação (Servlet Context).
 * Inicializa de forma segura e thread-safe o schema do banco de dados
 * a partir de schema.sql na inicialização do servidor.
 */
@WebListener
public class DatabaseInitializerListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializerListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Inicializando contexto da aplicação e schema do banco de dados...");
        try {
            FabricaConexao.inicializarSchema();
            LOGGER.info("Banco de dados da Pousada Paradiso inicializado com sucesso.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar banco de dados no startup da aplicação", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Contexto da aplicação finalizado.");
    }
}
