# Tarefa 4 – Corrigir permissões e help condicional

**Status: ✅ Concluída**

## Problema
- O comando `lealdadecavalo` exigia `lealdadecavalo.admin` (OP) para *todos* os subcomandos, bloqueando jogadores comuns.
- Subcomandos públicos (`get`, `item`, `rename`, `help`) devem ser acessíveis a qualquer jogador.
- Apenas `set` e `config` devem exigir `lealdadecavalo.admin`.
- O `help` deve mostrar somente os comandos que o jogador tem permissão para usar.

## Alterações planejadas

### `plugin.yml`
- Remover `permission: lealdadecavalo.admin` do comando `lealdadecavalo` (validação será feita por subcomando no código).
- Adicionar nova permissão `lealdadecavalo.use` (`default: true`) para subcomandos públicos.
- Adicionar `description` a todas as permissões.

### `HorseLealdadeCommand.java`
- Inserir bloco de verificação de permissão por subcomando logo após o cast de `Player`:
  - `set` e `config` → exigem `lealdadecavalo.admin`
  - demais subcomandos (e sem args) → exigem `lealdadecavalo.use`

### `config.yml`
- Atualizar `help-player` para incluir a entrada `/lealdadecavalo rename` e usar cores `&a`.
- Substituir `help-admin` por apenas 2 linhas com cores `&c` (set e config).

## Resultado esperado
- Jogadores sem OP usam `get`, `item`, `rename`, `help`, `horsecatalog`, `callhorse`, `favoritehorse`, `vemcapocoto` normalmente.
- `/lealdadecavalo set` e `/lealdadecavalo config` são negados para não-OP.
- `/lealdadecavalo help` mostra apenas as linhas públicas para não-OP; com OP, mostra também as linhas admin.
