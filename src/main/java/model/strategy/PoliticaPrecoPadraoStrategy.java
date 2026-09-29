package model.strategy;

import java.util.List;
import model.ItemServico;

/**
 * Padrão GoF: STRATEGY (Estratégia Composta Padrão).
 * Centraliza e orquestra a precificação oficial e unificada da Pousada Paradiso,
 * delegando cada regra de cálculo financeiro para suas respectivas estratégias:
 * 1. Subtotal de diárias brutas (noites * valorDiaria).
 * 2. Regra de Desconto Long-Stay (DescontoLongaEstadia).
 * 3. Totalização de serviços adicionais contratados.
 * 4. Regra de Taxa Ambiental de 3% (TaxaAmbiental).
 * 5. Regra de Desconto de 5% para PIX (DescontoPix).
 */
public class PoliticaPrecoPadraoStrategy implements PoliticaPrecoStrategy {

    private final DescontoLongaEstadia regraLongaEstadia;
    private final TaxaAmbiental regraTaxaAmbiental;
    private final DescontoPix regraDescontoPix;

    public PoliticaPrecoPadraoStrategy() {
        this.regraLongaEstadia = new DescontoLongaEstadia();
        this.regraTaxaAmbiental = new TaxaAmbiental();
        this.regraDescontoPix = new DescontoPix();
    }

    public PoliticaPrecoPadraoStrategy(DescontoLongaEstadia regraLongaEstadia,
                                       TaxaAmbiental regraTaxaAmbiental,
                                       DescontoPix regraDescontoPix) {
        this.regraLongaEstadia = (regraLongaEstadia != null) ? regraLongaEstadia : new DescontoLongaEstadia();
        this.regraTaxaAmbiental = (regraTaxaAmbiental != null) ? regraTaxaAmbiental : new TaxaAmbiental();
        this.regraDescontoPix = (regraDescontoPix != null) ? regraDescontoPix : new DescontoPix();
    }

    @Override
    public ResultadoCalculoPreco calcular(long noites, double valorDiaria, List<ItemServico> servicos, String formaPagamento) {
        if (noites <= 0) {
            noites = 1;
        }
        if (valorDiaria < 0) {
            valorDiaria = 0.0;
        }

        // 1. Subtotal das diárias brutas
        double subtotalDiarias = noites * valorDiaria;

        // 2. Desconto por tempo de estadia (Strategy)
        double percentualDescontoEstadia = regraLongaEstadia.getPercentual(noites);
        double valorDescontoEstadia = regraLongaEstadia.calcular(subtotalDiarias, noites, formaPagamento);
        double subtotalComDesconto = subtotalDiarias - valorDescontoEstadia;

        // 3. Serviços adicionais contratados (1:N)
        double totalServicos = 0.0;
        if (servicos != null) {
            for (ItemServico s : servicos) {
                if (s != null) {
                    totalServicos += s.getSubtotal();
                }
            }
        }

        // 4. Taxa de preservação ambiental da pousada (Strategy)
        double baseHospedagemEServicos = subtotalComDesconto + totalServicos;
        double taxaAmbiental = regraTaxaAmbiental.calcular(baseHospedagemEServicos, noites, formaPagamento);

        // Base total com incidência de taxas
        double baseComTaxas = baseHospedagemEServicos + taxaAmbiental;

        // 5. Desconto promocional para pagamento via PIX (Strategy)
        double percentualDescontoPix = regraDescontoPix.getPercentual(formaPagamento);
        double valorDescontoPix = regraDescontoPix.calcular(baseComTaxas, noites, formaPagamento);

        // Valor total final a pagar
        double valorTotalFinal = baseComTaxas - valorDescontoPix;

        return new ResultadoCalculoPreco(
                noites,
                valorDiaria,
                subtotalDiarias,
                percentualDescontoEstadia,
                valorDescontoEstadia,
                subtotalComDesconto,
                totalServicos,
                taxaAmbiental,
                percentualDescontoPix,
                valorDescontoPix,
                valorTotalFinal
        );
    }
}
