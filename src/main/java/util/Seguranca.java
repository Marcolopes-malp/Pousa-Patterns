package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilitário de segurança para a aplicação Pousada Paradiso.
 * Fornece:
 * 1. Validações contra Open Redirect e CRLF Injection.
 * 2. Hashing forte de senhas usando PBKDF2WithHmacSHA256 (sem dependências externas).
 * 3. Comparação em tempo constante para mitigação de timing attacks.
 */
public class Seguranca {

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 10000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;

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

    /**
     * Gera um hash criptograficamente seguro para a senha utilizando PBKDF2WithHmacSHA256
     * com salt aleatório de 128 bits e 10.000 iterações.
     *
     * Formato retornado: PBKDF2$10000$saltBase64$hashBase64
     *
     * @param senha Senha em texto puro a ser criptografada.
     * @return String codificada com algoritmo, iterações, salt e hash.
     */
    public static String gerarHashSenha(String senha) {
        if (senha == null) {
            return null;
        }
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);

            byte[] hash = pbkdf2(senha.toCharArray(), salt, ITERATIONS, HASH_BYTES);

            Base64.Encoder enc = Base64.getEncoder();
            return "PBKDF2$" + ITERATIONS + "$" + enc.encodeToString(salt) + "$" + enc.encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash de senha com PBKDF2", e);
        }
    }

    /**
     * Verifica se a senha informada corresponde ao hash armazenado.
     * Suporta senhas salvas em PBKDF2 e fornece compatibilidade retroativa
     * para senhas legadas em texto plano (permitindo rehash automático).
     *
     * Utiliza MessageDigest.isEqual para prevenção contra ataques de temporização (timing attacks).
     *
     * @param senhaInformada Senha em texto puro digitada pelo usuário.
     * @param hashArmazenado Hash (ou texto puro legado) salvo no banco de dados.
     * @return true se a senha for válida; false caso contrário.
     */
    public static boolean verificarSenha(String senhaInformada, String hashArmazenado) {
        if (senhaInformada == null || hashArmazenado == null) {
            return false;
        }

        // Se o hash armazenado estiver no padrão PBKDF2
        if (hashArmazenado.startsWith("PBKDF2$")) {
            try {
                String[] partes = hashArmazenado.split("\\$");
                if (partes.length != 4) {
                    return false;
                }
                int iter = Integer.parseInt(partes[1]);
                byte[] salt = Base64.getDecoder().decode(partes[2]);
                byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);

                byte[] hashCalculado = pbkdf2(senhaInformada.toCharArray(), salt, iter, hashEsperado.length);

                return MessageDigest.isEqual(hashEsperado, hashCalculado);
            } catch (Exception e) {
                return false;
            }
        }

        // Fallback para senhas legadas em texto plano (migração transparente)
        return MessageDigest.isEqual(senhaInformada.getBytes(), hashArmazenado.getBytes());
    }

    private static byte[] pbkdf2(char[] senha, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(senha, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Gera um PIN numérico de 4 dígitos criptograficamente seguro para fechaduras digitais (Smart-Lock),
     * utilizando java.security.SecureRandom para eliminar previsibilidade.
     *
     * @return PIN de 4 dígitos entre 1000 e 9999.
     */
    public static int gerarPinFechaduraSeguro() {
        SecureRandom random = new SecureRandom();
        return 1000 + random.nextInt(9000);
    }
}
