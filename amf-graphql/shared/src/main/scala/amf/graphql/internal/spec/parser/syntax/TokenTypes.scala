package amf.graphql.internal.spec.parser.syntax

import amf.core.client.scala.model.DataType

object TokenTypes {
  val DOCUMENT                       = "document"
  val DEFINITION                     = "definition"
  val TYPE_SYSTEM_DEFINITION         = "typeSystemDefinition"
  val TYPE_SYSTEM_EXTENSION          = "typeSystemExtension"
  val ROOT_TYPE_DEFINITION           = "rootOperationTypeDefinition"
  val TYPE_EXTENSION                 = "typeExtension"
  val SCALAR_TYPE_EXTENSION          = "scalarTypeExtension"
  val OBJECT_TYPE_EXTENSION          = "objectTypeExtension"
  val INTERFACE_TYPE_EXTENSION       = "interfaceTypeExtension"
  val UNION_TYPE_EXTENSION           = "unionTypeExtension"
  val ENUM_TYPE_EXTENSION            = "enumTypeExtension"
  val INPUT_OBJECT_TYPE_EXTENSION    = "inputObjectTypeExtension"
  val DESCRIPTION                    = "description"
  val SCHEMA_DEFINITION              = "schemaDefinition"
  val SCHEMA_EXTENSION               = "schemaExtension"
  val TYPE_DEFINITION                = "typeDefinition"
  val OBJECT_TYPE_DEFINITION         = "objectTypeDefinition"
  val INPUT_OBJECT_TYPE_DEFINITION   = "inputObjectTypeDefinition"
  val INPUT_FIELDS_DEFINITION        = "inputFieldsDefinition"
  val INPUT_VALUE_DEFINITION         = "inputValueDefinition"
  val INTERFACE_TYPE_DEFINITION      = "interfaceTypeDefinition"
  val EXTEND                         = "'extend'"
  val TYPE                           = "type"
  val NAME                           = "name"
  val KEYWORD                        = "keyword"
  val NAME_TERMINAL                  = "NAME"
  val ROOT_OPERATION_TYPE_DEFINITION = "rootOperationTypeDefinition"
  val OPERATION_TYPE_DEFINITION      = "operationTypeDefinition"
  val OPERATION_TYPE                 = "operationType"
  val NAMED_TYPE                     = "namedType"
  val IMPLEMENTS_INTERFACES          = "implementsInterfaces"
  val FIELDS_DEFINITION              = "fieldsDefinition"
  val FIELD_DEFINITION               = "fieldDefinition"
  val ARGUMENTS_DEFINITION           = "argumentsDefinition"
  val ARGUMENTS                      = "arguments"
  val ARGUMENT                       = "argument"
  val UNION_TYPE_DEFINITION          = "unionTypeDefinition"
  val UNION_MEMBER_TYPES             = "unionMemberTypes"
  val ENUM_TYPE_DEFINITION           = "enumTypeDefinition"
  val ENUM_VALUES_DEFINITION         = "enumValuesDefinition"
  val ENUM_VALUE_DEFINITION          = "enumValueDefinition"
  val TYPE_                          = "type_"
  val LIST_TYPE                      = "listType"
  val SCALAR_TYPE_DEFINITION         = "scalarTypeDefinition"
  val DIRECTIVE_DEFINITION           = "directiveDefinition"
  val DIRECTIVE_LOCATIONS            = "directiveLocations"
  val DIRECTIVE_LOCATION             = "directiveLocation"
  val TYPE_SYSTEM_DIRECTIVE_LOCATION = "typeSystemDirectiveLocation"
  val DIRECTIVES                     = "directives"
  val DIRECTIVE                      = "directive"
  val DEFAULT_VALUE                  = "defaultValue"
  val INT_VALUE                      = "intValue"
  val FLOAT_VALUE                    = "floatValue"
  val STRING_VALUE                   = "stringValue"
  val BOOLEAN_VALUE                  = "booleanValue"
  val NULL_VALUE                     = "nullValue"
  val ENUM_VALUE                     = "enumValue"
  val LIST_VALUE                     = "listValue"
  val OBJECT_VALUE                   = "objectValue"
  val VALUE                          = "value"
  val INT                            = "Int"
  val FLOAT                          = "Float"
  val STRING                         = "String"
  val BOOLEAN                        = "Boolean"
  val ENUM                           = "Enum"
  val ID                             = "ID"
  val INT_TERMINAL                   = "INT"
  val FLOAT_TERMINAL                 = "FLOAT"
  val STRING_TERMINAL                = "STRING"

  // Federation
  val FIELD_SET           = "fieldSet"
  val FIELD_SET_COMPONENT = "fieldSetComponent"
  val NAME_F              = "name_f"
  val NAME_TERMINAL_F     = "NAME_F"
  val KEYWORD_F           = "keyword_f"
  val NESTED_FIELD_SET    = "nestedFieldSet"
  val RESOLVABLE_KEYWORD  = "RESOLVABLE_KEYWORD"
  val BOOLEAN_VALUE_F     = "booleanValue_f"
  val FALSE_F             = "FALSE_F"
  val TRUE_F              = "TRUE_F"

  // Directives & federation directives
  val ENUM_DIRECTIVE                    = "enumDirective"
  val ENUM_FEDERATION_DIRECTIVE         = "enumFederationDirective"
  val ENUM_VALUE_DIRECTIVE              = "enumValueDirective"
  val ENUM_VALUE_FEDERATION_DIRECTIVE   = "enumValueFederationDirective"
  val EXTERNAL_DIRECTIVE                = "externalDirective"
  val FIELD_DIRECTIVE                   = "fieldDirective"
  val FIELD_FEDERATION_DIRECTIVE        = "fieldFederationDirective"
  val INACCESSIBLE_DIRECTIVE            = "inaccessibleDirective"
  val INPUT_FIELD_FEDERATION_DIRECTIVE  = "inputFieldFederationDirective"
  val INPUT_OBJECT_DIRECTIVE            = "inputObjectDirective"
  val INPUT_OBJECT_FEDERATION_DIRECTIVE = "inputObjectFederationDirective"
  val INPUT_VALUE_DIRECTIVE             = "inputValueDirective"
  val INTERFACE_DIRECTIVE               = "interfaceDirective"
  val INTERFACE_FEDERATION_DIRECTIVE    = "interfaceFederationDirective"
  val KEY_DIRECTIVE                     = "keyDirective"
  val OBJECT_DIRECTIVE                  = "objectDirective"
  val OBJECT_FEDERATION_DIRECTIVE       = "objectFederationDirective"
  val OVERRIDE_DIRECTIVE                = "overrideDirective"
  val PROVIDES_DIRECTIVE                = "providesDirective"
  val REQUIRES_DIRECTIVE                = "requiresDirective"
  val SCALAR_DIRECTIVE                  = "scalarDirective"
  val SCALAR_FEDERATION_DIRECTIVE       = "scalarFederationDirective"
  val SHAREABLE_DIRECTIVE               = "shareableDirective"
  val UNION_DIRECTIVE                   = "unionDirective"
  val UNION_FEDERATION_DIRECTIVE        = "unionFederationDirective"

  val SCALAR_TYPES: Seq[String] = Seq(INT, FLOAT, STRING, BOOLEAN, ID)

  val toDataType: Map[String, String] = Map(
    INT     -> DataType.Integer,
    FLOAT   -> DataType.Float,
    STRING  -> DataType.String,
    BOOLEAN -> DataType.Boolean,
    ENUM    -> DataType.String,
    ID      -> DataType.String
  )

  val toTerminal: Map[String, String] = Map(
    INT          -> INT_TERMINAL,
    FLOAT        -> FLOAT_TERMINAL,
    STRING       -> STRING_TERMINAL,
    INT_VALUE    -> INT_TERMINAL,
    FLOAT_VALUE  -> FLOAT_TERMINAL,
    STRING_VALUE -> STRING_TERMINAL
  )
}
