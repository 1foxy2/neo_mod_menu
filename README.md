fork of Mod menu https://modrinth.com/mod/modmenu for neoforge. compatible with connector and connector extras. 

do add more details about your mod just put those lines into neoforge.mods.toml

```
[modproperties.modmenu] 
badges=["client", "library"] # badges, available: client, library, deprecated
links=["translation.key.example=https://something.com"] # a link, before = is the translation key and after is the url
contributors=[ # list of contributors
"contributor1",
"contributor2"
]
sources="https://url to sources.sources" # a link to sources of the mod

[modproperties.modmenu_parent] # other than id is only needed if parent isnt a real mod
id="example-api" #id of a parrent mod
name= "Example API" #name of a parrent mod
description= "Modular example library"
icon= "assets/example-api-module-v1/parent_icon.png"
badges= ["library"]
```