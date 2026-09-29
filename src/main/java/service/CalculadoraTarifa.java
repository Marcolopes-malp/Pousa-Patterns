package service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import model.ItemServico;
import model.strategy.CalculadoraPreco;
import model.strategy.PoliticaPrecoStrategy;
import model.strategy.ResultadoCalculoPreco;

/**
 * Camada de Serviço de Tarifação da Pousada.
 * Fornece interface amigável para cálculo de diárias e reservas,
 * integrando datas de check-in/check-out e delegando a precificação ao padrão STRATEGY.
 */
public class CalculadoraTarifa {

    private final CalculadoraPreco calculadoraPreco;

    public CalculadoraTarifa() {
        this.calculadoraPreco = new CalculadoraPreco();
    }

    public CalculadoraTarifa(PoliticaPrecoStrategy strategy) {
        this.calculadoraPreco = new CalculadoraPreco(strategy);
    }

    /**
     * Calcula o resumo tarifário completo a partir dos parâmetros de reserva.
     */
    public ResultadoCalculoPreco calcular(double valorDiaria, String checkIn, String checkOut,
                                         int qtdHospedes, List<ItemServico> servicos, String formaPagamento) {
        long noites = 1;
        try {
            LocalDate dtIn = LocalDate.parse(checkIn);
            LocalDate dtOut = LocalDate.parse(checkOut);
            noites = ChronoUnit.DAYS.between(dtIn, dtOut);
            if (noites <= 0) {
                noites = 1;
            }
        } catch (Exception ignored) {
            noites = 1;
        }

        return calculadoraPreco.calcular(noites, valorDiaria, servicos, formaPagamento);
    }

    public ResultadoCalculoPreco calcular(long noites, double valorDiaria, List<ItemServico> servicos, String formaPagamento) {
        return calculadoraPreco.calcular(noites, valorDiaria, servicos, formaPagamento);
    }
}
