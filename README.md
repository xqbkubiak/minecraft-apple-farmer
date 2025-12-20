# 🍎 Apple Bot

<p align="center">
  <img src="src/main/resources/assets/applebot/icon_v2.png" alt="Apple Bot Logo" width="128" height="128">
</p>

<p align="center">
  <b>Automated Apple Farming Bot for Minecraft</b><br>
  <i>Run in background while you do other things! 🖥️</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.4-brightgreen?style=flat-square" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Mod%20Loader-Fabric-blue?style=flat-square" alt="Fabric">
  <img src="https://img.shields.io/badge/Version-2.6-orange?style=flat-square" alt="Version">
  <img src="https://img.shields.io/badge/Java-21+-red?style=flat-square" alt="Java">
</p>

## 📌 Description

Apple Bot is an **AFK farming automation mod** designed for apple farming on Minecraft servers. It automatically cycles through hotbar slots, places leaves, and breaks them with shears - all while keeping you fed and your tools repaired. 

**🖥️ Works in background!** You can minimize Minecraft and do other things while the bot farms for you. Perfect for servers with apple-based economies!

If you use this mod and find it helpful, feel free to share the love! ❤️

---

## ✨ Features

### 🤖 Core Automation
- **Automatic Leaf Breaking** - Cycles through hotbar slots, places leaves, and breaks them with shears
- **Smart Tool Rotation** - Automatically switches between placement and breaking tools
- **Cycle Counter** - Track your farming progress in real-time

### 🔧 Tool Management
- **Auto Repair (Command Mode)** - Automatically executes server repair commands when tools are damaged
- **Auto Craft (Craft Mode)** - Crafts new shears from iron ingots when the current ones break
- **Customizable Repair Command** - Set your server's specific repair command

### 🍖 Survival Features
- **Auto-Eat** - Automatically eats food from your hotbar when hungry
- **Hunger Threshold** - Configurable hunger level to trigger eating
- **Smart Food Detection** - Detects any food item in your hotbar

### 📦 Storage Integration
- **Auto Storage Mode** - Automatically deposits apples into nearby chests
- **Configurable Cycles** - Set how many farming cycles before depositing (20/100/500/1000/2000)
- **Smart Chest Interaction** - Rotates to chest, deposits apples, returns to farming

### 🌍 Localization
- **Multi-language Support** - Full Polish and English translations
- **Easy Switching** - Switch language with a simple command

---

## 📥 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.4
2. Download and install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download Apple Bot and place it in your `mods` folder
4. Launch Minecraft and enjoy!

---

## 🎮 Usage

### Commands

| Command | Description |
|---------|-------------|
| `/xqbk` | Show help menu |
| `/xqbk gui` | Open control panel GUI |
| `/xqbk start` | Start the bot |
| `/xqbk stop` | Stop the bot |
| `/xqbk config delay <1-20>` | Set tick delay (speed) |
| `/xqbk config repairmode <off/command/craft>` | Set repair mode |
| `/xqbk config repaircmd <command>` | Set repair command |
| `/xqbk config autoeat <on/off>` | Toggle auto-eating |
| `/xqbk config storage <on/off> [cycles]` | Configure storage mode |
| `/xqbk pl` | Switch to Polish |
| `/xqbk en` | Switch to English |

### GUI Controls
Open the control panel with `/xqbk gui` to access all settings through a beautiful, Minecraft-styled interface.

### Hotbar Setup
For optimal use, set up your hotbar like this:
- **Slot 1**: Shears (or tool for breaking leaves)
- **Slot 2**: Food (if Auto-Eat enabled)
- **Slots 3-9**: Leaves to place and break

---

## ⚙️ Configuration

### Repair Modes
- **OFF** - No automatic tool repair
- **COMMAND** - Uses server command (default: `/repair`)
- **CRAFT** - Crafts new shears from iron ingots in your inventory

### Speed Settings
- **Delay 1-2**: Very fast (may cause issues on some servers)
- **Delay 3-5**: Fast (recommended)
- **Delay 6-10**: Medium
- **Delay 11-20**: Slow (for laggy servers)

---

## 🔧 Requirements

- Minecraft 1.21.4
- Fabric Loader ≥0.16.10
- Fabric API
- Java 21+

---

## 📸 Screenshots

*Screenshots coming soon*

---

## 🤝 Support

- **Discord**: [discord.com/invite/getnotify](https://discord.com/invite/getnotify)
- **GitHub**: [github.com/xqbkubiak](https://github.com/xqbkubiak)

---

## 📜 License

MIT License © 2025 bkubiakdev

---

<p align="center">
  Made with ❤️ for the Minecraft community
</p>
