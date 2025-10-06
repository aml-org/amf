package amf.grpc.internal.spec.emitter.domain

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.grpc.internal.spec.emitter.context.GrpcEmitterContext
import amf.shapes.client.scala.model.domain.{AnyShape, ArrayShape, NodeShape, ScalarShape}

trait GrpcEmitter {

  // Constants for unknown type fallbacks
  private val UnknownMessage = "UnknownMessage"
  private val UnknownEnum    = "UnknownEnum"
  private val UnknownAny     = "UnknownAny"

  def emitOptions(domainElement: DomainElement, builder: StringDocBuilder, ctx: GrpcEmitterContext): Unit = {
    domainElement.customDomainProperties.foreach { extension =>
      GrpcOptionsEmitter(extension, builder, ctx).emit()
    }
  }

  def mustEmitOptions(domainElement: DomainElement): Boolean = domainElement.customDomainProperties.nonEmpty

  def buildSingleOptionContent(
      customDomainProperty: amf.core.client.scala.model.domain.extensions.DomainExtension,
      builder: StringDocBuilder,
      ctx: GrpcEmitterContext
  ): String = {
    val optionContent = builder.inlined { b =>
      GrpcOptionsEmitter(customDomainProperty, b, ctx).emitFieldExtension()
    }
    // Remove trailing semicolon from option content since caller adds it at their level
    if (optionContent.endsWith(";")) optionContent.dropRight(1) else optionContent
  }

  /** Converts a Shape to its corresponding gRPC field type string representation. */
  def fieldRange(range: Shape): String = {
    range match {
      case m: NodeShape if isMapShape(m) => mapRange(m)
      case a: ArrayShape                 => arrayRange(a)
      case s: ScalarShape                => scalarRange(s)
      case o: NodeShape                  => objectRange(o)
      case a: AnyShape                   => anyRange(a)
      case _                             => UnknownMessage
    }
  }

  private def isMapShape(nodeShape: NodeShape): Boolean =
    Option(nodeShape.additionalPropertiesKeySchema).isDefined

  /** Handles array shape type resolution */
  private def arrayRange(arrayShape: ArrayShape): String = {
    arrayShape.items match {
      case s: ScalarShape => scalarRange(s)
      case n: NodeShape   => objectRange(n)
      case a: AnyShape    => anyRange(a)
      case _              => UnknownMessage
    }
  }

  /** Converts a ScalarShape to its gRPC type representation */
  private def scalarRange(s: ScalarShape): String =
    if (s.isLink) { resolveShapeName(s, UnknownEnum) }
    else mapDataTypeToGrpcType(s)

  /** Maps AMF DataType to gRPC primitive types, considering format specifications */
  private def mapDataTypeToGrpcType(s: ScalarShape): String = {
    val maybeFormat = s.format.option()
    s.dataType.value() match {
      case DataType.Double if maybeFormat.isEmpty               => "double"
      case DataType.Float if maybeFormat.isEmpty                => "float"
      case DataType.Integer if maybeFormat.contains("uint32")   => "uint32"
      case DataType.Long if maybeFormat.contains("uint64")      => "uint64"
      case DataType.Integer if maybeFormat.contains("sint32")   => "sint32"
      case DataType.Long if maybeFormat.contains("sint64")      => "sint64"
      case DataType.Integer if maybeFormat.contains("fixed32")  => "fixed32"
      case DataType.Long if maybeFormat.contains("fixed64")     => "fixed64"
      case DataType.Integer if maybeFormat.contains("sfixed32") => "sfixed32"
      case DataType.Long if maybeFormat.contains("sfixed64")    => "sfixed64"
      case DataType.Integer                                   => "int32"
      case DataType.Long                                      => "int64"
      case DataType.Boolean                                   => "bool"
      case DataType.String                                    => "string"
      case DataType.Byte                                      => "bytes"
      case _                                                  => "string"
    }
  }

  private def objectRange(o: NodeShape): String = resolveShapeName(o, UnknownMessage)

  private def anyRange(anyShape: AnyShape): String = resolveShapeName(anyShape, UnknownAny)

  /** Generates gRPC map type syntax from a NodeShape with additional properties */
  private def mapRange(m: NodeShape): String = {
    val key   = fieldRange(m.additionalPropertiesKeySchema)
    val value = fieldRange(m.additionalPropertiesSchema)
    s"map<$key,$value>"
  }

  private def resolveShapeName(s: AnyShape, defaultName: String): String = {
    s.linkLabel
      .option()
      .orElse(s.displayName.option())
      .orElse(s.name.option())
      .getOrElse(defaultName)
  }
}
