package model.factory;

import model.ItemServico;

/**
 * Fábrica concreta para criação de Transfer Aeroporto.
 */
public class TransferAeroportoFactory extends ServicoFactory {
    @Override
    public ItemServico criarServico() {
        return new ItemServico("Transfer Executivo", "Translado privativo aeroporto/pousada em veículo com ar-condicionado", 180.0, 1);
    }
}
