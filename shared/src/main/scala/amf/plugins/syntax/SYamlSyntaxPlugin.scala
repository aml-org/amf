package amf.plugins.syntax

import amf.compiler.ParsedDocument
import amf.framework.plugins.AMFSyntaxPlugin
import org.yaml.model.{YComment, YDocument, YMap, YNode}
import org.yaml.parser.YamlParser

class SYamlSyntaxPlugin extends AMFSyntaxPlugin {

  override val ID = "SYaml"

  override def supportedMediaTypes() = Seq(
    "application/yaml",
    "application/x-yaml",
    "text/yaml",
    "text/x-yaml",
    "application/json",
    "text/json",
    "application/raml"
  )

  override def parse(text: CharSequence) = {
    val parser = YamlParser(text)
    val parts = parser.parse(true)

    if (parts.exists(v => v.isInstanceOf[YDocument])) {
      parts collectFirst { case d: YDocument => d } map { document =>
        val comment = parts collectFirst { case c: YComment => c }
        ParsedDocument(comment, document)
      }
    } else {
      parts collectFirst { case d: YComment => d } map { comment =>
        ParsedDocument(Some(comment), YDocument(IndexedSeq(YNode(YMap()))))
      }
    }
  }

}
