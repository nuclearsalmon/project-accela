# Project Accela
A modular framework for building plugin-driven server software, BBSes and MUDs.

The idea behind Project accela to provide a safe and easy-to-use framework for
building modular, plugin-driven servers. The plugins can be loaded into the server at any point during runtime 
and will run in a sandboxed environment where they only have access to certain exposed parts of the system.

This includes a purpose built [TUI window manager][#prismawm-window-manager] (an optional plugin),
allowing for plugins to provide a consistent and easy-to-use means of handling concurrent IO to and from client terminals.

## Table of Contents
- [Background](#background)
- [Notes](#notes)
- [Install](#install)
  - [Dependencies](#dependencies)
- [Usage](#usage)
- [Modules and Plugins](#modules-and-plugins)
  - [Server (core)](#server-core)
  - [PrismaWM (window manager)](#prismawm-window-manager)
  - [Telnet Provider](#telnet-provider)
  - [Session Introducer](#session-introducer)

## Background
My end goal with Project Accela is to utilize the framework to create a sandboxed virtual network 
for hackers to play around in, explore and exploit. 
I want it to be social, highly strategic and to reward those that think out of the box.
It will look and behave somewhat similarly to a [BBS][out_bbs] or a [MUD][out_mud].

The plugins that will make up this network will be publicly available along with the rest of the project 
(probably in a separate repository).
I have plenty of plans for this "game", but that is outside the scope of this README.

## Notes
* The full code for the [server module][module_server] and [PrismaWM][plugin_prismawm] will be uploaded at a later date,
as it's not quite ready yet and I'm dissatisfied with the current code quality.
* I plan on adding support for more protocols than just telnet, as well as implementing encryption for all protocols. 
However, as I'm currently only using it for local testing, unencrypted telnet is more than sufficient.

## Install
TBD.

### Dependencies
TBD.

## Usage
TBD.

<br>

---
## Modules and Plugins
In this context, modules refers to a feature from the IDE that I use,
not Java 9 modules. See: [IntelliJ IDEA/Modules][out_intellij_modules].
Plugins are loadable modules that provide additional functionality on top of the core server.

### Server (core)
This is what will provide the essential frameworks and functionality behind the project.
Including, but not limited to:
- Plugin, services and permissions management.
- Sandbox security features.
- User management, basic encryption and permissions features.
- Standard classes for creating *Session Providers* that add support for network communication
through various client-server protocols (such as [Telnet][plugin_telnet]). 
This is so that any future protocols can be easily implemented and interacted with in a standardized fashion.
- A purpose built library for creating and utilizing [ANSI Escape Sequences][out_ansi].

Link: [Server (core)][module_server]

### PrismaWM (window manager)
PrismaWM lets multiple plugins simultaneously provide text-based output (and input!) to users, 
without interfering with each other. 

It's lets you build "windows" similar to how one would do for a GUI program. 
This includes buttons, dropdown menus, input boxes, etc.

Link: [PrismaWM Window Manager][plugin_prismawm]

### Telnet Provider
A plugin that adds support for communication through the telnet protocol.
It provides users a means of connecting to the system and interacting with it.

The telnet server supports proper telnet negotiation, and can intelligently negotiate 
for features that the server and client both support or have in common. 
The plugin can be configured to listen on multiple ports.

Link: [Telnet Provider][plugin_telnet]

### Session Introducer
A simple plugin that listens for when a TextGraphicsSession is created. 
It will attempt to load a [window manager][plugin_prismawm] into the Session if it doesn't already have one loaded.

Having a window manager makes it possible for other plugins to facilitate graphical communication with the client.
The reason this is a standalone plugin rather than having this functionality being bundled in with the telnet provider 
is due to modularity. 
I want the SysOp to be able to pick which window manager to load, 
without having to modify the code of a *SessionCreator* (such as the [Telnet Provider][plugin_telnet] plugin).

Link: [Session Introducer][plugin_session_introducer]

<!-- Links -->
[module_server]: ./server/src "Server/\"Core\""
[library_ansi]: ./server/src/net/accela/ansi "ANSI EscSeq library"
[plugin_prismawm]: ./prismaPlugin/src "PrismaWM"
[plugin_telnet]: ./telnetProviderPlugin/src "Telnet Provider"
[plugin_session_introducer]: ./sessionIntroducerPlugin/src "Session Introducer"

[out_ansi]: https://en.wikipedia.org/wiki/ANSI_escape_code "ANSI Escape Code"
[out_bbs]: https://sv.wikipedia.org/wiki/Bulletin_board_system "Bulletin Board System"
[out_mud]: https://sv.wikipedia.org/wiki/MUD "Multi User Dungeon"
[out_intellij_modules]: https://www.jetbrains.com/help/idea/creating-and-managing-modules.html "Modules"
