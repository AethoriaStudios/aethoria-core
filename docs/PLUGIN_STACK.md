# Stack Inicial de Plugins

## Regra do Projeto

A partir de agora, a stack deve seguir esta regra:

- plugins gratuitos apenas para infraestrutura e utilidades maduras
- sistemas centrais de RPG feitos no `aethoria-core`
- evitar dependencias premium para combate, classes, itens e progressao

## Base do Servidor

- Paper 1.20.6
- LuckPerms
- PlaceholderAPI
- ProtocolLib
- Citizens
- Vault
- Spark
- WorldEdit
- WorldGuard
- DecentHolograms

## Sistemas Autorais

Esses sistemas passam a ser responsabilidade do `aethoria-core`:

- classes
- troca de classe
- moedas
- dungeon coins
- progressao basica
- regras de trade e bind
- rewards de dungeon
- validacao de acesso por nivel
- parte central de itens e economia do projeto

## Quests e NPCs

Plugins gratuitos aceitaveis:

- Citizens
- um plugin de quests gratuito a definir depois, se realmente for necessario

Observacao:

Se o plugin de quests atrapalhar mais do que ajudar, vale mais manter a linha principal inicial em logica autoral simples do que criar dependencia errada cedo demais.

## Economia

Base gratuita recomendada:

- Vault
- um plugin gratuito de leilao, se encontrarmos um que sirva
- lojas controladas simples, preferencialmente via configuracao ou codigo proprio

Observacao:

O ideal continua sendo separar claramente:

- moeda principal
- dungeon coins
- market de materiais
- AH de gear e cosmeticos

## Mundo e Regioes

- WorldEdit
- WorldGuard
- Multiverse-Core se precisar de varios mundos
- texture pack proprio para identidade visual e assets

## UI e Qualidade de Vida

- DeluxeMenus se continuar gratuito e fizer sentido
- PlaceholderAPI
- texture pack proprio para icones, modelos e assets customizados

## Banco e Infra

- MySQL ou MariaDB
- sistema de backup automatico
- Spark para profiling
- plugin de logs e moderacao basico

## Stack Recomendada para o Primeiro Teste

- Paper 1.20.6
- LuckPerms
- PlaceholderAPI
- ProtocolLib
- Citizens
- Vault
- WorldEdit
- WorldGuard
- Spark
- DecentHolograms
- `aethoria-core`

## O que Vai Ser Feito no Plugin Proprio

- sistema de classes inicial
- sistema de itens inicial
- moeda Aethor
- dungeon coins
- regras de market e AH
- rewards da dungeon 1
- progressao inicial do alpha

## Beneficio Dessa Direcao

Essa troca reduz custo, aumenta controle e evita que o servidor nasca preso a plugins premium que depois limitam o design do projeto.