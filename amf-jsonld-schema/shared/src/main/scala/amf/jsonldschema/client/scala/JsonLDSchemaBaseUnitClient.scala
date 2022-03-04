package amf.jsonldschema.client.scala

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.{Dialect, DialectInstance}
import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.domain.Value
import amf.jsonldschema.client.scala.model.JsonLdSchemaDocument
import amf.jsonldschema.internal.scala.{JsonLDDialectWrapper, JsonLDSchemaPayloadWrapper, WrappedDialect}
import amf.shapes.client.scala.config.SemanticBaseUnitClient
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticSchemaParser
import org.yaml.model.{YDocument, YNode, YSequence}
import org.yaml.render.JsonRender

import scala.concurrent.Future

class JsonLDSchemaBaseUnitClient private[amf] (override protected val configuration: JsonLDSchemaConfiguration)
    extends SemanticBaseUnitClient(configuration) {

  def parsePayloadWithDialect(payload: String, dialect: Dialect): Future[JsonLdSchemaDocument] = {
    val isArray = payload.trim.startsWith("[") // better option is to add a flat ad AML encodes field (at root documents model) to mark as an array. If we need top level array constraints then we need some particular node (redundant against property mapping array constraints

    val content        = JsonLDSchemaPayloadWrapper.wrap(payload, dialect.nameAndVersion())
    val wrappedDialect = JsonLDDialectWrapper.wrap(dialect, isArray)
    val client         = AMLConfiguration.predefined().withDialect(wrappedDialect.dialect).baseUnitClient()
    client
      .parseContent(content) // lexical info? parse by hand using syaml and initial line and column offset -2?
      .map(r => {
        r.baseUnit match {
          case d: DialectInstance =>
            JsonLDDialectWrapper.unWrap(wrappedDialect)
            JsonLdSchemaDocument().withId(d.id).withEncodes(extractRootNode(d, wrappedDialect))
          case _ => JsonLdSchemaDocument().withId("error")
        }
      })
  }

  def emitPayloadWithDialect(jsonLdSchemaDocument: JsonLdSchemaDocument, dialect: Dialect): String = {
    val wrappedDialect    = JsonLDDialectWrapper.wrap(dialect, jsonLdSchemaDocument.encodes.length > 1) // what if we have an array of only 1 element?
    val client            = AMLConfiguration.predefined().withDialect(wrappedDialect.dialect).elementClient()
    val nodes: Seq[YNode] = jsonLdSchemaDocument.encodes.map(client.renderElement(_, Nil))
    JsonLDDialectWrapper.unWrap(wrappedDialect)
    renderRootElements(nodes)
  }

  private def renderRootElements(nodes: Seq[YNode]) = {
    JsonRender.render(buildDocument(nodes))
  }

  private def buildDocument(nodes: Seq[YNode]) = {
    if (nodes.length < 2) YDocument(nodes.head)
    else {
      val value = YSequence(nodes.toArray)
      YDocument(YNode(value))
    }
  }
  private def extractRootNode(d: DialectInstance, wrappedDialect: WrappedDialect) = {
    d.encodes.fields.getValueAsOption(wrappedDialect.dataPropertyTerm) match {
      case Some(Value(obj: DialectDomainElement, _)) => Seq(obj)
      case Some(Value(arr: AmfArray, _))             => arr.values.collect({ case d: DialectDomainElement => d })
      case _                                         => Seq.empty
    }
  }
}
