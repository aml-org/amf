package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{DomainElement, Shape}
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, ScalarShape}

trait GrpcEmitter {

  def emitOptions(domainElement: DomainElement, builder: StringDocBuilder, ctx: GrpcEmitterContext): Unit =  {
    domainElement.customDomainProperties.foreach { extension =>
      GrpcOptionsEmitter(extension, builder, ctx).emit()
    }
  }

  def mustEmitOptions(domainElement: DomainElement): Boolean = domainElement.customDomainProperties.nonEmpty

  def fieldRange(range: Shape): String = {
    range match {
      case m: NodeShape if Option(m.additionalPropertiesKeySchema).isDefined => mapRange(m)
      case a: ArrayShape if a.items.isInstanceOf[ScalarShape] => scalarRange(a.items.asInstanceOf[ScalarShape])
      case a: ArrayShape if a.items.isInstanceOf[NodeShape]   => objectRange(a.items.asInstanceOf[NodeShape])
      case s: ScalarShape                                     => scalarRange(s)
      case o: NodeShape                                       => objectRange(o)
      case s                                                  => "UnknownMessage"
    }
  }

  def scalarRange(s: ScalarShape): String = {
    if (s.isLink) {
      s.linkLabel.option().orElse(s.displayName.option()).orElse(s.name.option()).getOrElse("UnknownEnum")
    } else {
      s.dataType.value() match {
        case DataType.Double if s.format.option().isEmpty               =>"double"
        case DataType.Float if s.format.option().isEmpty                => "float"
        case DataType.Integer if s.format.option().contains("uint32")   => "uint32"
        case DataType.Long if s.format.option().contains("uint64")      => "uint64"
        case DataType.Integer if s.format.option().contains("sint32")   => "sint32"
        case DataType.Long if s.format.option().contains("sint64")      => "sint64"
        case DataType.Integer if s.format.option().contains("fixed32")  => "fixed32"
        case DataType.Long if s.format.option().contains("fixed64")     => "fixed64"
        case DataType.Integer if s.format.option().contains("sfixed32") => "sfixed32"
        case DataType.Long if s.format.option().contains("sfixed64")    => "sfixed64"
        case DataType.Integer                                           => "int32"
        case DataType.Long                                              => "int64"
        case DataType.Boolean                                           => "bool"
        case DataType.String                                            => "string"
        case DataType.Byte                                              => "bytes"
        case _                                                          => "string"
      }
    }
  }

  def objectRange(o: NodeShape): String = {
    o.linkLabel.option().orElse(o.displayName.option()).orElse(o.name.option()).getOrElse("UnknownMessage")
  }

  def mapRange(m: NodeShape): String = {
    val key = fieldRange(m.additionalPropertiesKeySchema)
    val value = fieldRange(m.additionalPropertiesSchema)
    s"map<$key,$value>"
  }

}
