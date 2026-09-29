package model.strategy;

/**
 * Padrão GoF: STRATEGY.
 * Interface base para as regras de tarifação, acréscimos e descontos aplicados
 * sobre o valor de hospedagem na Pousada Paradiso.
 */
public interface RegraTarifa {

    /**
     * Calcula o valor da regra financeira (desconto ou taxa).
     *
     * @param baseDeCalculo valor monetário base sobre o qual a regra incide
     * @param noites total de noites de hospedagem
     * @param formaPagamento método de pagamento selecionado (ex: "PIX", "CARTAO_CREDITO")
     * @return o valor calculado em reais (R$) a ser somado ou deduzido
     */
    double calcular(double baseDeCalculo, long noites, String formaPagamento);
}
