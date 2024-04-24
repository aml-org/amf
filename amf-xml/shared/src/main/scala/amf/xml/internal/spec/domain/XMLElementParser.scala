package amf.xml.internal.spec.domain

import amf.aml.client.scala.model.domain.NodeMapping
import amf.aml.client.scala.model.domain.{DialectDomainElement, PropertyMapping}
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.vocabulary.Namespace
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.domain.Annotations
import amf.xml.internal.plugins.syntax.XMLDocumentParserHelper
import amf.xml.internal.spec.context.XMLDocContext

import scala.collection.mutable
import scala.xml.{Elem, PCData, Text}

class XMLElementParser(elem: Elem, ctx: XMLDocContext) extends XMLDocumentParserHelper {

  def parse(adopt: DialectDomainElement => Unit): DialectDomainElement = {
    ctx.registerNamespaces(elem)

    val nodeMapping = buildNodeMapping(elem)

    val nodeClass = instanceClass(elem, ctx)
    val parsed = DialectDomainElement()
      .withDefinedBy(nodeMapping)
      .withInstanceTypes(Seq(nodeClass))

    val literalPropertiesAcc = mutable.Map[String,List[String]]()
    val objectPropertiesAcc = mutable.Map[String,List[DialectDomainElement]]()

    var column: Option[Int] = None
    var line: Option[Int] = None

    for ((key, value) <- elem.attributes.asAttrMap) {
      if (key == "column") {
        column = Some(Integer.parseInt(value))
      } else if (key == "line") {
        line = Some(Integer.parseInt(value))
      } else {
        val propIri = propertyIri(key, elem, ctx)
        literalPropertiesAcc.get(propIri) match {
          case Some(values) =>
            literalPropertiesAcc.put(propIri, values ++ List(value))
          case None         =>
            literalPropertiesAcc.put(propIri, List(value))
        }
      }
    }

    var textChildren = ""
    for (child <- elem.child) {
      child match {
        case text: PCData =>
          val txt = text.text.trim()
          if (!txt.isBlank) {
            textChildren += txt
          }

        case text: Text =>
          val txt = text.text.trim()
          if (!txt.isBlank) {
            textChildren += txt
          }

        case childElem: Elem =>
          new XMLElementParser(childElem, ctx).parse({ (parsedChild: DialectDomainElement) =>
            val propIri = propertyIri(childElem.label, childElem, ctx)
            objectPropertiesAcc.get(propIri) match {
              case Some(values) =>
                objectPropertiesAcc.put(propIri, values ++ List(parsedChild))
              case None         =>
                objectPropertiesAcc.put(propIri, List(parsedChild))
            }
          })
        case _ => // ignore
      }
    }

    literalPropertiesAcc.foreach { case (propIri, values) =>
      parsed.withLiteralProperty(propIri, values)
    }
    if (textChildren.nonEmpty) {
      parsed.withLiteralProperty((Namespace.Data + "text").iri(), textChildren)
    }
    objectPropertiesAcc.foreach { case (propIri, values) =>
      parsed.withObjectCollectionProperty(propIri, values)
    }

    if (column.isDefined && line.isDefined) {
      val lexicalInformation = LexicalInformation(line.get, column.get, line.get, column.get + 1)
      parsed.annotations ++= Set(lexicalInformation)
    }

    adopt(parsed)

    parsed
  }

  /**
   * Dynamically generates a node mapping or enrich one in the context for this type of XML element
   * @param nodeClass
   * @param elem
   * @return
   */
  def buildNodeMapping(elem: Elem): NodeMapping = {
    val nodeClass = instanceClass(elem, ctx)
    val nodeMapping = ctx.nodeMappingFor(nodeClass)
    val propertyCache = mutable.HashMap[String,Boolean]()
    val properties = mutable.ListBuffer[PropertyMapping]()

    // update the cache of properties in case the NodeMapping comes from the ctx cache
    for (prop <- nodeMapping.propertiesMapping()) {
      val iri = prop.id
      propertyCache.put(iri, true)
      properties += prop
    }

    for ((key, _) <- elem.attributes.asAttrMap) {
      val iri = propertyIri(key, elem, ctx)
      if (!propertyCache.contains(iri)) {
        val propertyMapping: PropertyMapping = PropertyMapping().withId(iri).withName(key).withNodePropertyMapping(iri).withAllowMultiple(true).withLiteralRange((Namespace.Xsd + "string").iri())
        propertyCache.put(iri, true)
        properties += propertyMapping
      }
    }


    for (child <- elem.child) {
      child match {
        case childElem: Elem =>
          val iri = propertyIri(childElem.label, childElem, ctx)
          if (!propertyCache.contains(iri)) {
            val childNodeClass = instanceClass(childElem, ctx)
            val propertyMapping: PropertyMapping = PropertyMapping().withId(iri).withName(childElem.label).withNodePropertyMapping(iri).withAllowMultiple(true).withObjectRange(Seq(childNodeClass))
            propertyCache.put(iri, true)
            properties += propertyMapping
          }
        case _ => // ignore
      }
    }

    nodeMapping.withPropertiesMapping(properties)
  }
}
