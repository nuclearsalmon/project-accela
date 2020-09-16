# Project Accela
A modular framework for building plugin-driven virtual networks - for hackers to explore and exploit.

## Notes
* The 'core' will be uploaded at a later date, as it's not quite ready yet.
* The 'core' consists of multiple parts, including the following:
  * A terminal-based window manager API, which is somewhat similar to [curses](https://en.wikipedia.org/wiki/Curses_(programming_library)). 
  This is intended to be used by plugins as a means of providing text-based output to users, 
  as well as receiving input from said users without the plugins interfering with each other.
  * A system for loading and unloading plugins at runtime,
  as well as managing the communication between plugins and the system.
  The idea is that any user can write a plugin and load it into the network, 
  providing additional features and experiences for anyone to access. 
  The plugins run in a sandboxed environment where the plugins only have access 
  to certain exposed parts of the system.
* I plan on adding support for more protocols than telnet, 
as well as implementing encryption for all protocols. Telnet will do for now though.

## Plugins
### telnetPlugin
A plugin that adds support for communication through the telnet protocol.
It provides users a means of connecting to the system and interacting with it.
The telnet server supports proper telnet negotiation, and can intelligently negotiate 
for features that the server and client both share, for example character sets. 
Since it's a plugin, it can through config files be configured to start any amount of telnet servers, with custom port numbers for each one.

The telnet server builds on top of a few classes and interfaces provided by the 'core', 
so that any future protocols can be easily implemented and interacted with in a standardized fashion.
