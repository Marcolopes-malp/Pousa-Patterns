package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testes unitários para Tipos Fortes (Tarefa A4): FormaPagamento, LocalDate e BigDecimal.
 */
public class FormaPagamentoTest {

    @Test
    @DisplayName("A4: Enum FormaPagamento faz parsing seguro e mapeia descontos")
    public void testFormaPagamentoEnum() {
        assertEquals(FormaPagamento.PIX, FormaPagamento.fromString("PIX"));
        assertEquals(0.05, FormaPagamento.PIX.getPercentualDesconto());

        assertEquals(FormaPagamento.CARTAO_CREDITO, FormaPagamento.fromString("CARTAO_CREDITO"));
        assertEquals(FormaPagamento.CARTAO_CREDITO, FormaPagamento.fromString("cartao credito"));
        assertEquals(0.0, FormaPagamento.CARTAO_CREDITO.getPercentualDesconto());

        // Fallback seguro para nulo ou inválido
        assertEquals(FormaPagamento.PIX, FormaPagamento.fromString(null));
        assertEquals(FormaPagamento.PIX, FormaPagamento.fromString("desconhecido"));
    }

    @Test
    @DisplayName("A4: Reserva oferece métodos tipados LocalDate, BigDecimal e Enums")
    public void testReservaMetodosTipados() {
        LocalDate in = LocalDate.of(2026, 12, 1);
        LocalDate out = LocalDate.of(2026, 12, 5);

        Reserva r = ReservaBuilder.novo()
                .comCodigoLocalizador("LOC-TIPADO")
                .comPeriodo(in, out)
                .comStatus(StatusReserva.CONFIRMADA)
                .comFormaPagamento(FormaPagamento.PIX)
                .comValorDiaria(500.0)
                .comValorTotal(2000.0)
                .constroi();

        assertEquals(in, r.getCheckInLocalDate());
        assertEquals(out, r.getCheckOutLocalDate());
        assertEquals(StatusReserva.CONFIRMADA, r.getStatusEnum());
        assertEquals(FormaPagamento.PIX, r.getFormaPagamentoEnum());

        BigDecimal diaria = r.getValorDiariaBigDecimal();
        assertNotNull(diaria);
        assertEquals(0, BigDecimal.valueOf(500.0).compareTo(diaria));

        BigDecimal total = r.getValorTotalBigDecimal();
        assertNotNull(total);
        assertEquals(0, BigDecimal.valueOf(2000.0).compareTo(total));
    }
}
