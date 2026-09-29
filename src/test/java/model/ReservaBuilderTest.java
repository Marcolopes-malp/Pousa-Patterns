package model;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para validação de datas e capacidades em ReservaBuilder (Tarefa B3).
 */
public class ReservaBuilderTest {

    @Test
    @DisplayName("B3: Sucesso ao construir reserva com período válido e futuro")
    public void testConstroiReservaValida() {
        String dtIn = LocalDate.now().plusDays(2).toString();
        String dtOut = LocalDate.now().plusDays(5).toString();

        Reserva reserva = ReservaBuilder.novo()
                .comPeriodo(dtIn, dtOut)
                .comQuantidadeHospedes(2)
                .comTipoQuarto("Suíte Master")
                .comValorDiaria(350.0)
                .constroi();

        assertNotNull(reserva);
        assertEquals(dtIn, reserva.getDataCheckIn());
        assertEquals(dtOut, reserva.getDataCheckOut());
        assertEquals(2, reserva.getQuantidadeHospedes());
        assertTrue(reserva.getValorTotal() > 0.0);
    }

    @Test
    @DisplayName("B3: Lança exceção quando check-out é anterior ou igual ao check-in")
    public void testCheckOutAnteriorOuIgualAoCheckIn() {
        String dtIn = LocalDate.now().plusDays(5).toString();
        String dtOutAnterior = LocalDate.now().plusDays(3).toString();
        String dtOutIgual = LocalDate.now().plusDays(5).toString();

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            ReservaBuilder.novo()
                    .comPeriodo(dtIn, dtOutAnterior)
                    .comQuantidadeHospedes(2)
                    .constroi();
        });
        assertTrue(ex1.getMessage().contains("posterior"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            ReservaBuilder.novo()
                    .comPeriodo(dtIn, dtOutIgual)
                    .comQuantidadeHospedes(2)
                    .constroi();
        });
        assertTrue(ex2.getMessage().contains("posterior"));
    }

    @Test
    @DisplayName("B3: Lança exceção quando check-in de nova reserva está no passado")
    public void testCheckInNoPassadoNovaReserva() {
        String dtInPassado = LocalDate.now().minusDays(2).toString();
        String dtOutFuturo = LocalDate.now().plusDays(3).toString();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ReservaBuilder.novo()
                    .comPeriodo(dtInPassado, dtOutFuturo)
                    .comQuantidadeHospedes(2)
                    .constroi();
        });
        assertTrue(ex.getMessage().contains("passado"));
    }

    @Test
    @DisplayName("B3: Lança exceção quando quantidade de hóspedes é zero ou negativa")
    public void testQuantidadeHospedesInvalida() {
        String dtIn = LocalDate.now().plusDays(1).toString();
        String dtOut = LocalDate.now().plusDays(3).toString();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ReservaBuilder.novo()
                    .comPeriodo(dtIn, dtOut)
                    .comQuantidadeHospedes(0)
                    .constroi();
        });
        assertTrue(ex.getMessage().contains("pelo menos 1 pessoa"));
    }

    @Test
    @DisplayName("B3: Lança exceção quando quantidade de hóspedes excede capacidade máxima da acomodação")
    public void testQuantidadeHospedesExcedeCapacidade() {
        String dtIn = LocalDate.now().plusDays(1).toString();
        String dtOut = LocalDate.now().plusDays(3).toString();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ReservaBuilder.novo()
                    .comPeriodo(dtIn, dtOut)
                    .comCapacidadeMaxima(2)
                    .comQuantidadeHospedes(5)
                    .constroi();
        });
        assertTrue(ex.getMessage().contains("capacidade máxima"));
    }

    @Test
    @DisplayName("B3: Reserva existente com id > 0 permite leitura com datas passadas")
    public void testReservaExistentePermiteDatasHistoricas() {
        String dtInHistorica = "2020-01-10";
        String dtOutHistorica = "2020-01-15";

        assertDoesNotThrow(() -> {
            ReservaBuilder.novo()
                    .comId(99)
                    .comPeriodo(dtInHistorica, dtOutHistorica)
                    .comQuantidadeHospedes(2)
                    .constroi();
        });
    }
}
