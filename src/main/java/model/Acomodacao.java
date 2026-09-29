package model;

import java.io.Serializable;

public class Acomodacao implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private String tipo;
    private String descricao;
    private int capacidadePessoas;
    private double valorDiaria;
    private int vagasRestantes;
    private double avaliacao;
    private int totalAvaliacoes;
    private String imagemUrl;
    private String comodidades;

    public Acomodacao() {
    }

    public Acomodacao(int id, String nome, String tipo, String descricao, int capacidadePessoas,
                      double valorDiaria, int vagasRestantes, double avaliacao, int totalAvaliacoes,
                      String imagemUrl, String comodidades) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
        this.capacidadePessoas = capacidadePessoas;
        this.valorDiaria = valorDiaria;
        this.vagasRestantes = vagasRestantes;
        this.avaliacao = avaliacao;
        this.totalAvaliacoes = totalAvaliacoes;
        this.imagemUrl = imagemUrl;
        this.comodidades = comodidades;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getCapacidadePessoas() {
        return capacidadePessoas;
    }

    public void setCapacidadePessoas(int capacidadePessoas) {
        this.capacidadePessoas = capacidadePessoas;
    }

    public double getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(double valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public int getVagasRestantes() {
        return vagasRestantes;
    }

    public void setVagasRestantes(int vagasRestantes) {
        this.vagasRestantes = vagasRestantes;
    }

    public double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(double avaliacao) {
        this.avaliacao = avaliacao;
    }

    public int getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public void setTotalAvaliacoes(int totalAvaliacoes) {
        this.totalAvaliacoes = totalAvaliacoes;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public String getComodidades() {
        return comodidades;
    }

    public void setComodidades(String comodidades) {
        this.comodidades = comodidades;
    }
}
