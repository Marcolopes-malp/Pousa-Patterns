package model.strategy;

/**
 * DTO que encapsula o detalhamento financeiro do cálculo de uma reserva.
 * Utilizado pelo padrão STRATEGY para transportar os componentes do preço:
 * diárias brutas, descontos aplicados, serviços, taxas e valor final.
 */
public class ResultadoCalculoPreco {

    private final long noites;
    private final double valorDiaria;
    private final double subtotalDiarias;
    private final double percentualDescontoEstadia;
    private final double valorDescontoEstadia;
    private final double subtotalComDesconto;
    private final double totalServicos;
    private final double taxaAmbiental;
    private final double percentualDescontoPix;
    private final double valorDescontoPix;
    private final double valorTotalFinal;

    public ResultadoCalculoPreco(long noites, double valorDiaria, double subtotalDiarias,
                                 double percentualDescontoEstadia, double valorDescontoEstadia,
                                 double subtotalComDesconto, double totalServicos,
                                 double taxaAmbiental, double percentualDescontoPix,
                                 double valorDescontoPix, double valorTotalFinal) {
        this.noites = noites;
        this.valorDiaria = arredondar(valorDiaria);
        this.subtotalDiarias = arredondar(subtotalDiarias);
        this.percentualDescontoEstadia = percentualDescontoEstadia;
        this.valorDescontoEstadia = arredondar(valorDescontoEstadia);
        this.subtotalComDesconto = arredondar(subtotalComDesconto);
        this.totalServicos = arredondar(totalServicos);
        this.taxaAmbiental = arredondar(taxaAmbiental);
        this.percentualDescontoPix = percentualDescontoPix;
        this.valorDescontoPix = arredondar(valorDescontoPix);
        this.valorTotalFinal = arredondar(valorTotalFinal);
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public long getNoites() {
        return noites;
    }

    public double getValorDiaria() {
        return valorDiaria;
    }

    public double getSubtotalDiarias() {
        return subtotalDiarias;
    }

    public double getPercentualDescontoEstadia() {
        return percentualDescontoEstadia;
    }

    public double getValorDescontoEstadia() {
        return valorDescontoEstadia;
    }

    public double getSubtotalComDesconto() {
        return subtotalComDesconto;
    }

    public double getTotalServicos() {
        return totalServicos;
    }

    public double getTaxaAmbiental() {
        return taxaAmbiental;
    }

    public double getPercentualDescontoPix() {
        return percentualDescontoPix;
    }

    public double getValorDescontoPix() {
        return valorDescontoPix;
    }

    public double getValorTotalFinal() {
        return valorTotalFinal;
    }

    // Alias getters conforme especificação ResumoTarifa
    public double getDescontoEstadia() {
        return valorDescontoEstadia;
    }

    public double getDescontoPix() {
        return valorDescontoPix;
    }

    public double getTotal() {
        return valorTotalFinal;
    }
}
