# Gradient Texts - Update 1

## What's New

### Gradient Wands
- Two new building tools for recoloring blocks with themed gradient ramps:
  - **Gradient Wand** (`gradienttext:gradient_wand`) - recolors exposed faces of a region
  - **Gradient Depth Wand** (`gradienttext:depth_gradient_wand`) - paints down columns, only touching whitelisted blocks
- **Controls**:
  - **Right-click** anywhere - apply the gradient at the point you are aiming at (up to 32 blocks away, no need to walk up to a block)
  - **Shift + Right-click** anywhere - open the wand's configuration menu
- 10 themed gradients (dark -> light block ramps): Deepslate Vein, Nether Temple, End Vault, Desert Tomb, Twisted Growth, The Void, Heaven, Space, Matrix, Molten Core
- Surface wand options: gradient, radius (1-9), axis (vertical / horizontal)
- Depth wand options: gradient, radius (1-9), depth (1-32), and a searchable whitelist of blocks to recolor
- Per-block randomness so builds keep a natural, varied texture
- **Area preview**: holding a wand shows a wireframe box over the exact region that would be affected, tracking your aim point

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

### Default Material Gradients
- Auto-applies gradients to items based on their crafting material
- Resolves the material from the item's recipe hierarchy (root items)
- New `defaultMaterialGradients` config toggle

### Gradient Mode Selector
- **Static**: Gradient stays fixed on the item name
- **Dynamic**: Gradient scrolls through colors over time
- **Smooth (Lethality)**: All characters share one color that pulses through the palette

### Config GUI Updates
- "Tool Gradients" toggle button (top-right)
- "Armor Gradients" toggle button (below Tool Gradients)
- Mode selector buttons (Static / Dynamic / Smooth) with active indicator (`*`)
- Opaque backgrounds on all text labels and suggestion dropdowns

### Command Updates
- Color separator changed from `,` to `-` (e.g., `/gradient red-blue-yellow`)
- `/gradient config` now opens the GUI directly (no network packet needed)
- Fixed italic text issue — gradients no longer apply italic formatting

### Expanded Color Support
- Removed MAX_COLORS limit — unlimited colors per gradient
- 68+ named colors added (navy, teal, coral, neon, pastel, etc.)
- Creative inventory items now show gradient on tooltip

### Performance Optimizations
- `TextColor.fromRgb()` instead of `String.format()` + `TextColor.parseColor()`
- Early NBT checks before full gradient data parse
- Shared Style object for smooth mode characters
- Lore caching with WeakHashMap in tooltip handler

## Configuration

```json
{
  "smoothGradient": true,
  "defaultToolGradients": false,
  "defaultArmorGradients": false,
  "defaultMaterialGradients": false,
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
git checkout GTs-Update1
./gradlew build
```

## Compatibility

- Minecraft: 1.20.1
- Forge: 47.2.0+
- Java: 17+
