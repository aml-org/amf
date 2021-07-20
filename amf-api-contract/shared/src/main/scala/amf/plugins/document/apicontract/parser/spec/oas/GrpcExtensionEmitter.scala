package amf.plugins.document.apicontract.parser.spec.oas

import amf.client.remod.amfcore.plugins.render.StringDocBuilder
import amf.core.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes.{ENUM_OPTIONS, ENUM_VALUE_OPTIONS, EXTENSION_RANGE_OPTIONS, FIELD_OPTIONS, FILE_OPTIONS, MESSAGE_OPTIONS, METHOD_OPTIONS, ONEOF_OPTIONS, SERVICE_OPTIONS}
import amf.plugins.document.apicontract.vocabulary.VocabularyMappings

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
