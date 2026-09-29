package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Padrão de Projeto de Criação: BUILDER.
 * Permite a construção passo a passo e fluente de uma Reserva (objeto complexo com 11+ atributos),
 * isolando a lógica de validação de regras (ex: período de datas, quantidade de hóspedes)
 * e o cálculo prévio de totais.
 * Conforme ensinado na Aula 06 (Interface Fluente e constroi()).
 */
public class ReservaBuilder {

    private int id;
    private String codigoLocalizador;
    private String dataCheckIn;
    private String dataCheckOut;
    private int quantidadeHospedes = 1;
    private String tipoQuarto = "Standard";
    private double valorDiaria = 250.0;
    private double valorTotal = 0.0;
    private String status = "PENDENTE";
    private String formaPagamento = "PIX";
    private String observacoes = "";
    private String dataCriacao;
    private Hospede hospede;
    private List<ItemServico> servicos = new ArrayList<>();

    public ReservaBuilder() {
    }

    public static ReservaBuilder novo() {
        return new ReservaBuilder();
    }

    public ReservaBuilder comId(int id) {
        this.id = id;
        return this;
    }

    public ReservaBuilder comCodigoLocalizador(String codigo) {
        this.codigoLocalizador = codigo;
        return this;
    }

    public ReservaBuilder comCheckIn(String dataCheckIn) {
        this.dataCheckIn = dataCheckIn;
        return this;
    }

    public ReservaBuilder comCheckOut(String dataCheckOut) {
        this.dataCheckOut = dataCheckOut;
        return this;
    }

    public ReservaBuilder comPeriodo(String checkIn, String checkOut) {
        this.dataCheckIn = checkIn;
        this.dataCheckOut = checkOut;
        return this;
    }

    public ReservaBuilder comQuantidadeHospedes(int quantidade) {
        this.quantidadeHospedes = quantidade;
        return this;
    }

    public ReservaBuilder comTipoQuarto(String tipoQuarto) {
        this.tipoQuarto = tipoQuarto;
        return this;
    }

    public ReservaBuilder comValorDiaria(double valorDiaria) {
        this.valorDiaria = valorDiaria;
        return this;
    }

    public ReservaBuilder comValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
        return this;
    }

    public ReservaBuilder comStatus(String status) {
        this.status = status;
        return this;
    }

    public ReservaBuilder comFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
        return this;
    }

    public ReservaBuilder comObservacoes(String observacoes) {
        this.observacoes = observacoes;
        return this;
    }

    public ReservaBuilder comDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
        return this;
    }

    public ReservaBuilder comHospede(Hospede hospede) {
        this.hospede = hospede;
        return this;
    }

    public ReservaBuilder comServicos(List<ItemServico> servicos) {
        if (servicos != null) {
            this.servicos = servicos;
        }
        return this;
    }

    public ReservaBuilder adicionarServico(ItemServico servico) {
        if (servico != null) {
            this.servicos.add(servico);
        }
        return this;
    }

    /**
     * Valida os atributos e constrói a instância de Reserva consolidada.
     */
    public Reserva constroi() {
        if (this.dataCheckIn == null || this.dataCheckIn.trim().isEmpty()) {
            throw new IllegalArgumentException("Data de Check-In é obrigatória.");
        }
        if (this.dataCheckOut == null || this.dataCheckOut.trim().isEmpty()) {
            throw new IllegalArgumentException("Data de Check-Out é obrigatória.");
        }
        if (this.quantidadeHospedes <= 0) {
            this.quantidadeHospedes = 1;
        }

        // Gera código localizador se não tiver
        if (this.codigoLocalizador == null || this.codigoLocalizador.trim().isEmpty()) {
            String sufixo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            this.codigoLocalizador = "POUS-" + sufixo;
        }

        // Calcula total se não foi fixado usando a estratégia unificada de precificação
        if (this.valorTotal <= 0.0) {
            try {
                LocalDate dtIn = LocalDate.parse(this.dataCheckIn);
                LocalDate dtOut = LocalDate.parse(this.dataCheckOut);
                long dias = ChronoUnit.DAYS.between(dtIn, dtOut);
                if (dias <= 0) {
                    dias = 1;
                }
                model.strategy.CalculadoraPreco calc = new model.strategy.CalculadoraPreco();
                model.strategy.ResultadoCalculoPreco res = calc.calcular(dias, this.valorDiaria, this.servicos, this.formaPagamento);
                this.valorTotal = res.getValorTotalFinal();
            } catch (Exception e) {
                this.valorTotal = this.valorDiaria;
            }
        }

        if (this.dataCriacao == null || this.dataCriacao.trim().isEmpty()) {
            this.dataCriacao = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        return new Reserva(
                this.id,
                this.codigoLocalizador,
                this.dataCheckIn,
                this.dataCheckOut,
                this.quantidadeHospedes,
                this.tipoQuarto,
                this.valorDiaria,
                this.valorTotal,
                this.status,
                this.formaPagamento,
                this.observacoes,
                this.dataCriacao,
                this.hospede,
                this.servicos
        );
    }
}
