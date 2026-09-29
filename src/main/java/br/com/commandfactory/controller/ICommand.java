package br.com.commandfactory.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Padrão de Projeto Comportamental: COMMAND.
 * Interface que encapsula a execução de cada requisição do sistema em um objeto.
 * Conforme ensinado na Aula 07.
 */
public interface ICommand {
    /**
     * Executa a regra de negócio da ação e retorna a página JSP para encaminhamento (forward).
     *
     * @param request  Requisição HTTP
     * @param response Resposta HTTP
     * @return Nome da página JSP de destino
     * @throws Exception Em caso de erros de negócio ou persistência
     */
    String executar(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
