package amf.plugins.document.webapi.contexts

import amf.core.model.document.{ExternalFragment, Fragment, RecursiveUnit}
import amf.core.parser.ParsedReference
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.JsonSchemaPlugin
import org.yaml.model.YNode

case class JsonSchemaRefGuide(currentLoc: String, references: Seq[ParsedReference])(
    implicit val context: WebApiContext)
    extends PlatformSecrets {

  def obtainRemoteYNode(ref: String): Option[YNode] = {
    withFragmentAndInFileReference(ref) { (fragment, referenceUrl) =>
      JsonSchemaPlugin.obtainRootAst(fragment, referenceUrl)
    }
  }

  def withFragmentAndInFileReference[T](ref: String)(action: (Fragment, Option[String]) => Option[T]): Option[T] = {
    val fileUrl      = getFileUrl(ref)
    val referenceUrl = getReferenceUrl(fileUrl)
    obtainFragmentFromFullRef(fileUrl) flatMap { fragment =>
      action(fragment, referenceUrl)
    }
  }

  def getFileUrl(ref: String): String = context.resolvedPath(currentLoc, ref)

  def changeJsonSchemaSearchDestination(loc: String): JsonSchemaRefGuide = {
    val optionalRef = references
      .filter(r => r.unit.location().isDefined)
      .find(_.unit.location().get == loc)
    optionalRef
      .map(r => JsonSchemaRefGuide(loc, r.unit.references.map(unit => ParsedReference(unit, r.origin))))
      .getOrElse(this)
  }

  private def getReferenceUrl(fileUrl: String): Option[String] = {
    fileUrl.split("#") match {
      case s: Array[String] if s.size > 1 => Some(s.last)
      case _                              => None
    }
  }

  private def obtainFragmentFromFullRef(fileUrl: String): Option[Fragment] = {
    val baseFileUrl = fileUrl.split("#").head
    references
      .filter(r => r.unit.location().isDefined)
      .find(_.unit.location().get == baseFileUrl) collectFirst {
      case ref if ref.unit.isInstanceOf[ExternalFragment] => ref.unit.asInstanceOf[ExternalFragment]
      case ref if ref.unit.isInstanceOf[RecursiveUnit]    => ref.unit.asInstanceOf[RecursiveUnit]
    }
  }
}
