package model.factory;

import model.ItemServico;

/**
 * Padrão de Projeto de Criação: FACTORY METHOD (GoF).
 * Classe Criadora Abstrata (Creator) que define a assinatura do método fábrica.
 * Subclasses concretas decidem qual classe / variante de serviço instanciar.
 */
public abstract class ServicoFactory {

    /**
     * Factory Method que deve ser sobrescrito pelas fábricas concretas.
     */
    public abstract ItemServico criarServico();

    /**
     * Sobrecarga conveniente que cria o serviço e define a quantidade contratada.
     */
    public ItemServico criarServico(int quantidade) {
        ItemServico servico = criarServico();
        if (quantidade > 0) {
            servico.setQuantidade(quantidade);
        }
        return servico;
    }

    public static ServicoFactory obterFabrica(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de serviço não informado.");
        }
        switch (tipo.toLowerCase().trim()) {
            case "transfer":
                return new TransferAeroportoFactory();
            case "barco":
            case "passeio":
                return new PasseioBarcoFactory();
            case "spa":
            case "massagem":
                return new SpaRelaxanteFactory();
            case "cafe":
                return new CafeManhaFactory();
            default:
                throw new IllegalArgumentException("Tipo de serviço desconhecido ou não suportado: " + tipo);
        }
    }
}
