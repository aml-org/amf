package amf.plugins.document.webapi.parser.spec.domain

import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.DomainElement
import amf.core.parser._
import amf.core.vocabulary.Namespace.XsdTypes.{xsdString, xsdUri}
import amf.plugins.document.vocabularies.model.document.Dialect
import amf.plugins.document.vocabularies.model.domain.{NodeMapping, PropertyMapping}
import amf.plugins.document.vocabularies.parser.instances.{DialectInstanceContext, InstanceNPparser}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.LicenseModel
import amf.plugins.domain.webapi.models.License
import org.yaml.model.{YMap, YNode}

import scala.collection.mutable

/**
  *
  */
object LicenseParser {
  def apply(node: YNode)(implicit ctx: WebApiContext): LicenseParser = new LicenseParser(node)(toOas(ctx))

  def parse(node: YNode)(implicit ctx: WebApiContext): License =
    LicenseParser(node).parse()

  def parseAml(node: YNode)(implicit ctx: WebApiContext): License = {
    new BaseNodeParser(node.as[YMap], LicenseModel.`type`.head.iri()).parse().asInstanceOf[License]
  }
}

class LicenseParser(node: YNode)(implicit ctx: WebApiContext) extends SpecParserOps {
  def parse(): License = {
    val license = License(node)

    val map = node.as[YMap]
    map.key("url", LicenseModel.Url in license)
    map.key("name", LicenseModel.Name in license)

    AnnotationParser(license, map).parse()

    ctx.closedShape(license.id, map, "license")

    license
  }
}

object NodeMappingRegistry {

  case class AmlObjRelation(model: DomainElementModel, metadata: NodeMapping)
  // this should be two differents index, as exception parser could request generic instance building based on static model.
  private val index: mutable.Map[String, AmlObjRelation] =
    mutable.Map(LicenseModel.`type`.head.iri() -> AmlObjRelation(LicenseModel, AMLLicenseObject.node))

  def getMetadataRelation(iri: String): AmlObjRelation = {
    index.getOrElse(iri, throw new UnsupportedOperationException("Cannot find metadata"))
  }

}

// this will be a recursive registry. All parsers should request to the registry for a parser and invoke it.
// Tide up for poc, this should be the default parser if not exception is defined
// registry get parser Model.iri() or default.
class BaseNodeParser(map: YMap, term: String)(implicit ctx: WebApiContext) extends SpecParserOps {

  def parse(): DomainElement = {
    val relation = NodeMappingRegistry.getMetadataRelation(term)
    val instance = relation.model.modelInstance.asInstanceOf[DomainElement] // handle this with interface
    instance.annotations ++= Annotations(map) // handle key term cases

    new InstanceNPparser(instance, map, relation.metadata)(new DialectInstanceContext(Dialect(), ctx, None)).parse()

  }
}

object AMLLicenseObject {

  val location = "file://amf-dialect.com/root"
  val node = NodeMapping()
    .withId("file://amf-dialect.com/root" + "/#declarations/" + "LicenseObject")
    .withName("LicenseObject")
    .withNodeTypeMapping(LicenseModel.`type`.head.iri())
    .withPropertiesMapping(Seq(
      PropertyMapping()
        .withId(location + "#/declarations/LicenseObject/name")
        .withName("name")
        .withMinCount(1)
        .withNodePropertyMapping(LicenseModel.Name.value.iri())
        .withLiteralRange(xsdString.iri()),
      PropertyMapping()
        .withId(location + "#/declarations/LicenseObject/url")
        .withName("url")
        .withNodePropertyMapping(LicenseModel.Url.value.iri())
        .withLiteralRange(xsdUri.iri())
    ))
}
