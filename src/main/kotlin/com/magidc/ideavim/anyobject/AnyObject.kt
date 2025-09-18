package com.magidc.ideavim.anyobject

import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.magidc.ideavim.anyobject.handlers.AnyArgumentHandlers
import com.magidc.ideavim.anyobject.handlers.AnyBlockCommentHandlers
import com.magidc.ideavim.anyobject.handlers.AnyBracketHandlers
import com.magidc.ideavim.anyobject.handlers.AnyClassHandlers
import com.magidc.ideavim.anyobject.handlers.AnyConditionalHandlers
import com.magidc.ideavim.anyobject.handlers.AnyDocumentHandlers
import com.magidc.ideavim.anyobject.handlers.AnyFunctionHandlers
import com.magidc.ideavim.anyobject.handlers.AnyIndentBlockHandlers
import com.magidc.ideavim.anyobject.handlers.AnyItemHandlers
import com.magidc.ideavim.anyobject.handlers.AnyLoopHandlers
import com.magidc.ideavim.anyobject.handlers.AnyQuoteHandlers
import com.magidc.ideavim.anyobject.handlers.base.HandlerFactory


class AnyObject : VimExtension {

    override fun getName(): String = "anyobject"

    override fun init() {
        registerTextObjects("AnyQuote", 'q', AnyQuoteHandlers())
        registerTextObjects("AnyBracket", 'o', AnyBracketHandlers())
        registerTextObjects("AnyItem", 'i', AnyItemHandlers())
        registerTextObjects("AnyBlockComment", 'k', AnyBlockCommentHandlers())
        registerTextObjects("AnyArgument", 'a', AnyArgumentHandlers())
        registerTextObjects("AnyFunction", 'f', AnyFunctionHandlers())
        registerTextObjects("AnyClass", 'c', AnyClassHandlers())
        registerTextObjects("AnyDocument", 'd', AnyDocumentHandlers())
        registerTextObjects("AnyLoop", 'l', AnyLoopHandlers())
        registerTextObjects("AnyIndentBlock", 'n', AnyIndentBlockHandlers())
        registerTextObjects("AnyConditional", 't', AnyConditionalHandlers())
//        registerTextObjects("AnyField", 'p', AnyFieldHandlers())
    }

    /**
     * Registers the mapping for the text objects defined by the given delimiter pairs.
     */
    private fun registerTextObjects(command: String, mapping: Char, handlerFactory: HandlerFactory) {
        // Inner selection
        VimExtensionFacade.putExtensionHandlerMapping(
            MappingMode.XO,
            injector.parser.parseKeys("<Plug>Inner$command"),
            owner,
            handlerFactory.getInnerHandler(),
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
            handlerFactory.getOuterHandler(),
            false
        )

        VimExtensionFacade.putKeyMappingIfMissing(
            MappingMode.XO,
            injector.parser.parseKeys("a$mapping"),
            owner,
            injector.parser.parseKeys("<Plug>Outer$command"),
            true
        )
    }
}






