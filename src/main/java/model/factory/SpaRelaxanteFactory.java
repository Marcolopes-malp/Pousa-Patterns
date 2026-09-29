package model.factory;

import model.ItemServico;

/**
 * Fábrica concreta para criação de Sessão de SPA e Massagem.
 */
public class SpaRelaxanteFactory extends ServicoFactory {
    @Override
    public ItemServico criarServico() {
        return new ItemServico("Massagem Terapêutica", "Sessão individual de relaxamento com pedras quentes e aromaterapia 50min", 150.0, 1);
    }
}
