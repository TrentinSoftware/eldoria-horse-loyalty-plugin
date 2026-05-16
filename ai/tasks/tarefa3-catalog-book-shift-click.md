# Tarefa 3 – Shift + Clique Direito com Livro de Catálogo no Cavalo

**Status: ✅ Concluída**

## Objetivo
Ao segurar o livro de catálogo e usar Shift + clique direito em um cavalo, o catálogo é atualizado e aberto.

## Arquivos modificados

### HorseCatalogListener.java
- Método `onBookOpen` refatorado para chamar `refreshAndOpenBook` estático
- Adicionado método `public static refreshAndOpenBook(Player, ItemStack, HorseLoyaltyPlugin, LoyaltyManager, NamespacedKey)` que centraliza a lógica de montar e abrir o livro
- Métodos privados `buildHorsePage`, `getPlayersHorses`, `getRequiredForNext` refatorados para variantes estáticas (`buildHorsePageStatic`, `getPlayersHorsesStatic`, `getRequiredForNextStatic`)
- Níveis `8` e `10` substituídos por `manager.getCallLevel()` / `manager.getFavoriteLevel()`
- `/10` no display de lealdade substituído por `manager.getMaxLevel()`

### HorseInfoListener.java
- Adicionado campo `NamespacedKey catalogKey`
- Construtor atualizado para aceitar `catalogKey`
- Adicionado handler `onCatalogBookClick(PlayerInteractEntityEvent)`:
  - Verifica `EquipmentSlot.HAND`, `player.isSneaking()`, entidade clicada é `Horse`, item é `WRITTEN_BOOK` com a chave `horse_catalog`
  - Cancela o evento e chama `HorseCatalogListener.refreshAndOpenBook()`
- `showHorseInfo` atualizado para usar `manager.getMaxLevel()`

### HorseLoyaltyPlugin.java
- `HorseInfoListener` agora recebe `catalogKey` no construtor

## Comportamento
- Clique normal no cavalo com o livro: não é bloqueado (montaria funciona normalmente)
- Shift + clique direito com o livro no cavalo: abre o catálogo atualizado
- Clique com o papel de ficha: inalterado (mostra info do cavalo)
