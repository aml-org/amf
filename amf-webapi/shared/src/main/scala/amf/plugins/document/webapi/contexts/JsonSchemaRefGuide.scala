package amf.plugins.document.webapi.contexts

import amf.core.model.document.{ExternalFragment, Fragment, InferredModuleFragment, RecursiveUnit}
import amf.core.parser.ParsedReference
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.parser.spec.jsonschema.AstFinder
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaRootCreator.getYNodeFrom
import org.yaml.model.YNode

case class JsonSchemaRefGuide(currentLoc: String, references: Seq[ParsedReference])(
    implicit val context: WebApiContext)
    extends PlatformSecrets {

  def obtainRemoteYNode(ref: String): Option[YNode] = {
    withFragmentAndInFileReference(ref) { (fragment, referenceUrl) =>
      AstFinder.findAst(fragment, referenceUrl)
    }
  }

  def getRootYNode(ref: String): Option[YNode] = {
    withFragmentAndInFileReference(ref) { (fragment, _) =>
      Some(getYNodeFrom(fragment, context.eh))
    }
  }

  def withFragmentAndInFileReference[T](ref: String)(action: (Fragment, Option[String]) => Option[T]): Option[T] = {
    if (!context.validateRefFormatWithError(ref)) return None
    val fileUrl      = getResolvedFileUri(ref)
    val uriFragment  = getUriFragment(fileUrl)
    obtainFragmentFromFullRef(fileUrl) flatMap { fragment =>
      action(fragment, uriFragment)
    }
  }

  def getResolvedFileUri(ref: String): String = context.resolvedPath(currentLoc, ref)

  def changeJsonSchemaSearchDestination(loc: String): JsonSchemaRefGuide = {
    val optionalRef = references
      .filter(r => r.unit.location().isDefined)
      .find(_.unit.location().get == loc)
    optionalRef
      .map(r => JsonSchemaRefGuide(loc, r.unit.references.map(unit => ParsedReference(unit, r.origin))))
      .getOrElse(this)
  }

  private def getUriFragment(fileUrl: String): Option[String] = {
    fileUrl.split("#") match {
      case s: Array[String] if s.size > 1 => Some(s.last)
      case _                              => None
    }
  }

  private def uriWithoutFragment(uri: String): Option[String] = uri.split("#").headOption

  private def obtainFragmentFromFullRef(fileUrl: String): Option[Fragment] = {
    uriWithoutFragment(fileUrl).flatMap { findReferenceWithLocation }.collectFirst {
        case ref if ref.unit.isInstanceOf[ExternalFragment] => ref.unit.asInstanceOf[ExternalFragment]
        case ref if ref.unit.isInstanceOf[RecursiveUnit] => ref.unit.asInstanceOf[RecursiveUnit]
        case ref if ref.unit.isInstanceOf[InferredModuleFragment] => ref.unit.asInstanceOf[InferredModuleFragment]
    }
  }

  private def findReferenceWithLocation(uri: String): Option[ParsedReference] = {
      references
        .filter(r => r.unit.location().isDefined)
        .find(_.unit.location().get == uri)
  }
}