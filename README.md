# Gradient Texts

A Minecraft Forge 1.20.1 mod that adds beautiful gradient color effects to item names and tooltips.

## What's New (Update 2)

### Apotheosis Compatibility
- Fixed compatibility issues with Apotheosis mod
- Resolved conflicts with item tooltip rendering
- Gradients now work correctly alongside Apotheosis enchanting and spawner systems

## Features

### Default Tool Gradients
- Auto-applies gradients to all tools in your inventory
- Detects tools by type (sword, axe, pickaxe, shovel, hoe) — works with modded tools
- Material-based colors:
  - **Wood**: Brown / Orange
  - **Stone**: Gray / Light Gray
  - **Iron**: White / Sky Blue
  - **Gold**: Gold / Orange
  - **Diamond**: Cyan / Light Blue
  - **Netherite**: Gray / Dark Red

### Default Armor Gradients
- Auto-applies gradients to all armor in your inventory
- Detects armor by material — works with modded armor
- Material-based colors:
  - **Leather**: Brown / Tan
  - **Chain**: Gray / Silver
  - **Iron**: Light Gray / White
  - **Gold**: Gold / Light Gold
  - **Diamond**: Teal / Mint
  - **Netherite**: Gray / Dark Red

### Gradient Mode Selector
- **Static**: Gradient stays fixed on the item name
- **Dynamic**: Gradient scrolls through colors over time
- **Smooth (Lethality)**: All characters share one color that pulses through the palette

### Config GUI
- "Tool Gradients" toggle button (top-right)
- "Armor Gradients" toggle button (below Tool Gradients)
- Mode selector buttons (Static / Dynamic / Smooth) with active indicator (`*`)
- Opaque backgrounds on all text labels and suggestion dropdowns

### Expanded Color Support
- 68+ named colors (navy, teal, coral, neon, pastel, etc.)
- Unlimited colors per gradient
- Creative inventory items show gradient on tooltip

### Performance Optimizations
- `TextColor.fromRgb()` for faster rendering
- Early NBT checks before full gradient data parse
- Shared Style object for smooth mode characters
- Lore caching with WeakHashMap in tooltip handler

## Configuration

```json
{
  "smoothGradient": true,
  "defaultToolGradients": false,
  "defaultArmorGradients": false,
  "defaultGradientMode": "static",
  "blacklistedItems": [],
  "forcedGradients": {}
}
```

## Commands

| Command | Description |
|---------|-------------|
| `/gradient <colors>` | Apply gradient (dash-separated) |
| `/gradient config` | Open config GUI |
| `/gradient remove` | Remove gradient from held item |
| `/gradient info` | Show gradient info |
| `/gradient blacklist <id>` | Blacklist an item |
| `/gradient reload` | Reload config from disk |

## Building

```bash
git clone https://github.com/telinii/Gradient-Texts.git
cd Gradient-Texts
git checkout GTs-Update2
./gradlew build
```

## Compatibility

- Minecraft: 1.20.1
- Forge: 47.2.0+
- Java: 17+
