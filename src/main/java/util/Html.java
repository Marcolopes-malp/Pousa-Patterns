package util;

/**
 * Utilitário de segurança para prevenção contra Cross-Site Scripting (XSS).
 * Higieniza saídas dinâmicas em páginas JSP, convertendo caracteres HTML especiais
 * em suas respectivas entidades numéricas/nomeadas seguras.
 */
public class Html {

    /**
     * Escapa caracteres perigosos para HTML (&, <, >, ", ').
     *
     * @param value Objeto ou texto a ser impresso na página.
     * @return String tratada e segura contra injeção de script.
     */
    public static String esc(Object value) {
        if (value == null) {
            return "";
        }
        String str = String.valueOf(value);
        StringBuilder sb = new StringBuilder(str.length() + 16);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#x27;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
