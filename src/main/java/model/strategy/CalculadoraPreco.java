package model.strategy;

import java.util.List;
import model.ItemServico;

/**
 * Contexto (Context) do Padrão STRATEGY.
 * Mantém uma referência para a estratégia de precificação ativa e delega
 * a execução dos cálculos, permitindo a substituição transparente de algoritmos
 * (ex: preço de alta temporada, pacotes corporativos, feriados prolongados).
 */
public class CalculadoraPreco {

    private PoliticaPrecoStrategy strategy;

    public CalculadoraPreco() {
        this(new PoliticaPrecoPadraoStrategy());
    }

    public CalculadoraPreco(PoliticaPrecoStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(PoliticaPrecoStrategy strategy) {
        if (strategy != null) {
            this.strategy = strategy;
        }
    }

    public PoliticaPrecoStrategy getStrategy() {
        return strategy;
    }

    public ResultadoCalculoPreco calcular(long noites, double valorDiaria, List<ItemServico> servicos, String formaPagamento) {
        return this.strategy.calcular(noites, valorDiaria, servicos, formaPagamento);
    }
}
