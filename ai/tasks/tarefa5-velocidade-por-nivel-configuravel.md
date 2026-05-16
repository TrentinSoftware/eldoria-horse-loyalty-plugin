# Tarefa 5 – Velocidade por nível configurável

**Status:** ✅ Concluída

## O que foi feito

### `LoyaltyManager.java` – método `applySpeed`
- Substituída a linha `double extra = loyalty * 0.1;` (hardcoded) pela leitura das configurações:
  - Verifica `speed-bonus.enabled` (fallback `true`).
  - Se habilitado, lê `speed-bonus.per-level` (fallback `0.1`) e aplica o bônus.
  - Se desabilitado, restaura a velocidade genética original sem nenhum bônus.

### `src/main/resources/config.yml`
- Adicionada seção `speed-bonus` logo após `level-up-message`:
  ```yaml
  speed-bonus:
    enabled: true
    per-level: 0.1
  ```

## Compatibilidade
- Fallback em `getBoolean`/`getDouble` garante comportamento idêntico ao anterior para servidores que não atualizarem o `config.yml`.
- O comando `/lealdadecavalo config` (Tarefa 2) suporta automaticamente as novas chaves.
