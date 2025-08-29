<!-- Plugin description -->

# IdeaVim AnyObject Plugin

A powerful text object extension for [IdeaVim](https://github.com/JetBrains/ideavim) that adds intelligent "any" text objects to enhance your Vim experience in JetBrains IDEs.


The AnyObject plugin extends IdeaVim with smart text objects that can automatically detect and operate on various types of delimited content, making text manipulation more intuitive and efficient. Instead of remembering specific quote types or bracket pairs, you can use universal text objects that work with any delimiter.

## Features

### AnyQuote (`q`)
Automatically detects and selects content within any type of quotes:

| Quote Type    | Example      | Description                       |
|---------------|--------------|-----------------------------------|
| Single quotes | `'text'`     | Standard single-quoted strings    |
| Double quotes | `"text"`     | Standard double-quoted strings    |
| Backticks     | `` `text` `` | Template literals and code blocks |

### AnyBracket (`o`)
Automatically detects and selects content within any type of brackets:

| Bracket Type      | Example   | Description              |
|-------------------|-----------|--------------------------|
| Parentheses       | `(text)`  | Function calls, grouping |
| Square brackets   | `[text]`  | Arrays, indexing         |
| Curly braces      | `{text}`  | Objects, code blocks     |
| Angle brackets    | `<text>`  | Generics                 |
| HTML/XML brackets | `<text/>` | HTML/XML tags, generics  |

<!-- Plugin description end -->

## Usage

The plugin follows standard Vim text object conventions with `i` (inner) and `a` (around) modifiers:

### AnyQuote Examples
- `diq` - Delete text inside any quotes
- `daq` - Delete text including the quotes
- `ciq` - Change text inside any quotes
- `yiq` - Yank text inside any quotes
- `viq` - Visually select text inside any quotes

### AnyBracket Examples
- `dio` - Delete text inside any brackets
- `dao` - Delete text including the brackets
- `cio` - Change text inside any brackets
- `yio` - Yank text inside any brackets
- `vio` - Visually select text inside any brackets

## Installation
### Jetbrains Marketplace

1. Install the plugin from the IntelliJ IDEA Plugin Marketplace
2. Ensure you have the IdeaVim plugin installed and enabled
3. Activate the plugin in your `.ideavimrc`
4. Restart IntelliJ IDEA

### Manual Installation

1. Download the [latest release](https://github.com/magidc/ideavim-anyobject/releases)
2. Install manually using <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

### Configuration

Configure which transformation groups to enable in your `.ideavimrc`:

```vimscript
" Activate plugin
set anyobject
```

### Customization

You can customize the default mappings by adding the following to your `.ideavimrc`:

```vimscript
" Use 'k' instead of 'o' for any bracket text object
omap ik <Plug>InnerAnyBracket
omap ak <Plug>OuterAnyBracket
vmap ik <Plug>InnerAnyBracket
vmap ak <Plug>OuterAnyBracket
```

## Contributing
Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.


## License
This project is licensed under the MIT License - see the LICENSE file for details.


## Acknowledgments
- Built on top of [IdeaVim](https://github.com/JetBrains/ideavim) plugin
- Inspired by the super useful Neovim plugin [nvim-various-textobjs](https://github.com/chrisgrieser/nvim-various-textobjs)
- Vim community for inspiring powerful text object concepts
