# Gradient Text Plugin - Forge 1.20.1

A Minecraft Forge mod that adds dynamic gradient text support for items and display text.

## Features

- **Gradient Item Names**: Apply color gradients to item display names
- **Gradient Lore**: Apply gradients to item description text
- **Multiple Colors**: Support for 2+ color gradients
- **Direction Control**: Horizontal or vertical gradient direction
- **NBT Storage**: Gradient data saved directly on items
- **GUI Configuration**: In-game inventory-based GUI for easy setup
- **Chat Commands**: Quick gradient application via commands

## Installation

1. Install Minecraft Forge 1.20.1 (47.2.0 or compatible)
2. Build the mod:
   ```bash
   ./gradlew build
   ```
3. Copy `build/libs/gradienttext-1.0.0.jar` to your Minecraft `mods` folder

## Usage

### Commands

```
/gradient <startColor> <endColor>           - Apply 2-color gradient
/gradient <startColor> <endColor> <dir>     - With direction (horizontal/vertical)
/gradient remove                            - Remove gradient from held item
/gradient info                              - Show gradient info
/gradient help                              - Show help
```

**Color Formats:**
- Hex: `#FF5500` or `FF5500`
- RGB: `255,85,0`
- Named: `red`, `blue`, `aqua`, `gold`, etc.

**Examples:**
```
/gradient #FF0000 #00FF00
/gradient red blue horizontal
/gradient FF5500,FF00,0 0,FF,FF vertical
```

### GUI

Hold an item and run `/gradientgui` or use a right-click interaction to open the gradient configuration screen.

### Presets

The GUI includes 6 built-in gradient presets that you can cycle through.

## Building

```bash
# Build the mod
./gradlew build

# Run client for testing
./gradlew runClient

# Run server for testing
./gradlew runServer
```

## Development

The mod uses:
- ForgeGradle 6.x for building
- Official Mojang mappings
- Brigadier for commands
- Forge's SimpleChannel for networking

## License

MIT
