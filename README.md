# Player Brightness Dimmer

A Minecraft mod (v26.1.2) that gives you control over the brightness of the player model. This helps reduce eye strain caused by overly bright player models in dimly lit areas (such as caves) when using third-person or specific first-person mods.

## Features
* **Brightness Interpolation:** Three modes (Fancy, Fast, Off) to smoothly transition the player's lighting and eliminate jarring "strobe" effects when crossing block boundaries.
* **Configurable Interpolation Speed:** Adjust the speed at which the brightness visually transitions.
* Configurable brightness reduction.
* Configurable maximum brightness limit.
* Configurable minimum brightness limit.
* Options to apply the dimmer exclusively to the main player or to all players.
* Accessible via Mod Menu.

## Requirements
* Minecraft v26.1.2
* Fabric Loader (>=0.14.22)
* Java 25 or higher
* Cloth Config API

## Authors
* Gemini 3.1 Pro
* cwright814
* Gemma 4


## Development
To increment the version number for a new release, use the included version bumping script. This ensures all relevant files are kept in sync:
```bash
./bump-version.sh <new_version>
```
Example: `./bump-version.sh 1.0.2`
