<div align="center">

# 🏨 Pousada Paradiso • Sistema de Gestão de Reservas

### ⚡ Plataforma Web com Padrões de Projeto (GoF), Arquitetura em Camadas e Segurança Defensiva

[![Java 17](https://img.shields.io/badge/Java-17-7928CA?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Apache Tomcat](https://img.shields.io/badge/Tomcat-9.0.98-9333EA?style=for-the-badge&logo=apachetomcat&logoColor=white)](https://tomcat.apache.org/)
[![Maven 3.9](https://img.shields.io/badge/Maven-3.9-A855F7?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Docker Ready](https://img.shields.io/badge/Docker-Enabled-6366F1?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License MIT](https://img.shields.io/badge/License-MIT-8B5CF6?style=for-the-badge)](LICENSE)

<br/>

<img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=12,24,35,46&height=180&section=header&text=Pousada%20Paradiso&fontSize=42&fontColor=ffffff&animation=fadeIn&fontAlignY=38&desc=Sistema%20de%20Gerenciamento%20de%20Reservas%20%7C%20Padrões%20de%20Projeto%20UMC&descFontSize=16&descAlignY=58" width="100%" alt="Header Banner" />

</div>

> 📚 **Material de Apresentação:** Consulte o [GUIA_APRESENTACAO.md](GUIA_APRESENTACAO.md) para o roteiro completo de apresentação para a banca acadêmica, resumo dos padrões GoF, scripts de explicação e FAQ de defesa.

---

## 🌌 Visão Geral

Inspirado em plataformas de hospitalidade de alto padrão (*Airbnb*, *Booking*), o **Pousada Paradiso** é uma solução Web desenvolvida em **Java EE (JSP/Servlets 4)** para administração integral de reservas, acomodações, hóspedes e experiências de hotelaria.

O ecossistema implementa regras de negócio para tarifação dinâmica, controle de disponibilidade de quartos, ciclo de vida de check-in automatizado e emissão de chaves digitais (*smart-lock PIN*), sustentado por uma arquitetura em camadas desacoplada e padrões de projeto GoF e arquiteturais.

```
       ┌────────────────────────────────────────────────────────┐
       │   🟣 Padrões GoF: Command, Factory Method, Builder,   │
       │                   Strategy, State                      │
       │   🟣 Arquitetura: MVC + Front Controller, Service, DAO │
       │   🟣 Persistência: JDBC Transacional ACID (H2 / MySQL) │
       │   🟣 Segurança: PBKDF2 (Salt), CSRF, XSS, SecureRandom │
       └────────────────────────────────────────────────────────┘
```

---

## 🔮 Destaques da Solução

- 🛎️ **Gestão Completa de Reservas (CRUD):** 11 atributos de domínio detalhados, controle de status em tempo real (`PENDENTE`, `CONFIRMADA`, `CHECKIN_ATIVO`, `FINALIZADA`, `CANCELADA`) e cálculo dinâmico de diárias.
- 👤 **Hóspede Titular (1:1):** Cadastro completo, credenciais com hash PBKDF2 + salt, controle de perfil (`CLIENTE` / `RECEPCAO`) e proteção contra sequestro de contas e CSRF.
- 🍹 **Itens de Serviços Adicionais (1:N):** Adição modular de serviços de lazer (Café Colonial na Cama, Transfer Privativo, Passeio de Barco, Massagem Terapêutica) persistidos atomicamente.
- ⚡ **Check-in Inteligente Automatizado:** Rotina de validação operacional com máquina de estados que impede check-ins repetidos ou em reservas canceladas, e gera PIN seguro via `SecureRandom`.
- 🛡️ **Banco de Dados Transacional e Resiliente:** Transações ACID com commit/rollback em conexão única, eliminação de N+1 queries via `JOIN` e carga em lote, e suporte a H2 embutido ou MySQL configurado por variáveis de ambiente.

---

## 🧩 Padrões de Projeto (Design Patterns)

O projeto separa claramente os padrões de projeto **GoF (Gang of Four)** dos padrões arquiteturais e de persistência:

### Padrões GoF Implementados

| Padrão | Tipo GoF | Implementação no Projeto | Propósito |
| :--- | :--- | :--- | :--- |
| **Command** | Comportamental | Interface `ICommand` e subclasses (`CadastraReservaAction`, `AtualizaReservaAction`, `DeletaReservaAction`, etc.) | Encapsula cada requisição como um objeto autônomo, desacoplando o Front Controller das operações específicas. |
| **Factory Method** | Criacional | `ServicoFactory` e subclasses concretas (`CafeManhaFactory`, `TransferAeroportoFactory`, `PasseioBarcoFactory`, `SpaRelaxanteFactory`) | Delega a instanciação de serviços adicionais específicos para subclasses especialistas sem acoplamento direto. |
| **Builder** | Criacional | `ReservaBuilder` com interface fluente (`with...()`, `constroi()`) | Garante a construção segura e validada do objeto complexo `Reserva`, conferindo regras defensivas de períodos e capacidades. |
| **Strategy** | Comportamental | `CalculadoraTarifa` e interface `RegraTarifa` (`DescontoLongaEstadia`, `DescontoPix`, `TaxaAmbiental`) | Permite a aplicação dinâmica e extensível de políticas de descontos e taxas de preservação. |
| **State** | Comportamental | `StatusReserva` (enum com regras de transição válidas) | Modela o ciclo de vida formal da reserva (`PENDENTE` ➔ `CONFIRMADA` ➔ `CHECKIN_ATIVO` ➔ `FINALIZADA`), rejeitando transições ilegais. |

### Padrões Arquiteturais e Estruturais

| Padrão | Tipo | Implementação | Propósito |
| :--- | :--- | :--- | :--- |
| **Front Controller + MVC** | Arquitetural | `controller.ManterReserva` mapeado em `/controller.do` | Ponto único de entrada para todas as requisições HTTP, despachando para as Views JSP protegidas em `WEB-INF/views/`. |
| **Service Layer** | Arquitetural | `service.ReservaService` | Centraliza as regras de negócio da pousada, isolando os Controllers da camada de dados. |
| **DAO (Data Access Object)** | Persistência | `ReservaDAO`, `HospedeDAO`, `ItemServicoDAO`, `AcomodacaoDAO` | Encapsula o acesso JDBC com transações atômicas ACID (`setAutoCommit(false)`, `commit`, `rollback`) e elimina consultas N+1. |
| **Factory (Conexão)** | Criacional / Infra | `util.FabricaConexao` | Centraliza a obtenção de conexões JDBC com inicialização thread-safe e schema automático. |

---

## 🔒 Arquitetura de Segurança (OWASP)

A aplicação conta com uma camada de segurança robusta implementada em `util.Seguranca` e no Front Controller:

- 🔑 **Hashing de Senhas (PBKDF2):** Utiliza `PBKDF2WithHmacSHA256` com Salt de 16 bytes e 10.000 iterações. Senhas nunca são persistidas em texto plano.
- ⏱️ **Mitigação de Timing Attacks:** Validação de credenciais e tokens em tempo constante utilizando `MessageDigest.isEqual`.
- 🛡️ **Proteção CSRF:** Tokens de sincronização gerados na sessão do usuário e validados obrigatoriamente em todas as operações POST que alteram estado.
- 🚫 **Prevenção de XSS:** Todas as saídas de texto dinâmicas em páginas JSP passam pelo método `util.Html.esc()`, impedindo injeção de scripts maliciosos.
- 🧭 **Sanitização de Open Redirect:** Bloqueio de redirecionamentos externos e ataques de *CRLF Injection*, aceitando apenas rotas internas controladas.
- 🔐 **Geração Segura de PIN:** Códigos de acesso digital gerados por gerador de números pseudoaleatórios criptograficamente seguro (`java.security.SecureRandom`).

---

## 👥 Contas de Demonstração (Seed)

Para facilitar testes e avaliação da banca examinadora, a aplicação inicializa automaticamente com os seguintes usuários:

| Perfil | E-mail | Senha | Permissões |
| :--- | :--- | :--- | :--- |
| **Recepção** | `recepcao@pousada.com.br` | `admin123` | Acesso ao Painel Administrativo, edição/exclusão de reservas, cadastro manual |
| **Hóspede** | `marco.pedro@pousada.com.br` | `123456` | Acesso a "Minhas Reservas", check-in automático de suas estadias |
| **Hóspede** | `mariana.ramos@email.com` | `123456` | Consulta e visualização de reservas do usuário |

---

## ⚙️ Variáveis de Ambiente

A aplicação segue os princípios do *12-Factor App* e pode ser configurada via variáveis de ambiente:

| Variável | Padrão | Descrição |
| :--- | :--- | :--- |
| `PORT` | `8080` | Porta do servidor HTTP |
| `DB_ENGINE` | `h2` | Motor de banco de dados (`h2` ou `mysql`) |
| `DB_URL` | *(calculado)* | String JDBC completa (ex: `jdbc:mysql://localhost:3306/pousada_db`) |
| `DB_USER` | `sa` (H2) / `root` (MySQL) | Usuário do banco de dados |
| `DB_PASS` | `""` | Senha do banco de dados |
| `DB_PATH` | `./pousada_db` | Caminho do arquivo de dados do H2 local |

---

## 📐 Modelagem e Diagramas UML

Os diagramas representam a arquitetura estrutural e o fluxo dinâmico de comandos da aplicação:

### 🟣 1. Diagrama de Classes
Contempla o relacionamento entre o Front Controller, a interface `ICommand`, as Actions, Factories, Builders, DAOs e Entidades de Domínio.

<div align="center">
  <img src="diagrama_classes_uml.png" alt="Diagrama de Classes UML" width="95%" style="border-radius: 8px; box-shadow: 0 0 20px rgba(147, 51, 234, 0.4);" />
</div>

<br/>

### 🟣 2. Diagrama de Sequência
Exemplifica o ciclo de vida completo de uma requisição Web: submissão no formulário JSP, despacho no Front Controller, execução da Command, chamada ao Service e persistência no DAO.

<div align="center">
  <img src="diagrama_sequencia_uml.png" alt="Diagrama de Sequência UML" width="95%" style="border-radius: 8px; box-shadow: 0 0 20px rgba(147, 51, 234, 0.4);" />
</div>

---

## 🚀 Como Executar o Projeto

### Opção A: Execução Imediata via Maven (Recomendada)

O projeto conta com o **Apache Tomcat 9 Embutido** e banco embutido **H2**, não exigindo nada além do JDK 17 e Maven:

```bash
# 1. Clone o repositório
git clone https://github.com/Marcolopes-malp/Pousa-Patterns.git
cd Pousa-Patterns

# 2. Compile e inicie o servidor instantaneamente
mvn compile exec:java
```

👉 **Acesse no navegador:** [http://localhost:8080/controller.do](http://localhost:8080/controller.do)

---

### Opção B: Execução via Docker (Ambiente Isolado)

Graças ao *multi-stage build* otimizado com cache no `Dockerfile`, o código é compilado com Maven e servido no container oficial do Tomcat 9 com usuário não-root:

```bash
# 1. Construir a imagem Docker
docker build -t pousada-reservas .

# 2. Executar o container na porta 8080
docker run -p 8080:8080 --name pousada pousada-reservas
```

👉 **Acesse no navegador:** [http://localhost:8080/controller.do](http://localhost:8080/controller.do)

---

## 🧪 Suíte de Testes Automatizados (42 Testes)

O projeto conta com uma suíte de testes unitários e de integração cobrindo regras de negócio, Builder, Factory Method, Strategy, State, integridade ACID e controle de concorrência:

```bash
mvn test
```

| Classe de Teste | Quantidade | Escopo Validado |
| :--- | :--- | :--- |
| `CalculadoraTarifaTest` | 8 testes | Padrão Strategy: diárias, descontos longa estadia, PIX e taxa ambiental |
| `ReservaBuilderTest` | 6 testes | Padrão Builder: validações defensivas de datas, períodos e capacidades |
| `StatusReservaTest` | 4 testes | Padrão State: máquina de estados e validações de transição de check-in |
| `ServicoFactoryTest` | 4 testes | Padrão Factory Method: criação de itens de serviços adicionais (1:N) |
| `CommandFactoryTest` | 4 testes | Padrão Command: registro de ações tipadas e resposta HTTP 404 |
| `AcomodacaoDisponibilidadeTest` | 4 testes | Regra de negócio: detecção de reservas sobrepostas e conflito de vagas |
| `ReservaDAOPersistenciaTest` | 3 testes | Padrão DAO: transações ACID, integridade 1:N e comprovação de Rollback |
| `HospedeReservaDesacoplamentoTest`| 2 testes | Integridade: separação da edição de hóspede da edição de reserva |
| `AcomodacaoDAOTest` | 2 testes | Consulta de acomodações e busca por identificador |
| `ReservaServiceTest` | 2 testes | Camada de serviço: orquestração de reservas e validação de check-in |
| `RedirectPreservacaoTest` | 1 teste | Segurança: preservação de parâmetros com URLEncoder |
| `FormaPagamentoTest` | 2 testes | Tipagem forte: conversões e validação do enum de formas de pagamento |

---

## 🗂️ Estrutura do Projeto

```text
pousada-reservas/
├── 🐳 Dockerfile                         # Multi-stage build otimizado com cache (Tomcat 9.0.98)
├── 🐳 .dockerignore                      # Arquivos ignorados pelo build Docker
├── 📦 pom.xml                            # Configurações do Maven (JDK 17 release, escopos provided)
├── 📄 LICENSE                            # Licença MIT
├── 📄 GUIA_APRESENTACAO.md               # Guia mestre de apresentação acadêmica e defesa M1
├── 🖼️ diagrama_classes_uml.png           # Diagrama estrutural de classes
├── 🖼️ diagrama_sequencia_uml.png         # Diagrama comportamental de sequência
├── 📄 README.md                          # Documentação técnica do projeto
└── 📁 src/
    ├── 📁 main/
    │   ├── 📁 java/                      # Código-fonte Java 17
    │   │   ├── 📁 br/com/commandfactory/controller/ # Commands (ICommand, Actions e CommandFactory)
    │   │   ├── 📁 controller/            # Front Controller (ManterReserva)
    │   │   ├── 📁 dao/                   # Persistência JDBC com transações ACID (ReservaDAO, etc.)
    │   │   ├── 📁 model/                 # Entidades, Enums, Builder, Factory Method e Strategy
    │   │   ├── 📁 service/               # Camada de Serviço (ReservaService)
    │   │   └── 📁 util/                  # Conexão, Segurança (PBKDF2), ServidorTomcat e Listeners
    │   ├── 📁 resources/
    │   │   └── 📄 schema.sql             # DDL e inicialização de tabelas compatível com H2 e MySQL
    │   └── 📁 webapp/                    # Interface Visual Web
    │       ├── 📁 css/                   # Estilos responsivos e acessíveis (style.css)
    │       └── 📁 WEB-INF/
    │           ├── 📄 web.xml            # Mapeamento do Front Controller e Listeners
    │           └── 📁 views/             # Views JSP protegidas contra acesso direto
    │               ├── 📁 fragments/     # Fragmentos reutilizáveis (header.jspf, footer.jspf)
    │               └── 📄 *.jsp          # Telas do sistema (index, reserva, admin, etc.)
    └── 📁 test/
        └── 📁 java/                      # Suíte de 42 testes unitários (JUnit 5)
```

---

## 🎓 Informações Acadêmicas

- **Instituição:** Universidade de Mogi das Cruzes (UMC)
- **Curso:** Bacharelado em Engenharia de Software (5º Semestre)
- **Disciplina:** Padrões de Projeto (PP) - Avaliação M1
- **Aluno:** Marco Antonio Lopes Pedro *(Grupo G14)*
- **Corpo Docente:** Prof. Me. Wolley W. Silva & Profa. Dra. Danielle Martin

---

<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=46,35,24,12&height=120&section=footer" width="100%" alt="Footer Glow" />

<sub>Desenvolvido com 💜 por <b>Marco Antonio Lopes Pedro</b> • Engenharia de Software UMC</sub>

</div>
