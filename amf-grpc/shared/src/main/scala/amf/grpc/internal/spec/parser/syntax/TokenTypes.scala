package amf.grpc.internal.spec.parser.syntax

object TokenTypes {
  val PROTO = "proto"
  val SYNTAX = "syntax"
  val EXTENDS_STATEMENT = "extendsStatement"
  val EXTEND_IDENTIFIER = "extendedIdentifier"
  val IMPORT_STATEMENT = "importStatement"
  val TOP_LEVEL_DEF = "topLevelDef"
  val PACKAGE_STATEMENT = "packageStatement"
  val FULL_IDENTIFIER = "fullIdent"
  val SERVICE_DEF = "serviceDef"
  val SERVICE_NAME = "serviceName"
  val SERVICE_ELEMENT = "serviceElement"
  val RPC = "rpc"
  val RPC_NAME = "rpcName"
  val MESSAGE_DEF = "messageDef"
  val MESSAGE_BODY = "messageBody"
  val MESSAGE_ELEMENT = "messageElement"
  val MESSAGE_NAME = "messageName"
  val MESSAGE_TYPE = "messageType"
  val FIELD = "field"
  val FIELD_NAME = "fieldName"
  val FIELD_NUMBER = "fieldNumber"
  val FIELD_TYPE = "type_"
  val FIELD_OPTIONS_ELEMENTS = "fieldOptions"
  val FIELD_OPTION = "fieldOption"
  val ENUM_DEF = "enumDef"
  val ENUM_NAME = "enumName"
  val ENUM_BODY = "enumBody"
  val ENUM_ELEMENT = "enumElement"
  val ENUM_FIELD = "enumField"
  val ONE_OF = "oneof"
  val ONE_OF_NAME = "oneofName"
  val ONE_OF_FIELD = "oneofField"
  val MAP_FIELD = "mapField"
  val KEY_TYPE = "keyType"
  val MAP_NAME = "mapName"
  val REPEATED = "REPEATED"
  val IDENTIFIER = "ident"
  val KEYWORDS = "keywords"
  val OPTION_STATEMENT = "optionStatement"
  val OPTION = "option"
  val OPTION_NAME = "optionName"
  val CONSTANT = "constant"
  val BLOCK_LITERAL = "blockLit"
  val FLOAT_LITERAL = " floatLit"
  val STRING_LITERAL = "strLit"
  val BOOL_LITERAL = "boolLit"
  val INT_LITERAL = "intLit"
  val FIELD_OPTIONS = "google.protobuf.FieldOptions"
  val ENUM_OPTIONS = "google.protobuf.EnumOptions"
  val ENUM_VALUE_OPTIONS = "google.protobuf.EnumValueOptions"
  val EXTENSION_RANGE_OPTIONS = "google.protobuf.ExtensionRangeOptions"
  val MESSAGE_OPTIONS = "google.protobuf.MessageOptions"
  val METHOD_OPTIONS = "google.protobuf.MethodOptions"
  val SERVICE_OPTIONS = "google.protobuf.ServiceOptions"
  val FILE_OPTIONS = "google.protobuf.FileOptions"
  val ONEOF_OPTIONS = "google.protobuf.OneofOptions"

}