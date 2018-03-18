package amf.plugins.document

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

import amf.client.model.document.Dialect
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin

import scala.concurrent.ExecutionContext.Implicits.global

@JSExportAll
object Vocabularies extends PlatformSecrets {

  def register() = {
    amf.plugins.domain.Vocabularies.register(platform)
    amf.Core.registerPlugin(RAMLVocabulariesPlugin)
  }

  def registerDialect(url: String): js.Promise[Dialect] =
    RAMLVocabulariesPlugin.registry.registerDialect(url).map(Dialect).toJSPromise


  def registerDialect(url: String, dialectText: String): js.Promise[Dialect] =
    RAMLVocabulariesPlugin.registry.registerDialect(url, dialectText).map(Dialect).toJSPromise

}
