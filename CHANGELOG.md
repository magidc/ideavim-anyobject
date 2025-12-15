# Changelog
## 3.0.7
### AnyString (`g`)

String data to assign to a variable or field.

- **Inner selection (`ig`)**: Selects only the string content
- **Outer selection (`ag`)**: Selects the entire string including quotes and leading formatting symbols

### AnyVariable (`v`)

Field/variable declarations and assignments.

- **Inner selection (`iv`)**: Selects only the value assigned to the field/variable if any
- **Outer selection (`av`)**: Selects the entire field/variable declaration


## 3.0.6
Improved Ruby on Rails support
Extended AnyItem scope to include hash collection items. Kudos to **@ZimCodes** for the suggestion.

### AnyField (`v`)

Field and variable declarations. It only works in languages that require explicit field declarations; it won't select anything in languages like Python, Ruby, or PHP that use implicit field declarations.

- **Inner selection (`iv`)**: Selects only the value assigned to the field/variable if any
- **Outer selection (`av`)**: Selects the entire field/variable declaration

## 3.0.5
Bug fixes for Kotlin support
Visual text objects jumps seems to be ready

## 3.0.4
Byte offsets bug fixes
Disabling continuous Treesitter parsing until it is stable enough
Visual text objects jumps

## 3.0.3
Better Kotlin support
Fixed document offset caches; it was wrongly set as a global variable shared across all documents

## 3.0.2
Fixed compatibility with Rider 2025.3.0.3

## 3.0.1
Solved many reported issues in the first release based on Treesitter

## 3.0.0
### Tree-sitter
From version 3.0.0 the plugin replaces the approach to detect coding text objects from PSI-based approach with Tree-sitter.
This provides a more consistent model across different languages.

Removed AnyBlockComment in favor of more generic AnyComment.

## 2.0.7
Improving argument selection in PHP.

## 2.0.6
Fixed issues selecting collection items and arguments in various platforms.

## 2.0.5
### Improved C# support
Fixed multiple issues detecting C# text objects.

## 2.0.4
If the current cursor position is within a text object, it will be selected. Otherwise, the nearest text object in the specified direction will be selected.
This is a standard behavior of Vim's text objects that was not properly implemented in this plugin. Kudos to **@kbilsted** for reporting this issue.

## 2.0.3

- **Improved implementation for Item and Argument text objects**
- **Bug fix: Class inner selection was not working correctly**
- **[Experimental] Added caches to improve performance on PSIFile element lookup**

## 2.0.2

- **New approach to identify PSI target elements**
- **Improved PHPStorm support**

### AnySubword (`u`)

Nested words in different case styles:

| Case type  | Example   | 
|------------|-----------|
| Camel case | `fooBar`  |
| Snake case | `foo_bar` |
| Dash case  | `foo-bar` | 

## 2.0.1

Changed default mappings for AnyConditional to `y` to avoid confusion with Vim default text object for tags `t`

## 2.0.0

### Jump!!

It is possible now to jump to the next or previous text object.

Default mapping is `]<textObject>` to jump to next and `[<textObject>` to jump to previous text object.

### New way of customization

Don't want to use all the provided text objects? Specify which ones to enable using `anyobject_included` variable in your `.ideavimrc`:

```vimscript
let g:anyobject_included = "anyDocument,anyFunction"
```

If you prefer to specify which ones to exclude, use the `anyobject_excluded` variable instead:

```vimscript
let g:anyobject_excluded = "anyDocument,anyFunction"
```

You can also customize the mappings by adding the following to your `.ideavimrc`. For example:

```vimscript
" Use 'm' instead of default 'f' for any function text object
let g:anyobject_map_anyfunction = "m"

" Use 's' instead of default 'd' for any document text object
let g:anyobject_map_anydocument = "s"
```

In case of mapping conflicts, the custom mappings will take precedence and invalidate any other handler using the same mapping.

Jump motion can be also customized:

```vimscript
" Use '<' instead of default '[' for jumping to the previous text object
let g:anyobject_map_jump_prev = "<" 

" Use '>' instead of default ']' for jumping to the next text object
let g:anyobject_map_jump_next = ">" 
```

### Other changes

- **Multiple object outer selection for AnyArgument and AnyItem**: You can now select multiple objects of the same type in a single command. For example, `d2a` deletes the current
  and the next argument.
- **Curson won't change positions on yank actions**

## 1.3.2

- **Bug fix**: Fix bug in outer argument selection. In some cases the right separator was not selected.

## 1.3.1

- **Bug fix**: Fix bug in Python literal list item selection

## 1.3.0

### AnyConditional (`t`)

Conditional statements and expressions like `if-else`, `switch` or `try-catch` statements.

- **Inner Selection (`it`)**: Selects only the statements within the current branch/case where the cursor is positioned
- **Outer Selection (`at`)**: Selects the entire conditional construct including all branches and control keywords

### AnyIndentBlock (`n`)

Code blocks based on indentation levels. This is particularly useful for indentation-based languages like Python, YAML, or Haskell, but also works with brace-based languages to
select logical indentation blocks.

## 1.2.1

Loop inner selection bug fix: Surrounding brackets (if any) were also selected

AnyBracket bug fix: Nested loops were not properly selected

Performance improvements

## 1.2.0

Add support for selecting functions, classes, and loops.

### AnyFunction (`f`)

Select functions, methods, and procedures.

### AnyClass (`c`)

Select class, interface, struct, and similar type definitions.

### AnyLoop (`l`)

Select loop statements and iterative constructs (for, while, do-while, foreach, enhanced for, range-based, and language-specific constructs).

### AnyDocument (`d`)

Select the entire document content.

## 1.1.1

Bug in argument selection: When using outer mode (a), if the argument was the first, the right separator was not selected too, leaving the code inconsistent

## 1.1.0

Adds support for block comments, function arguments, and collection items.

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

| Structure Type      | Example                        | Selected Item             |
|---------------------|--------------------------------|---------------------------|
| Arrays              | `[item1, item2, item3]`        | Current item under cursor |
| Function parameters | `function(arg1, arg2, arg3)`   | Current parameter         |
| Object properties   | `{key1: value1, key2: value2}` | Current key-value pair    |
| List literals       | `(item1, item2, item3)`        | Current list item         |

### AnyArgument (`a`)

Function arguments, method parameters, and callable expressions:

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

Content enclosed between any type of quotes:

| Quote Type    | Example      | Description                       |
|---------------|--------------|-----------------------------------|
| Single quotes | `'text'`     | Standard single-quoted strings    |
| Double quotes | `"text"`     | Standard double-quoted strings    |
| Backticks     | `` `text` `` | Template literals and code blocks |

### AnyBracket (`o`)

Content enclosed between any type of brackets:

| Bracket Type      | Example   | Description              |
|-------------------|-----------|--------------------------|
| Parentheses       | `(text)`  | Function calls, grouping |
| Square brackets   | `[text]`  | Arrays, indexing         |
| Curly braces      | `{text}`  | Objects, code blocks     |
| Angle brackets    | `<text>`  | Generics                 |
| HTML/XML brackets | `<text/>` | HTML/XML tags, generics  |

