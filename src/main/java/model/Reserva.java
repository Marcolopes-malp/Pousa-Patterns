package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Principal de Informação: Reserva de Pousada.
 * Possui 11 atributos de informação diretos (exigência: no mínimo 10 atributos),
 * além do relacionamento 1:1 com Hospede e 1:N com ItemServico.
 */
public class Reserva implements Serializable {
    private static final long serialVersionUID = 1L;

    // 11 Atributos principais da Entidade Reserva:
    private int id;                          // 1. Identificador único no banco
    private String codigoLocalizador;        // 2. Código alfanumérico (ex: RES-2026-X89)
    private String dataCheckIn;              // 3. Data de entrada (formato YYYY-MM-DD)
    private String dataCheckOut;             // 4. Data de saída (formato YYYY-MM-DD)
    private int quantidadeHospedes;          // 5. Quantidade de ocupantes
    private String tipoQuarto;               // 6. Suíte Master, Bangalô Mar, Chalé Família, etc.
    private double valorDiaria;              // 7. Preço base da diária
    private double valorTotal;               // 8. Valor consolidado calculado
    private String status;                   // 9. PENDENTE, CONFIRMADA, CHECKIN_ATIVO, FINALIZADA, CANCELADA
    private String formaPagamento;           // 10. PIX, CARTAO_CREDITO, DINHEIRO, TRANSFERENCIA
    private String observacoes;              // 11. Pedidos especiais ou restrições do hóspede

    // Atributos de Auditoria e Relacionamentos:
    private String dataCriacao;              // Data e hora de geração do registro
    private Hospede hospede;                 // Relacionamento 1:1 (Hóspede Titular)
    private List<ItemServico> servicos;      // Relacionamento 1:N (Serviços Adicionais Contratados)

    public Reserva() {
        this.servicos = new ArrayList<>();
    }

    public Reserva(int id, String codigoLocalizador, String dataCheckIn, String dataCheckOut,
                   int quantidadeHospedes, String tipoQuarto, double valorDiaria, double valorTotal,
                   String status, String formaPagamento, String observacoes, String dataCriacao,
                   Hospede hospede, List<ItemServico> servicos) {
        this.id = id;
        this.codigoLocalizador = codigoLocalizador;
        this.dataCheckIn = dataCheckIn;
        this.dataCheckOut = dataCheckOut;
        this.quantidadeHospedes = quantidadeHospedes;
        this.tipoQuarto = tipoQuarto;
        this.valorDiaria = valorDiaria;
        this.valorTotal = valorTotal;
        this.status = status;
        this.formaPagamento = formaPagamento;
        this.observacoes = observacoes;
        this.dataCriacao = dataCriacao;
        this.hospede = hospede;
        this.servicos = servicos != null ? servicos : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoLocalizador() {
        return codigoLocalizador;
    }

    public void setCodigoLocalizador(String codigoLocalizador) {
        this.codigoLocalizador = codigoLocalizador;
    }

    public String getDataCheckIn() {
        return dataCheckIn;
    }

    public void setDataCheckIn(String dataCheckIn) {
        this.dataCheckIn = dataCheckIn;
    }

    public String getDataCheckOut() {
        return dataCheckOut;
    }

    public void setDataCheckOut(String dataCheckOut) {
        this.dataCheckOut = dataCheckOut;
    }

    public int getQuantidadeHospedes() {
        return quantidadeHospedes;
    }

    public void setQuantidadeHospedes(int quantidadeHospedes) {
        this.quantidadeHospedes = quantidadeHospedes;
    }

    public String getTipoQuarto() {
        return tipoQuarto;
    }

    public void setTipoQuarto(String tipoQuarto) {
        this.tipoQuarto = tipoQuarto;
    }

    public double getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(double valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Hospede getHospede() {
        return hospede;
    }

    public void setHospede(Hospede hospede) {
        this.hospede = hospede;
    }

    public List<ItemServico> getServicos() {
        return servicos;
    }

    public void setServicos(List<ItemServico> servicos) {
        this.servicos = servicos;
    }

    public void adicionarServico(ItemServico servico) {
        if (this.servicos == null) {
            this.servicos = new ArrayList<>();
        }
        this.servicos.add(servico);
    }

    public double calcularTotalServicos() {
        if (servicos == null || servicos.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (ItemServico s : servicos) {
            total += s.getSubtotal();
        }
        return total;
    }
}
