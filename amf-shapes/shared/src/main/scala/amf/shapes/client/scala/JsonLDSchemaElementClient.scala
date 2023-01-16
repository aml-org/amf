package amf.shapes.client.scala

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.render.BaseEmitters.traverse
import amf.shapes.client.scala.config.JsonLDSchemaConfiguration
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import amf.shapes.client.scala.render.{JsonLDObjectRender, TermNameSyntaxProvider}
import org.yaml.builder.YDocumentBuilder
import org.yaml.model.{YDocument, YNode}

class JsonLDSchemaElementClient private[amf] (override protected val configuration: JsonLDSchemaConfiguration)
    extends ShapesElementClient(configuration) {
  override def renderElement(element: DomainElement, references: Seq[BaseUnit]): YNode = {
    element match {
      case jsonLdObject: JsonLDObject =>
        val emitter = new JsonLDObjectRender(jsonLdObject, TermNameSyntaxProvider)
        val document = YDocument { b =>
          traverse(Seq(emitter), b)
        }
        document.node
      case _ => super.renderElement(element, references)
    }
  }

  override def getConfiguration: JsonLDSchemaConfiguration = configuration
}
