package model.strategy;

/**
 * Padrão GoF: STRATEGY (Estratégia Concreta).
 * Regra de Desconto por Longa Estadia (Fidelidade / Long-Stay):
 * - A partir de 7 noites: 15% de desconto sobre o subtotal de diárias.
 * - A partir de 4 noites: 10% de desconto sobre o subtotal de diárias.
 * - Menos de 4 noites: sem desconto (0%).
 */
public class DescontoLongaEstadia implements RegraTarifa {

    @Override
    public double calcular(double subtotalDiarias, long noites, String formaPagamento) {
        double percentual = getPercentual(noites);
        return subtotalDiarias * percentual;
    }

    public double getPercentual(long noites) {
        if (noites >= 7) {
            return 0.15;
        } else if (noites >= 4) {
            return 0.10;
        }
        return 0.0;
    }
}
