# Blinded

Estrutura inicial de plugin Paper 1.21.1 para economia com menus visuais e integração Bedrock (Geyser/Floodgate).

## Próximos passos
- Implementar menus visuais e sistema de economia.
- Detalhar moedas, taxas e comandos administrativos.

## Comandos
- /money: mostra seu saldo
- /money pay <jogador> <valor>: enviar dinheiro
- /money set <jogador> <valor>: definir saldo (perm: blinded.admin)
- /money add <jogador> <valor>: adicionar saldo (perm: blinded.admin)
- /money take <jogador> <valor>: remover saldo (perm: blinded.admin)
- /painel: abre o painel principal
- /login: abre painel de login
- /register: abre painel de registro

## Configuração
Arquivo config.yml:
- economy.currency-symbol
- economy.starting-balance
- auth.min-password-length
- jobs.cooldown-seconds
- jobs.multiplier
- Implementar menus visuais e sistema de economia.

## Build
Use Maven para gerar o JAR do plugin.
