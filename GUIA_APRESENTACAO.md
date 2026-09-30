# 🎓 Guia Completo de Apresentação e Defesa Acadêmica (M1)
## Pousada Paradiso — Sistema de Gerenciamento de Reservas
> **Instituição:** Universidade de Mogi das Cruzes (UMC)  
> **Curso:** Bacharelado em Engenharia de Software — 5º Semestre  
> **Disciplina:** Padrões de Projeto (PP)  
> **Aluno:** Marco Antonio Lopes Pedro (Grupo G14)  
> **Docentes:** Prof. Me. Wolley W. Silva & Profa. Dra. Danielle Martin  
> **Versão do Projeto:** 1.0.0 (Release Acadêmica M1)

---

## 📑 Sumário

1. [Visão Geral e Contexto do Projeto](#1-visão-geral-e-contexto-do-projeto)
2. [Roteiro Prático de Apresentação (Passo a Passo)](#2-roteiro-prático-de-apresentação-passo-a-passo)
3. [Mapeamento Completo dos Padrões de Projeto (Onde Mostrar)](#3-mapeamento-completo-dos-padrões-de-projeto-onde-mostrar)
   - [3.1 Front Controller + MVC](#31-front-controller--mvc)
   - [3.2 Command Pattern](#32-command-pattern)
   - [3.3 Builder Pattern (GoF)](#33-builder-pattern-gof)
   - [3.4 Factory Method (GoF)](#34-factory-method-gof)
   - [3.5 Strategy Pattern (GoF)](#35-strategy-pattern-gof)
   - [3.6 State Pattern (GoF)](#36-state-pattern-gof)
   - [3.7 DAO & Transações ACID](#37-dao--transações-acid)
   - [3.8 Service Layer](#38-service-layer)
4. [Camada de Segurança e Boas Práticas (OWASP)](#4-camada-de-segurança-e-boas-práticas-owasp)
5. [Banco de Dados: Resiliência, Schema e Dualidade](#5-banco-de-dados-resiliência-schema-e-dualidade)
6. [Suíte de Testes Automatizados (Evidências)](#6-suíte-de-testes-automatizados-evidências)
7. [Perguntas Prováveis da Banca e Respostas na Ponta da Língua (FAQ)](#7-perguntas-prováveis-da-banca-e-respostas-na-ponta-da-língua-faq)
8. [Glossário Rápido de Conceitos Técnicos](#8-glossário-rápido-de-conceitos-técnicos)

---

## 1. Visão Geral e Contexto do Projeto

O **Pousada Paradiso** é uma aplicação corporativa para reservas e gestão hoteleira inspirada nas melhores práticas de plataformas de hospitalidade modernas (*Airbnb*, *Booking*).

### 🎯 Objetivos Centrais
- **Demonstrar domínio prático dos Padrões de Projeto (GoF e Arquiteturais):** Command, Factory Method, Builder, Strategy, State, DAO, MVC e Front Controller.
- **Implementar integridade transacional real (ACID):** Commit e rollback explícitos em conexão JDBC única para operações com relacionamentos 1:1 e 1:N.
- **Adotar segurança defensiva (OWASP):** Hashing PBKDF2 com Salt de 16 bytes, proteção CSRF, sanitização contra XSS e Open Redirect, e chaves seguras (*smart PIN*).
- **Garantir portabilidade absoluta:** Execução instantânea com Tomcat 9 embutido e banco H2 local, além de suporte a MySQL e empacotamento Docker multi-stage.

---

## 2. Roteiro Prático de Apresentação (Passo a Passo)

Siga este roteiro cronometrado para garantir uma apresentação fluida, profissional e segura:

```
[00:00 - 01:00]  Abertura & Contexto da Aplicação
[01:00 - 03:00]  Demonstração Prática no Navegador (Cliente e Recepção)
[03:00 - 07:00]  Apresentação do Código-Fonte e Padrões GoF
[07:00 - 08:30]  Segurança (PBKDF2) e Persistência ACID (Rollback)
[08:30 - 09:30]  Execução dos Testes Automatizados (mvn test)
[09:30 - 10:00]  Conclusão e Perguntas da Banca
```

### 1️⃣ Subindo a aplicação antes de chamar a banca:
Abra o terminal no diretório do projeto e execute:
```bash
mvn compile exec:java
```
Acesse no navegador: **[http://localhost:8080/controller.do](http://localhost:8080/controller.do)**

### 2️⃣ Fluxo 1 — Visão do Hóspede (No ar):
1. **Catálogo:** Mostre as acomodações (Bangalô Vista Mar, Suíte Master, etc.) carregadas dinamicamente.
2. **Nova Reserva:** Clique em **Reservar** no Bangalô.
3. **Cálculo Dinâmico (Strategy):**
   - Mude as datas para 5 noites ➔ Aponte o **Desconto de Longa Estadia (10%)**.
   - Marque o **Café da Manhã Colonial** (+ R$ 65/noite) e **Translado**.
   - Selecione a opção **PIX** ➔ Mostre o **Desconto de 5%**.
4. **Finalizar:** Envie o formulário e mostre a tela de confirmação imediata com o resumo transparente de valores.

### 3️⃣ Fluxo 2 — Visão da Recepção (Painel Administrativo):
1. Clique em **Entrar** e utilize as credenciais de demonstração:
   - **E-mail:** `recepcao@pousada.com.br`
   - **Senha:** `admin123`
2. Mostre o **Painel da Recepção** (`btnop=Admin`):
   - Métricas em tempo real (Total de reservas e Faturamento acumulado).
   - Tabela administrativa com status de cada hóspede.
   - Demonstre a realização do **Check-in Automático** (transição de status para `CHECKIN_ATIVO` e geração de PIN digital).

---

## 3. Mapeamento Completo dos Padrões de Projeto (Onde Mostrar)

### 3.1 Front Controller + MVC
* **Classes:** `controller.ManterReserva` e `web.xml`
* **Conceito:** Ponto único centralizador de requisições que intercepta chamadas HTTP, gerencia segurança de sessão e despacha a execução para as Commands especializadas.
* **Views Protegidas:** Todas as páginas JSP ficam em `WEB-INF/views/`, impedindo que usuários acessem diretamente formulários ou relatórios digitando URLs no navegador.
* **Onde mostrar no código:**
  - `src/main/java/controller/ManterReserva.java` (métodos `doGet` e `doPost`).
  - `src/main/webapp/WEB-INF/web.xml` (mapeamento do servlet para `/controller.do`).

---

### 3.2 Command Pattern
* **Interface:** `br.com.commandfactory.controller.ICommand`
* **Fábrica Tipada:** `br.com.commandfactory.controller.CommandFactory`
* **Implementações:** `CadastraReservaAction`, `AtualizaReservaAction`, `DeletaReservaAction`, `ConsultaTodosReservaAction`, `ProcessarCheckInAutomaticoReservaAction`, etc.
* **Por que é superior:**
  - Elimina condicionais gigantescas (`if/else` ou `switch`).
  - Substitui `Class.forName` frágil por um `Map<String, Supplier<ICommand>>` imutável e tipado.
  - Devolve **HTTP 404** caso alguém tente invocar um comando inexistente.
* **Trecho para mostrar:**
```java
// CommandFactory.java
private static final Map<String, Supplier<ICommand>> COMANDOS = new HashMap<>();
static {
    COMANDOS.put("Cadastra", CadastraReservaAction::new);
    COMANDOS.put("Atualiza", AtualizaReservaAction::new);
    COMANDOS.put("Deleta", DeletaReservaAction::new);
    COMANDOS.put("ConsultaTodos", ConsultaTodosReservaAction::new);
    COMANDOS.put("ProcessarCheckInAutomatico", ProcessarCheckInAutomaticoReservaAction::new);
    // ...
}
```

---

### 3.3 Builder Pattern (GoF)
* **Classe:** `model.ReservaBuilder`
* **Propósito:** Montar a entidade complexa `Reserva` (mais de 11 atributos) passo a passo de forma legível e com **validação defensiva no método `constroi()`**.
* **Validações Aplicadas no `constroi()`:**
  - `checkOut` obrigatoriamente posterior ao `checkIn`.
  - Data de entrada não pode ser no passado para novas reservas.
  - Quantidade de hóspedes deve respeitar o intervalo `1 <= qtd <= capacidadeMaxima`.
* **Trecho para mostrar:**
```java
// ReservaBuilder.java
public Reserva constroi() {
    if (this.dataCheckIn == null || this.dataCheckOut == null) {
        throw new IllegalStateException("Datas de check-in e check-out são obrigatórias.");
    }
    if (!this.dataCheckOut.isAfter(this.dataCheckIn)) {
        throw new IllegalArgumentException("A data de check-out deve ser posterior ao check-in.");
    }
    // instanciação segura e cálculo automático da tarifa oficial
    return reserva;
}
```

---

### 3.4 Factory Method (GoF)
* **Fábrica Abstrata:** `model.factory.ServicoFactory`
* **Fábricas Concretas:**
  - `CafeManhaFactory` ➔ produz `ItemServico` de Café Colonial
  - `TransferAeroportoFactory` ➔ produz `ItemServico` de Translado Privativo
  - `PasseioBarcoFactory` ➔ produz `ItemServico` de Passeio de Escuna
  - `SpaRelaxanteFactory` ➔ produz `ItemServico` de Massagem Terapêutica
* **Conceito Acadêmico:** Princípio de Responsabilidade Única (SRP) e Aberto/Fechado (OCP). A criação de serviços não está acoplada a quem solicita o serviço.

---

### 3.5 Strategy Pattern (GoF)
* **Contexto:** `model.strategy.CalculadoraTarifa`
* **Interface:** `model.strategy.RegraTarifa`
* **Estratégias Concretas:**
  - `DescontoLongaEstadia`: 10% de desconto para >= 4 noites; 15% para >= 7 noites.
  - `DescontoPix`: 5% de abatimento sobre a base total.
  - `TaxaAmbiental`: 3% de preservação ambiental sobre diárias e serviços.
* **Benefício:** Acabou com a discrepância de cálculo entre JavaScript da tela, banco de dados e comprovante.

---

### 3.6 State Pattern (GoF)
* **Enum com Validação:** `model.StatusReserva`
* **Estados Possíveis:** `PENDENTE`, `CONFIRMADA`, `CHECKIN_ATIVO`, `FINALIZADA`, `CANCELADA`.
* **Regras de Transição:**
  - `PENDENTE` ➔ pode ir para `CONFIRMADA` ou `CANCELADA`.
  - `CONFIRMADA` ➔ pode ir para `CHECKIN_ATIVO` ou `CANCELADA`.
  - `CHECKIN_ATIVO` ➔ pode ir para `FINALIZADA`.
  - Estados terminais (`FINALIZADA`, `CANCELADA`) não aceitam check-in.
* **Benefício:** Impede que um usuário execute check-in repetido ou tente fazer check-in de uma reserva cancelada.

---

### 3.7 DAO & Transações ACID
* **Classes:** `dao.ReservaDAO`, `dao.HospedeDAO`, `dao.ItemServicoDAO`, `dao.AcomodacaoDAO`
* **O grande destaque técnico para a banca:**
  1. **Atomicidade e Consistência (ACID):**
     No método `ReservaDAO.cadastrar()`, hóspede, reserva e serviços 1:N são gravados na mesma conexão com `con.setAutoCommit(false)`. Se qualquer etapa falhar, o método executa `con.rollback()`.
  2. **Eliminação de N+1 Queries:**
     No método `ReservaDAO.consultarTodos()`, em vez de fazer 1 query para cada hóspede e 1 query para cada serviço de cada reserva (100+ queries), são executadas **apenas 2 queries**:
     - Query 1: `SELECT r.*, h.* FROM reservas r LEFT JOIN hospedes h ON r.hospede_id = h.id`
     - Query 2: `SELECT * FROM itens_servicos WHERE reserva_id IN (?, ?, ...)`

---

### 3.8 Service Layer
* **Classe:** `service.ReservaService`
* **Conceito:** Isola as regras de negócio puras (validação de disponibilidade sobreposta de acomodações, verificação de datas e ciclo de check-in) fora das Commands do controller, respeitando a arquitetura em 3 camadas (View ➔ Controller ➔ Service ➔ DAO).

---

## 4. Camada de Segurança e Boas Práticas (OWASP)

| Vulnerabilidade Mitigada | Solução no Projeto | Arquivo de Referência |
| :--- | :--- | :--- |
| **Senhas em texto puro** | Hashing com **PBKDF2WithHmacSHA256**, Salt de 16 bytes e 10.000 iterações. | `util.Seguranca.java` |
| **Timing Attacks** | Comparação de hashes em tempo constante com `MessageDigest.isEqual`. | `util.Seguranca.java` |
| **Sequestro de Conta** | Bloqueio de login automático por e-mail; e-mails cadastrados exigem autenticação com senha. | `CadastraReservaAction.java` |
| **Cross-Site Request Forgery (CSRF)** | Token CSRF gerado na sessão e validado em todas as ações POST. | `ManterReserva.java` |
| **Cross-Site Scripting (XSS)** | Sanitização e escape de caracteres HTML em todas as exibições das JSPs (`Html.esc`). | `util.Html.java` |
| **Open Redirect & CRLF Injection** | Validação estrita aceitando apenas rotas internas que iniciem com `controller.do`. | `util.Seguranca.java` |
| **PINs Previsíveis** | Geração de PIN digital de acesso utilizando `java.security.SecureRandom`. | `ReservaService.java` |

---

## 5. Banco de Dados: Resiliência, Schema e Dualidade

A aplicação adota o princípio de **Configuração Externa (12-Factor App)** e inicialização thread-safe:

- **Modo Padrão (Sem Instalação):** Banco **H2 Database** embutido em arquivo (`./pousada_db`). O projeto sobe em qualquer computador sem precisar de MySQL ou Docker instalados.
- **Modo Produção / Universidade:** Suporte nativo a **MySQL** bastando definir as variáveis de ambiente:
  - `DB_ENGINE=mysql`
  - `DB_URL=jdbc:mysql://localhost:3306/pousada_db`
- **Inicialização do Schema:**
  - Arquivo `src/main/resources/schema.sql` padronizado.
  - Listener de inicialização `DatabaseInitializerListener` disparado automaticamente na subida do container.
  - Sem uso de sintaxes proprietárias incompatíveis (como `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`), garantindo portabilidade entre H2 e MySQL 8.x.

---

## 6. Suíte de Testes Automatizados (Evidências)

A aplicação conta com **42 testes automatizados** via JUnit 5. Para demonstrar à banca:

```bash
mvn test
```

### Cobertura de Testes:
1. `CalculadoraTarifaTest` (8 testes): Validação das estratégias de diárias, longa estadia, PIX e taxas.
2. `ReservaBuilderTest` (6 testes): Validação de regras defensivas de check-in, check-out e capacidades.
3. `ReservaDAOPersistenciaTest` (3 testes): Validação de **transações ACID**, integridade de serviços 1:N e **comprovação de Rollback**.
4. `AcomodacaoDisponibilidadeTest` (4 testes): Detecção de conflito de datas sobrepostas em reservas ativas.
5. `StatusReservaTest` (4 testes): Validação da máquina de estados do ciclo de vida da reserva.
6. `ServicoFactoryTest` (4 testes): Instanciação desacoplada de itens de serviço via Factory Method.
7. `CommandFactoryTest` (4 testes): Despacho de comandos e tratamento de 404.
8. `HospedeReservaDesacoplamentoTest` (2 testes): Desacoplamento da edição de hóspede da reserva.
9. `RedirectPreservacaoTest`, `FormaPagamentoTest`, `AcomodacaoDAOTest`, `ReservaServiceTest`.

---

## 7. Perguntas Prováveis da Banca e Respostas na Ponta da Língua (FAQ)

### ❓ P1: "Por que vocês usaram Command Pattern em vez de mapear um Servlet para cada ação?"
> **Resposta:**  
> *"Porque seguir o padrão Front Controller com Command centraliza o tratamento de requisições, filtros de autenticação, logs e proteção contra CSRF em um único ponto (`ManterReserva`). O Command transforma cada ação em um objeto isolado e reutilizável (`ICommand`), eliminando duplicação de código e facilitando a manutenção e testes unitários."*

---

### ❓ P2: "Qual a diferença prática entre Factory Method e Builder no projeto de vocês?"
> **Resposta:**  
> *"O **Factory Method** (`ServicoFactory`) foi usado para decidir **qual subclasse de serviço instanciar** com base no tipo contratado (Café Colonial, Transfer, etc.), desacoplando o cliente da classe concreta. Já o **Builder** (`ReservaBuilder`) foi utilizado para **construir um único objeto complexo** (`Reserva`) composto por múltiplos atributos e validações defensivas de integridade antes da sua criação."*

---

### ❓ P3: "Como vocês garantem que se o sistema cair ao salvar uma reserva, não ficam serviços órfãos no banco?"
> **Resposta:**  
> *"No `ReservaDAO.cadastrar()`, desabilitamos o auto-commit da conexão JDBC com `con.setAutoCommit(false)`. Inserimos o hóspede, a reserva e a lista de serviços usando a mesma conexão. Se qualquer comando lançar `SQLException`, o bloco `catch` executa `con.rollback()`, desfazendo todas as operações no banco. Temos inclusive um teste unitário (`ReservaDAOPersistenciaTest`) que comprova esse rollback."*

---

### ❓ P4: "Qual é o problema N+1 que vocês resolveram no DAO?"
> **Resposta:**  
> *"No código anterior, a consulta de reservas fazia uma query para listar as reservas e, dentro de um laço de repetição, abria uma nova consulta para cada hóspede e outra para cada serviço. Com 50 reservas, eram 101 idas ao banco de dados. Nós eliminamos isso fazendo um `LEFT JOIN` com os hóspedes na primeira query e buscando todos os serviços em uma única segunda query via `WHERE reserva_id IN (...)`."*

---

### ❓ P5: "Como funciona a segurança das senhas no sistema?"
> **Resposta:**  
> *"Não armazenamos senhas em texto puro. Utilizamos o algoritmo nativo `PBKDF2WithHmacSHA256` em `util.Seguranca`, com 10.000 iterações de derivação de chave e Salt aleatório de 16 bytes por usuário. Além disso, as comparações de senha utilizam tempo constante (`MessageDigest.isEqual`) para mitigar ataques de temporização (Timing Attacks)."*

---

### ❓ P6: "Por que as páginas JSP estão dentro de WEB-INF?"
> **Resposta:**  
> *"Por diretriz de segurança de containers Java EE. Qualquer arquivo dentro do diretório `WEB-INF` fica inacessível via URL direta pelo navegador (devolve 404). Assim, garantimos que nenhum usuário acesse formulários ou telas administrativas contornando os filtros de controle de acesso do `ManterReserva`."*

---

## 8. Glossário Rápido de Conceitos Técnicos

* **ACID:** Atomicidade, Consistência, Isolamento e Durabilidade — propriedades fundamentais de transações em bancos de dados.
* **Front Controller:** Padrão arquitetural Java EE onde um único servlet central recebe todas as requisições da aplicação.
* **GoF (Gang of Four):** Catálogo clássico de 23 padrões de projeto orientados a objetos (Erich Gamma, Richard Helm, Ralph Johnson e John Vlissides).
* **N+1 Queries:** Antipadrão de desempenho em bancos de dados onde o sistema executa 1 consulta inicial e mais N consultas adicionais para obter registros relacionados.
* **PBKDF2:** *Password-Based Key Derivation Function 2* — padrão criptográfico seguro para armazenamento de senhas contra ataques de dicionário e tabelas arco-íris.
* **Open/Closed Principle (OCP):** Princípio de projeto segundo o qual entidades de software devem estar abertas para extensão, mas fechadas para modificação.
* **Timing Attack:** Tipo de ataque cibernético baseado na medição do tempo gasto pelo servidor para comparar dados sensíveis (ex: senhas ou tokens).
