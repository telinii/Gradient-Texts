# Gradient Texts - Forge 1.20.1

A Minecraft Forge mod that adds dynamic gradient text support for items with smooth color animations.

## Features

- **Gradient Item Names**: Apply color gradients to item display names
- **Up to 4 Colors**: Create smooth gradients with 2, 3, or 4 colors
- **3 Animation Modes**: Static, Dynamic (scrolling), and Smooth (color transition)
- **Direction Control**: Horizontal, Vertical, or Fix (for smooth mode)
- **Speed Control**: Adjust animation speed from 0.1x to 10x
- **Bold Text**: Optional bold formatting
- **Tab Auto-complete**: Full command auto-completion with Tab key
- **Config GUI**: In-game configuration screen with edit support
- **Forced Gradients**: Apply gradients automatically to specific items
- **Blacklisting**: Exclude items from receiving gradients
- **Hot Reload**: Reload config without restarting Minecraft

## Installation

1. Install Minecraft Forge 1.20.1 (47.2.0 or higher)
2. Download `gradienttext-1.0.0.jar` from [Releases](https://github.com/telinii/Gradient-Texts/releases)
3. Place the jar in your Minecraft `mods` folder
4. Launch Minecraft with Forge profile

## Commands

### Basic Usage

```
/gradient <color1> [color2] [color3] [color4]
```

Apply a gradient with 1-4 colors. Hold an item in your hand first.

### With Options

```
/gradient <colors> options <direction> <mode> <bold> <speed>
```

### All Commands

| Command | Description |
|---------|-------------|
| `/gradient <colors>` | Apply gradient with default settings |
| `/gradient <colors> options <dir> <mode> <bold> <speed>` | Apply with custom options |
| `/gradient remove` | Remove gradient from held item |
| `/gradient info` | Show gradient info on held item |
| `/gradient config` | Open config GUI |
| `/gradient reload` | Reload config from disk |
| `/gradient blacklist <id>` | Blacklist an item |
| `/gradient unblacklist <id>` | Remove from blacklist |
| `/gradient help` | Show help message |

### Color Formats

| Format | Example |
|--------|---------|
| Named | `red`, `blue`, `aqua`, `gold`, `green`, `yellow` |
| Hex | `#FF5500`, `FF00FF` |
| RGB | `255,85,0` |

**Available Named Colors:**
`black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`, `orange`, `pink`, `brown`, `purple`, `magenta`, `cyan`

### Directions

| Direction | Description |
|-----------|-------------|
| `horizontal` | Gradient flows left to right (default) |
| `vertical` | Gradient flows top to bottom |
| `fix` | No direction, used for smooth mode |

### Modes

| Mode | Description |
|------|-------------|
| `static` | Fixed gradient, no animation (default) |
| `dynamic` | Gradient scrolls across the text |
| `smooth` | Whole word changes color smoothly |

### Speed

Values from `0.1` to `10.0`:
- `0.5` - Slow animation
- `1.0` - Normal speed (default)
- `2.0` - Fast animation
- `5.0` - Very fast

### Examples

**Basic 2-color gradient:**
```
/gradient red blue
```

**4-color rainbow gradient:**
```
/gradient red yellow green blue
```

**Dynamic scrolling gradient:**
```
/gradient red blue options horizontal dynamic
```

**Smooth color transition with bold:**
```
/gradient red blue options fix smooth true
```

**Fast vertical gradient:**
```
/gradient #FF0000 #00FF00 options vertical static false 3.0
```

## Configuration

### Config File Location

```
config/gradienttext/gradienttext.json
```

### Config GUI

Run `/gradient config` in-game to open the configuration screen.

**Left Panel:**
- Item ID input (with auto-complete)
- Custom Name (optional)
- Colors input (comma-separated)
- Direction, Mode, Speed settings
- Add/Update and Blacklist buttons

**Right Panel:**
- FORCED list - items with automatic gradients
- BLACKLIST list - items excluded from gradients

**Editing Forced Items:**
1. Click an item in the FORCED list
2. Click "Edit" button - fields populate
3. Make changes
4. Click "Update"
5. Click "Save"

### Manual Config Edit

```json
{
  "smoothGradient": true,
  "blacklistedItems": [
    "minecraft:stick"
  ],
  "forcedGradients": {
    "minecraft:diamond_sword": {
      "colors": [16711680, 65280],
      "direction": "horizontal",
      "mode": "static",
      "bold": false,
      "speed": 1.0,
      "customName": ""
    }
  }
}
```

### Reload Config

After editing the config file, run:
```
/gradient reload
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/telinii/Gradient-Texts.git
cd Gradient-Texts

# Build the mod
./gradlew build

# The jar will be in build/libs/
```

## Compatibility

- Minecraft: 1.20.1
- Forge: 47.2.0 or higher
- Java: 17 or higher

## License

MIT
