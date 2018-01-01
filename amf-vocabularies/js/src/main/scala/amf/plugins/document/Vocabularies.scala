package amf.plugins.document

import amf.core.metamodel.Obj
import amf.core.unsafe.PlatformSecrets
import amf.model.document.DialectFragment
import amf.model.domain.DomainEntity
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import amf.plugins.document.vocabularies.metamodel.document.DialectNodeFragmentModel
import amf.plugins.document.vocabularies.metamodel.domain.DialectEntityModel
import amf.plugins.document.vocabularies.model.{document, domain}
import amf.plugins.document.vocabularies.registries.PlatformDialectRegistry
import amf.plugins.document.vocabularies.spec.Dialect

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object Vocabularies extends PlatformSecrets {

  def register() = {
    val p: (Obj) => Boolean = (x: Obj) => x.isInstanceOf[DialectEntityModel]
    platform.registerWrapperPredicate(p) {
      case m: domain.DomainEntity => new DomainEntity(m)
    }
    platform.registerWrapper(DialectNodeFragmentModel) {
      case d: document.DialectFragment => new DialectFragment(d)
    }

    amf.Core.registerPlugin(RAMLVocabulariesPlugin)
  }

  def registerDialect(url: String): Promise[Dialect] = PlatformDialectRegistry.registerDialect(url).toJSPromise
  def registerDialect(url: String, dialectText: String): Promise[Dialect] =
    PlatformDialectRegistry.registerDialect(url, dialectText).toJSPromise
}
