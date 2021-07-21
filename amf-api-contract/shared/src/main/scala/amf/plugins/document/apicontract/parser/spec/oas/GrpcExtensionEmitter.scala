package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes._
import amf.shapes.internal.vocabulary.VocabularyMappings

case class GrpcExtensionEmitter(extensions: Seq[CustomDomainProperty], builder: StringDocBuilder, domain: String, ctx: GrpcEmitterContext) {

  def emit(): Unit = {
    builder.fixed { f =>
      f += (s"extend ${emitDomain(domain)} {")
      f.obj { o =>
        o.list { l =>
          extensions.foreach { customDomainProperty =>
            emitExtensionField(l, customDomainProperty)
          }
        }
      }
      f += "}"
    }
  }

  def emitDomain(domain: String): String = {
    domain match {
      case "field"                      => FIELD_OPTIONS
      case "enum"                       => ENUM_OPTIONS
      case "enum_value"                 => ENUM_VALUE_OPTIONS
      case "extension_range"            => EXTENSION_RANGE_OPTIONS
      case VocabularyMappings.shape     => MESSAGE_OPTIONS
      case VocabularyMappings.operation => METHOD_OPTIONS
      case VocabularyMappings.endpoint  => SERVICE_OPTIONS
      case VocabularyMappings.webapi    => FILE_OPTIONS
      case "oneof"                      => ONEOF_OPTIONS
      case _                            => FILE_OPTIONS
    }
  }

  def emitExtensionField(builder: StringDocBuilder, property: CustomDomainProperty): Unit = {
    val fieldShape = PropertyShape(property.annotations)
      .withRange(property.schema)
      .withName(property.name.value())
      .withSerializationOrder(property.serializationOrder.value())
    new GrpcFieldEmitter(fieldShape, builder, ctx).emit()
  }
}
