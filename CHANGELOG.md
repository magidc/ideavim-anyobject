# Changelog

## 1.1.0

Adds support for block comments, function arguments, and collection items.

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

## 1.0.1

Fixes bug in when using text objects with operators. Ranges where often miscalculated.

## 1.0.0

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

