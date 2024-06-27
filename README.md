# Minecraft server wrapper
This repository is a Kotlin rewrite of [this one](https://github.com/Contrabass26/minecraft-wrapper), because Kotlin is just better.

## Project aim
This project provides a GUI for locally hosted Minecraft servers, with QOL tools for easy setup and configuration.

The project is targeted at two groups of people:
- People who want to self-host Minecraft servers but don't know how to
- People who have experience self-hosting but want to streamline setup and configuration

In the future I would like to support remote configuration over a network, or perhaps a command line alternative. For now, all servers are hosted on the computer that is running the application.

## Supported mod loaders
- [Vanilla](https://www.minecraft.net/en-us/download/server) (obviously!)
- [Fabric](https://fabricmc.net/use/server/)
- [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/)
- [NeoForge](https://neoforged.net/)
- [Pufferfish](https://pufferfish.host/downloads) (high-performance Paper fork)

## Building
Clone the repo and run `gradlew run` (varies slightly depending on OS) in the project root directory. In the future you may need to provide a CurseForge API key; this is not necessary for now. The application is not useful in its current state; there will probably be bugs and the code might not even compile.
