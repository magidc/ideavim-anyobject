<!-- Plugin description -->

# IdeaVim AnyObject Plugin

A powerful text object extension for [IdeaVim](https://github.com/JetBrains/ideavim) that adds intelligent "any" text objects to enhance your Vim experience in JetBrains IDEs.

The AnyObject plugin extends IdeaVim with smart text objects that can automatically detect and operate on various types of delimited content, making text manipulation more
intuitive and efficient. Instead of remembering specific quote types or bracket pairs, you can use universal text objects that work with any delimiter.

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

### AnyBlockComment (`c`)

Automatically detects and selects content within any type of block comments across different programming languages:

| Comment Type | Languages                                                                               | Example                |
|--------------|-----------------------------------------------------------------------------------------|------------------------|
| `/* */`      | C, C++, Java, C#, JavaScript, TypeScript, Kotlin, Scala, Swift, Go, Rust, PHP, CSS, SQL | `/* comment */`        |
| `/** */`     | Java (Javadoc), Rust (doc comments)                                                     | `/** documentation */` |
| `<!-- -->`   | HTML, XML, XHTML, Markdown                                                              | `<!-- comment -->`     |
| `""" """`    | Python (docstrings)                                                                     | `"""comment"""`        |
| `''' '''`    | Python (docstrings)                                                                     | `'''comment'''`        |
| `(* *)`      | Pascal, Delphi, OCaml, F#, AppleScript                                                  | `(* comment *)`        |
| `{ }`        | Pascal, Delphi (alternative style)                                                      | `{ comment }`          |
| `%{ %}`      | MATLAB, Octave                                                                          | `%{ comment %}`        |
| `--[[ ]]`    | Lua                                                                                     | `--[[ comment ]]`      |
| `--[=[ ]=]`  | Lua (custom delimiters)                                                                 | `--[=[ comment ]=]`    |
| `<# #>`      | PowerShell                                                                              | `<# comment #>`        |
| `{- -}`      | Haskell                                                                                 | `{- comment -}`        |
| `#'` to `'`  | R (roxygen comments)                                                                    | `#' comment '`         |
| `!* *!`      | Some Fortran variants                                                                   | `!* comment *!`        |

### AnyItem (`i`)

Automatically detects and selects individual items within collections, lists, or arrays. This text object intelligently identifies list-like structures and selects the current item
based on the cursor position:

| Structure Type      | Example                        | Selected Item                   |
|---------------------|--------------------------------|---------------------------------|
| Arrays              | `[item1, item2, item3]`        | Current item under cursor       |
| Function parameters | `function(arg1, arg2, arg3)`   | Current parameter               |
| Object properties   | `{key1: value1, key2: value2}` | Current key-value pair          |
| List literals       | `(item1, item2, item3)`        | Current list item               |
| For loop conditions | `for (int i = 0; i < 10; i++)` | `int i = 0`, `i < 10`, or `i++` |

### AnyArgument (`a`)

Automatically detects and selects function arguments, method parameters, and callable expressions. This text object intelligently identifies argument lists and selects the current
argument based on the cursor position:

| Structure Type          | Example                      | Selected Argument             |
|-------------------------|------------------------------|-------------------------------|
| Function calls          | `func(arg1, arg2, arg3)`     | Current argument under cursor |
| Method calls            | `obj.method(param1, param2)` | Current parameter             |
| Constructor calls       | `new Object(value1, value2)` | Current constructor argument  |
| Array/List constructors | `Array(item1, item2, item3)` | Current array element         |
| Generic type parameters | `List<String, Integer>`      | Current type parameter        |
| Lambda parameters       | `(param1, param2) => body`   | Current lambda parameter      |
| Tuple elements          | `(first, second, third)`     | Current tuple element         |
| Macro arguments         | `macro!(arg1, arg2)`         | Current macro argument        |

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
