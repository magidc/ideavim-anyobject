package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler

class AnyFieldHandler : AbstractTSBasedHandler() {
    override val targetTypes = setOf(
         "property_declaration", "field_declaration", "field_definition", "local_declaration_statement", "local_variable_declaration", "declaration"
    )
}