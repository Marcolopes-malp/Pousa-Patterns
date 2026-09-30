package model;

/**
 * Enumeração das Formas de Pagamento aceitas pela Pousada Paradiso (Tarefa A4).
 */
public enum FormaPagamento {
    PIX("PIX", "PIX (5% de desconto)", 0.05),
    CARTAO_CREDITO("CARTAO_CREDITO", "Cartão de Crédito", 0.0),
    TRANSFERENCIA("TRANSFERENCIA", "Transferência Bancária", 0.0),
    DINHEIRO("DINHEIRO", "Dinheiro em Espécie", 0.0);

    private final String codigo;
    private final String descricao;
    private final double percentualDesconto;

    FormaPagamento(String codigo, String descricao, double percentualDesconto) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.percentualDesconto = percentualDesconto;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getPercentualDesconto() {
        return percentualDesconto;
    }

    public static FormaPagamento fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return PIX;
        }
        String v = valor.trim().toUpperCase().replace(" ", "_");
        for (FormaPagamento fp : values()) {
            if (fp.name().equalsIgnoreCase(v) || fp.getCodigo().equalsIgnoreCase(v)) {
                return fp;
            }
        }
        return PIX;
    }
}
