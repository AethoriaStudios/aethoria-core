# Arquitetura Tecnica Inicial

## Objetivo

Montar uma arquitetura simples o bastante para caber no alpha, mas organizada o bastante para crescer sem reescrever tudo.

## Filosofia

- usar plugins de terceiros apenas para infraestrutura gratuita e madura
- concentrar a identidade do servidor em configuracao propria e plugin proprio
- evitar dezenas de plugins pequenos com responsabilidades sobrepostas
- manter classes, progressao e itens dentro do `aethoria-core`

## Camadas do Projeto

### 1. Infraestrutura do Servidor

Responsavel por:

- iniciar o servidor
- permissions
- placeholders
- logs
- banco
- profiling
- configuracoes basicas

Plugins recomendados:

- Paper 1.20.6
- LuckPerms
- PlaceholderAPI
- ProtocolLib
- Spark
- Vault
- WorldEdit
- WorldGuard
- Citizens

### 2. Conteudo Baseado em Plugin Proprio

Responsavel por:

- classes
- moedas
- dungeon coins
- itens iniciais
- recompensas da dungeon
- troca de classe paga
- regras de bind e trade
- progressao inicial
- integracoes especificas com texture pack

Recomendacao:

O plugin proprio principal continua sendo `aethoria-core`.

## Plugin Proprio Recomendado

### Nome

`aethoria-core`

### Responsabilidades da primeira versao grande

- registrar Aethor e moedas especiais
- controlar dungeon coins
- controlar regras de bind e trade
- controlar troca de classe com custo
- controlar liberacao de dungeons por nivel
- expor placeholders proprios
- integrar menus e NPCs
- servir de base para progressao autoral

### O que nao precisa entrar ainda

- guildas
- continente 2
- sistemas sociais avancados
- full endgame

## Separacao por Sistemas

### Sistema de Classes

Base:

- `aethoria-core`

Entregas:

- classe ativa
- troca de classe
- custo de troca
- progressao futura por classe

### Sistema de Itens

Base:

- `aethoria-core`
- texture pack proprio para visual

Entregas:

- tiers de raridade
- armas iniciais
- materiais iniciais
- consumiveis basicos
- reforja inicial

### Sistema de Economia

Base:

- Vault
- codigo proprio para regras centrais

Entregas:

- Aethor
- Dungeon Coins
- diferenca entre AH e Market
- taxas e sinks

### Sistema de Dungeon

Base:

- regioes, NPCs e logica propria

Entregas:

- verificacao de nivel minimo
- entrega de dungeon coins
- abertura de bau final
- rewards vinculadas ao progresso

## Banco de Dados

### Uso Recomendado

- YAML no comeco
- MySQL ou MariaDB quando entrar fase mais seria de testes

### Dados que precisam persistir

- classe ativa
- moedas
- progresso de dungeon
- bonus diario
- flags de unlock

## Asset Pipeline

Como a decisao agora e usar texture pack proprio:

- modelos e icones vao por `CustomModelData`
- UI continua simples e baseada em inventories
- assets visuais devem ser planejados para nao bloquear o alpha

Recomendacao:

- prototipar primeiro com itens vanilla renomeados
- adicionar o texture pack assim que o loop estiver funcional

## Ordem Tecnica Recomendada

1. subir servidor local em Paper 1.20.6
2. manter infraestrutura gratuita minima
3. consolidar `aethoria-core`
4. implementar classe, item e economia iniciais
5. implementar dungeon 1 e economy loop
6. conectar MariaDB quando o loop estiver estavel

## Risco Principal da Arquitetura

O maior risco agora e tentar substituir cedo demais cada plugin por sistema proprio sem validar o loop. O ideal e manter o que for utilitario em plugins gratuitos e colocar o esforco autoral apenas no que define a identidade do servidor.