# AutoClef - modifed [altoclef](https://github.com/gaucho-matrero/altoclef)

Добавлено и адаптировано множество функций. Одни из самых главных - автоматическая игра в режим SkyWars на немодерируемых серверах.
Пример немодерируемого сервера:
<details>
  ip: mc.musteryworld.ru
  заходим на режим SkyWars
</details>

Гифки:
<details>
Здесь я вставил несколько фрагментов со стримов с демонстрацией некоторых игровых функций бота. 

Режим SkyWars в Minecraft начинается с выпуска каждого игрока в колбу над своим островом. На островах есть сундуки с ценными ресурсами, которые игроки должны собирать, чтобы получать различные преимущества. Таким образом, при получении сообщения о начале игры бот активирует написанный ранее таск для режима SkyWars, в одну из задач которого входит добыча ресурсов из сундуков.

![Начало игры и лутание сундуков](https://github.com/3ndetz/autoclef/assets/30196290/aa44993e-a7e8-4285-bba6-a690b0ac29a2)

При встрече с игроками кроме получения необходимых ресурсов (брони, мечей и т. п.), чтобы хоть как‑то сравниться с живыми игроками, бот должен уметь пользоваться такими плюшками, как золотые яблоки и эндер‑жемчуги.
![Использование золотого яблока и эндер‑жемчуга для нападения](https://github.com/3ndetz/autoclef/assets/30196290/0d3e73d2-2e1f-40e7-a53b-be43d3d9335d)

Наконец, сочетание разнообразных навыков и скорость реакции бота на алгоритмах делают своё дело — бот способен побеждать в бою реальных людей и собирать с них ресурсы!
![Победа над игроком и сбор выпавших ресурсов](https://github.com/3ndetz/autoclef/assets/30196290/7377ec79-1c3d-493b-9a1d-5d701f19d9c9)

Если нет возможности приблизиться к игроку, нужно уметь использовать и дальнее оружие. В Minecraft это чаще всего лук. В качестве бонуса я научил бота стрелять не только по кратчайшей параболе, но и навесом. Такая артиллерия точно преподнесёт игрокам, спрятавшимся где‑нибудь за горой, нежданчик, не говоря уже о том, что за разнообразными тактиками боя зрителям будет интересно наблюдать!
![Стрельба из лука](https://github.com/3ndetz/autoclef/assets/30196290/9bae7aee-f535-4704-83a3-3dd9ec885a80)

</details>

# altoclef: Оригинальный Readme

Plays block game.

Powered by Baritone.

A client side bot that can accomplish any Minecraft task that is relatively simple and can be split into smaller tasks. "Relatively Simple" is a vague term, so check the list of current capabilities to see examples.

Became [the first bot to beat Minecraft fully autonomously](https://youtu.be/baAa6s8tahA) on May 24, 2021.

**Join the [Discord Server](https://discord.gg/JdFP4Kqdqc)** for discussions/updates/goofs & gaffs

## How it works

Take a look at this [Guide from the wiki](https://github.com/gaucho-matrero/altoclef/wiki/1:-Documentation:-Big-Picture) or this [Video explanation](https://youtu.be/q5OmcinQ2ck?t=387)

## Current capabilities, Examples:
- Obtain 400+ Items from a fresh survival world, like diamond armor, cake, and nether brick stairs
- Dodge mob projectiles and force field mobs away while accomplishing arbitrary tasks
- Collect + smelt food from animals, hay, & crops
- Receive commands from chat whispers via /msg. Whitelist + Blacklist configurable (hereby dubbed the Butler System). Here's a [Butler system demo video](https://drive.google.com/file/d/1axVYYMJ5VjmVHaWlCifFHTwiXlFssOUc/view?usp=sharing)
- Simple config file that can be reloaded via command (check .minecraft directory)
- Beat the entire game on its own (no user input.)
- Print the entire bee movie script with signs in a straight line, automatically collecting signs + bridging materials along the way.
- Become the terminator: Run away from players while unarmed, gather diamond gear in secret, then return and wreak havoc.

## Download

**Note:** After installing, please move/delete your old baritone configurations if you have any. Preexisting baritone configurations will interfere with altoclef and introduce bugs. This will be fixed in the future.

### Alternate Versions (Recommended) (Unofficial)

If you are looking for 1.19.2 - 1.19.4 support, check out this [gist](https://gist.github.com/JustaSqu1d/171df3ff386859da31d37534122d3b10). Note that these projects are forks of this original project and not directly affliated with Alto Clef. It is also more up-to-date with bug fixes and features.

#### (old) Nightly Release

Start by downloading [the Latest Long Term Release](https://github.com/gaucho-matrero/altoclef/releases), then [Download the Nightly](https://nightly.link/gaucho-matrero/altoclef/workflows/gradle/main/Artifacts.zip) & replace `altoclef-4.0-SNAPSHOT.jar`.

If the Nightly Link doesn't work, check the latest [Build Action](https://github.com/gaucho-matrero/altoclef/actions) that succeeded and download `Artifacts.zip` (you must be signed into GitHub). Replace your existing `altoclef-4.0-SNAPSHOT.jar` with the one found in `Artifacts.zip`

Then, copy `altoclef-4.0-SNAPSHOT.jar` from `Artifacts.zip` to `./mods`.

Then, copy the `baritone-unoptimized-fabric-1.XX.X.jar` from the long term release zip file to `./mods`

#### (old) Long Term Release

[Check releases](https://github.com/gaucho-matrero/altoclef/releases). Note you will need to copy over both jar files for the mod to work.

#### (old) Meloweh's Extra Features Release (Unofficial)

Has some schematic support, command macros and a few utility features. Will eventually be merged, but if you can try it out now if you'd like:

- [AltoClef jar](https://github.com/Meloweh/altoclef/releases)
- [Baritone jar](https://github.com/Meloweh/baritone/releases)

### Versions

This is a **fabric only** mod, currently only available for **Minecraft 1.18**.

For older MC versions, try [multiconnect](https://www.curseforge.com/minecraft/mc-mods/multiconnect) (NOTE: multiconnect is untested and not affiliated with altoclef, use at your own risk!)


## [Usage Guide](usage.md)

## [TODO's/Future Features](todos.md)

## [Development Guide](develop.md)
