package amf

import java.util.concurrent.{CompletableFuture, Future}

import amf.model.document.BaseUnit
import amf.plugins.document.Vocabularies
import amf.plugins.document.vocabularies.spec.Dialect
import amf.validation.AMFValidationReport

object AMF {

  def init(): CompletableFuture[Nothing] = {
    amf.plugins.document.WebApi.register()
    amf.plugins.document.Vocabularies.register()
    amf.plugins.features.AMFValidation.register()
    amf.Core.init()
  }

  def ramlParser(): RamlParser = new RamlParser()

  def raml10Parser(): Raml10Parser = new Raml10Parser()

  def raml10Generator(): Raml10Generator = new Raml10Generator()

  def raml08Parser(): Raml08Parser = new Raml08Parser()

  def raml08Generator(): Raml08Generator = new Raml08Generator()

  def oas20Parser(): Oas20Parser = new Oas20Parser()

  def oas20Generator(): Oas20Generator = new Oas20Generator()

  def amfGraphParser(): AmfGraphParser = new AmfGraphParser()

  def amfGraphGenerator(): AmfGraphGenerator = new AmfGraphGenerator()

  def validate(model: BaseUnit,
               profileName: String,
               messageStyle: String = "AMF"): CompletableFuture[AMFValidationReport] =
    Core.validate(model, profileName, messageStyle)

  def loadValidationProfile(url: String): CompletableFuture[Nothing] = Core.loadValidationProfile(url)

  def registerNamespace(alias: String, prefix: String): Boolean = Core.registerNamespace(alias, prefix)

  def registerDialect(url: String): Future[Dialect] = Vocabularies.registerDialect(url)

  def resolveRaml10(unit: BaseUnit): BaseUnit = new Raml10Resolver().resolve(unit)

  def resolveOas20(unit: BaseUnit): BaseUnit = new Oas20Resolver().resolve(unit)

  def resolveAmfGraph(unit: BaseUnit): BaseUnit = new AmfGraphResolver().resolve(unit)
}
