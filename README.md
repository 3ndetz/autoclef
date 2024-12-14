AutoClef - multiplayer [altoclef](https://github.com/gaucho-matrero/altoclef)
=========


Plays block game. Allows multiplayer and py-scripts support. 

![Победа над игроком и сбор выпавших ресурсов](https://github.com/3ndetz/autoclef/assets/30196290/7377ec79-1c3d-493b-9a1d-5d701f19d9c9)


[<img src="https://img.shields.io/badge/Habr-%D0%A7%D0%B8%D1%82%D0%B0%D1%82%D1%8C-%23000000?style=for-the-badge&link=https://habr.com/ru/articles/812387&logo=habr&logoColor=%23FFFFFF&labelColor=%2365A3BE"/>](https://habr.com/ru/articles/812387/#SkyWarsBot)

[<img src="https://img.shields.io/github/stars/3ndetz/AutoClef?style=flat&label=this-repo-stars&link=https%3A%2F%2Fgithub.com%2F3ndetz%2FAutoClef"/>](https://github.com/3ndetz/autoclef)
[<img src="https://img.shields.io/github/stars/3ndetz/NeuroDeva?style=flat&label=virtual-streamer-repo&link=https%3A%2F%2Fgithub.com%2F3ndetz%2FNeuroDeva"/>](https://github.com/3ndetz/NeuroDeva)


A**l**toClef can do a minecraft walkthrough. A**u**toClef can do walkthrough skywars.

_SkyWars is a Minecraft multiplayer minigame with islands._


> [!CAUTION]
> <details><summary>RepoCodeDisclaimer ⚠️</summary>
>
> The "code" presented in the repository is mostly for prototyping. It should not be considered as a sample, it can be useful only to those who will be interested in repeating my experience, and not for "seekers of other people's mistakes" =)
>
> Furthermore, my experience in Java at the time of writing was extremely small (zero) and improving over time, so bugs or very silly things are possible. Unlike my other repositories, where I was in a hurry and would not really want to receive negative feedback, here it is the opposite, please report if you see something potentially bad in the code, because I still know Java not at a high level and would be happy to understand my mistakes!
>
> In the code you can see huge commented out dumps, don't pay attention, because I had a choice to publish the code or not. I didn't format it in any way and didn't prepare it for publishing, so I didn't hesitate to leave crutches and other nasty things in there, like debug prints.
> </details>


<details><summary>IDE and other software</summary>

- JB Intellij Idea
- Made on Windows 10
</details>

<details><summary>Features differs from the orig AltoClef</summary>

- Smooth mouse look (for multiplayer anticheats killaura bypass)
  - buggy, but working!
- New tasks
  - SkyWarsTask for playing SkyWars
    - supports teammates
      - adds nearest people in radius 5 when starting command
  - ThePitTask and others new is in development
    - you can help!
- Support for connecting Python-scripts using Py4J library
  - it uses the connection port for this
  - interface is two-way
    - you can send to Python-side position, etc.
    - you can send to Java-side AltoClef commands, chat msgs, etc.
</details>


> [!TIP]
> This mod for minecraft is an interface designed for use with autonomous virtual streamer. It has a Python-callback interface to connect with Python-app and some functions to get info from the game to pass them to  streamer agent. Also it has a rich improved command system, compatible for using with LLM agent.


## Коротко о главном

В этом форке по сравнению с оригинальным [altoclef](https://github.com/gaucho-matrero/altoclef) добавлено и адаптировано множество функций. Одна из самых главных - автоматическая игра в режим SkyWars на немодерируемых серверах.

Для игры в мультиплеере был изменён механизм поворта при атаке, из-за этого может быть много проблем и багов, но оно работает, а это — главное!

А ещё с помощью py4j реализовано подключение к Python-процессу по сети. Можете подключить к боту свою ChatGPT!)) Интерфейс на Python-стороне можно [нагуглить](https://stackoverflow.com/questions/47607463/py4j-callback-from-java-runnable) и сделать самому либо дождаться когда я выпущу отдельную репу для него (маякните, если это кому-то позарез нужно, выпущу раньше) =)

## Гифки с демонстрацией / Demo GIFs:
<details>
<summary>SkyWars bot autoplay Demo GIFs</summary>
Здесь я вставил несколько фрагментов с демонстрацией некоторых игровых функций бота!



### Looting chests
![Начало игры и лутание сундуков](https://github.com/3ndetz/autoclef/assets/30196290/aa44993e-a7e8-4285-bba6-a690b0ac29a2)

Начало игры и лутание сундуков

### SkyWars — briefly (кратко о режиме)
Режим SkyWars в Minecraft начинается с выпуска каждого игрока в колбу над своим островом. На островах есть сундуки с ценными ресурсами, которые игроки должны собирать, чтобы получать различные преимущества. Таким образом, при получении сообщения о начале игры бот активирует написанный ранее таск для режима SkyWars, в одну из задач которого входит добыча ресурсов из сундуков.

### Gapple & EnderPearl

![Использование золотого яблока и эндер‑жемчуга для нападения](https://github.com/3ndetz/autoclef/assets/30196290/0d3e73d2-2e1f-40e7-a53b-be43d3d9335d)

При встрече с игроками кроме получения необходимых ресурсов (брони, мечей и т. п.), чтобы хоть как‑то сравниться с живыми игроками, бот должен уметь пользоваться такими плюшками, как золотые яблоки и эндер‑жемчуги.

### Kill&loot
![Победа над игроком и сбор выпавших ресурсов](https://github.com/3ndetz/autoclef/assets/30196290/7377ec79-1c3d-493b-9a1d-5d701f19d9c9)

Наконец, сочетание разнообразных навыков и скорость реакции бота на алгоритмах делают своё дело — бот способен побеждать в бою реальных людей и собирать с них ресурсы!

### Bow master
![Стрельба из лука](https://github.com/3ndetz/autoclef/assets/30196290/9bae7aee-f535-4704-83a3-3dd9ec885a80)

Если нет возможности приблизиться к игроку, нужно уметь использовать и дальнее оружие. В Minecraft это чаще всего лук. В качестве бонуса я научил бота стрелять не только по кратчайшей параболе, но и навесом. Такая артиллерия точно преподнесёт игрокам, спрятавшимся где‑нибудь за горой, нежданчик, не говоря уже о том, что за разнообразными тактиками боя зрителям будет интересно наблюдать!

</details>

## Где можно зайти протестировать
<details>
<summary>
Инструкция для одного из немодерируемых серверов
</summary>

1. Подключаемся с этого клиента, например, по следующему ip: `mc.musteryworld.ru:25565`
2. Регистрируемся согласно инструкции на сервере
3. Бот сам войдёт на режим SkyWars и начнёт игру, если включён автозаход (он включен по умолчанию)

Если автозаход отключён (чтобы отключить автозаход можно написать в чат `@set autojoin false`):
1. Подключаемся к серверу по ip.
2. Заходим на режим SkyWars через портал или через меню.
3. Заходим в портал (вода) для подключения к арене.
4. При запуске игры, во время выпуска игроков из колбы, напишите команду `@test killall` для включения режима игры в SkyWars.
</details>


> [!NOTE]
> This repo (autoclef) is a fork of original [altoclef](https://github.com/gaucho-matrero/altoclef/wiki/1:-Documentation:-Big-Picture).
>
> Note that original bot is not capable of automatically playing multiplayer servers, but it still can walkthrough minecraft.

AltoClef (не AutoClef, который здесь, а AltoClef) — оригинальный репозиторий клиентского бота, который впервые смог пройти майнкрафт полностью. Имейте ввиду, что [оригинальный](https://github.com/gaucho-matrero/altoclef) бот не приспособлен для игры на многопользовательских серверах с установленными античитами (по крайней мере, на данный момент).