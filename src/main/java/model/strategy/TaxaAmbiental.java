package model.strategy;

/**
 * Padrão GoF: STRATEGY (Estratégia Concreta).
 * Regra da Taxa de Preservação Ambiental da Pousada:
 * Aplica 3% sobre a base consolidada de hospedagem (diárias líquidas + serviços adicionais).
 */
public class TaxaAmbiental implements RegraTarifa {

    public static final double PERCENTUAL = 0.03; // 3%

    @Override
    public double calcular(double baseHospedagemEServicos, long noites, String formaPagamento) {
        if (baseHospedagemEServicos <= 0) {
            return 0.0;
        }
        return baseHospedagemEServicos * PERCENTUAL;
    }

    public double getPercentual() {
        return PERCENTUAL;
    }
}
