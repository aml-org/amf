package amf.graphql.internal.spec.parser.syntax

import amf.core.client.scala.model.DataType

object TokenTypes {
  val DOCUMENT                       = "document"
  val DEFINITION                     = "definition"
  val TYPE_SYSTEM_DEFINITION         = "typeSystemDefinition"
  val ROOT_TYPE_DEFINITION           = "rootOperationTypeDefinition"
  val DESCRIPTION                    = "description"
  val SCHEMA_DEFINITION              = "schemaDefinition"
  val TYPE_DEFINITION                = "typeDefinition"
  val OBJECT_TYPE_DEFINITION         = "objectTypeDefinition"
  val INPUT_OBJECT_TYPE_DEFINITION   = "inputObjectTypeDefinition"
  val INPUT_FIELDS_DEFINITION        = "inputFieldsDefinition"
  val INPUT_VALUE_DEFINITION         = "inputValueDefinition"
  val INTERFACE_TYPE_DEFINITION      = "interfaceTypeDefinition"
  val TYPE                           = "type"
  val NAME                           = "name"
  val KEYWORD                        = "keyword"
  val NAME_TERMINAL                  = "NAME"
  val ROOT_OPERATION_TYPE_DEFINITION = "rootOperationTypeDefinition"
  val OPERATION_TYPE                 = "operationType"
  val NAMED_TYPE                     = "namedType"
  val IMPLEMENTS_INTERFACES          = "implementsInterfaces"
  val FIELDS_DEFINITION              = "fieldsDefinition"
  val FIELD_DEFINITION               = "fieldDefinition"
  val ARGUMENTS_DEFINITION           = "argumentsDefinition"
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
