package br.com.commandfactory.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Padrão de Projeto GoF: FACTORY METHOD (CommandFactory).
 * Registro centralizado e explícito de todos os Commands suportados pela aplicação.
 * Substitui o despacho reflexivo dinâmico por um mapeamento tipado, seguro e determinístico (Tarefa A2).
 */
public class CommandFactory {

    private static final Map<String, Supplier<ICommand>> COMANDOS;

    static {
        Map<String, Supplier<ICommand>> map = new HashMap<>();

        // Navegação e Catálogo
        map.put("consultatodos", ConsultaTodosReservaAction::new);
        map.put("novareserva", NovaReservaAction::new);
        map.put("consultabyid", ConsultaByIdReservaAction::new);

        // Operações de Reserva
        map.put("cadastra", CadastraReservaAction::new);
        map.put("edita", EditaReservaAction::new);
        map.put("atualiza", AtualizaReservaAction::new);
        map.put("deleta", DeletaReservaAction::new);
        map.put("processarcheckinautomatico", ProcessarCheckInAutomaticoReservaAction::new);

        // Área do Cliente
        map.put("minhasreservas", MinhasReservasAction::new);
        map.put("login", LoginReservaAction::new);
        map.put("logincliente", LoginClienteAction::new);
        map.put("cadastro", CadastroReservaAction::new);
        map.put("cadastracliente", CadastraClienteAction::new);
        map.put("logoutcliente", LogoutClienteAction::new);

        // Painel Administrativo / Recepção
        map.put("admin", AdminReservaAction::new);
        map.put("cadastromanual", CadastroManualReservaAction::new);

        COMANDOS = Collections.unmodifiableMap(map);
    }

    /**
     * Obtém uma nova instância do Command correspondente à ação solicitada.
     *
     * @param nomeAcao Nome do comando enviado por parâmetro na requisição.
     * @return Instância de ICommand ou null caso o comando não seja reconhecido.
     */
    public static ICommand criarComando(String nomeAcao) {
        if (nomeAcao == null || nomeAcao.trim().isEmpty()) {
            return new ConsultaTodosReservaAction();
        }
        Supplier<ICommand> supplier = COMANDOS.get(nomeAcao.trim().toLowerCase());
        return (supplier != null) ? supplier.get() : null;
    }

    /**
     * Verifica se o comando informado está registrado na fábrica.
     */
    public static boolean existeComando(String nomeAcao) {
        if (nomeAcao == null) return false;
        return COMANDOS.containsKey(nomeAcao.trim().toLowerCase());
    }
}
