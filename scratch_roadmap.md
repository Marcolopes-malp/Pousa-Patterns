<USER_REQUEST>
# Análise de melhorias — Pousada Paradiso (Pousa-Patterns)

> Revisão do código no commit `d2b1991` (29/09/2026). Os números de linha se referem a esse commit.
> A análise foi feita lendo o código, sem compilar nem executar a aplicação.

## Como usar este documento (instruções para a IA)

- **Stack a manter:** Java 17, `javax.servlet` 4 / JSP, Tomcat 9 e JDBC puro (H2/MySQL), sem Spring. **Não** migre para `jakarta.*`, porque o Tomcat 9 usa `javax.*`.
- **Contexto acadêmico:** é a disciplina de Padrões de Projeto (UMC). Preserve os padrões exigidos, que são DAO, MVC + Front Controller, Command, Factory Method e Builder, e mantenha os nomes em português.
- Trabalhe **na ordem de prioridade**, uma tarefa por commit, e rode `mvn compile` depois de cada uma.
- Não altere o visual das páginas, a menos que a tarefa peça.
- Cada tarefa traz **Onde**, **Problema**, **Correção** e **Pronto quando** (critério de aceite).

## Resumo

| ID | Prioridade | Tarefa |
| :-- | :-- | :-- |
| S1 | P0 | Sequestro de conta ao reservar sem login |
| S2 | P0 | Falta de controle de acesso (admin e ações de reserva) + CSRF |
| S3 | P0 | XSS em todas as JSPs |
| S4 | P0 | Open redirect no login/cadastro |
| S5 | P0 | Senhas em texto puro, senha padrão e sessão |
| S6 | P0 | Valor da diária enviado pelo navegador |
| S7 | P1 | PIN inseguro, vazamento de erros, credenciais no código |
| B1 | P1 | Três cálculos de preço diferentes |
| B2 | P1 | Check-in sem validação de status (repetível) |
| B3 | P1 | Datas fixas e ausência de validação de período |
| B4 | P1 | Redirect do login perde a acomodação escolhida |
| B5 | P2 | Valores padrão silenciosos que escondem erros |
| B6 | P2 | Reserva sem vínculo com a acomodação e sem controle de disponibilidade |
| B7 | P2 | Editar reserva altera o e-mail de login do hóspede |
| A1–A6 | P2 | Arquitetura: JSP chamando DAO, reflexão, camada de serviço, tipos, pacotes, layout |
| D1–D3 | P2 | Persistência: N+1, transações, fallback de banco |
| C1–C2 | P3 | Build (pom) e Docker |
| T1–T3 | P3 | README, diagramas UML, testes e CI |

---

## P0 — Segurança

### S1 — Sequestro de conta ao reservar sem login
**Onde:** `src/main/java/br/com/commandfactory/controller/CadastraReservaAction.java:22-41`
**Problema:** quando o visitante não está logado, a action busca o hóspede pelo e-mail digitado (`hdao.buscarPorEmail(email)`). Se o e-mail já existir, esse hóspede vai para a sessão (linha 40) **sem pedir senha**. Qualquer pessoa que saiba o e-mail de um cliente passa a ver as reservas e os PINs dele em "Minhas Reservas". Além disso, contas novas são criadas com a senha padrão `123456` (linhas 29-31), e o cliente não fica sabendo disso.
**Correção:**
- Se o e-mail já existe e não há sessão, **não** autentique. Redirecione para `login.jsp` com a mensagem "E-mail já cadastrado, faça login", preservando o `redirect`.
- Se o e-mail é novo, exija senha na etapa 2 do `reserva.jsp` (novo campo `txtSenha`) e remova o fallback `123456`.

**Pronto quando:** uma reserva enviada sem login com o e-mail de outro hóspede não cria sessão nem dá acesso às reservas dele.

### S2 — Falta de controle de acesso e CSRF
**Onde:**
- `src/main/webapp/admin.jsp`, `formCadastro.jsp` e `formEditar.jsp`, que qualquer pessoa acessa direto;
- `index.jsp:127`, com link público para o admin;
- as actions `DeletaReservaAction`, `EditaReservaAction`, `AtualizaReservaAction`, `ConsultaByIdReservaAction` e `ProcessarCheckInAutomaticoReservaAction`, que não checam login nem dono da reserva.

**Problema:** `controller.do?btnop=ConsultaById&id=1` mostra CPF, e-mail e telefone de qualquer hóspede. `controller.do?btnop=Deleta&id=1` apaga uma reserva via GET, e um simples `<img src=...>` num site externo já basta para disparar isso (CSRF).
**Correção:**
- Criar um perfil de usuário (por exemplo, coluna `perfil` em `hospedes` com os valores `CLIENTE`/`RECEPCAO`) e um seed de usuário da recepção.
- Criar um `javax.servlet.Filter`, ou fazer a checagem no `ManterReserva`, com a lista de comandos restritos à `RECEPCAO` (`Deleta`, `Edita`, `Atualiza`, painel admin, cadastro manual).
- Para o perfil `CLIENTE`, `ConsultaById` e `ProcessarCheckInAutomatico` só devem funcionar se `reserva.getHospede().getId() == usuarioLogado.getId()`.
- Mover as JSPs para `WEB-INF/views/`, para bloquear o acesso direto. O admin passa a ser servido por um Command (ver A1).
- Ações que alteram estado (`Deleta`, `ProcessarCheckInAutomatico`, `Cadastra`, `Atualiza`, `LogoutCliente`) devem aceitar só POST e validar um token CSRF guardado na sessão e enviado num campo hidden.
- Remover o link do admin do rodapé público.

**Pronto quando:**
- sem login, `ConsultaById&id=1` redireciona para o login;
- o cliente A recebe erro ao abrir a reserva do cliente B;
- `GET ...btnop=Deleta&id=1` não apaga nada.

### S3 — XSS em todas as JSPs
**Onde:** todo `<%= ... %>` imprime o valor sem escape. Exemplos:
- refletido: `login.jsp:45,63`, `cadastro.jsp:45,84`, `reserva.jsp:152,156` (parâmetros da URL);
- armazenado: `detalhesReserva.jsp:138`, `minhasReservas.jsp:76`, `admin.jsp:111` e `formEditar.jsp:58-137` (dentro de `value="..."`);
- `resultado.jsp:37`, que exibe `msg`, e essa mensagem pode conter `e.getMessage()` com dados do usuário.

**Correção:** o JSTL já está no `pom.xml` e não é usado. Adicione `<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>` e troque os scriptlets por EL + `<c:out value="${...}"/>`, incluindo os atributos HTML. Se preferir uma migração mais rápida, crie um helper `util.Html.esc(String)` e aplique em todos os `<%= %>` que imprimem texto.
**Pronto quando:**
- `login.jsp?redirect=%22%3E%3Cscript%3Ealert(1)%3C/script%3E` não executa o script;
- uma observação com `<b>teste</b>` aparece como texto literal.

### S4 — Open redirect
**Onde:** `LoginClienteAction.java:24-27` e `CadastraClienteAction.java:37-40`, com `response.sendRedirect(redirect)` sem validação.
**Correção:** aceitar apenas caminhos internos. Por exemplo, o valor precisa começar com `controller.do` e não pode conter `//`, `://` nem `\`. Qualquer outro valor cai no destino padrão.
**Pronto quando:** `redirect=https://site-externo.com` leva para `controller.do?btnop=MinhasReservas`.

### S5 — Senhas e sessão
**Onde:**
- `HospedeDAO.java:15-51`, que grava a senha em texto puro e autentica com `WHERE email = ? AND senha = ?`;
- `HospedeDAO.java:24`, `Hospede.java:33-35,46-48` e `FabricaConexao.java:61,94-97`, com a senha padrão `123456`;
- `login.jsp:49,54`, com e-mail e senha reais pré-preenchidos.

**Correção:**
- Guardar hash com salt. Sem dependência nova, dá para usar PBKDF2 via `javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")`; também serve adicionar jBCrypt. Salve algo como `salt:hash` na coluna `senha`, aumentando o tamanho dela.
- Em `autenticar`, buscar por e-mail e comparar o hash em Java.
- Remover os construtores de `Hospede` que colocam `"123456"`.
- Validar no servidor uma senha de pelo menos 6 caracteres.
- Depois do login, chamar `request.changeSessionId()` para evitar session fixation, e fazer `setSenha(null)` antes de colocar o `Hospede` na sessão.
- Tirar os `value=` do `login.jsp`. Se quiser facilitar a apresentação, mostre as credenciais de demonstração como texto de ajuda.
- Os seeds devem gravar o hash.

**Pronto quando:** nenhuma senha aparece em texto puro no banco, o login continua funcionando com os usuários seed e a sessão guarda o `Hospede` sem senha.

### S6 — Valor da diária enviado pelo navegador
**Onde:** `reserva.jsp:125-126`, com os campos hidden `txtTipoQuarto` e `txtValorDiaria`, e `CadastraReservaAction.java:50-54`.
**Problema:** basta alterar o hidden para reservar a R$ 1.
**Correção:**
- Enviar `acomodacaoId`. No servidor, buscar a acomodação com `AcomodacaoDAO.buscarPorId` e usar o `valorDiaria` e o nome que vêm dela.
- Validar que `1 <= qtdHospedes <= capacidadePessoas`.
- No cadastro manual da recepção (`formCadastro.jsp`), um valor manual pode ser aceito, mas somente para o perfil `RECEPCAO`.

**Pronto quando:** um POST com `txtValorDiaria=1` grava o preço correto da acomodação.

### S7 — Outros pontos de segurança
- **PIN:** está em `ProcessarCheckInAutomaticoReservaAction.java:73`. Trocar `Math.random()` por `java.security.SecureRandom` e parar de gravar o PIN em `observacoes` (linhas 78-81). Guardá-lo numa coluna própria (`pin_acesso`) exibida só ao dono da reserva.
- **Erros:** todas as actions e o `ManterReserva.java:67` devolvem `e.getMessage()` para a tela. Registre o erro com `java.util.logging.Logger`, em vez de `printStackTrace`, e mostre uma mensagem genérica.
- **Credenciais do banco:** estão fixas em `FabricaConexao.java:24-26`, com `root` e senha vazia. Ler de variáveis de ambiente (`DB_URL`, `DB_USER`, `DB_PASS`); ver D3.

---

## P1 — Bugs de regra de negócio

### B1 — Três cálculos de preço diferentes
**Onde:**
- `reserva.jsp:405-466`, o JS da tela;
- `ReservaBuilder.java:146-164`, que calcula o valor gravado no banco;
- `ProcessarCheckInAutomaticoReservaAction.java:38-70`, que recalcula no check-in;
- `Reserva.java:176-185`, com `calcularTotalServicos()`, que ninguém usa;
- as fábricas em `model/factory/*`, que criam sempre quantidade 1.

**Problema:** no exemplo do Bangalô (R$ 450) por 5 noites, pagando com PIX e sem serviços, o sistema produz três totais:

| Onde | Total | Regra aplicada |
| :-- | :-- | :-- |
| Tela | R$ 1.923,75 | 10% de longa estadia + 5% de PIX |
| Banco | R$ 2.250,00 | nenhum desconto |
| Depois do check-in | R$ 2.085,75 | 10% de desconto + taxa ambiental de 3% |

A taxa ambiental não aparece no checkout, que ainda anuncia "Taxa de serviço: Grátis" (`reserva.jsp:356`). A tela cobra o café por diária e o passeio por pessoa, mas as fábricas criam sempre `quantidade = 1`. O desconto do PIX nunca é aplicado no servidor.
**Correção:**
1. Definir as regras oficiais: quais descontos e taxas valem e quando.
2. Criar **uma** classe no servidor, por exemplo `service.CalculadoraTarifa`. Ela recebe `valorDiaria, checkIn, checkOut, qtdHospedes, servicos, formaPagamento` e devolve um `ResumoTarifa` com `noites, subtotalDiarias, descontoEstadia, totalServicos, descontoPix, taxaAmbiental, total`.
3. Implementar cada regra como **Strategy** (`interface RegraTarifa`, com as implementações `DescontoLongaEstadia`, `DescontoPix` e `TaxaAmbiental`). Isso também acrescenta um padrão ao trabalho.
4. Usar a calculadora no `ReservaBuilder`/`CadastraReservaAction`. O check-in **não** recalcula o preço, só muda o status (ver B2).
5. Ajustar a quantidade dos serviços: café = noites, passeio = hóspedes. Uma opção é `ServicoFactory.criarServico(noites, hospedes)`; outra é a action chamar `setQuantidade`.
6. O JS da tela serve apenas de prévia e deve usar as mesmas constantes. O valor definitivo é o que o servidor devolve na página de confirmação.
7. Remover o loop duplicado e usar `Reserva.calcularTotalServicos()`.

**Pronto quando:** tela, banco e comprovante mostram o mesmo total no exemplo acima, e existem testes JUnit para 1, 4 e 7 noites, PIX e cartão, com e sem serviços.

### B2 — Check-in sem validação de status
**Onde:**
- `ProcessarCheckInAutomaticoReservaAction.java:26-36`;
- os botões em `detalhesReserva.jsp:67`, `admin.jsp:124` e `minhasReservas.jsp:92`.

**Problema:** o servidor não confere o status. O check-in pode ser repetido e também roda em reserva `CANCELADA`. Cada execução gera um PIN novo e concatena cerca de 90 caracteres em `observacoes`, que é `VARCHAR(500)`, então depois de umas 5 execuções o UPDATE falha.
**Correção:**
- Criar um enum `StatusReserva` com as transições válidas. O padrão **State** é opcional e fica bom no trabalho.
- O check-in só vale para `CONFIRMADA` e a partir da data de entrada (defina se a véspera também é aceita). Rejeite com mensagem os status `CHECKIN_ATIVO`, `CANCELADA` e `FINALIZADA`.
- Não concatenar texto em `observacoes`.
- As JSPs devem mostrar o botão apenas quando a transição for válida.

**Pronto quando:** o segundo check-in devolve um aviso sem alterar os dados, e o check-in de uma reserva cancelada devolve erro.

### B3 — Datas fixas e validação de período
**Onde:**
- `index.jsp:62,66`, `reserva.jsp:17,19` e `formCadastro.jsp:67,71`, com as datas fixas `2026-10-10` e `2026-10-15`, que ficam no passado a partir de 10/10/2026;
- `ReservaBuilder.java:130-138,152-154`, onde um check-out anterior ao check-in vira 1 diária sem aviso.

**Correção:**
- Padrões calculados: `LocalDate.now().plusDays(1)` e `plusDays(6)`.
- Atributo `min` nos `<input type="date">`.
- No `constroi()`, lançar `IllegalArgumentException` quando `checkOut <= checkIn`, quando o `checkIn` estiver no passado (em reservas novas) ou quando `qtdHospedes` estiver fora de `1..capacidade`.

**Pronto quando:** as datas padrão ficam sempre no futuro e um período inválido gera uma mensagem de erro clara.

### B4 — Redirect do login perde a acomodação
**Onde:** `reserva.jsp:110`, `login.jsp:63` e `cadastro.jsp:84`.
**Problema:** o link `login.jsp?redirect=controller.do?btnop=NovaReserva&acomodacaoId=3` não usa URL-encode. O `acomodacaoId` vira parâmetro do próprio login e, depois de entrar, o usuário sempre cai na acomodação 1.
**Correção:** usar `URLEncoder.encode(..., "UTF-8")` ou `<c:url>`/`<c:param>`, incluindo também as datas e a quantidade de hóspedes.
**Pronto quando:** quem entra pela tela da acomodação 3 volta para a acomodação 3 com as mesmas datas.

---

## P2 — Correções de robustez

### B5 — Valores padrão silenciosos
- `AcomodacaoDAO.java:73-80` devolve a primeira acomodação quando o id não existe. Deve devolver `null` ou `Optional`, e a action deve mostrar "não encontrada".
- `ServicoFactory.java:17-33` devolve café para um tipo nulo ou desconhecido. Deve lançar `IllegalArgumentException`.
- `NovaReservaAction.java:12-15` e `CadastraReservaAction.java:45-54` engolem erros de parse e usam valores padrão. Devem validar e informar o erro.

### B6 — Reserva sem vínculo com a acomodação
**Onde:**
- `Reserva.tipoQuarto` é texto livre, editável em `formEditar.jsp:96`;
- os seeds em `FabricaConexao.java:99-102` usam nomes que não batem com o catálogo ("Bangalô Vista Mar" contra "Bangalô Vista Mar & Deck Privativo");
- `vagasRestantes` é um número fixo em `AcomodacaoDAO`.

**Correção:**
- Criar a tabela `acomodacoes` e a coluna `reservas.acomodacao_id` (FK).
- No `formEditar`, trocar o texto livre por um `<select>`.
- Opcionalmente, checar a disponibilidade contando as reservas que se sobrepõem ao período: `checkin < :out AND checkout > :in AND status <> 'CANCELADA'`.

### B7 — Editar reserva altera o login do hóspede
**Onde:** `AtualizaReservaAction.java:18-27`, que chama `ReservaDAO.atualizar` (`ReservaDAO.java:79-81`), que por sua vez chama `HospedeDAO.atualizar`.
**Problema:** ao editar uma reserva, o e-mail do hóspede, que também é o login, muda junto. Se o novo e-mail já existir em outra conta, o banco estoura com violação de `UNIQUE` e mostra um erro cru.
**Correção:** separar a edição do hóspede da edição da reserva, ou pelo menos validar a duplicidade antes de salvar e deixar claro na tela que o login será alterado.

### A — Arquitetura e padrões
- **A1. JSPs chamando DAO:** `admin.jsp:5-23`, `index.jsp:6-10` e `reserva.jsp:7-14` acessam o banco direto e contornam o Front Controller/Command. Crie Commands para essas telas (por exemplo, `PainelRecepcaoAction`), faça o `ConsultaTodos` carregar as **acomodações** e mova as views para `WEB-INF/views/`, com forward para `/WEB-INF/views/x.jsp`.
- **A2. Despacho por reflexão:** `ManterReserva.java:39-50` monta o nome da classe a partir do parâmetro da requisição, com um fallback "com ou sem o sufixo Reserva".
  - Substitua por um registro explícito (classe `CommandFactory` com `Map<String, Supplier<ICommand>>`) e padronize os nomes das actions.
  - Corrija o Javadoc da linha 14, que chama `Class.forName` de Factory Method. O registro, sim, pode ser apresentado como uma fábrica de Commands.
  - Os comandos desconhecidos devem retornar 404.
- **A3. Camada de serviço:** a regra de negócio está dentro da Command (`ProcessarCheckIn...:38-81`). Crie um `ReservaService` com `cadastrar`, `checkIn` e `atualizar`; as Commands ficam só com a leitura da requisição, a chamada ao serviço e a escolha da view.
- **A4. Tipos:**
  - datas: `String` vira `LocalDate`;
  - dinheiro: `double` vira `BigDecimal`;
  - status e forma de pagamento: `String` vira enum (`StatusReserva`, `FormaPagamento`);
  - no banco: colunas `DATE` e `DECIMAL(10,2)`.
- **A5. Pacotes:** unificar `controller`, `dao`, `model`, `util` e `br.com.commandfactory.controller` sob `br.com.pousada.*` (`controller`, `command`, `dao`, `model`, `model.factory`, `service`, `util`). Atualize junto o `web.xml`, o `mainClass` no `pom.xml` e o registro de Commands.
- **A6. Views:**
  - header e footer estão duplicados em 10 JSPs; extraia para `WEB-INF/views/fragments/*.jspf` e inclua com `<%@ include %>`;
  - leve os `style=""` inline para o `style.css`;
  - `detalhesReserva.jsp:79` usa `grid-template-columns: 1fr 1fr` sem breakpoint (existe só `@media (max-width: 900px)` em `style.css:630`);
  - os radios de pagamento com `display:none` (`reserva.jsp:80-82`) não funcionam pelo teclado.

### D — Persistência e desempenho
- **D1. N+1 e consulta inútil:** `ConsultaTodosReservaAction.java:17-20` é a ação padrão da home. Ela carrega **todas** as reservas em `listaReservas`, que o `index.jsp` nunca lê. Além disso, `ReservaDAO.mapearReservaCompleta` (linhas 167-193) abre 2 conexões extras por reserva. Remova a consulta da home e, no admin, use `JOIN` com `hospedes` e busque os serviços com um único `WHERE reserva_id IN (...)`.
- **D2. Transações:** `ReservaDAO.cadastrar` (24-76) e `atualizar` (78-111) gravam reserva, hóspede e serviços em conexões separadas. Use uma única `Connection` com `setAutoCommit(false)`, `commit` e `rollback`, e faça os sub-DAOs receberem essa `Connection` como parâmetro.
- **D3. `FabricaConexao`:**
  - hoje tenta o MySQL a cada chamada e, se falhar, cai em silêncio para o H2 (linhas 22-35), podendo trocar de banco com a aplicação no ar;
  - a flag `tabelasInicializadas` não é thread-safe e fica `true` mesmo quando a inicialização falha;
  - correção: escolher o banco por variável de ambiente (H2 como padrão), inicializar o schema uma única vez num `ServletContextListener` a partir de `src/main/resources/schema.sql` e, se possível, usar um pool (HikariCP);
  - o caminho do H2 (`./pousada_db`) depende do diretório de execução: configure-o por variável de ambiente;
  - o `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` (linha 61) não é compatível com MySQL.

---

## P3 — Build, docs e testes

### C1 — `pom.xml`
- O Tomcat embutido (`tomcat-embed-*`, `tomcat-jasper`, `tomcat-util-scan`, linhas 49-73) está com escopo `compile` e acaba inteiro dentro do WAR, que o Docker implanta em outro Tomcat. Use `<scope>provided</scope>` e, no `exec-maven-plugin` (linhas 87-94), `<configuration><classpathScope>compile</classpathScope>`, para o `mvn compile exec:java` continuar funcionando.
- Atualizar `tomcat.version` (linha 17, hoje 9.0.86) para a última 9.0.x, que corrige CVEs.
- Trocar `maven.compiler.source`/`target` por `maven.compiler.release=17`.
- O servlet está registrado duas vezes, via `@WebServlet` (`ManterReserva.java:18`) e via `web.xml:10-23`. Mantenha só uma das formas.
- **Pronto quando:** o `WEB-INF/lib` do WAR não contém nenhum `tomcat-*.jar` e o `mvn compile exec:java` sobe normalmente.

### C2 — Docker
- Copiar o `pom.xml` e rodar `mvn -B dependency:go-offline` **antes** do `COPY src`, para aproveitar o cache.
- Fixar a imagem numa versão `tomcat:9.0.<x>-jdk17-temurin` igual à do pom.
- Rodar com um usuário que não seja root.
- Criar um volume para os dados do H2, ou configurar o MySQL por variável de ambiente.
- Adicionar um `.dockerignore` (com `target/`, `.git`, `*.png`).

### T1 — README
- O badge MIT aponta para um `LICENSE` que não existe: adicione o arquivo ou remova o badge.
- Remover as promessas que o código não cumpre: "transações ACID" (linha 53, antes de D2), "imutabilidade" (linha 57, já que `Reserva` tem setters) e "validação cadastral" (linha 40).
- DAO, MVC e Front Controller **não** são GoF (linhas 49-57). Separe os padrões GoF (Command, Factory Method, Builder e, se forem implementados, Strategy e State) dos padrões arquiteturais.
- Corrigir a árvore de pastas (linhas 122-140): as Actions ficam em `br/com/commandfactory/controller/`, ou no novo pacote, se A5 for feito.
- Documentar as credenciais de demonstração e as variáveis de ambiente.

</USER_REQUEST>
<ADDITIONAL_METADATA>
The current local time is: 2026-09-29T13:40:24-03:00.

The user's current state is as follows:
Active Document: /Users/mestresdaweb/estimulos/estimulos-app-atendente-terapeutico/app.json (LANGUAGE_JSON)
Cursor is on line: 59
Other open documents:
- /Users/mestresdaweb/estimulos/estimulos-app-atendente-terapeutico/app.json (LANGUAGE_JSON)
Running terminal commands:
- yarn dev (in /Users/mestresdaweb/estimulos/estimulus-backend, running for 2h18m35s)
- yarn dev (in /Users/mestresdaweb/estimulos/estimulos-web-master, running for 2h18m15s)
</ADDITIONAL_METADATA>