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

    public static ServicoFactory obterFabrica(String tipo) {
        if (tipo == null) {
            return new CafeManhaFactory();
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
            default:
                return new CafeManhaFactory();
        }
    }
}
