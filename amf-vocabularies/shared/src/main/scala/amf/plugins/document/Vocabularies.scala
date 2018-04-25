package amf.plugins.document

import amf.client.convert.VocabulariesClientConverter._
import amf.client.environment.Environment
import amf.client.model.document.Dialect
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import amf.plugins.domain.VocabulariesRegister

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
object Vocabularies extends PlatformSecrets {

  def register(): Unit = {
    VocabulariesRegister.register(platform)
    amf.Core.registerPlugin(RAMLVocabulariesPlugin)
  }

  def registerDialect(url: String): ClientFuture[Dialect] =
    RAMLVocabulariesPlugin.registry.registerDialect(url).asClient

  def registerDialect(url: String, env: Environment): ClientFuture[Dialect] =
    RAMLVocabulariesPlugin.registry.registerDialect(url, env._internal).asClient

  def registerDialect(url: String, dialectText: String): ClientFuture[Dialect] =
    RAMLVocabulariesPlugin.registry.registerDialect(url, dialectText).asClient

  def registerDialect(url: String, dialectText: String, env: Environment): ClientFuture[Dialect] =
    RAMLVocabulariesPlugin.registry.registerDialect(url, dialectText, env._internal).asClient
}
