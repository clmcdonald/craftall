# CraftAll

## Getting Started
To install this plugin, drop the JAR into the plugins folder of your Spigot server, and restart the server.

## Features
**Command:** `/craftall`

**Alias:** `/ca`

**Purpose:**  
The CraftAll command crafts as many of a given item as possible using the materials in the user's inventory. For
example, if the user has an inventory full of snow balls, and they wanted to easily turn them all into snow blocks, they
could use the command `/craftall snow_block`, which would craft as many snow blocks as they have snow balls for.

**Usage:** `/craftall <material> [max]`  
The `material` argument can be any built in Minecraft material. If you have Essentials installed, you can use the nicknames
for materials that are provided by Essentials. The plugin features tab completion to allow you to easily specify
the material.

The optional `max` argument is the maximum number of items that you want to craft. It should be an integer.

**Permissions:**
craftall.ca: Access to the /craftall command

## Contributing
The repo uses maven. Clone the repository and run `mvn clean install` to build and create the JAR into the target folder.
