<!-- Plugin description -->

# Vim AnyObject

### *Unleash the power of text objects*

An extension for [IdeaVim](https://github.com/JetBrains/ideavim) plugin that adds useful text objects to improve your productivity on JetBrains IDEs.

Text objects allow a more efficient way of communicating edition or selection actions in the editor. Instead of thinking in terms of characters, words, lines, or paragraphs, use
more advance text constructs like quoted text, text between brackets, items in a collection, or programming language constructs like arguments, classes, functions, loops, or
comments.

## Provided text objects

### AnyQuote (`q`)

Content enclosed between any type of quotes:

| Quote Type    | Example      | 
|---------------|--------------|
| Single quotes | `'text'`     |
| Double quotes | `"text"`     |
| Backticks     | `` `text` `` | 

### AnyBracket (`o`)

Content enclosed between any type of brackets:

| Bracket Type      | Example   | Description              |
|-------------------|-----------|--------------------------|
| Parentheses       | `(text)`  | Function calls, grouping |
| Square brackets   | `[text]`  | Arrays, indexing         |
| Curly braces      | `{text}`  | Objects, code blocks     |
| Angle brackets    | `<text>`  | Generics                 |
| HTML/XML brackets | `<text/>` | HTML/XML tags, generics  |

### AnyDocument (`d`)

Select the entire document content.

### AnyBlockComment (`c`)

Block comments across different programming languages:

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

Items within collections, lists, or arrays:

| Structure Type    | Example                        |
|-------------------|--------------------------------|
| Arrays            | `[item1, item2, item3]`        |
| Object properties | `{key1: value1, key2: value2}` |
| List literals     | `(item1, item2, item3)`        |

### AnyArgument (`a`)

Function arguments, method parameters, and callable expressions.

### AnyFunction (`f`)

Select functions, methods, and procedures.

### AnyClass (`c`)

Select class, interface, struct, and similar type definitions.

### AnyLoop (`l`)

Select loop statements and iterative constructs (for, while, do-while, foreach, enhanced for, range-based, and language-specific constructs).

<!-- Plugin description end -->

## Usage

The plugin follows standard Vim text object conventions with `i` (inner) and `a` (around) modifiers

## Some examples

#### AnyQuote

- `diq` - Delete text inside any quotes
- `daq` - Delete text including the quotes
- `ciq` - Change text inside any quotes
- `yiq` - Yank text inside any quotes
- `viq` - Visually select text inside any quotes

#### AnyBracket

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

You can customize the default mappings by adding the following to your `.ideavimrc`. For example:

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
