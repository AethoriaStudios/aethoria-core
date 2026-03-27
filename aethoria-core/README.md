# Aethoria Core

Plugin proprio inicial para Kingdoms of Aethoria.

## Escopo da v0.3

- base do plugin Paper 1.20.6
- comando administrativo `/aethoria`
- persistencia de perfil do jogador em YAML
- suporte opcional a MariaDB por configuracao
- servico de moeda principal `Aethor`
- servico de `Dungeon Coins`
- servico de troca de classe
- servico de recompensa diaria de dungeon

## Subcomandos atuais

- `/aethoria status [player]`
- `/aethoria aethor <get|set|add|remove> <player> [amount]`
- `/aethoria dungeoncoins <get|set|add|remove> <player> [amount]`
- `/aethoria class <get|set|swap> <player> [class]`
- `/aethoria dailybonus <player>`
- `/aethoria reload`

## Proximos passos

- sistema autoral de progressao de classe
- sistema autoral de itens
- regras de bind e trade
- abertura de baus de dungeon
- NPC de troca de classe
- validacao de entrada em dungeon por nivel