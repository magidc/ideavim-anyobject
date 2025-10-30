package com.magidc.ideavim.anyobject

import com.intellij.openapi.diagnostic.Logger
import com.maddyhome.idea.vim.KeyHandler
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.ImmutableVimCaret
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.Argument
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.command.MotionType
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.command.TextObjectVisualType
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.extension.ExtensionHandler
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.maddyhome.idea.vim.handler.Motion
import com.maddyhome.idea.vim.handler.MotionActionHandler
import com.maddyhome.idea.vim.handler.TextObjectActionHandler
import com.maddyhome.idea.vim.handler.toMotion
import com.maddyhome.idea.vim.state.mode.SelectionType
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
import com.magidc.ideavim.anyobject.handlers.AnySubwordHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseJumpHandler
import com.magidc.ideavim.anyobject.handlers.base.BaseSelectionHandler


val handlerSupplierMap = mapOf(
    "anyquote" to Pair("q", ::AnyQuoteHandler),
    "anybracket" to Pair("o", ::AnyBracketHandler),
    "anyitem" to Pair("i", ::AnyItemHandler),
    "anyblockcomment" to Pair("k", ::AnyBlockCommentHandler),
    "anyargument" to Pair("a", ::AnyArgumentHandler),
    "anyfunction" to Pair("f", ::AnyFunctionHandler),
    "anyclass" to Pair("c", ::AnyClassHandler),
    "anydocument" to Pair("d", ::AnyDocumentHandler),
    "anyloop" to Pair("l", ::AnyLoopHandler),
    "anyindentblock" to Pair("n", ::AnyIndentBlockHandler),
    "anyconditional" to Pair("y", ::AnyConditionalHandler),
    "anysubword" to Pair("u", ::AnySubwordHandler),
)

val builtInVimTextObjectsMappings = setOf("w", "p", "t", "b", "s")

class AnyObject : VimExtension {
    companion object {
        private val LOG = Logger.getInstance(AnyObject::class.java)
    }

    fun getGlobalVariableSet(variableName: String): Set<String>? {
        return VimPlugin.getVariableService().getGlobalVariableValue(variableName)?.asString()
            ?.split(",")
            ?.map { it.trim() }
            ?.map { it.lowercase() }
            ?.toSet()
    }

    override fun getName(): String = "anyobject"

    override fun init() {
        val includedHandlers = getGlobalVariableSet("anyobject_included") ?: handlerSupplierMap.keys
        val excludedHandlers = getGlobalVariableSet("anyobject_excluded") ?: emptySet()

        val customMappingMap = VimPlugin.getVariableService().getGlobalVariables().entries
            .filter { it.key.lowercase().startsWith("anyobject_map_") }
            .associate { it.key.substringAfter("anyobject_map_").lowercase() to it.value.asString() }

        val jumpNextMapping = customMappingMap.getOrDefault("jump_next", "]")
        val jumpPrevMapping = customMappingMap.getOrDefault("jump_prev", "[")

        val usedMappingsSet = mutableSetOf<String>()
        val usedHandlersSet = mutableSetOf(*excludedHandlers.toTypedArray())

        for (handlerName in includedHandlers) {
            if (!usedHandlersSet.add(handlerName)) continue
            val (defaultMapping, handlerSupplier) = handlerSupplierMap[handlerName] ?: continue
            val mapping = customMappingMap.getOrDefault(handlerName, defaultMapping)
            if (mapping in builtInVimTextObjectsMappings) {
                LOG.warn("Mapping $mapping for $handlerName conflicts with built-in mapping. Skipping")
                continue
            }
            if (!usedMappingsSet.add(mapping)) {
                LOG.warn("Mapping $mapping for $handlerName is already used. Skipping")
                continue
            }
            registerTextObjects(handlerName, mapping, handlerSupplier(), jumpNextMapping, jumpPrevMapping)
        }
//        registerTextObjects("AnyField", 'p', AnyFieldHandler())
    }

    /**
     * Registers the mapping for the text objects defined by the given delimiter pairs.
     */
    private fun registerTextObjects(command: String, mapping: String, handler: BaseSelectionHandler, jumpNextMapping: String, jumpPrevMapping: String) {
        // Inner selection
        VimExtensionFacade.putExtensionHandlerMapping(
            MappingMode.XO, injector.parser.parseKeys("<Plug>Inner$command"),
            owner,
            createSelection(handler, true),
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
            createSelection(handler, false),
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
                    createSelection(handler, false, i),
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
                createMotionAction(handler, true),
                false
            )

            VimExtensionFacade.putKeyMappingIfMissing(
                MappingMode.N,
                injector.parser.parseKeys("$jumpNextMapping$mapping"),
                owner,
                injector.parser.parseKeys("<Plug>Next$command"),
                true
            )

            // Jump to previous
            VimExtensionFacade.putExtensionHandlerMapping(
                MappingMode.N,
                injector.parser.parseKeys("<Plug>Prev$command"),
                owner,
                createMotionAction(handler, false),
                false
            )

            VimExtensionFacade.putKeyMappingIfMissing(
                MappingMode.N,
                injector.parser.parseKeys("$jumpPrevMapping$mapping"),
                owner,
                injector.parser.parseKeys("<Plug>Prev$command"),
                true
            )
        }
    }

    private fun createSelection(handler: BaseSelectionHandler, isInner: Boolean, size: Int = 1): ExtensionHandler = object : ExtensionHandler {
        override fun execute(editor: VimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
            val textObjectHandler = object : TextObjectActionHandler() {
                override val visualType: TextObjectVisualType = TextObjectVisualType.CHARACTER_WISE
                override fun getRange(editor: VimEditor, caret: ImmutableVimCaret, context: ExecutionContext, count: Int, rawCount: Int): TextRange? {
                    val range = handler.findSelection(editor, isInner, size) ?: return null
                    // Avoiding change caret position in yank actions
                    val isYankOperation = KeyHandler.getInstance().keyHandlerState.digraphSequence.toString().endsWith("char = y")
                    if (isYankOperation) {
                        injector.yank.yankRange(editor, context, range, SelectionType.CHARACTER_WISE, false)
                        return null
                    }
                    return range
                }
            }
            KeyHandler.getInstance().keyHandlerState.commandBuilder.addAction(textObjectHandler)
        }
    }

    private fun createMotionAction(handler: BaseJumpHandler, next: Boolean): ExtensionHandler = object : ExtensionHandler {
        override fun execute(editor: VimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
            val action = object : MotionActionHandler.SingleExecution() {
                override val motionType: MotionType = MotionType.EXCLUSIVE
                override fun getOffset(editor: VimEditor, context: ExecutionContext, argument: Argument?, operatorArguments: OperatorArguments): Motion {
                    return handler.findJumpElementStartOffset(editor, next)?.toMotion() ?: Motion.Error
                }
            }
            KeyHandler.getInstance().keyHandlerState.commandBuilder.addAction(action)
        }
    }
}