# Sistema de Gerenciamento de Reservas de Pousada (Pousada Paradiso)

**Disciplina:** Padrões de Projeto (PP) - 5B Eng. Software  
**Instituição:** Universidade de Mogi das Cruzes (UMC)  
**Aluno:** Marco Antonio Lopes Pedro (Grupo G14)  
**Professores:** Prof. Me. Wolley W. Silva / Profa. Dra. Danielle Martin  

---

## 🏖️ Sobre o Projeto
Aplicação Web completa inspirada em plataformas de hospitalidade (Airbnb) para administração de pousadas, permitindo:
- Gestão completa de reservas de hospedagem com 11 atributos de informação;
- Associação 1:1 com o Hóspede titular;
- Associação 1:N com Serviços adicionais da pousada (Café Colonial, Translado, Passeio de Barco, Spa);
- Automação inteligente do processo de negócio (Check-in automático, cálculo de diárias, desconto de fidelidade long-stay, taxa de preservação ambiental e emissão de PIN para fechadura smart-lock).

---

## 🧩 Padrões de Projeto Implementados

1. **DAO (Data Access Object):** Persistência desacoplada com JDBC puro (`PreparedStatement`, `ResultSet`), transações e conexão híbrida (`FabricaConexao` com MySQL e fallback automático para H2 embutido).
2. **MVC (Model-View-Controller) + Front Controller:** Arquitetura Java EE com servlet central (`controller.ManterReserva`), mapeada para `/controller.do` e views JSP modernas e responsivas.
3. **COMMAND:** Interface `ICommand` com classes dedicadas para cada ação do sistema (`CadastraReservaAction`, `AtualizaReservaAction`, `DeletaReservaAction`, `ConsultaByIdReservaAction`, `ConsultaTodosReservaAction`, `EditaReservaAction` e `ProcessarCheckInAutomaticoReservaAction`).
4. **FACTORY METHOD:**
   - Na camada de Controller: instanciação reflexiva dinâmica de comandos via parâmetro `btnop`;
   - Na camada de Domínio: classe criadora abstrata `ServicoFactory` e subclasses concretas (`CafeManhaFactory`, `TransferAeroportoFactory`, `PasseioBarcoFactory`, `SpaRelaxanteFactory`).
5. **BUILDER:** Classe `ReservaBuilder` com interface fluente (`return this;`) e método `constroi()` para validação prévia de regras e construção imutável da entidade complexa.

---

## 🚀 Como Executar Localmente

### Opção 1: Execução Imediata via Maven (com Tomcat Embutido)
```bash
cd pousada-reservas
mvn compile exec:java
```
Acesse no navegador:
👉 **[http://localhost:8080/controller.do](http://localhost:8080/controller.do)**

### Opção 2: Gerar Pacote WAR para Tomcat Tradicional
```bash
mvn clean package
```
O arquivo `.war` estará pronto em `target/pousada-reservas.war`.

### Opção 3: Executar via Docker
```bash
docker build -t pousada-reservas .
docker run -p 8080:8080 pousada-reservas
```

---

## 📊 Diagramas UML
- **Diagrama de Classes:** `diagrama_classes_uml.png`
- **Diagrama de Sequência:** `diagrama_sequencia_uml.png`

---

## 📄 Documentação Completa
O documento formal para entrega da avaliação encontra-se na raiz do projeto:
- `Documentacao_M1_Gerenciamento_Reservas_Pousada.docx` (Formatado em conformidade com as normas ABNT e a estrutura institucional da UMC).
