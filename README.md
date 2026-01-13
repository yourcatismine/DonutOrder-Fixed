# DonutOrder

> **Created by h2ph**

A professional Minecraft plugin that enables players to create and manage item orders with a beautifully designed GUI system. Perfect for survival servers looking to streamline item trading and marketplace functionality.

![Version](https://img.shields.io/badge/version-1.1.3-green.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.21-blue.svg)
![Java](https://img.shields.io/badge/java-21-orange.svg)
![Folia](https://img.shields.io/badge/folia-supported-brightgreen.svg)

---

## 📋 Features

- **📦 Order Management System**: Players can create custom item orders with specific quantities and prices
- **💰 Economy Integration**: Seamless integration with Vault for secure economy transactions
- **🎨 Beautiful GUI**: Professionally designed inventory menus with color-coded elements
- **🔍 Advanced Search & Filter**: Search for specific items and filter orders by multiple criteria
- **📊 Smart Sorting**: Sort orders by most paid, most delivered, recently listed, or money per item
- **✅ Confirmation System**: Safe delivery confirmations to prevent accidental transactions
- **🔔 Sound Effects**: Custom sound effects for enhanced user experience
- **🛡️ Folia Support**: Full compatibility with Folia for multi-threaded server performance
- **⚙️ Highly Configurable**: Extensive configuration options for customization

---

## 🚀 Installation

1. **Download** the latest `DonutOrder-1.1.3.jar` from the releases
2. **Install Dependencies**: Ensure you have [Vault](https://www.spigotmc.org/resources/vault.34315/) installed
3. **Place** the JAR file in your server's `plugins` folder
4. **Restart** your server
5. **Configure** the plugin by editing `plugins/DonutOrder/config.yml`

---

## 📖 Usage

### Commands

| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/orders` | `/order` | Open the orders menu | None |
| `/donutorder reload` | `/dorder`, `/dorders` | Reload the plugin configuration | `donutorder.admin` |

### Permissions

- `donutorder.admin` - Access to admin commands (default: op)

---

## 🎮 How to Use

### For Players

1. **Creating an Order**:
   - Run `/orders` to open the main menu
   - Click "Your Orders" and then "New Order"
   - Select the item you want to order
   - Set the amount and price per item
   - Confirm your order

2. **Delivering Items**:
   - Browse orders in the main menu
   - Click on an order to deliver items
   - Place the items you want to deliver in the GUI
   - Confirm the delivery to receive payment

3. **Managing Your Orders**:
   - View your active orders in "Your Orders"
   - Click on your order to collect delivered items
   - Cancel orders if needed

### For Administrators

- Configure disabled items in `config.yml`
- Customize GUI layouts, colors, and messages
- Adjust sound effects for different actions
- Set up search and filter options

---

## ⚙️ Configuration

The plugin offers extensive configuration options:

- **Disabled Items**: Prevent certain items from being ordered (e.g., spawners)
- **Custom Messages**: Personalize all player-facing messages
- **GUI Customization**: Modify menu titles, item names, lore, and slots
- **Sound Effects**: Configure sounds for clicks, confirmations, and notifications
- **Sort Names**: Customize sorting option names
- **Sign Input**: Configure sign-based input for searches and amounts

For detailed configuration, see the `config.yml` file.

---

## 🛠️ Technical Details

### Dependencies

- **Paper API**: 1.21-R0.1-SNAPSHOT
- **Vault API**: 1.7
- **Java**: 21

### Build

This plugin is built with Maven. To compile from source:

```bash
mvn clean package
```

The compiled JAR will be located in the `target` directory.

---

## 🔧 Development

### Project Structure

```
DonutOrder/
├── src/main/java/me/clanify/donutOrder/
│   ├── cmd/          # Command handlers
│   ├── gui/          # GUI menu implementations
│   ├── store/        # Data managers and storage
│   ├── catalog/      # Item catalog system
│   ├── data/         # Data models and types
│   ├── input/        # Chat and sign input handlers
│   └── util/         # Utility classes
├── src/main/resources/
│   └── plugin.yml    # Plugin metadata
├── config.yml        # Configuration file
└── pom.xml          # Maven build configuration
```

---

## 📝 License

This plugin is created by **h2ph**. All rights reserved.

---

## 🤝 Support

For issues, features requests, or questions:
- Open an issue on the project repository
- Contact the development team

---

## 🌟 Credits

**Created by h2ph**

Special thanks to:
- The Paper development team
- The Vault API maintainers
- The Minecraft plugin development community

---

## 📊 Changelog

### Version 1.1.3
- Current stable release
- Full Folia support
- Enhanced GUI system
- Improved order management
- Bug fixes and performance improvements

---

<div align="center">
  <strong>Made with ❤️ by h2ph</strong>
</div>
