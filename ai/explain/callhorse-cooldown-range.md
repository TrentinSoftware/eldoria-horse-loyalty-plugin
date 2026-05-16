# Como configurar o Cooldown e o Alcance do `/callhorse`

Essas duas configurações ficam no arquivo `plugins/HorseLoyalty/config.yml` do seu servidor.  
Você pode alterar o arquivo manualmente (reinicie o servidor para aplicar) **ou** usar o comando `/lealdadecavalo config set` sem precisar reiniciar.

---

## ⏱️ Cooldown (tempo de espera entre usos)

```yaml
call-cooldown:
  enabled: false       # true = ativa o cooldown, false = desativa
  base-seconds: 10     # tempo de espera em segundos
```

**Como funciona:**  
Quando ativado, o jogador precisa esperar `base-seconds` segundos após cada uso bem-sucedido do `/callhorse`. Se tentar antes do tempo, recebe a mensagem: *"Aguarde X segundo(s) para chamar outro cavalo."*

O cooldown **só é contado** quando o cavalo é teleportado com sucesso — erros (UUID errado, cavalo muito longe, etc.) não iniciam o temporizador.

### Exemplos de uso via comando (sem reiniciar o servidor):

| Objetivo | Comando |
|---|---|
| Ativar o cooldown | `/lealdadecavalo config set call-cooldown.enabled true` |
| Desativar o cooldown | `/lealdadecavalo config set call-cooldown.enabled false` |
| Mudar para 30 segundos | `/lealdadecavalo config set call-cooldown.base-seconds 30` |

---

## 📏 Alcance (distância máxima para chamar)

```yaml
call-range:
  enabled: true        # true = limita alcance por nível, false = alcance ilimitado
  range-per-level:     # distância máxima (em blocos) para cada nível de lealdade
    - 50               # nível 1 → 50 blocos
    - 60               # nível 2 → 60 blocos
    - 70               # nível 3 → 70 blocos
    - 80               # nível 4 → 80 blocos
    - 100              # nível 5 → 100 blocos
    - 120              # nível 6 → 120 blocos
    - 140              # nível 7 → 140 blocos
    - 160              # nível 8 → 160 blocos
    - 180              # nível 9 → 180 blocos
    - 200              # nível 10 → 200 blocos
```

**Como funciona:**  
O alcance do cavalo depende do nível de lealdade dele. Um cavalo nível 8 pode ser chamado de até 160 blocos de distância (pelo padrão). Se o cavalo estiver além desse limite, o jogador recebe: *"Este cavalo está muito longe! Alcance máximo: X blocos. (Distância atual: Y)"*

Se o cavalo estiver em **outra dimensão** (ex.: Nether ou End enquanto você está no Overworld), ele nunca pode ser chamado, independentemente da configuração.

### Exemplos de uso via comando:

| Objetivo | Comando |
|---|---|
| Desativar o limite de alcance | `/lealdadecavalo config set call-range.enabled false` |
| Reativar o limite de alcance | `/lealdadecavalo config set call-range.enabled true` |
| Ver os valores atuais da lista | `/lealdadecavalo config get level-xp-requirements` *(exemplo de get)* |

Para alterar um valor específico da lista `range-per-level`, use:
- `/lealdadecavalo config add call-range.range-per-level <blocos>` — adiciona um nível ao final
- `/lealdadecavalo config remove call-range.range-per-level <índice>` — remove pelo índice (começa em 1)
- `/lealdadecavalo config get call-range.range-per-level` — exibe a lista atual

Para substituir todos os valores de uma vez, edite o `config.yml` manualmente.

---

## 📋 Configuração sugerida para servidores survival

```yaml
call-cooldown:
  enabled: true
  base-seconds: 30

call-range:
  enabled: true
  range-per-level:
    - 50
    - 70
    - 90
    - 110
    - 130
    - 150
    - 170
    - 200
    - 250
    - 300
```

Isso torna o comando estratégico: cavalos de baixo nível têm alcance curto e precisam de espera, enquanto cavalos de nível máximo podem ser chamados de longe e com menos tempo de espera.
