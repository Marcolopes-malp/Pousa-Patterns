package model.strategy;

/**
 * Padrão GoF: STRATEGY (Estratégia Concreta).
 * Regra de Bonificação por Pagamento Instantâneo via PIX:
 * Aplica 5% de desconto promocional sobre o valor total consolidado com taxas.
 */
public class DescontoPix implements RegraTarifa {

    public static final double PERCENTUAL = 0.05; // 5%

    @Override
    public double calcular(double baseComTaxas, long noites, String formaPagamento) {
        if (formaPagamento != null && "PIX".equalsIgnoreCase(formaPagamento.trim())) {
            return baseComTaxas * PERCENTUAL;
        }
        return 0.0;
    }

    public double getPercentual(String formaPagamento) {
        if (formaPagamento != null && "PIX".equalsIgnoreCase(formaPagamento.trim())) {
            return PERCENTUAL;
        }
        return 0.0;
    }
}
