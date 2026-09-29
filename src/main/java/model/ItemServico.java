package model;

import java.io.Serializable;

/**
 * Entidade que representa um serviço adicional contratado na pousada.
 * Participa do relacionamento 1:N com a Reserva.
 */
public class ItemServico implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int reservaId;
    private String nome;
    private String descricao;
    private double precoUnitario;
    private int quantidade;

    public ItemServico() {
    }

    public ItemServico(int id, int reservaId, String nome, String descricao, double precoUnitario, int quantidade) {
        this.id = id;
        this.reservaId = reservaId;
        this.nome = nome;
        this.descricao = descricao;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
    }

    public ItemServico(String nome, String descricao, double precoUnitario, int quantidade) {
        this.nome = nome;
        this.descricao = descricao;
        this.precoUnitario = precoUnitario;
        this.quantidade = quantidade;
    }

    public double getSubtotal() {
        return precoUnitario * quantidade;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReservaId() {
        return reservaId;
    }

    public void setReservaId(int reservaId) {
        this.reservaId = reservaId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        return "ItemServico{" +
                "id=" + id +
                ", reservaId=" + reservaId +
                ", nome='" + nome + '\'' +
                ", precoUnitario=" + precoUnitario +
                ", quantidade=" + quantidade +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
