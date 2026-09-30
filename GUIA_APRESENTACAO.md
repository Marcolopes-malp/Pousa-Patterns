# UNIVERSIDADE DE MOGI DAS CRUZES — UMC
## ENGENHARIA DE SOFTWARE — 5º SEMESTRE | PADRÕES DE PROJETO (PP)

# POUSADA PARADISO — GUIA DE DEFESA M1
**Aluno:** Marco Antonio Lopes Pedro (G14)  
**Docentes:** Prof. Me. Wolley W. Silva & Profa. Dra. Danielle Martin  

---

## 1. Roteiro de Apresentação (Passo a Passo)

| Etapa | Foco | O que Falar e Demonstrar |
| :--- | :--- | :--- |
| **Etapa 1** | **Abertura** | Apresentar o sistema Pousada Paradiso desenvolvido em Java 17, Tomcat e JDBC puro com arquitetura em camadas e padrões GoF. |
| **Etapa 2** | **Fluxo do Cliente** | Realizar uma reserva no Bangalô: demonstrar o cálculo automático de diárias, seleção de serviços adicionais (1:N) e desconto do PIX. |
| **Etapa 3** | **Painel da Recepção** | Logar com a conta da recepção (`recepcao@pousada.com.br` / `admin123`), exibir métricas administrativas e efetuar o check-in com geração de PIN. |
| **Etapa 4** | **Defesa dos Padrões** | Abrir a IDE e explicar os padrões Command, Builder, Factory Method, Strategy, State e DAO. |
| **Etapa 5** | **Conclusão e Testes** | Executar `mvn test` no terminal, comprovando os 42 testes automatizados passando com sucesso. |

---

## 2. Requisitos Funcionais (RF) e Regras de Negócio (RN)

### Requisitos Funcionais (RF)

| ID | Requisito | Descrição Direta |
| :--- | :--- | :--- |
| **RF01** | Catálogo de Acomodações | Listagem dinâmica com fotos, capacidade, valor da diária e comodidades. |
| **RF02** | Reserva de Estadia | Seleção de datas, quantidade de hóspedes e cálculo automático de valores. |
| **RF03** | Serviços Opcionais (1:N) | Inclusão de Café Colonial, Translado, Passeio de Barco e Massagem Terapêutica. |
| **RF04** | Autenticação e Perfis | Login seguro diferenciando Hóspede (CLIENTE) e Atendente (RECEPCAO). |
| **RF05** | Check-in Inteligente | Validação de status, atualização para CHECKIN_ATIVO e emissão de PIN digital. |
| **RF06** | Gestão da Recepção (CRUD) | Consulta centralizada de reservas, busca por ID, edição e exclusão de registros. |

### Regras de Negócio (RN)

| ID | Regra | Comportamento do Sistema |
| :--- | :--- | :--- |
| **RN01** | Validação de Período | A data de check-out deve ser posterior ao check-in; datas no passado são rejeitadas. |
| **RN02** | Limite de Hóspedes | A quantidade de hóspedes deve respeitar a capacidade máxima da acomodação. |
| **RN03** | Desconto de Estadia | Aplica 10% de desconto para 4 ou mais diárias; 15% para 7 ou mais diárias. |
| **RN04** | Desconto via PIX | Aplica 5% de desconto sobre o valor total ao escolher pagamento por PIX. |
| **RN05** | Taxa Ambiental | Adiciona taxa de preservação de 3% sobre o subtotal de diárias e serviços. |
| **RN06** | Ciclo de Check-in | Check-in permitido apenas para reservas CONFIRMADAS; canceladas ou ativas são bloqueadas. |
| **RN07** | Proteção de Senhas | Senhas criptografadas obrigatoriamente com PBKDF2 e Salt de 16 bytes; texto puro proibido. |

---

## 3. Padrões de Projeto no Código (O que Mostrar na IDE)

| Padrão | Arquivo Principal | Papel na Arquitetura |
| :--- | :--- | :--- |
| **Front Controller** | `controller.ManterReserva` | Ponto único de entrada (`/controller.do`), gerencia sessão e despacha comandos. |
| **Command** | `CommandFactory` e `ICommand` | Cada ação é um comando isolado; `CommandFactory` tipada elimina reflexão frágil. |
| **Builder (GoF)** | `model.ReservaBuilder` | Construção fluente da Reserva com validações defensivas no método `constroi()`. |
| **Factory Method** | `model.factory.ServicoFactory` | Subclasses especialistas (`CafeManha`, `Transfer`, etc.) instanciam itens 1:N. |
| **Strategy (GoF)** | `model.strategy.CalculadoraTarifa` | Encapsula regras de tarifas (diárias, PIX, taxa ambiental) de forma desacoplada. |
| **State (GoF)** | `model.StatusReserva` | Controla transições válidas de status da reserva (`PENDENTE` ➔ `CONFIRMADA` ➔ `CHECKIN_ATIVO`). |
| **DAO & Transações** | `dao.ReservaDAO` | Persistência JDBC com transações atômicas (commit/rollback) e sem N+1 queries. |

---

## 4. Resumo de Segurança (OWASP)

* **Criptografia de Senhas (PBKDF2WithHmacSHA256):** Salt individual de 16 bytes e 10.000 iterações na classe `util.Seguranca`.
* **Proteção CSRF (Tokens de Sessão):** Validação de token em todos os formulários POST que alteram estado.
* **Proteção XSS (Escape de HTML):** Método `util.Html.esc()` aplicado em todas as saídas dinâmicas das JSPs.
* **Proteção de Rotas (Views em WEB-INF):** JSPs inacessíveis diretamente por URL, forçando passagem pelo Front Controller.
* **PIN de Acesso Seguro (SecureRandom):** Geração criptograficamente segura para fechaduras eletrônicas.

---

## 5. Perguntas e Respostas da Banca (FAQ de Defesa)

* **P: Por que usar Command com Front Controller?**  
  **R:** Centraliza segurança, sessão e roteamento no `ManterReserva`. O Command transforma cada operação em uma classe isolada (`ICommand`), facilitando testes e manutenção.

* **P: Qual a diferença entre o Factory Method e o Builder no projeto?**  
  **R:** O Factory Method (`ServicoFactory`) decide qual subclasse concreta instanciar para os serviços (1:N). O Builder (`ReservaBuilder`) constrói um único objeto complexo (`Reserva`) com validações defensivas.

* **P: Como garantem que o banco não fica inconsistente se ocorrer erro?**  
  **R:** O `ReservaDAO` usa transações ACID: desabilita `setAutoCommit`, salva hóspede, reserva e serviços na mesma conexão e faz commit. Se qualquer etapa falhar, o catch executa `rollback`.

* **P: Como resolveram o problema N+1?**  
  **R:** Substituímos o laço de consultas por um `LEFT JOIN` com a tabela hóspedes e buscamos os serviços de todas as reservas em lote com uma única segunda query via `WHERE reserva_id IN (...)`.

* **P: Como as senhas são armazenadas?**  
  **R:** Utilizamos `PBKDF2WithHmacSHA256` com Salt de 16 bytes e 10.000 iterações, comparando em tempo constante com `MessageDigest.isEqual` para evitar timing attacks.

---

## 6. Validação com Testes Automatizados

Suíte completa com 42 testes unitários (JUnit 5) cobrindo regras de negócio, persistência ACID e padrões:

```text
mvn test
[INFO] Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
