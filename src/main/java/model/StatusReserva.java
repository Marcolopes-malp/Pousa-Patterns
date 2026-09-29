package model;

/**
 * Enum que representa o Ciclo de Vida e Estados de uma Reserva na Pousada Paradiso.
 * Define as transições válidas de estado conforme as regras de negócio:
 * - PENDENTE -> CONFIRMADA, CANCELADA
 * - CONFIRMADA -> CHECKIN_ATIVO, CANCELADA
 * - CHECKIN_ATIVO -> FINALIZADA
 * - FINALIZADA -> (Estado Terminal)
 * - CANCELADA -> (Estado Terminal)
 */
public enum StatusReserva {

    PENDENTE("Pendente de Pagamento") {
        @Override
        public boolean podeFazerCheckIn() {
            return false;
        }

        @Override
        public boolean podeCancelar() {
            return true;
        }

        @Override
        public boolean podeFinalizar() {
            return false;
        }
    },

    CONFIRMADA("Confirmada") {
        @Override
        public boolean podeFazerCheckIn() {
            return true;
        }

        @Override
        public boolean podeCancelar() {
            return true;
        }

        @Override
        public boolean podeFinalizar() {
            return false;
        }
    },

    CHECKIN_ATIVO("Check-in Ativo") {
        @Override
        public boolean podeFazerCheckIn() {
            return false;
        }

        @Override
        public boolean podeCancelar() {
            return false;
        }

        @Override
        public boolean podeFinalizar() {
            return true;
        }
    },

    FINALIZADA("Finalizada") {
        @Override
        public boolean podeFazerCheckIn() {
            return false;
        }

        @Override
        public boolean podeCancelar() {
            return false;
        }

        @Override
        public boolean podeFinalizar() {
            return false;
        }
    },

    CANCELADA("Cancelada") {
        @Override
        public boolean podeFazerCheckIn() {
            return false;
        }

        @Override
        public boolean podeCancelar() {
            return false;
        }

        @Override
        public boolean podeFinalizar() {
            return false;
        }
    };

    private final String descricao;

    StatusReserva(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Informa se a reserva no estado atual está apta a realizar o processo de check-in.
     * Apenas reservas com status CONFIRMADA podem realizar check-in.
     */
    public abstract boolean podeFazerCheckIn();

    /**
     * Informa se a reserva pode ser cancelada.
     */
    public abstract boolean podeCancelar();

    /**
     * Informa se a reserva pode ser finalizada (check-out).
     */
    public abstract boolean podeFinalizar();

    /**
     * Realiza o parse seguro a partir da string armazenada no banco.
     */
    public static StatusReserva fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return PENDENTE;
        }
        for (StatusReserva s : values()) {
            if (s.name().equalsIgnoreCase(valor.trim())) {
                return s;
            }
        }
        return PENDENTE;
    }
}
