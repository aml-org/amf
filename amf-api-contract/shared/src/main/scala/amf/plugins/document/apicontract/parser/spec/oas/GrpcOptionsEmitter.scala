package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.scala.model.domain.{DataNode, ObjectNode}
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.parser.domain.ScalarNode
import amf.core.internal.plugins.syntax.StringDocBuilder

case class GrpcOptionsEmitter(domainExtension: DomainExtension, builder: StringDocBuilder, ctx: GrpcEmitterContext) {
  def emit(): Unit = {
    emitExtension(builder)
  }

  val name = domainExtension.name.value()

  def emitExtension(builder: StringDocBuilder) = {
    val prefix = if (isDefaultOption) {
      s"option $name ="
    } else {
      s"option ($name) ="
    }
    emitOptionData(prefix, builder, domainExtension.extension, ";")
  }

  def emitOptionData(prefix: String, builder: StringDocBuilder, node: DataNode, eol: String = ""): Unit = {
    node match {
      case node: ScalarNode =>
        node.dataType.option().getOrElse(DataType.String) match {
          case DataType.String => builder += (prefix + " \"" + node.value.value() + "\"" + eol, pos(node.annotations))
          case _               => builder += (s"$prefix ${node.value.value()}$eol", pos(node.annotations))
        }
      case node: ObjectNode =>
        builder.fixed { f =>
          f += (s"$prefix {", pos(node.annotations))
          f.obj { o =>
            o.list { l =>
              node.allPropertiesWithName() foreach { case (k,v) =>
                emitOptionData(s"${k}: ", l, v)
              }
            }
          }
          f += ("}")
        }
    }
  }

  def isDefaultOption = DEFAULT_OPTIONS.contains(name)

  val DEFAULT_OPTIONS = Set(
    "go_package",
    "java_package",
    "java_outer_classname",
    "java_multiple_files",
    "csharp_namespace",
    "objc_class_prefix",
    "cc_enable_arenas",
    "optimize_for"
  )
}
