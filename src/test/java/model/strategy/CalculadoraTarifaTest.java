package model.strategy;

import java.util.ArrayList;
import java.util.List;
import model.ItemServico;
import model.factory.ServicoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.CalculadoraTarifa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para validação da unificação das regras de precificação (Tarefa B1).
 * Cobre:
 * - 1 noite (sem desconto de estadia), 4 noites (10% de desconto) e 7 noites (15% de desconto).
 * - Pagamento com PIX (5% de desconto) e Cartão de Crédito (preço cheio).
 * - Cenários com e sem serviços adicionais.
 * - Caso canônico do Bangalô (R$ 450, 5 noites, PIX, sem serviços).
 */
public class CalculadoraTarifaTest {

    private CalculadoraPreco calculadora;
    private CalculadoraTarifa servicoTarifa;

    @BeforeEach
    public void setUp() {
        calculadora = new CalculadoraPreco();
        servicoTarifa = new CalculadoraTarifa();
    }

    @Test
    @DisplayName("B1: Caso Canônico Bangalô - R$ 450, 5 noites, PIX, sem serviços")
    public void testExemploBangaloPixSemServicos() {
        long noites = 5;
        double valorDiaria = 450.0;
        List<ItemServico> servicos = new ArrayList<>();
        String formaPagamento = "PIX";

        ResultadoCalculoPreco resultado = calculadora.calcular(noites, valorDiaria, servicos, formaPagamento);

        // Subtotal diárias: 5 * 450 = 2250.00
        assertEquals(2250.00, resultado.getSubtotalDiarias(), 0.01);
        // 5 noites está na faixa >= 4 noites: 10% de desconto = 225.00
        assertEquals(0.10, resultado.getPercentualDescontoEstadia(), 0.001);
        assertEquals(225.00, resultado.getValorDescontoEstadia(), 0.01);
        assertEquals(2025.00, resultado.getSubtotalComDesconto(), 0.01);
        // Sem serviços
        assertEquals(0.00, resultado.getTotalServicos(), 0.01);
        // Taxa Ambiental (3% de 2025.00) = 60.75
        assertEquals(60.75, resultado.getTaxaAmbiental(), 0.01);
        // Base com taxas: 2025 + 60.75 = 2085.75
        // Desconto PIX (5% de 2085.75) = 104.29
        assertEquals(104.29, resultado.getValorDescontoPix(), 0.01);
        // Total final: 2085.75 - 104.29 = 1981.46
        assertEquals(1981.46, resultado.getValorTotalFinal(), 0.01);
    }

    @Test
    @DisplayName("1 Noite - Sem desconto de estadia, Cartão de Crédito, Sem serviços")
    public void testUmaNoiteCartaoSemServicos() {
        ResultadoCalculoPreco res = calculadora.calcular(1, 250.0, null, "CARTAO_CREDITO");

        assertEquals(250.00, res.getSubtotalDiarias(), 0.01);
        assertEquals(0.00, res.getValorDescontoEstadia(), 0.01);
        assertEquals(0.00, res.getTotalServicos(), 0.01);
        assertEquals(7.50, res.getTaxaAmbiental(), 0.01); // 3% de 250.00
        assertEquals(0.00, res.getValorDescontoPix(), 0.01); // Cartão não recebe desconto
        assertEquals(257.50, res.getValorTotalFinal(), 0.01);
    }

    @Test
    @DisplayName("1 Noite - Sem desconto de estadia, PIX, Com serviços (Café + Transfer)")
    public void testUmaNoitePixComServicos() {
        List<ItemServico> servicos = new ArrayList<>();
        servicos.add(ServicoFactory.obterFabrica("cafe").criarServico(1)); // 65.00
        servicos.add(ServicoFactory.obterFabrica("transfer").criarServico()); // 180.00

        ResultadoCalculoPreco res = calculadora.calcular(1, 250.0, servicos, "PIX");

        assertEquals(250.00, res.getSubtotalDiarias(), 0.01);
        assertEquals(0.00, res.getValorDescontoEstadia(), 0.01);
        assertEquals(245.00, res.getTotalServicos(), 0.01); // 65 + 180
        // Base = 250 + 245 = 495.00
        assertEquals(14.85, res.getTaxaAmbiental(), 0.01); // 3% de 495
        // Base com taxas = 509.85
        assertEquals(25.49, res.getValorDescontoPix(), 0.01); // 5% de 509.85
        assertEquals(484.36, res.getValorTotalFinal(), 0.01);
    }

    @Test
    @DisplayName("4 Noites - Limiar de 10% Long-Stay, Cartão de Crédito, Sem serviços")
    public void testQuatroNoitesCartaoSemServicos() {
        ResultadoCalculoPreco res = calculadora.calcular(4, 300.0, null, "CARTAO_CREDITO");

        assertEquals(1200.00, res.getSubtotalDiarias(), 0.01);
        assertEquals(0.10, res.getPercentualDescontoEstadia(), 0.001);
        assertEquals(120.00, res.getValorDescontoEstadia(), 0.01);
        assertEquals(1080.00, res.getSubtotalComDesconto(), 0.01);
        assertEquals(32.40, res.getTaxaAmbiental(), 0.01); // 3% de 1080
        assertEquals(0.00, res.getValorDescontoPix(), 0.01);
        assertEquals(1112.40, res.getValorTotalFinal(), 0.01);
    }

    @Test
    @DisplayName("4 Noites - Limiar de 10% Long-Stay, PIX, Com serviços (Café x 4 + Passeio x 2)")
    public void testQuatroNoitesPixComServicos() {
        List<ItemServico> servicos = new ArrayList<>();
        servicos.add(ServicoFactory.obterFabrica("cafe").criarServico(4)); // 65 * 4 = 260
        servicos.add(ServicoFactory.obterFabrica("passeio").criarServico(2)); // 120 * 2 = 240

        ResultadoCalculoPreco res = calculadora.calcular(4, 300.0, servicos, "PIX");

        assertEquals(1200.00, res.getSubtotalDiarias(), 0.01);
        assertEquals(120.00, res.getValorDescontoEstadia(), 0.01);
        assertEquals(1080.00, res.getSubtotalComDesconto(), 0.01);
        assertEquals(500.00, res.getTotalServicos(), 0.01); // 260 + 240
        // Base = 1080 + 500 = 1580.00
        assertEquals(47.40, res.getTaxaAmbiental(), 0.01); // 3% de 1580
        // Base com taxas = 1627.40
        assertEquals(81.37, res.getValorDescontoPix(), 0.01); // 5% de 1627.40
        assertEquals(1546.03, res.getValorTotalFinal(), 0.01);
    }

    @Test
    @DisplayName("7 Noites - Limiar de 15% Long-Stay, Cartão de Crédito, Sem serviços")
    public void testSeteNoitesCartaoSemServicos() {
        ResultadoCalculoPreco res = calculadora.calcular(7, 200.0, null, "CARTAO_CREDITO");

        assertEquals(1400.00, res.getSubtotalDiarias(), 0.01);
        assertEquals(0.15, res.getPercentualDescontoEstadia(), 0.001);
        assertEquals(210.00, res.getValorDescontoEstadia(), 0.01);
        assertEquals(1190.00, res.getSubtotalComDesconto(), 0.01);
        assertEquals(35.70, res.getTaxaAmbiental(), 0.01); // 3% de 1190
        assertEquals(0.00, res.getValorDescontoPix(), 0.01);
        assertEquals(1225.70, res.getValorTotalFinal(), 0.01);
    }

    @Test
    @DisplayName("7 Noites - Limiar de 15% Long-Stay, PIX, Com serviços (Transfer + Spa)")
    public void testSeteNoitesPixComServicos() {
        List<ItemServico> servicos = new ArrayList<>();
        servicos.add(ServicoFactory.obterFabrica("transfer").criarServico()); // 180.00
        servicos.add(ServicoFactory.obterFabrica("spa").criarServico()); // 150.00

        ResultadoCalculoPreco res = calculadora.calcular(7, 200.0, servicos, "PIX");

        assertEquals(1400.00, res.getSubtotalDiarias(), 0.01);
        assertEquals(210.00, res.getValorDescontoEstadia(), 0.01);
        assertEquals(1190.00, res.getSubtotalComDesconto(), 0.01);
        assertEquals(330.00, res.getTotalServicos(), 0.01); // 180 + 150
        // Base = 1190 + 330 = 1520.00
        assertEquals(45.60, res.getTaxaAmbiental(), 0.01); // 3% de 1520
        // Base com taxas = 1565.60
        assertEquals(78.28, res.getValorDescontoPix(), 0.01); // 5% de 1565.60
        assertEquals(1487.32, res.getValorTotalFinal(), 0.01);
    }

    @Test
    @DisplayName("CalculadoraTarifa (Camada Service) produz resultado idêntico à Strategy")
    public void testCalculadoraTarifaServiceEquivalencia() {
        ResultadoCalculoPreco resService = servicoTarifa.calcular(300.0, "2026-10-10", "2026-10-14", 2, null, "PIX");
        ResultadoCalculoPreco resStrategy = calculadora.calcular(4, 300.0, null, "PIX");

        assertEquals(resStrategy.getValorTotalFinal(), resService.getValorTotalFinal(), 0.01);
        assertEquals(resStrategy.getDescontoEstadia(), resService.getDescontoEstadia(), 0.01);
        assertEquals(resStrategy.getTaxaAmbiental(), resService.getTaxaAmbiental(), 0.01);
        assertEquals(resStrategy.getDescontoPix(), resService.getDescontoPix(), 0.01);
    }
}
