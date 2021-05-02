# Project Accela
A framework for building modular, plugin-driven server software, BBSes and MUDs.

It also includes a purpose built [TUI window manager](#prismatic-window-manager) (optional) that plugins can interact with,
allowing for a consistent and easy-to-use means of handling concurrent IO to and from client terminals.

Feel free to check out the [wiki].

## Table of Contents
- [Background](#background)
- [Notes](#notes)
- [Dependencies](#dependencies)
- [Install](#install)
- [Usage](#usage)
- [Modules and Plugins](#modules-and-plugins)
  - [Server Core](#server-core)
  - [Prismatic (Window manager)](#prismatic-window-manager)
  - [Telnet Provider](#telnet-provider)

## Background
My end goal with Project Accela is to utilize the framework to create a sandboxed virtual network "game"
for hackers to play around in, explore and exploit. 
I want it to be social, highly strategic and to reward those that think out of the box.
It will look and behave somewhat similarly to a [BBS][out_bbs] or a [MUD][out_mud].

The plugins that will be used to create said environment will be publicly available along with the rest of the project 
(though probably in a separate repository). I have plenty of plans for this "game", but that is outside the scope of this README.

## Notes
* The full code for the [server module][repo_server] and [Prismatic][repo_prismatic] will be uploaded at a later date,
as it's not quite ready yet and I'm dissatisfied with the current code quality.
* I plan on adding support for more protocols than just telnet, as well as implementing encryption for all protocols. 
However, as I'm currently only using it for local testing, unencrypted telnet is more than sufficient.

<br>

---

## Dependencies
* JetBrains annotations (optional)
* SnakeYAML
* Apache Commons Lang
* Google Guava

## Install
TBD.

## Usage
TBD.

<br>

---

## Modules and Plugins
In this context, modules refers to a feature from the IDE that I use,
not Java 9 modules. See: [IntelliJ IDEA/Modules][out_intellij_modules].
Plugins are loadable modules that provide additional functionality on top of the core server.
They can be loaded into the server at any point during runtime 
and will run in a sandboxed environment where they are only allowed access to certain parts of the framework.

### Server Core
This is what will provide the essential frameworks and functionality behind the project.
Including, but not limited to:
- Plugin, services and permissions management.
- Sandbox security features.
- User management, basic encryption and permissions features.
- Standard classes for creating *Session Providers* that add support for network communication
through various client-server protocols (such as [Telnet][repo_telnet_provider]). 
This is so that any future protocols can be easily implemented and interacted with in a standardized fashion.

<!--Repo link: [Server (Core)][repo_server]-->

### Prismatic Window Manager
Prismatic lets multiple plugins simultaneously provide terminal-based I/O to and from users, 
without the plugins interfering with each other.

It lets you build "windows" similar to how one would do for a GUI program. 
This includes buttons, dropdown menus, input boxes, etc.

Repo link: [Prismatic Window Manager][repo_prismatic]

### Telnet Provider
A plugin that adds support for communication through the telnet protocol.
It provides users a means of connecting to the system and interacting with it.

The telnet server supports proper telnet negotiation, and can intelligently negotiate 
for features that the server and client both support or have in common. 
The plugin can also be configured to listen on multiple ports if needed.

Repo link: [Telnet Provider][repo_telnet_provider]

<!-- Links -->
[repo_server]: ./server/src "Server Core"

[repo_ansi_library]: prismaPlugin/src/net/accela/prisma/ansi "ANSI EscSeq library"

[repo_prismatic]: ./prismaPlugin/src "Prismatic Window Manager"
[repo_telnet_provider]: ./telnetProviderPlugin/src "Telnet Provider"

[wiki]: https://github.com/gustavdersjo/project-accela/wiki

[out_ansi]: https://en.wikipedia.org/wiki/ANSI_escape_code "ANSI Escape Code"
[out_bbs]: https://sv.wikipedia.org/wiki/Bulletin_board_system "Bulletin Board System"
[out_mud]: https://sv.wikipedia.org/wiki/MUD "Multi User Dungeon"
[out_intellij_modules]: https://www.jetbrains.com/help/idea/creating-and-managing-modules.html "Modules"
