package com.magidc.ideavim.anyobject.handlers

import com.magidc.ideavim.anyobject.handlers.base.TSBasedHandler


class AnyClassHandler : TSBasedHandler() {
    override val innerBlockTypes = setOf(
        "class_body", "interface_body", "field_declaration_list", "declaration_list", "enum_body", "class", "module",
        "enum_class_body", "block", "interface_type", "enum_variant_list", "protocol_body", "template_body", "object_type",
    )
    override val targetTypes: Set<String> = setOf(
        "class", "class_declaration", "class_definition", "class_implementation", "class_specifier", "enum_declaration", "enum_item", "impl_item",
        "interface_declaration", "mixin_declaration", "module", "object_declaration", "object_definition", "protocol_declaration",
        "record_declaration", "struct_declaration", "struct_item", "struct_specifier", "superclass", "trait_declaration", "trait_definition",
        "trait_item", "type_alias_declaration", "type_declaration"
    )
}