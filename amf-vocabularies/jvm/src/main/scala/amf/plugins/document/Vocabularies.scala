package amf.plugins.document

import java.util.concurrent.CompletableFuture

import amf.client.model.document.Dialect
import amf.core.remote.FutureConverter._
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin

import scala.concurrent.ExecutionContext.Implicits.global

object Vocabularies extends PlatformSecrets {

  def register() = {
    amf.plugins.domain.Vocabularies.register(platform)
    amf.Core.registerPlugin(RAMLVocabulariesPlugin)
  }

  def registerDialect(url: String): CompletableFuture[Dialect] =
    RAMLVocabulariesPlugin.registry.registerDialect(url).map(Dialect).asJava


  def registerDialect(url: String, dialectText: String): CompletableFuture[Dialect] =
    RAMLVocabulariesPlugin.registry.registerDialect(url, dialectText).map(Dialect).asJava

}
