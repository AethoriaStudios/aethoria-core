# Servidor Local

Esta pasta sera usada para o servidor local de testes do Kingdoms of Aethoria.

## Estrutura

- `plugins/` recebe o jar do `aethoria-core`
- `paper.jar` deve ser colocado aqui quando formos subir o Paper local
- `eula.txt` sera gerado na primeira execucao do servidor

## Fluxo recomendado

1. Build e deploy do plugin:
   `powershell -ExecutionPolicy Bypass -File .\scripts\build-and-deploy.ps1`
2. Colocar o `paper.jar` nesta pasta
3. Rodar o servidor local
4. Testar os comandos do plugin dentro do jogo

## Comandos atuais do plugin

- `/aethoria status [player]`
- `/aethoria aethor <get|set|add|remove> <player> [amount]`
- `/aethoria dungeoncoins <get|set|add|remove> <player> [amount]`
- `/aethoria class <get|set|swap> <player> [class]`
- `/aethoria dailybonus <player>`
- `/aethoria reload`