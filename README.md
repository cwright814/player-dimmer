# Player Brightness Dimmer

A Minecraft mod (v26.1.2) that began as an idea to reduce headaches caused by overly-bright tools in dark caves. Its main purpose is to give you control over the brightness of player models - a great companion with Beryl shaders. Interpolation was later added to eliminate brightness strobing on player models as the vanilla 16 light levels is not enough when your bright-to-dark contrast is heavily boosted - again, a must-have with Beryl. I then saw the opportunity to apply this to all other entities as well, so I did! Default settings should remain near-vanilla speeds for standard situations, automatically disabling interpolation when near congested mob grinders. A Fancy setting for all entities is available as well, which collects light samples from the 8 nearest blocks and uses trilinear filtering to calculate an average... giving you the most accurate lighting calculation with zero smoothing latency.

## Features
* **Eye saver** - reduce brightness for any entity with several config options.
* **Brightness smoothing** - add interpolation to entity light levels for smooth transitions - perfect for VulkanMod's Beryl shaders.
* **Configurable** - adjust with Mod Menu or the auto-generated json file to get the exact look and optimization that you'd like.
* **Performant** - no shaders (literally) so it's gpu and renderer agnostic (works great on mobiles!).

## Requirements
* Minecraft v26.1.2 (JDK >=25)
* Fabric Loader (>=0.14.22)
* Cloth Config (>=26.1.154)
* Mod Menu (>=18.0.0)

## Authors
* cwright814
  * Concept, guidance, platform-testing, and documentation
* Gemini 3.1 Pro
  * Planning and initial implementation for player entities

## Contributors
* [Gemma 4](https://huggingface.co/bartowski/google_gemma-4-31B-it-GGUF) ([llama.cpp](https://github.com/ggml-org/llama.cpp) and [Cline CLI](https://github.com/cline/cline/blob/main/apps/cli/README.md))
  * Generalization for all other entities, performance optimizations, and bug fixes

## Development
To increment the version number for a new release, use the included version bumping script. This ensures all relevant files are kept in sync:
```bash
./bump-version.sh <new_version>
```
Example: `./bump-version.sh 1.0.2`
