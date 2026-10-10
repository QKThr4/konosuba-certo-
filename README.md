# KonoRPG - Blessings on this Wonderful Guild (versão de TESTE 0.1.0)

Mod de RPG para **Minecraft 1.21.1 + NeoForge 21.1.x** inspirado em KonoSuba.

## Como gerar o .jar (GitHub)
1. Crie um repositório e envie TODO o conteúdo desta pasta (inclusive a pasta `.github`).
2. Abra a aba **Actions** > "Build mod" > o build roda sozinho (ou "Run workflow").
3. Baixe o artefato **konorpg-jar** (o .jar fica dentro). Coloque em `mods/`.
4. Se o build falhar, copie o erro e mande para o Claude.

(Local: instale Gradle 8.10+ e JDK 21, rode `gradle build`; o jar sai em `build/libs`.)

## Como jogar (teste)
- Entre num mundo novo: aparece a tela **"A Escolha"** (1 presente permanente). Também: `/konorpg intro` (op).
- Você é levado à cidade de **Axel** (construída ao lado do spawn). Vá ao prédio da **Guilda** (norte da praça), fale com **Luna** (botão direito), pague a taxa (1000 Eris) e receba o **Cartão de Aventureiro**.
- **K** abre o painel (Status, Skills, Quests, Reputação, Companion, Cartão, Equipamento, Guilda, HUD). **J** liga/desliga o HUD de progresso. **Z X C V** usam as habilidades.
- Mural de quests: bloco na praça e dentro da Guilda. Monstros dão XP/Eris e itens que vendem nas lojas.
- Se escolheu **Aqua**, ela aparece depois do registro (fale com ela, ela cura, comenta, ressuscita você).
- Comandos úteis: `/konorpg city`, `/konorpg hub`, `/konorpg xp <n>`, `/konorpg money <n>`, `/konorpg quest <id>`, `/konorpg reset` (op).

## EM BREVE (ainda não implementado)
Dungeons e chefes, mimic/cabbage/aranha/golem/demônio/dragão, eventos aleatórios, missões de grupo da guilda, criar a própria guilda,
recrutar aventureiros, crafting/serviços do ferreiro e alquimista, armaduras RPG, rotinas de sono/taverna dos NPCs, classes avançadas
(skills do Crusader, Sword Master, High Priest, Assassin, Necromancer, Battle Mage, Paladin) e modelo próprio do sapo gigante.

## Estrutura
`src/main/java/com/konorpg/...` (registry, data, rpg, skill, quest, dialogue, shop, entity, world, network, event, client).
Quests, diálogos, lojas e preços ficam em `src/main/resources/data/konorpg/` (JSON). `tools/gen_assets.py` regenera texturas/JSON.
