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

  def emitExtension(builder: StringDocBuilder): Unit = {
    val prefix = if (isDefaultOption) {
      s"option $name ="
    } else {
      s"option ($name) ="
    }
    emitOptionData(prefix, builder, domainExtension.extension, ";")
  }

  def emitFieldExtension(): Unit = {
    val prefix = if (isDefaultOption) {
      s"$name ="
    } else {
      s"($name) ="
    }
    emitOptionData(prefix, builder, domainExtension.extension)
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
    "allow_alias",
    "cc_enable_arenas",
    "cc_generic_services",
    "csharp_namespace",
    "ctype",
    "deprecated",
    "go_package",
    "go_package",
    "idempotency_level",
    "java_generate_equals_and_hash",
    "java_generic_services",
    "java_multiple_files",
    "java_outer_classname",
    "java_package",
    "java_string_check_utf8",
    "jstype",
    "lazy",
    "map_entry",
    "message_set_wire_format",
    "no_standard_descriptor_accessor",
    "objc_class_prefix",
    "optimize_for",
    "packed",
    "php_class_prefix",
    "php_generic_services",
    "php_metadata_namespace",
    "php_namespace",
    "py_generic_services",
    "ruby_package",
    "swift_prefix",
    "uninterpreted_option",
    "weak"
  )
}
