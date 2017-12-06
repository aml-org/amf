package amf.plugins.document

import amf.core.metamodel.Obj
import amf.core.registries.AMFPluginsRegistry
import amf.core.unsafe.PlatformSecrets
import amf.model.{DialectFragment, DomainEntity}
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import amf.plugins.document.vocabularies.metamodel.document.DialectNodeFragmentModel
import amf.plugins.document.vocabularies.metamodel.domain.DialectEntityModel
import amf.plugins.document.vocabularies.model.{document, domain}
import amf.plugins.document.vocabularies.registries.PlatformDialectRegistry
import amf.plugins.document.vocabularies.spec.Dialect

import scala.scalajs.js.Promise
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll
import scala.concurrent.ExecutionContext.Implicits.global

@JSExportAll
object Vocabularies extends PlatformSecrets{

  def init() = {
    val p: (Obj) => Boolean = (x: Obj) => x.isInstanceOf[DialectEntityModel]
    platform.registerWrapperPredicate(p) {
      case m: domain.DomainEntity => DomainEntity(m)
    }
    platform.registerWrapper(DialectNodeFragmentModel) {
      case d: document.DialectFragment => new DialectFragment(d)
    }

    AMFPluginsRegistry.registerDocumentPlugin(RAMLVocabulariesPlugin)
  }

  def registerDialect(url: String): Promise[Dialect] = PlatformDialectRegistry.registerDialect(url).toJSPromise
  def registerDialect(url: String, dialectText: String): Promise[Dialect] = PlatformDialectRegistry.registerDialect(url, dialectText).toJSPromise
}
