# CleanFinance - Gestor Financeiro Pessoal

Sistema desenvolvido em **Java** para controle de gastos pessoais, projetado para substituir a rigidez das planilhas convencionais por uma solução automatizada, integrada e inteligente.

## 🚀 Objetivo do Projeto
O projeto visa facilitar o lançamento de despesas, tratando de forma inteligente:
- **Gastos Mensais:** Automação de contas que se repetem todo mês.
- **Parcelamentos:** Cálculo automático e projeção de parcelas em meses futuros.
- **Gestão de Cartões:** Controle individualizado por meio de pagamento.
- **Integração com API:** O sistema **já utiliza** serviços de API para a sincronização, consumo e persistência remota de lançamentos, categorias e métodos de pagamento.

## 🏗️ Arquitetura
Este software utiliza os princípios da **Clean Architecture**, dividindo-se em:
- **Domain:** Entidades puras e contratos de repositório.
- **UseCase:** Regras de negócio para parcelamentos, recorrências e manipulação de dados.
- **Infrastructure:** Implementação de adaptadores de persistência e repositórios baseados em API ativa, unificados com uma interface gráfica rica em JavaFX.

## 🛠️ Tecnologias
- Java 21 (LTS)
- Maven (Gerenciador de dependências)
- Integração ativa com API REST (Consumo de dados e persistência remota)
- JavaFX (Interface de usuário)

---
*Status: Em desenvolvimento (Com API Integrada e Funcional)* 🏗️