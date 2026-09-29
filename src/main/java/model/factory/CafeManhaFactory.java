package model.factory;

import model.ItemServico;

/**
 * Fábrica concreta para criação de Café da Manhã Colonial.
 */
public class CafeManhaFactory extends ServicoFactory {
    @Override
    public ItemServico criarServico() {
        return new ItemServico("Café da Manhã Colonial", "Buffet artesanal completo servido na varanda do quarto", 65.0, 1);
    }
}
