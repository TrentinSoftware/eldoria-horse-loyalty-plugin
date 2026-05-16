# Tarefa 2 – Configuração Dinâmica

**Status: ✅ Concluída**

## Objetivo
Permitir que administradores alterem configurações em tempo real via `/horselealdade config`.

## Arquivos modificados / criados

### config.yml
- Adicionadas: `max-level: 10`, `exclusive-mount-level: 5`, `call-level: 8`, `favorite-level: 10`

### LoyaltyManager.java
- Adicionados métodos: `getMaxLevel()`, `getExclusiveMountLevel()`, `getCallLevel()`, `getFavoriteLevel()`
- `setLoyalty`: usa `getMaxLevel()` em vez de `10` hardcoded
- `addXP`: usa `getMaxLevel()` em vez de `10` hardcoded

### HorseProtectListener.java
- Usa `manager.getExclusiveMountLevel()` em vez de `5`

### CallHorseCommand.java
- Usa `manager.getCallLevel()` em vez de `8`

### FavoriteHorseCommand.java
- Usa `manager.getFavoriteLevel()` em vez de `10`

### HorseLealdadeCommand.java
- Agora implementa `CommandExecutor, TabCompleter`
- Adicionado case `"config"` no switch que delega para `ConfigCommand.handle()`
- Corrigido exibição de nível máximo em `get` para usar `manager.getMaxLevel()`
- Método `onTabComplete`: sugere subcomandos e delega para `ConfigCommand.tabComplete()`

### ConfigCommand.java (nova classe)
- Chaves int suportadas: `max-level`, `exclusive-mount-level`, `call-level`, `favorite-level`, `riding-xp-per-second`, `riding-xp-per-block`, `feeding.apple`, `feeding.golden_apple`, `feeding.enchanted_golden_apple`
- Chaves de lista: `level-xp-requirements`
- Operações: `get`, `set`, `add`, `remove`
- Persiste com `plugin.saveConfig()` após cada alteração
- Tab-completion via `ConfigCommand.tabComplete()`

### HorseLoyaltyPlugin.java
- `HorseLealdadeCommand` registrado como `TabCompleter` também

### plugin.yml
- Uso do `horselealdade` atualizado para incluir `config|help`

## Exemplos de uso
```
/horselealdade config get max-level
/horselealdade config set max-level 15
/horselealdade config set riding-xp-per-second 2
/horselealdade config get level-xp-requirements
/horselealdade config add level-xp-requirements 250
/horselealdade config remove level-xp-requirements 3
```
