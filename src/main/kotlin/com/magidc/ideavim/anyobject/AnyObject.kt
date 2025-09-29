package com.magidc.ideavim.anyobject

import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.extension.ExtensionHandler
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.magidc.ideavim.anyobject.handlers.AnyArgumentHandler
import com.magidc.ideavim.anyobject.handlers.AnyBlockCommentHandler
import com.magidc.ideavim.anyobject.handlers.AnyBracketHandler
import com.magidc.ideavim.anyobject.handlers.AnyClassHandler
import com.magidc.ideavim.anyobject.handlers.AnyConditionalHandler
import com.magidc.ideavim.anyobject.handlers.AnyDocumentHandler
import com.magidc.ideavim.anyobject.handlers.AnyFunctionHandler
import com.magidc.ideavim.anyobject.handlers.AnyIndentBlockHandler
import com.magidc.ideavim.anyobject.handlers.AnyItemHandler
import com.magidc.ideavim.anyobject.handlers.AnyLoopHandler
import com.magidc.ideavim.anyobject.handlers.AnyQuoteHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseJumpHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseSelectionHandler


class AnyObject : VimExtension {

    override fun getName(): String = "anyobject"

    override fun init() {
        registerTextObjects("AnyQuote", 'q', AnyQuoteHandler())
        registerTextObjects("AnyBracket", 'o', AnyBracketHandler())
        registerTextObjects("AnyItem", 'i', AnyItemHandler())
        registerTextObjects("AnyBlockComment", 'k', AnyBlockCommentHandler())
        registerTextObjects("AnyArgument", 'a', AnyArgumentHandler())
        registerTextObjects("AnyFunction", 'f', AnyFunctionHandler())
        registerTextObjects("AnyClass", 'c', AnyClassHandler())
        registerTextObjects("AnyDocument", 'd', AnyDocumentHandler())
        registerTextObjects("AnyLoop", 'l', AnyLoopHandler())
        registerTextObjects("AnyIndentBlock", 'n', AnyIndentBlockHandler())
        registerTextObjects("AnyConditional", 't', AnyConditionalHandler())
//        registerTextObjects("AnyField", 'p', AnyFieldHandler())
    }

    /**
     * Registers the mapping for the text objects defined by the given delimiter pairs.
     */
    private fun registerTextObjects(command: String, mapping: Char, handler: BaseHandler) {
        if (handler is BaseSelectionHandler) {
            // Inner selection
            VimExtensionFacade.putExtensionHandlerMapping(
                MappingMode.XO,
                injector.parser.parseKeys("<Plug>Inner$command"),
                owner,
                object : ExtensionHandler {
                    override fun execute(editor: VimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
                        handler.executeSelection(editor, true)
                    }
                },
                false
            )

            VimExtensionFacade.putKeyMappingIfMissing(
                MappingMode.XO,
                injector.parser.parseKeys("i$mapping"),
                owner,
                injector.parser.parseKeys("<Plug>Inner$command"),
                true
            )
            // Outer selection
            VimExtensionFacade.putExtensionHandlerMapping(
                MappingMode.XO,
                injector.parser.parseKeys("<Plug>Outer$command"),
                owner,
                object : ExtensionHandler {
                    override fun execute(editor: VimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
                        handler.executeSelection(editor, false)
                    }
                },
                false
            )

            VimExtensionFacade.putKeyMappingIfMissing(
                MappingMode.XO,
                injector.parser.parseKeys("a$mapping"),
                owner,
                injector.parser.parseKeys("<Plug>Outer$command"),
                true
            )

            if (handler is BaseJumpHandler) {
                // Multiple selection commands
                for (i in 1..10) {
                    // Outer selection
                    VimExtensionFacade.putExtensionHandlerMapping(
                        MappingMode.XO,
                        injector.parser.parseKeys("<Plug>" + i + "Outer$command"),
                        owner,
                        object : ExtensionHandler {
                            override fun execute(editor: VimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
                                handler.executeSelection(editor, false, i)
                            }
                        },
                        false
                    )

                    VimExtensionFacade.putKeyMappingIfMissing(
                        MappingMode.XO,
                        injector.parser.parseKeys("$i$mapping"),
                        owner,
                        injector.parser.parseKeys("<Plug>" + i + "Outer$command"),
                        true
                    )
                }
                // Jumping commands
                // Jump to next
                VimExtensionFacade.putExtensionHandlerMapping(
                    MappingMode.N,
                    injector.parser.parseKeys("<Plug>Next$command"),
                    owner,
                    object : ExtensionHandler {
                        override fun execute(editor: VimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
                            handler.executeJump(editor, true)
                        }
                    },
                    false
                )

                VimExtensionFacade.putKeyMappingIfMissing(
                    MappingMode.N,
                    injector.parser.parseKeys("]$mapping"),
                    owner,
                    injector.parser.parseKeys("<Plug>Next$command"),
                    true
                )

                // Jump to previous
                VimExtensionFacade.putExtensionHandlerMapping(
                    MappingMode.N,
                    injector.parser.parseKeys("<Plug>Prev$command"),
                    owner,
                    object : ExtensionHandler {
                        override fun execute(editor: VimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
                            handler.executeJump(editor, false)
                        }
                    },
                    false
                )

                VimExtensionFacade.putKeyMappingIfMissing(
                    MappingMode.N,
                    injector.parser.parseKeys("[$mapping"),
                    owner,
                    injector.parser.parseKeys("<Plug>Prev$command"),
                    true
                )
            }
        }
    }
}






