package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.AbstractTSBasedHandler


class AnyClassHandler : AbstractTSBasedHandler() {
    override val innerBlockTypes = setOf("class_body", "interface_body", "field_declaration_list", "declaration_list", "body_statement")
    override val targetTypes: Set<String> = setOf(
        "class", "class_declaration", "class_definition", "class_specifier", "interface_declaration", "record_declaration",
        "class_specifier", "superclass", "object_declaration", "struct_declaration", "trait_declaration", "mixin_declaration", "enum_declaration",
    )
}