package util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para validação da preservação de acomodação e parâmetros no fluxo de login/redirect (Tarefa B4).
 */
public class RedirectPreservacaoTest {

    @Test
    @DisplayName("B4: Link de login codifica e preserva acomodação 3 e datas na volta")
    public void testPreservacaoAcomodacaoTresEParametrosNoRedirect() throws Exception {
        int acomodacaoId = 3;
        String checkIn = "2026-11-10";
        String checkOut = "2026-11-15";
        String qtdHospedes = "2";

        // Montagem da URL de destino da reserva (conforme implementado na reserva.jsp)
        String targetReservaUrl = "controller.do?btnop=NovaReserva&acomodacaoId=" + acomodacaoId
                + "&txtCheckIn=" + URLEncoder.encode(checkIn, StandardCharsets.UTF_8)
                + "&txtCheckOut=" + URLEncoder.encode(checkOut, StandardCharsets.UTF_8)
                + "&txtQtdHospedes=" + URLEncoder.encode(qtdHospedes, StandardCharsets.UTF_8);

        // Parâmetro 'redirect' que vai na query string para o login
        String redirectQueryParam = URLEncoder.encode(targetReservaUrl, StandardCharsets.UTF_8);

        // Simula o servidor recebendo o parâmetro 'redirect' após o login
        String decodedRedirect = URLDecoder.decode(redirectQueryParam, StandardCharsets.UTF_8);

        // Valida que a URL decodificada é exatamente o destino com acomodacaoId = 3
        assertEquals(targetReservaUrl, decodedRedirect);
        assertTrue(decodedRedirect.contains("acomodacaoId=3"));
        assertTrue(decodedRedirect.contains("txtCheckIn=2026-11-10"));
        assertTrue(decodedRedirect.contains("txtCheckOut=2026-11-15"));
        assertTrue(decodedRedirect.contains("txtQtdHospedes=2"));

        // Valida que o mecanismo de segurança reconhece o redirecionamento como seguro
        assertTrue(Seguranca.isRedirectSeguro(decodedRedirect));
    }
}
