package amf.plugins.document.apicontract.parser.spec.declaration.common

import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.client.scala.model.domain.Linkable

object ExternalLinkQuery {

  def queryResidenceUnitOfLinkTarget(link: Linkable, refs: Seq[BaseUnit]): Option[BaseUnit] = {
    link.linkTarget match {
      case Some(element) =>
        val linkTarget = element.id
        refs.find {
          case fragment: EncodesModel =>
            Option(fragment.encodes).exists(_.id == linkTarget)
          case library: DeclaresModel =>
            library.declares.exists(_.id == linkTarget)
          case _ => false
        }

      case _ => None
    }
  }
}
