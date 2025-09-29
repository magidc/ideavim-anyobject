<!-- Plugin description -->

# Vim AnyObject

### *Edit code the way you think it*

An extension for [IdeaVim](https://github.com/JetBrains/ideavim) plugin that adds useful text objects to improve your productivity on JetBrains IDEs.

Text objects provide a more natural way to tell the editor what to edit, select or jump. Instead of working with characters, words, lines, or paragraphs, you can use the same
concepts programmers use to think about code: classes, functions, arguments, loops, comments, quotes, brackets, and more.

## Usage

### Deletion, Change, Visual Selection

The plugin follows standard Vim text object conventions with `i` (inner) and `a` (around) modifiers

### Jumping

The plugin supports jumping to the next/previous text object using `]` (next) and `[` (previous)

### Example with argument text object (`a`)

- `dia` - Delete argument
- `daa` - Delete argument and its separator
- `d2a` - Deletes argument and the next one including separators
- `cia` - Change argument
- `caa` - Change argument and its separator
- `c2a` - Change argument and next one including separators
- `yia` - Yank/Copy argument
- `yaa` - Yank/Copy argument and its separator
- `via` - Visually select argument
- `v3a` - Visually select three arguments
- `]a` - Jump to next argument
- `[a` - Jump to previous argument

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

Entire document content.

### AnyBlockComment (`k`)

Block comments across different programming languages:

| Comment Type | Languages                                                                               | Example                |
|--------------|-----------------------------------------------------------------------------------------|------------------------|
| `/* */`      | C, C++, Java, C#, JavaScript, TypeScript, Kotlin, Scala, Swift, Go, Rust, PHP, CSS, SQL | `/* comment */`        |
| `/** */`     | Java (Javadoc), Rust (doc comments)                                                     | `/** documentation */` |
| `<!-- -->`   | HTML, XML, XHTML, Markdown                                                              | `<!-- comment -->`     |
| `""" """`    | Python (docstrings)                                                                     | `"""comment"""`        |
| `''' '''`    | Python (docstrings)                                                                     | `'''comment'''`        |
| `--[[ ]]`    | Lua                                                                                     | `--[[ comment ]]`      |
| `--[=[ ]=]`  | Lua (custom delimiters)                                                                 | `--[=[ comment ]=]`    |
| `#'` to `'`  | R (roxygen comments)                                                                    | `#' comment '`         |

### AnyItem (`i`)

Items within collections, lists, or arrays:

| Structure Type    | Example                        |
|-------------------|--------------------------------|
| Arrays            | `[item1, item2, item3]`        |
| Object properties | `{key1: value1, key2: value2}` |
| List literals     | `(item1, item2, item3)`        |

- **Inner selection (`ii`)**: Selects only the item itself
- **Outer selection (`ai`)**: Selects the entire item including the separator and the item itself

**Multiple outer selections are supported**. For example `d2i` will delete the current item and the next one

### AnyArgument (`a`)

Function arguments, method parameters, and callable expressions.

- **Inner selection (`ia`)**: Selects only the argument itself
- **Outer selection (`ia`)**: Selects the entire argument including the separator and the argument itself

**Multiple outer selections are supported**. For example `d2a` will delete the current argument and the next one

### AnyFunction (`f`)

Functions, methods, and procedures.

- **Inner selection (`if`)**: Selects only the function body
- **Outer selection (`af`)**: Selects the entire function including the function name, decorators and the body

### AnyClass (`c`)

Class, interface, struct, and similar type definitions.

- **Inner selection (`ic`)**: Selects only the class body
- **Outer selection (`ac`)**: Selects the entire class including the name, decorators and the body

### AnyLoop (`l`)

Loop statements and iterative constructs like `for`, `foreach`, `while`, `until`, `do`, `repeat`, `until`, `loop`, `for`, `foreach`, `while`, `until`, `do`, `repeat`, `until`,
`loop`.

- **Inner Selection (`il`)**: Selects only the statements within the current loop/iteration where the cursor is positioned
- **Outer Selection (`al`)**: Selects the entire loop construct including all branches and control keywords

### AnyConditional (`t`)

Conditional statements and expressions like `if-else`, `switch` or `try-catch` statements.

- **Inner Selection (`it`)**: Selects only the statements within the current branch/case where the cursor is positioned
- **Outer Selection (`at`)**: Selects the entire conditional construct including all branches and control keywords

### AnyIndentBlock (`n`)

Code blocks based on indentation levels. This is particularly useful for indentation-based languages like Python, YAML, or Haskell, but also works with brace-based languages to
select logical indentation blocks.

## Configuration

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

<!-- Plugin description end -->

### Some examples

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

#### AnyArgument

- `dia` - Delete argument
- `daa` - Delete argument and its separator
- `d2a` - Deletes argument and the next one including separators
- `cia` - Change argument
- `caa` - Change argument and its separator
- `c2a` - Change argument and next one including separators
- `yia` - Yank/Copy argument
- `yaa` - Yank/Copy argument and its separator
- `via` - Visually select argument
- `v3a` - Visually select three arguments
- `]a` - Jump to next argument
- `[a` - Jump to previous argument

## Installation

### Jetbrains Marketplace

1. Install the plugin from the IntelliJ IDEA Plugin Marketplace
2. Ensure you have the IdeaVim plugin installed and enabled
3. Activate the plugin in your `.ideavimrc`
4. Restart IntelliJ IDEA

### Manual Installation

1. Download the [latest release](https://github.com/magidc/ideavim-anyobject/releases)
2. Install manually using <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built on top of [IdeaVim](https://github.com/JetBrains/ideavim) plugin
- Inspired by the super useful Neovim plugin [nvim-various-textobjs](https://github.com/chrisgrieser/nvim-various-textobjs)
- Vim community for inspiring powerful text object concepts
