# 🐴 HorseLoyalty — Guia Completo:

Plugin de lealdade para cavalos. Quanto mais o jogador cuida e usa o cavalo, maior o nível de lealdade dele — e mais habilidades o cavalo desbloqueia.

---

## 📖 Como funciona o sistema de lealdade

Cada cavalo tem um **nível de lealdade** (0 a 10) e **XP acumulado**. O XP sobe de três formas:

| Ação | XP ganho (padrão) |
|---|---|
| Alimentar com **maçã** | +1 XP |
| Alimentar com **maçã dourada** | +20 XP |
| Alimentar com **maçã dourada encantada** | +200 XP |
| Montar o cavalo (a cada segundo) | +1 XP |
| Montar o cavalo (a cada bloco percorrido) | +10 XP |

Ao atingir o XP necessário, o cavalo **sobe de nível** automaticamente, com som e mensagem para o dono.  
A partir do **nível 3**, o cavalo recebe automaticamente o nome "Cavalo Leal" (configurável).  
A partir do **nível 5**, o cavalo **só pode ser montado pelo dono**.  
A cada nível, o cavalo ganha **+0.1 de velocidade** sobre a velocidade genética original.

---

## 🎮 Comandos para jogadores

### `/lealdadecavalo help`
Exibe a lista de comandos disponíveis.  
**Permissão:** todos os jogadores

---

### `/lealdadecavalo get`
Mostra o nível de lealdade e XP atual do cavalo que você está olhando (distância de até 5 blocos).  
**Permissão:** todos os jogadores  
**Exemplo de saída:**
```
=== Cavalo Leal ===
Nível: 6/10
XP: 45/120
```

---

### `/lealdadecavalo item`
Recebe uma **Ficha do Cavalo** (papel especial) no inventário.  
Com ela em mãos, clique com **botão direito** em qualquer cavalo para ver as informações dele.  
Também funciona com **Shift + clique esquerdo** no cavalo (sem item na mão).  
**Permissão:** todos os jogadores

---

### `/lealdadecavalo rename <UUID>`
Inicia o processo de renomeação de um cavalo. Após digitar o comando, o plugin aguarda você **digitar o novo nome no chat**.  
Digite `cancelar` para desistir.  
**Permissão:** todos os jogadores  
> 💡 Na prática você não vai usar esse comando diretamente — ele é acionado automaticamente pelo botão `[Renomear]` do catálogo.

---

### `/horsecatalog`
Recebe um **Livro de Catálogo de Cavalos** no inventário.  
Clique com **botão direito** para abrir e ver todos os seus cavalos, com informações de nível, XP e velocidade.  
Cada cavalo no livro tem botões:
- `[Renomear]` — inicia renomeação pelo chat
- `[Chamar]` — chama o cavalo até você (requer nível 8)
- `[Favoritar]` — define como cavalo favorito (requer nível 10)

Também é possível abrir o catálogo clicando com **Shift + botão direito** no próprio cavalo enquanto segura o livro.  
**Permissão:** todos os jogadores

---

### `/callhorse <UUID>`
Teleporta um cavalo específico até você.  
**Requisitos:**
- O cavalo precisa ter **nível de lealdade ≥ 8** (configurável)
- O cavalo precisa estar **no mesmo mundo** que você
- O cavalo precisa estar **dentro do alcance máximo** (varia por nível)

**Permissão:** todos os jogadores  
> 💡 O UUID do cavalo aparece no catálogo. Na prática, use o botão `[Chamar]` do catálogo.

---

### `/favoritehorse <UUID>`
Define um cavalo como seu **favorito**.  
**Requisito:** o cavalo precisa ter **nível de lealdade ≥ 10** (configurável).  
**Permissão:** todos os jogadores

---

### `/vemcapocoto`
Chama o seu **cavalo favorito** até você (o mesmo que `/callhorse`, mas sem precisar saber o UUID).  
Sujeito às mesmas restrições de alcance e dimensão do `/callhorse`.  
**Permissão:** todos os jogadores

---

## 🔧 Comandos exclusivos da Staff (requer OP ou `lealdadecavalo.admin`)

### `/lealdadecavalo set <nível>`
Define manualmente o nível de lealdade do cavalo que você está olhando.  
Útil para testes ou para recompensar um jogador.  
**Exemplo:** `/lealdadecavalo set 8` → define lealdade 8 no cavalo alvo  
**Permissão:** `lealdadecavalo.admin` (OP por padrão)

---

### `/lealdadecavalo config get <chave>`
Consulta o valor atual de uma configuração sem precisar abrir o arquivo.  
**Exemplos:**
```
/lealdadecavalo config get max-level
/lealdadecavalo config get call-cooldown.enabled
/lealdadecavalo config get call-range.range-per-level
```

---

### `/lealdadecavalo config set <chave> <valor>`
Altera uma configuração em tempo real, sem reiniciar o servidor.  
**Chaves inteiras disponíveis:**

| Chave | Descrição | Padrão |
|---|---|---|
| `max-level` | Nível máximo de lealdade | 10 |
| `exclusive-mount-level` | Nível a partir do qual só o dono pode montar | 5 |
| `call-level` | Nível mínimo para usar `/callhorse` | 8 |
| `favorite-level` | Nível mínimo para usar `/favoritehorse` | 10 |
| `riding-xp-per-second` | XP ganho por segundo montado | 1 |
| `riding-xp-per-block` | XP ganho por bloco percorrido montado | 10 |
| `feeding.apple` | XP ganho ao alimentar com maçã | 1 |
| `feeding.golden_apple` | XP ganho ao alimentar com maçã dourada | 20 |
| `feeding.enchanted_golden_apple` | XP ganho ao alimentar com maçã dourada encantada | 200 |
| `call-cooldown.base-seconds` | Tempo de espera (em segundos) entre usos do `/callhorse` | 10 |

**Chaves booleanas (true/false):**

| Chave | Descrição | Padrão |
|---|---|---|
| `call-cooldown.enabled` | Ativa/desativa o cooldown do `/callhorse` | false |
| `call-range.enabled` | Ativa/desativa o limite de alcance do `/callhorse` | true |

**Exemplos:**
```
/lealdadecavalo config set call-level 6
/lealdadecavalo config set call-cooldown.enabled true
/lealdadecavalo config set call-cooldown.base-seconds 30
```

---

### `/lealdadecavalo config add <chave-lista> <valor>`
Adiciona um valor ao final de uma lista de configuração.  
**Listas disponíveis:**

| Chave | Descrição |
|---|---|
| `level-xp-requirements` | XP necessário para cada nível (índice 1 = nível 1) |
| `call-range.range-per-level` | Alcance máximo em blocos por nível de lealdade |

**Exemplo:**
```
/lealdadecavalo config add call-range.range-per-level 250
```

---

### `/lealdadecavalo config remove <chave-lista> <índice>`
Remove um item de uma lista pelo índice (começa em 1).  
**Exemplo:**
```
/lealdadecavalo config remove call-range.range-per-level 10
```

---

## 🔑 Tabela de permissões

| Permissão | Quem tem por padrão | O que permite |
|---|---|---|
| `lealdadecavalo.use` | Todos os jogadores | Subcomandos básicos (get, item, rename, help) |
| `lealdadecavalo.catalog` | Todos os jogadores | Usar `/horsecatalog` |
| `lealdadecavalo.call` | Todos os jogadores | Usar `/callhorse` |
| `lealdadecavalo.favorite` | Todos os jogadores | Usar `/favoritehorse` e `/vemcapocoto` |
| `lealdadecavalo.admin` | Somente OP | Usar `set` e `config`; ver seção admin no `/help` |

---

## 📊 Resumo dos níveis e o que desbloqueiam

| Nível | O que acontece |
|---|---|
| 0 | Estado inicial; qualquer um pode montar |
| 3 | Cavalo recebe nome automático "Cavalo Leal" |
| 5 | Somente o dono pode montar (`exclusive-mount-level`) |
| 8 | Dono pode usar `/callhorse` (`call-level`) |
| 10 | Dono pode usar `/favoritehorse` (`favorite-level`) |

> Todos esses níveis são configuráveis pelo comando `/lealdadecavalo config set`.
