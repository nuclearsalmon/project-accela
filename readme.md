# Project Accela
A modular framework for building plugin-driven virtual networks - for hackers to explore and exploit. Enjoy ;)

## The concept
The basic idea is that any user can write a plugin and load it into the network, 
providing additional features and experiences for anyone to access. 
The plugins run in a sandboxed environment where the plugins only have access to certain exposed parts of the system.

## The purpose
My end goal with the project is to utilize it to create a sandboxed network for hackers to play around in, explore and exploit.
It will be almost like a game, though I dislike calling it such. I want it to be highly strategic and to reward those that think out of the box.
<br><br>
The plugins that will make up this network will be publicly availible along with the rest of the project.
I have plenty of plans for this "game", but that is outside the scope of this README.

## Notes
* The full code for the [server module][module_server] and [PrismaWM][plugin_prismawm] will be uploaded at a later date,
as it's not quite ready yet and I'm disstatisfied with the current code quality.
* I plan on adding support for more protocols than simply telnet, as well as implementing encryption for all protocols. Telnet will do for now though.

---

# Modules and Plugins
## [Server/"Core" module][module_server]
This is what will provide the core framework and functionality, such as plugin, service and permissions management, along with automatic sandboxing,
and standards for creating "providers", that add network connectivity and new protocols (See: [Telnet Provider plugin][telnet_plugin]).
It also includes my own library for crafting [ANSI Escape Sequences][out_ansi], along with related utilities.

## [PrismaWM Window Manager][plugin_prismawm]
This is to be used by plugins as a means of providing text-based output to users, 
as well as receiving input from said users without the plugins interfering with each other.

## [Telnet Provider][plugin_telnet]
A plugin that adds support for communication through the telnet protocol.
It provides users a means of connecting to the system and interacting with it.
The telnet server supports proper telnet negotiation, and can intelligently negotiate 
for features that the server and client both share, for example character sets. 
Since it's a plugin, it can through config files be configured to start any amount of telnet servers, with custom port numbers for each one.

The telnet server builds on top of a few classes and interfaces provided by the [core][module_server],
so that any future protocols can be easily implemented and interacted with in a standardized fashion.

## [Session Introducer][plugin_session_introducer]
A simple plugin that listens for creation events for graphical sessions. Once such an event gets called,
the session introducer will attempt to load a window manager into the session.
Having a window manager makes it possible for other plugins to facilitate graphical communication with the client.
The reason this is a standalone plugin rather than having this functionality bundled in the telnet provider is due to modularity.
I want the SysOp to be able to pick which window manager to load, without neccesarily having to modify the code of a 
session creator (such as the [Telnet Provider][telnet_plugin] plugin).



[module_server]: ./server/src/ "Server/\"Core\" module"
[library_ansi]: ./server/src/net/accela/ansi/ "ANSI Escape Sequences library"
[plugin_prismawm]: ./prismaPlugin/src/ "\"PrismaWM\" Window Manager plugin module"
[plugin_telnet]: ./telnetPlugin/src/ "Telnet Provider plugin module"
[plugin_session_introducer]: ./sessionIntroducerPlugin/src/ "Session Introducer plugin module"

[out_ansi]: https://en.wikipedia.org/wiki/ANSI_escape_code "ANSI Escape Code"
