# Tarefa 1 – Ajustar `/horselealdade help`

**Status: ✅ Concluída**

## Problema
O `config.yml` padrão não continha as novas chaves de configuração dinâmica.

## Solução aplicada
- `config.yml`: Adicionadas as chaves `max-level`, `exclusive-mount-level`, `call-level`, `favorite-level`.
- `help-messages` atualizado para incluir a linha do novo subcomando `config`.
- `sendHelp` em `HorseLealdadeCommand` já estava correto (usa `plugin.getConfig().getStringList`).

## Resultado
Ao apagar o `plugins/HorseLoyalty/config.yml` do servidor e reiniciar, o `saveDefaultConfig()` recria o arquivo com todas as chaves, e `/horselealdade help` exibe todas as mensagens corretamente.
