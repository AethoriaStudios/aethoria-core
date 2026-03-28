# Minecraft MMO RPG - GDD Inicial

## Visao

Servidor de Minecraft focado em MMORPG PvE medieval, com progressao viciante inspirada em servidores MMO de alta qualidade, mas com uma regra central:

- a dungeon acelera a progressao
- a dungeon nao substitui o mundo aberto

O loop principal do servidor e:

`quest -> grind -> profissao -> craft -> market -> dungeon -> world boss -> upgrade`

## Objetivo do Projeto

Criar um servidor MMO RPG feito apenas com plugins, com identidade propria, forte economia entre jogadores, progresso constante e espaco para expansao por continentes.

## Publico-Alvo

- jogadores que gostam de grind e progressao longa
- jogadores que valorizam economia, itens raros e mercado
- jogadores que preferem PvE a PvP
- jogadores que gostam de repetir conteudo buscando otimizar gear e recursos

## Direcao de Design

- Fantasia principal: medieval classico
- Estrutura do mundo: continentes com dungeons proprias
- Foco do gameplay: PvE com economia e progresso
- Plataforma alvo: Paper/Purpur
- Banco de dados: MySQL/MariaDB
- PvP: fora do escopo inicial
- Guildas: fora do escopo do alpha inicial

## Estrutura de Progressao

### Progressao Global

- Nivel de Aventureiro global de 1 a 30
- Conteudo inicial do alpha ate nivel 12-15
- Liberacao de conteudo por nivel + quest principal

### Classes

- Guerreiro
- Mago
- Paladino

### Regras de Classe

- O jogador escolhe uma classe ativa
- Maestria de classe sobe apenas jogando com aquela classe
- A troca de classe acontece em NPC
- A troca de classe custa moeda do jogo
- Recomenda-se um cooldown curto para troca de classe

### Papel das Classes

- Guerreiro: dano corpo a corpo, consistencia, pressao
- Mago: dano magico, utilidade, controle
- Paladino: tanque/suporte, cura leve, protecao, controle

## Combate

- Estilo: action RPG simples
- Poucas habilidades ativas por classe
- Cooldowns curtos e legiveis
- Sem dash/esquiva no projeto inicial
- Profundidade vem de timing, posicionamento, gear e mecanicas de encounter

## Mundo Inicial

### Tema

Reino de fronteira construido sobre ruinas de um reino antigo.

### Estrutura Recomendada

- Cidade inicial como hub
- Campos e estrada para onboarding
- Floresta antiga para mid early game
- Zona de ruinas e entradas subterraneas para late early game

### Funcao da Cidade

- tutorial
- NPC de classe
- NPC de quests
- NPC de profissao
- AH
- Market
- repair
- travel
- crafting

## Profissoes

Profissoes iniciais:

- Mineracao
- Pesca
- Herbalismo

### Regras

- Recursos aparecem em rotas especificas
- Coleta alimenta crafts relevantes
- Profissoes precisam ser uteis no loop principal

### Uso dos Recursos

- consumiveis
- reforja
- componentes de upgrade
- receitas raras
- crafting de alto nivel

## Itens e Progressao de Gear

### Filosofia

- Nivel e gear dividem a progressao
- O poder nao deve vir apenas de dungeon
- Itens fortes precisam ter multiplas fontes

### Raridades

- comum
- incomum
- raro
- epico
- lendario

### Lendarios

- extremamente raros
- ligados a boss e dungeon
- tambem craftaveis com receita muito dificil

### Reforja

- RNG com protecao de azar
- forte sink de moeda
- parte relevante do endgame

## Economia

### Estrutura

- AH para gear pronto, itens unicos e cosmeticos
- Market para materiais, stackables, consumiveis e boosters

### Entrada de Moeda

- venda entre jogadores
- quests
- drops limitados

### Saida de Moeda

- reforja
- troca de classe
- fast travel
- taxas do AH
- taxas do Market
- repair
- crafts especiais

### Regra de Ouro da Economia

Economia aberta so funciona se o servidor tiver sinks fortes e fontes de moeda controladas.

## Monetizacao

Direcao aceita:

- cosmeticos
- conveniencia leve
- alguns itens vendaveis

Restricoes recomendadas:

- evitar vender poder direto
- evitar gear premium com vantagem clara
- boosters premium negociaveis exigem cuidado extremo

### Risco Principal

Se boosters premium forem negociaveis, a economia pode se aproximar de pay-to-win. Se forem mantidos, devem ser:

- fracos
- temporarios
- inferiores ao beneficio de um jogador ativo
- incapazes de substituir o farm do jogo

## Dungeon 1

### Tema

Catacumbas e ruinas antigas sob o continente inicial.

### Entrada

- nivel minimo
- unlock por progressao
- sem key
- bonus diario de bau

### Estrutura

- 3 alas curtas
- 1 miniboss
- 1 boss final
- duracao alvo de 12 a 18 minutos

### Boss Final

- construto antigo

### Recompensas

- materiais raros
- recipes
- relics
- pecas de set com chance controlada
- pouca moeda bruta
- dungeon coins para abrir baus e desbloquear recompensas

### Regra de Design

A dungeon precisa ser relevante, mas nao pode matar o valor do mundo aberto.

## World Boss

- horario fixo
- sistema de fragmentos para summon ou acesso
- foco em encontro social recorrente

## Fora do Escopo do Alpha

- PvP
- guildas completas
- continente 2
- endgame completo
- excesso de classes

## MVP do Alpha Fechado

- 1 cidade inicial
- 1 continente inicial
- 3 classes
- 15 a 25 quests uteis
- 8 a 12 mobs customizados
- 1 dungeon de teste
- 1 world boss
- 3 profissoes
- AH + Market
- sistema de party
- UI limpa e legivel

## Principios de Design

- cada sessao de jogo precisa render progresso visivel
- o jogador precisa sempre ter mais de uma forma valida de progredir
- profissao, dungeon e mundo aberto precisam ter valor real
- o alpha precisa validar o loop, nao impressionar com quantidade
- o servidor precisa parecer autoral, nao apenas uma copia de minigame
