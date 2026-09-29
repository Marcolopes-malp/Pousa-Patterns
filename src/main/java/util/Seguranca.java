package util;

/**
 * Utilitário de segurança para a aplicação Pousada Paradiso.
 * Fornece validações contra vulnerabilidades comuns da web como Open Redirect,
 * CRLF Injection e suporte para operações criptográficas seguras.
 */
public class Seguranca {

    /**
     * Valida se uma URL de redirecionamento é estritamente interna e segura,
     * prevenindo ataques de Open Redirect e HTTP Response Splitting.
     *
     * @param url URL recebida por parâmetro (ex: redirect).
     * @return true se o destino for um caminho interno válido do Front Controller; false caso contrário.
     */
    public static boolean isRedirectSeguro(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        String clean = url.trim();

        // Rejeita tentativas de CRLF injection (quebras de linha no cabeçalho HTTP)
        if (clean.indexOf('\r') != -1 || clean.indexOf('\n') != -1) {
            return false;
        }

        // Rejeita esquemas absolutos (http:, https:, javascript:, data:, etc.)
        if (clean.contains(":") || clean.contains("\\")) {
            return false;
        }

        // Rejeita URLs relativas a protocolo (//malicious-site.com)
        if (clean.startsWith("//")) {
            return false;
        }

        // Garante que o redirecionamento aponte estritamente para o Front Controller da aplicação
        if (clean.equals("controller.do") || clean.startsWith("controller.do?")
                || clean.equals("/controller.do") || clean.startsWith("/controller.do?")) {
            return true;
        }

        return false;
    }

    /**
     * Obtém uma URL de redirecionamento segura. Caso a URL fornecida não seja
     * válida ou seja suspeita, retorna a URL padrão de fallback.
     *
     * @param url URL alvo fornecida pelo cliente.
     * @param fallback URL segura de destino padrão.
     * @return URL validada para redirecionamento.
     */
    public static String obterRedirectSeguro(String url, String fallback) {
        if (isRedirectSeguro(url)) {
            return url.trim();
        }
        return fallback;
    }
}
