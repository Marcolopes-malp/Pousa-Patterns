package model;

import java.io.Serializable;

/**
 * Entidade que representa o Hóspede titular da reserva.
 * Participa do relacionamento 1:1 com a entidade Reserva.
 */
public class Hospede implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nomeCompleto;
    private String cpf;
    private String email;
    private String telefone;
    private String cidadeOrigem;
    private String senha;

    public Hospede() {
    }

    public Hospede(int id, String nomeCompleto, String cpf, String email, String telefone, String cidadeOrigem, String senha) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.cidadeOrigem = cidadeOrigem;
        this.senha = senha;
    }

    public Hospede(int id, String nomeCompleto, String cpf, String email, String telefone, String cidadeOrigem) {
        this(id, nomeCompleto, cpf, email, telefone, cidadeOrigem, "123456");
    }

    public Hospede(String nomeCompleto, String cpf, String email, String telefone, String cidadeOrigem, String senha) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.cidadeOrigem = cidadeOrigem;
        this.senha = senha;
    }

    public Hospede(String nomeCompleto, String cpf, String email, String telefone, String cidadeOrigem) {
        this(nomeCompleto, cpf, email, telefone, cidadeOrigem, "123456");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCidadeOrigem() {
        return cidadeOrigem;
    }

    public void setCidadeOrigem(String cidadeOrigem) {
        this.cidadeOrigem = cidadeOrigem;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public String toString() {
        return "Hospede{" +
                "id=" + id +
                ", nomeCompleto='" + nomeCompleto + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", cidadeOrigem='" + cidadeOrigem + '\'' +
                '}';
    }
}
