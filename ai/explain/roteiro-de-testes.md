# 🧪 Roteiro de Testes — HorseLoyalty

Use este roteiro para validar todas as funcionalidades do plugin antes de colocar em produção.  
Execute os testes **em ordem**, pois alguns dependem de estado criado nos anteriores.

**Pré-requisitos:**
- Servidor Paper 1.21.4 rodando com o plugin instalado
- Dois perfis de teste: **Conta OP** (admin) e **Conta Não-OP** (jogador comum)
- Um cavalo domado e de sua propriedade no mundo

---

## 1. Permissões básicas

| # | Ação | Conta | Resultado esperado |
|---|---|---|---|
| 1.1 | `/lealdadecavalo help` | Não-OP | Exibe apenas os comandos públicos (sem `set` e `config`) |
| 1.2 | `/lealdadecavalo help` | OP | Exibe comandos públicos **mais** a seção admin em vermelho |
| 1.3 | `/lealdadecavalo set 5` (olhando cavalo) | Não-OP | Mensagem: "Você não tem permissão para usar este comando." |
| 1.4 | `/lealdadecavalo config get max-level` | Não-OP | Mensagem: "Você não tem permissão para usar este comando." |
| 1.5 | `/lealdadecavalo get` (olhando cavalo) | Não-OP | Exibe nível e XP normalmente ✅ |

---

## 2. XP por alimentação

| # | Ação | Resultado esperado |
|---|---|---|
| 2.1 | Com uma **maçã** na mão, clique com botão direito no cavalo | Maçã é consumida, XP +1 |
| 2.2 | Com uma **maçã dourada** na mão, clique com botão direito no cavalo | XP +20 |
| 2.3 | Com uma **maçã dourada encantada**, clique com botão direito no cavalo | XP +200 (pode subir vários níveis) |
| 2.4 | Use `/lealdadecavalo get` após cada alimentação | XP deve aumentar conforme esperado |

---

## 3. XP por montaria

| # | Ação | Resultado esperado |
|---|---|---|
| 3.1 | Monte o cavalo e **fique parado** por 10 segundos | XP +10 (1 por segundo) |
| 3.2 | Monte o cavalo e **percorra ~10 blocos** | XP +100 aproximadamente (10 por bloco) |
| 3.3 | Desmonte e use `/lealdadecavalo get` | XP acumulado deve refletir os ganhos acima |

---

## 4. Level-up e efeitos

| # | Ação | Resultado esperado |
|---|---|---|
| 4.1 | Alimente/monte até o cavalo subir de nível | Som de level-up é reproduzido |
| 4.2 | Mensagem de level-up | Exibe "Seu cavalo subiu para o nível X!" em azul |
| 4.3 | Verifique velocidade após subir nível | Cavalo deve andar mais rápido (+0.1 por nível) |
| 4.4 | Use `/lealdadecavalo set 3` e observe o nome | Cavalo recebe o nome automático "Cavalo Leal" |

---

## 5. Proteção de montaria

| # | Ação | Conta | Resultado esperado |
|---|---|---|---|
| 5.1 | `/lealdadecavalo set 4` no cavalo | OP | Lealdade 4 — qualquer um pode montar |
| 5.2 | Conta Não-OP tenta montar | Não-OP | Montaria **permitida** (abaixo do nível 5) |
| 5.3 | `/lealdadecavalo set 5` no cavalo | OP | Lealdade 5 — somente dono pode montar |
| 5.4 | Conta Não-OP tenta montar (sem ser dona) | Não-OP | Mensagem: "Este cavalo é leal apenas ao seu dono." |
| 5.5 | Dono do cavalo monta | Dono | Montaria **permitida** normalmente ✅ |

---

## 6. Ficha do Cavalo

| # | Ação | Resultado esperado |
|---|---|---|
| 6.1 | `/lealdadecavalo item` | Recebe papel "Ficha do Cavalo" no inventário |
| 6.2 | Com a ficha na mão, clique com **botão direito** num cavalo seu | Exibe informações do cavalo (nível, XP) |
| 6.3 | Com a ficha na mão, clique num cavalo **de outro dono** | Exibe mensagem "Você não é o dono deste cavalo." |
| 6.4 | Sem item na mão, **Shift + clique esquerdo** num cavalo seu | Exibe informações do cavalo |

---

## 7. Catálogo de Cavalos

| # | Ação | Resultado esperado |
|---|---|---|
| 7.1 | `/horsecatalog` | Recebe livro "Catálogo de Cavalos" no inventário |
| 7.2 | Clique com **botão direito** com o livro na mão | Abre o livro com lista de cavalos domados do jogador |
| 7.3 | Verifique se o cavalo aparece com nome, nível, XP e velocidade | Dados corretos ✅ |
| 7.4 | Com o livro na mão, **Shift + botão direito** num cavalo seu | Abre/atualiza o catálogo com aquele cavalo em destaque |
| 7.5 | Botão `[Renomear]` no catálogo (cavalo com qualquer nível) | Chat aguarda novo nome |
| 7.6 | Digite o novo nome no chat | Cavalo é renomeado com o nome digitado |
| 7.7 | Digite `cancelar` durante renomeação | Processo é cancelado sem renomear |

---

## 8. Chamar Cavalo (`/callhorse`)

**Prepare:** use `/lealdadecavalo set 7` no cavalo (abaixo do nível mínimo de 8).

| # | Ação | Resultado esperado |
|---|---|---|
| 8.1 | Clique em `[Chamar]` no catálogo (cavalo nível 7) | Botão não aparece (nível insuficiente) |
| 8.2 | `/lealdadecavalo set 8` no cavalo | Define lealdade 8 |
| 8.3 | Fique a menos de 160 blocos do cavalo e use `[Chamar]` ou `/callhorse <UUID>` | Cavalo teleporta até você ✅ |
| 8.4 | Vá para mais de 160 blocos e tente chamar | Mensagem: "Este cavalo está muito longe! Alcance máximo: 160 blocos." |
| 8.5 | Vá para o Nether e tente chamar cavalo do Overworld | Mensagem: "Seu cavalo está em outra dimensão..." |

---

## 9. Cooldown do `/callhorse`

| # | Ação | Resultado esperado |
|---|---|---|
| 9.1 | `/lealdadecavalo config set call-cooldown.enabled true` | Confirmação de alteração |
| 9.2 | `/lealdadecavalo config set call-cooldown.base-seconds 15` | Cooldown definido para 15s |
| 9.3 | Chame o cavalo com sucesso | Teleporta normalmente |
| 9.4 | Tente chamar novamente imediatamente | Mensagem: "Aguarde X segundo(s) para chamar outro cavalo." |
| 9.5 | Espere 15 segundos e tente novamente | Funciona normalmente ✅ |
| 9.6 | `/lealdadecavalo config set call-cooldown.enabled false` | Desativa para não atrapalhar outros testes |

---

## 10. Favoritar Cavalo

**Prepare:** cavalo com lealdade 9 (abaixo do mínimo de 10).

| # | Ação | Resultado esperado |
|---|---|---|
| 10.1 | Botão `[Favoritar]` no catálogo (cavalo nível 9) | Botão não aparece (nível insuficiente) |
| 10.2 | `/lealdadecavalo set 10` no cavalo | Define lealdade 10 |
| 10.3 | Use `[Favoritar]` no catálogo ou `/favoritehorse <UUID>` | Mensagem: "Cavalo favoritado! Use /vemcapocoto para chamá-lo." |
| 10.4 | Afaste-se e use `/vemcapocoto` | Cavalo favorito teleporta até você ✅ |
| 10.5 | Sem cavalo favorito definido, use `/vemcapocoto` | Mensagem: "Você não tem um cavalo favorito." |

---

## 11. Configuração dinâmica (admin)

| # | Ação | Resultado esperado |
|---|---|---|
| 11.1 | `/lealdadecavalo config get max-level` | Exibe `max-level = 10` |
| 11.2 | `/lealdadecavalo config set exclusive-mount-level 3` | Cavalo passa a exigir dono a partir do nível 3 |
| 11.3 | Verifique se um cavalo nível 3 bloqueia montagem de outros | Bloqueio funcionando com o novo valor |
| 11.4 | `/lealdadecavalo config set exclusive-mount-level 5` | Restaura o padrão |
| 11.5 | `/lealdadecavalo config get call-range.range-per-level` | Exibe a lista com os 10 valores |
| 11.6 | `/lealdadecavalo config add call-range.range-per-level 250` | Adiciona 250 ao final da lista |
| 11.7 | `/lealdadecavalo config get call-range.range-per-level` | Lista agora tem 11 valores |
| 11.8 | `/lealdadecavalo config remove call-range.range-per-level 11` | Remove o último item |
| 11.9 | Chave inexistente: `/lealdadecavalo config get xablau` | Mensagem: "Chave desconhecida: xablau" + lista de chaves válidas |

---

## 12. Novo config no servidor (teste de merge automático)

| # | Ação | Resultado esperado |
|---|---|---|
| 12.1 | Apague o `config.yml` da pasta `plugins/HorseLoyalty/` | Arquivo removido |
| 12.2 | Reinicie o servidor | Arquivo recriado com todos os valores padrão |
| 12.3 | Verifique se `call-range` e `call-cooldown` existem no arquivo | Seções presentes ✅ |
| 12.4 | Edite uma chave no `config.yml` manualmente e reinicie | Valor editado é preservado; novas chaves são adicionadas |

---

## ✅ Checklist final

- [ ] Jogador comum consegue usar todos os comandos públicos
- [ ] Jogador comum é bloqueado em `set` e `config`
- [ ] XP por alimentação funciona corretamente
- [ ] XP por montaria (tempo e distância) funciona
- [ ] Level-up exibe som e mensagem
- [ ] Velocidade aumenta com o nível
- [ ] Nome automático aplicado no nível 3
- [ ] Proteção de montaria ativa no nível 5
- [ ] Ficha do Cavalo exibe informações corretamente
- [ ] Catálogo abre e exibe cavalos corretamente
- [ ] Renomeação via chat funciona
- [ ] `/callhorse` funciona com restrição de nível, alcance e dimensão
- [ ] Cooldown do `/callhorse` funciona e é configurável
- [ ] `/favoritehorse` e `/vemcapocoto` funcionam
- [ ] `/lealdadecavalo config` altera valores em tempo real
- [ ] Config do servidor é atualizado com novas chaves ao reiniciar
