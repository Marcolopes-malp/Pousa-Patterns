package model.strategy;

import java.util.List;
import model.ItemServico;

/**
 * Padrão de Projeto Comportamental: STRATEGY (GoF).
 * Define a interface comum para os algoritmos de cálculo de preços,
 * políticas de desconto de estadia (Long-Stay), taxas ambientais
 * e condições por forma de pagamento.
 */
public interface PoliticaPrecoStrategy {

    /**
     * Executa o cálculo unificado de preço e retorna o detalhamento financeiro.
     *
     * @param noites Quantidade de noites da hospedagem.
     * @param valorDiaria Valor da diária base da acomodação.
     * @param servicos Lista de serviços adicionais contratados.
     * @param formaPagamento Método de pagamento escolhido (PIX, CARTAO_CREDITO, etc.).
     * @return Instância de ResultadoCalculoPreco com todos os valores calculados.
     */
    ResultadoCalculoPreco calcular(long noites, double valorDiaria, List<ItemServico> servicos, String formaPagamento);
}
