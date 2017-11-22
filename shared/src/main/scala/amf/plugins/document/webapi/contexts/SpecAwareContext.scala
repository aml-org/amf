package amf.plugins.document.webapi.contexts

import org.yaml.model.YNode

trait SpecAwareContext {
  def link(node: YNode): Either[String, YNode]
  def ignore(shape: String, property: String): Boolean
}