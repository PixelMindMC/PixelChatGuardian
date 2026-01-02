# PixelChat Guardian - AI-Powered Chat Moderation

Advanced AI-powered chat moderation for Minecraft servers. Automatically filter inappropriate messages, support emojis &
chat formatting, and integrate seamlessly with your existing setup.

![](https://img.shields.io/badge/Minecraft-1.17--1.21.11-blue)
![](https://img.shields.io/badge/Server-Spigot%20and%20Paper-green)
![](https://img.shields.io/badge/License-GPLv3-yellow)

## 🚀 Features

### 🤖 AI-Powered Moderation

- **Smart Filtering**: Uses AI models to detect and filter inappropriate content
- **Multi-Language Support**: Automatically moderates messages in multiple languages
- **Configurable Rules**: Control what gets filtered (offensive language, personal info, websites, etc.)
- **Flexible Actions**: Choose to censor or block messages entirely
- **Strike System**: Automatic punishment system with kick/ban thresholds

### 💬 Enhanced Chat Experience

- **Emoji Support**: 🎉 Use emojis in chat with **:emoji:** codes
- **Chat Codes**: Format messages with **:color:** and **:format:** codes
- **Plugin Integration**: Compatible with CarbonChat and other chat plugins

### ⚙️ Advanced Configuration

- **Multi-Language Interface**: Plugin interface available in 8+ languages
- **Customizable AI Prompts**: Fine-tune the AI moderation behavior
- **Granular Controls**: Enable/disable specific modules as needed
- **Custom Prefix**: Add a custom moderation message prefix

## 📦 Installation

1. **Download**: Get the latest version from [Modrinth](https://modrinth.com/plugin/pixelchatguardian/) or one of our
   other
   distribution platforms
2. **Install**: Place the `.jar` file in your server's `plugins/` folder
3. **Configure**: Open the `config.yml` file located in the `plugins/PixelChatGuardian/` directory
   ```yaml
   api:
     endpoint: "https://api.groq.com/openai/v1/chat/completions" # Optionally, specify a different API endpoint
     ai-model: "llama-3.3-70b-versatile" #  Optionally, specify a different AI model
     key: "API-KEY" # Add your API key
   ```
4. **Restart**: Restart your server

## ⚡ Quick Start for using Groq

1. Sign up for a free account at [Groq Cloud](https://console.groq.com/)
2. Navigate to API Keys section
3. Create a new API key
4. Add it to your `config.yml`
5. Restart your server

## 🌐 Supported Languages

The plugin interface supports:

- English (en) - Default
- German (de)
- Spanish (es)
- French (fr)
- Dutch (nl)
- Simplified Chinese (zh-cn)
- Traditional Chinese (zh-tw)
- Custom translations

AI moderation works with messages in all languages!

## 🔌 Plugin Integration

### CarbonChat Support

PixelChat Guardian integrates seamlessly with [CarbonChat](https://modrinth.com/plugin/carbon):

```yaml
plugin-support:
  carbonchat: true  # Tested with CarbonChat 3.0.0-beta.36+
```

### Other Chat Plugins

The plugin is designed to work alongside most chat plugins. If you encounter compatibility issues, please report them on
GitHub.

## ❓ Troubleshooting

### Common Issues

1. **AI not filtering messages**
    - Check your API key in `config.yml`
    - Verify internet connectivity
    - Ensure `modules.chatguard: true`
2. **Emojis/Chat codes not working**
    - Check player permissions
    - Verify `modules.emojis` and `modules.chat-codes` are enabled
3. **Compatibility issues**
    - Try disabling other chat plugins temporarily
    - Check server logs for errors

### Getting Help

- **GitHub Issues**: [Report bugs or request features](https://github.com/PixelMindMC/PixelChatGuardian/issues)
- **Discord**: [Join our community](https://discord.com/invite/hyGpwCp3zq)
- **Wiki**: [Detailed documentation](https://github.com/PixelMindMC/PixelChatGuardian/wiki)

## 🔗 Important Links

| Platform          | Link                                                                                         | Description                      |
|-------------------|----------------------------------------------------------------------------------------------|----------------------------------|
| **Wiki**          | [Documentation](https://github.com/PixelMindMC/PixelChatGuardian/wiki)                       | Complete documentation           |
| **Issues**        | [Issue Tracker](https://github.com/PixelMindMC/PixelChatGuardian/issues)                     | Bug reports and feature requests |
| **Pull Requests** | [Contribute](https://github.com/PixelMindMC/PixelChatGuardian/pulls)                         | Contribute to the project        |
| **Changelog**     | [Version History](https://github.com/PixelMindMC/PixelChatGuardian/blob/master/CHANGELOG.md) | Update history and changes       |
| **Modrinth**      | [Download](https://modrinth.com/plugin/pixelchatguardian/)                                   | Primary download platform        |
| **SpigotMC**      | [SpigotMC Page](https://www.spigotmc.org/resources/pixelchat-guardian.120146/)               | SpigotMC download                |
| **Hangar**        | [Paper Plugin Page](https://hangar.papermc.io/PixelMindMC/PixelChat_Guardian)                | Paper Plugin Hangar download     |
| **CurseForge**    | [CurseForge Page](https://www.curseforge.com/minecraft/bukkit-plugins/pixelchat-guardian)    | CurseForge download              |
| **Discord**       | [Community](https://discord.gg/hyGpwCp3zq)                                                   | Support and community            |
| **bStats**        | [Statistics](https://bstats.org/plugin/bukkit/PixelChat%20Guardian/23371)                    | Usage statistics                 |

## 📄 License

This project is licensed under the **GNU General Public License v3.0**. See
the [LICENSE](https://github.com/PixelMindMC/PixelChatGuardian/blob/master/LICENSE) file for details.

## 🌟 Support the Project

If you find PixelChat Guardian useful, please consider:

- ⭐ **Starring** the project on GitHub, Modrinth, SpigotMC, Hangar and/or CurseForge
- 📝 **Leaving** a review on SpigotMC and/or CurseForge
- 💬 **Sharing** with other server owners
- 🐛 **Reporting** bugs and suggesting features

Thank you for choosing PixelChat Guardian for your server's moderation needs!