package dao;

import java.util.ArrayList;
import java.util.List;
import model.Acomodacao;

public class AcomodacaoDAO {

    private static final List<Acomodacao> CATALOGO = new ArrayList<>();

    static {
        CATALOGO.add(new Acomodacao(
                1,
                "Bangalô Vista Mar & Deck Privativo",
                "Bangalô",
                "Bangalô exclusivo situado a 30 metros da areia, com varanda panorâmica, rede de descanso e banheira de hidromassagem externa.",
                2,
                450.0,
                2,
                4.97,
                64,
                "https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&w=800&q=80",
                "Wi-Fi 500 Mbps • Cama King • Ar Split • Cafeteira Nespresso • Vista Livre"
        ));

        CATALOGO.add(new Acomodacao(
                2,
                "Suíte Master com Hidro & Lareira",
                "Suíte Master",
                "Ambiente aconchegante com piso de madeira nobre, cama king size com lençóis 400 fios e hidromassagem dupla integrada ao quarto.",
                2,
                520.0,
                1,
                4.99,
                82,
                "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80",
                "Hidro Dupla • Cama King • Frigobar Retrô • Smart TV 55\" • Lareira Ecológica"
        ));

        CATALOGO.add(new Acomodacao(
                3,
                "Chalé Família nas Palmeiras",
                "Chalé",
                "Espaço amplo com dois ambientes integrados, cozinha de apoio compacta e área externa privativa cercada por jardim tropical.",
                4,
                380.0,
                3,
                4.88,
                41,
                "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80",
                "2 Quartos • Cozinha de Apoio • Varanda com Rede • Churrasqueira • Aceita Pet"
        ));

        CATALOGO.add(new Acomodacao(
                4,
                "Suíte Standard Jardim Colonial",
                "Standard",
                "Acomodação prática e silenciosa com vista para o jardim interno da pousada, equipada com mesa de trabalho e iluminação aconchegante.",
                2,
                250.0,
                4,
                4.85,
                29,
                "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80",
                "Wi-Fi • Ar Split • Cama Queen • Chuveiro a Gás • Mesa de Trabalho"
        ));
    }

    public List<Acomodacao> listarTodas() {
        return new ArrayList<>(CATALOGO);
    }

    public Acomodacao buscarPorId(int id) {
        for (Acomodacao a : CATALOGO) {
            if (a.getId() == id) {
                return a;
            }
        }
        return CATALOGO.get(0);
    }
}
