package com.magidc.ideavim.anyobject

import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.MappingMode
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.extension.VimExtensionFacade
import com.magidc.ideavim.anyObject.handlers.AnyBracketHandlers
import com.magidc.ideavim.anyObject.handlers.AnyQuoteHandlers
import com.magidc.ideavim.anyobject.handlers.BaseHandlers


class AnyObject : VimExtension {

    override fun getName(): String = "anyobject"

    override fun init() {
        // Matches any kind of text between quotes
        registerTextObjects("AnyQuote", 'q', AnyQuoteHandlers())
        registerTextObjects("AnyBracket", 'o', AnyBracketHandlers())

        //TODO: "AnyItem", "Anything", "AnyTaggedValue",
    }

    /**
     * Registers the mapping for the text objects defined by the given delimiter pairs.
     */
    private fun registerTextObjects(command: String, mapping: Char, handlers: BaseHandlers) {
        // Inner selection
        VimExtensionFacade.putExtensionHandlerMapping(
            MappingMode.XO,
            injector.parser.parseKeys("<Plug>Inner$command"),
            owner,
            handlers.getInnerHandler(),
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
            handlers.getOuterHandler(),
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






