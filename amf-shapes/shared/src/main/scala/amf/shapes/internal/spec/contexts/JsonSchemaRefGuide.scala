package amf.shapes.internal.spec.contexts

import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment, RecursiveUnit}
import amf.core.client.scala.parse.document.ParsedReference
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.contexts.ReferenceFinder.{getFileUrl, getJsonReferenceFragment}
import amf.shapes.internal.spec.jsonschema.ref.AstFinder
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaRootCreator.getYNodeFrom
import org.yaml.model.YNode

case class JsonSchemaRefGuide(currentLoc: String, currentUnit: Option[BaseUnit], references: Seq[ParsedReference])(
    implicit val context: ShapeParserContext
) extends PlatformSecrets {

  def obtainRemoteYNode(ref: String): Option[YNode] = {
    findJsonReferenceFragment(ref) flatMap { case (fragment, referenceUrl) =>
      AstFinder.findAst(fragment, referenceUrl)
    }
  }

  def getRootYNode(ref: String): Option[YNode] = {
    findJsonReferenceFragment(ref) map { case (fragment, _) =>
      getYNodeFrom(fragment, context)
    }
  }

  def findJsonReferenceFragment(ref: String): Option[(BaseUnit, Option[String])] = {
    if (!context.validateRefFormatWithError(ref)) return None
    val fileUrl      = getFileUrl(ref, currentLoc)
    val referenceUrl = getJsonReferenceFragment(fileUrl)
    obtainFragmentFromFullRef(fileUrl, ref) map (fragment => (fragment, referenceUrl))
  }

  def changeJsonSchemaSearchDestination(loc: String): JsonSchemaRefGuide = {
    val optionalRef = references
      .filter(r => r.unit.location().isDefined)
      .find(_.unit.location().get == loc)
    optionalRef
      .map(r => JsonSchemaRefGuide(loc, Some(r.unit), r.unit.references.map(unit => ParsedReference(unit, r.origin))))
      .getOrElse(this)
  }

  private def obtainFragmentFromFullRef(fileUrl: String, rawRef: String): Option[BaseUnit] = {
    // TODO: the json schema ref guide should not have doubled state. It should get its references from the context maybe ?? Or remove the context
    ReferenceFinder.findJsonReferencedUnit(fileUrl, rawRef, references) collectFirst {
      case unit: JsonSchemaDocument => unit
      case unit: ExternalFragment   => unit
      case unit: RecursiveUnit      => unit
    }
  }
}
