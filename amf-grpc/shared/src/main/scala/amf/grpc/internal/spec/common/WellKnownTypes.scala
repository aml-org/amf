package amf.grpc.internal.spec.common

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.domain.Annotations._
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.NodeShapeModel

/** Utility object for managing Protocol Buffer well-known types. Provides mappings between import URLs and type names
  * for all Google well-known types.
  */
object WellKnownTypes {

  /** Gets the type names provided by a specific import URL using pattern matching */
  def getTypeNamesForImport(importUrl: String): List[String] = importUrl match {
    case "google/protobuf/descriptor.proto" => List("google.protobuf.Descriptor")
    case "google/protobuf/any.proto" => List("google.protobuf.Any")
    case "google/protobuf/api.proto" => List("google.protobuf.Api", "google.protobuf.Method", "google.protobuf.Mixin")
    case "google/protobuf/duration.proto"       => List("google.protobuf.Duration")
    case "google/protobuf/empty.proto"          => List("google.protobuf.Empty")
    case "google/protobuf/field_mask.proto"     => List("google.protobuf.FieldMask")
    case "google/protobuf/source_context.proto" => List("google.protobuf.SourceContext")
    case "google/protobuf/struct.proto" =>
      List("google.protobuf.Struct", "google.protobuf.Value", "google.protobuf.ListValue", "google.protobuf.NullValue")
    case "google/protobuf/timestamp.proto" => List("google.protobuf.Timestamp")
    case "google/protobuf/type.proto" =>
      List(
        "google.protobuf.Type",
        "google.protobuf.Field",
        "google.protobuf.Enum",
        "google.protobuf.EnumValue",
        "google.protobuf.Option",
        "google.protobuf.Syntax"
      )
    case "google/protobuf/wrappers.proto" =>
      List(
        "google.protobuf.DoubleValue",
        "google.protobuf.FloatValue",
        "google.protobuf.Int64Value",
        "google.protobuf.UInt64Value",
        "google.protobuf.Int32Value",
        "google.protobuf.UInt32Value",
        "google.protobuf.BoolValue",
        "google.protobuf.StringValue",
        "google.protobuf.BytesValue"
      )
    case _ => List.empty
  }

  /** Gets the import URL for a given type name using pattern matching */
  def getImportUrlForKey(key: String): Option[String] = {
    val normalizedKey = if (key.startsWith(".")) key.substring(1) else key
    normalizedKey match {
      case "google.protobuf.Any" => Some("google/protobuf/any.proto")

      case "google.protobuf.Descriptor" => Some("google/protobuf/descriptor.proto")

      // API types
      case "google.protobuf.Api" | "google.protobuf.Method" | "google.protobuf.Mixin" =>
        Some("google/protobuf/api.proto")

      // Time types
      case "google.protobuf.Duration"  => Some("google/protobuf/duration.proto")
      case "google.protobuf.Timestamp" => Some("google/protobuf/timestamp.proto")

      // Empty and utility types
      case "google.protobuf.Empty"         => Some("google/protobuf/empty.proto")
      case "google.protobuf.FieldMask"     => Some("google/protobuf/field_mask.proto")
      case "google.protobuf.SourceContext" => Some("google/protobuf/source_context.proto")

      // Struct types
      case "google.protobuf.Struct" | "google.protobuf.Value" | "google.protobuf.ListValue" |
           "google.protobuf.NullValue" =>
        Some("google/protobuf/struct.proto")

      // Type reflection types
      case "google.protobuf.Type" | "google.protobuf.Field" | "google.protobuf.Enum" | "google.protobuf.EnumValue" |
           "google.protobuf.Option" | "google.protobuf.Syntax" =>
        Some("google/protobuf/type.proto")

      // Wrapper types
      case "google.protobuf.DoubleValue" | "google.protobuf.FloatValue" | "google.protobuf.Int64Value" |
           "google.protobuf.UInt64Value" | "google.protobuf.Int32Value" | "google.protobuf.UInt32Value" |
           "google.protobuf.BoolValue" | "google.protobuf.StringValue" | "google.protobuf.BytesValue" =>
        Some("google/protobuf/wrappers.proto")

      case _ => None
    }
  }

  /** Creates a Shape instance for a well-known type using pattern matching */
  private def createShapeByType(typeName: String, ann: Annotations = synthesized()): AnyShape = {
    typeName match {
      case "google.protobuf.Any" | "google.protobuf.Value" =>
        AnyShape(ann).withName(typeName)

      // Scalar types for time-related well-known types (represented as strings)
      case "google.protobuf.Duration" | "google.protobuf.Timestamp" | "google.protobuf.FieldMask" |
          "google.protobuf.Syntax" =>
        scalarShape(ann, typeName, DataType.String)

      // Null value enum (represented as nil)
      case "google.protobuf.NullValue" => scalarShape(ann, typeName, DataType.Nil)

      // Wrapper types - represent nullable primitives (kept as scalars for their wrapped values)
      case "google.protobuf.DoubleValue" => scalarShape(ann, typeName, DataType.Double)
      case "google.protobuf.FloatValue"  => scalarShape(ann, typeName, DataType.Float)
      case "google.protobuf.Int64Value" | "google.protobuf.UInt64Value" =>
        scalarShape(ann, typeName, DataType.Long)
      case "google.protobuf.Int32Value" | "google.protobuf.UInt32Value" =>
        scalarShape(ann, typeName, DataType.Integer)
      case "google.protobuf.BoolValue"   => scalarShape(ann, typeName, DataType.Boolean)
      case "google.protobuf.StringValue" => scalarShape(ann, typeName, DataType.String)
      case "google.protobuf.BytesValue"  => scalarShape(ann, typeName, DataType.Byte)

      // NodeShapes with properties modeled according to their protobuf definitions
      case "google.protobuf.Empty"         => nodeShape(ann, typeName)
      case "google.protobuf.Struct"        => struct(nodeShape(ann, typeName), ann)
      case "google.protobuf.ListValue"     => listValue(nodeShape(ann, typeName), ann)
      case "google.protobuf.Api"           => api(nodeShape(ann, typeName), ann)
      case "google.protobuf.Method"        => method(nodeShape(ann, typeName), ann)
      case "google.protobuf.Mixin"         => mixin(nodeShape(ann, typeName), ann)
      case "google.protobuf.Type"          => `type`(nodeShape(ann, typeName), ann)
      case "google.protobuf.Field"         => field(nodeShape(ann, typeName), ann)
      case "google.protobuf.Enum"          => `enum`(nodeShape(ann, typeName), ann)
      case "google.protobuf.EnumValue"     => enumValue(nodeShape(ann, typeName), ann)
      case "google.protobuf.Option"        => option(nodeShape(ann, typeName), ann)
      case "google.protobuf.SourceContext" => sourceContext(nodeShape(ann, typeName), ann)

      // Default case for unknown types
      case _ => AnyShape(ann).withName(typeName)
    }
  }

  /** Creates Shape instances for all types in a given import URL */
  def createShapesForImport(importUrl: String, ann: Annotations): List[AnyShape] = {
    getTypeNamesForImport(importUrl).map(typeName => createShapeByType(typeName, ann))
  }

  /** Checks if an import URL is a well-known type */
  def isWellKnownImport(importUrl: String): Boolean = getTypeNamesForImport(importUrl).nonEmpty

  /** Helper method to add an array property to a NodeShape with a specific item type */
  private def addArrayPropertyToNode(nodeShape: NodeShape, propertyName: String, itemShape: AnyShape): NodeShape = {
    val arrayRange = ArrayShape(virtual())
    arrayRange.withItems(itemShape)
    val property = PropertyShape(nodeShape.annotations).withName(propertyName).withRange(arrayRange)
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, nodeShape.properties :+ property, nodeShape.annotations)
    nodeShape
  }

  /** Helper method to add an options property (array of google.protobuf.Option) to a NodeShape */
  private def addOptionsProperty(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    addArrayPropertyToNode(nodeShape, "options", createShapeByType("google.protobuf.Option", ann))
  }

  /** Helper method to add source_context property to a NodeShape */
  private def addSourceContextProperty(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val sourceContext =
      PropertyShape(ann).withName("source_context").withRange(createShapeByType("google.protobuf.SourceContext", ann))
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, nodeShape.properties :+ sourceContext, ann)
    nodeShape
  }

  /** Helper method to add syntax property to a NodeShape */
  private def addSyntaxProperty(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val syntax = PropertyShape(ann).withName("syntax").withRange(scalarShape(ann, DataType.String)) // Syntax enum
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, nodeShape.properties :+ syntax, ann)
    nodeShape
  }

  /** Helper method to add common properties (options, source_context, syntax) to a NodeShape */
  private def addCommonTypeProperties(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    addOptionsProperty(nodeShape, ann)
    addSourceContextProperty(nodeShape, ann)
    addSyntaxProperty(nodeShape, ann)
    nodeShape
  }

  private def nodeShape(ann: Annotations, typeName: String): NodeShape = NodeShape(ann).withName(typeName)

  private def scalarShape(ann: Annotations, typeName: String, dataType: String): ScalarShape = {
    ScalarShape(ann).withName(typeName).withDataType(dataType)
  }

  private def scalarShape(ann: Annotations, dataType: String): ScalarShape = {
    ScalarShape(ann).withDataType(dataType)
  }

  private def struct(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    // fields: map<string, Value>
    val mapRange = NodeShape(ann).withName("StructFieldsMap")
    // In protobuf, maps are represented as repeated entries with key/value pairs
    val key   = PropertyShape(ann).withName("key").withRange(scalarShape(synthesized(), DataType.String))
    val value = PropertyShape(ann).withName("value").withRange(createShapeByType("google.protobuf.Value", ann))
    mapRange.setArrayWithoutId(NodeShapeModel.Properties, Seq(key, value), ann)

    val arrayRange = ArrayShape(virtual())
    arrayRange.withItems(mapRange)
    val fieldsProperty = PropertyShape(ann).withName("fields").withRange(arrayRange)
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, Seq(fieldsProperty), ann)
    nodeShape
  }

  private def listValue(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    // values: repeated Value
    addArrayPropertyToNode(nodeShape, "values", createShapeByType("google.protobuf.Value", ann))
    nodeShape
  }

  private def api(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val name    = PropertyShape(ann).withName("name").withRange(scalarShape(ann, DataType.String))
    val version = PropertyShape(ann).withName("version").withRange(scalarShape(ann, DataType.String))
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, Seq(name, version), ann)

    addArrayPropertyToNode(nodeShape, "methods", createShapeByType("google.protobuf.Method", ann))
    addOptionsProperty(nodeShape, ann)
    addSourceContextProperty(nodeShape, ann)
    addArrayPropertyToNode(nodeShape, "mixins", createShapeByType("google.protobuf.Mixin", ann))
    addSyntaxProperty(nodeShape, ann)
    nodeShape
  }

  private def method(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val name           = PropertyShape(ann).withName("name").withRange(scalarShape(ann, DataType.String))
    val requestTypeUrl = PropertyShape(ann).withName("request_type_url").withRange(scalarShape(ann, DataType.String))
    val requestStreaming =
      PropertyShape(ann).withName("request_streaming").withRange(ScalarShape(ann).withDataType(DataType.Boolean))
    val responseTypeUrl = PropertyShape(ann).withName("response_type_url").withRange(scalarShape(ann, DataType.String))
    val responseStreaming =
      PropertyShape(ann).withName("response_streaming").withRange(ScalarShape(ann).withDataType(DataType.Boolean))
    nodeShape.setArrayWithoutId(
      NodeShapeModel.Properties,
      Seq(name, requestTypeUrl, requestStreaming, responseTypeUrl, responseStreaming),
      ann
    )

    addOptionsProperty(nodeShape, ann)
    addSyntaxProperty(nodeShape, ann)
    nodeShape
  }

  private def mixin(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val name = PropertyShape(ann).withName("name").withRange(scalarShape(ann, DataType.String))
    val root = PropertyShape(ann).withName("root").withRange(scalarShape(ann, DataType.String))
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, Seq(name, root), ann)
    nodeShape
  }

  private def `type`(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val name = PropertyShape(ann).withName("name").withRange(scalarShape(ann, DataType.String))
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, Seq(name), ann)

    addArrayPropertyToNode(nodeShape, "fields", createShapeByType("google.protobuf.Field", ann))
    addArrayPropertyToNode(nodeShape, "oneofs", scalarShape(ann, DataType.String))
    addCommonTypeProperties(nodeShape, ann)
    nodeShape
  }

  private def field(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val kind        = PropertyShape(ann).withName("kind").withRange(scalarShape(ann, DataType.String))
    val cardinality = PropertyShape(ann).withName("cardinality").withRange(scalarShape(ann, DataType.String))
    val number      = PropertyShape(ann).withName("number").withRange(ScalarShape(ann).withDataType(DataType.Integer))
    val name        = PropertyShape(ann).withName("name").withRange(scalarShape(ann, DataType.String))
    val typeUrl     = PropertyShape(ann).withName("type_url").withRange(scalarShape(ann, DataType.String))
    val oneofIndex =
      PropertyShape(ann).withName("oneof_index").withRange(ScalarShape(ann).withDataType(DataType.Integer))
    val packed       = PropertyShape(ann).withName("packed").withRange(ScalarShape(ann).withDataType(DataType.Boolean))
    val jsonName     = PropertyShape(ann).withName("json_name").withRange(scalarShape(ann, DataType.String))
    val defaultValue = PropertyShape(ann).withName("default_value").withRange(scalarShape(ann, DataType.String))
    nodeShape.setArrayWithoutId(
      NodeShapeModel.Properties,
      Seq(kind, cardinality, number, name, typeUrl, oneofIndex, packed, jsonName, defaultValue),
      ann
    )

    addOptionsProperty(nodeShape, ann)
    nodeShape
  }

  private def `enum`(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val name = PropertyShape(ann).withName("name").withRange(scalarShape(ann, DataType.String))
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, Seq(name), ann)

    addArrayPropertyToNode(nodeShape, "enumvalue", createShapeByType("google.protobuf.EnumValue", ann))
    addCommonTypeProperties(nodeShape, ann)
    nodeShape
  }

  private def enumValue(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val name   = PropertyShape(ann).withName("name").withRange(scalarShape(ann, DataType.String))
    val number = PropertyShape(ann).withName("number").withRange(ScalarShape(ann).withDataType(DataType.Integer))
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, Seq(name, number), ann)

    addOptionsProperty(nodeShape, ann)
    nodeShape
  }

  private def option(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val name  = PropertyShape(ann).withName("name").withRange(scalarShape(ann, DataType.String))
    val value = PropertyShape(ann).withName("value").withRange(createShapeByType("google.protobuf.Any", ann))
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, Seq(name, value), ann)
    nodeShape
  }

  private def sourceContext(nodeShape: NodeShape, ann: Annotations): NodeShape = {
    val fileName = PropertyShape(ann).withName("file_name").withRange(scalarShape(ann, DataType.String))
    nodeShape.setArrayWithoutId(NodeShapeModel.Properties, Seq(fileName), ann)
    nodeShape
  }
}
