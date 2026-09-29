package model.factory;

import model.ItemServico;

/**
 * Fábrica concreta para criação de Passeio de Barco / Escuna.
 */
public class PasseioBarcoFactory extends ServicoFactory {
    @Override
    public ItemServico criarServico() {
        return new ItemServico("Passeio de Escuna", "Tour pelas ilhas e praias paradisíacas com paradas para mergulho", 120.0, 1);
    }
}
