package amf

import java.util.concurrent.CompletableFuture

import amf.core.client.Validator
import amf.core.registries.AMFPluginsRegistry
import amf.core.validation.AMFValidationReport
import amf.model.document.BaseUnit
import amf.plugins.syntax.SYamlSyntaxPlugin

object AMF {

  def init(): CompletableFuture[Any] = {
    amf.Core.init()
    AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
    amf.plugins.document.WebApi.init()
    amf.plugins.document.Vocabularies.init()
    amf.plugins.domain.WebApi.init()
    amf.plugins.domain.DataShapes.init()
    amf.plugins.document.Vocabularies.init()
    amf.plugins.features.AMFValidation.init()
  }


  def raml10Parser(): Raml10Parser = new Raml10Parser()

  def raml10Generator(): Raml10Generator = new Raml10Generator()

  def oas20Parser(): Oas20Parser = new Oas20Parser()

  def oas20Generator(): Oas20Generator = new Oas20Generator()

  def amfGraphParser(): AmfGraphParser = new AmfGraphParser()

  def amfGraphGenerator(): AmfGraphGenerator = new AmfGraphGenerator()

  def validate(model: BaseUnit, profileName: String, messageStyle: String = "AMF"): CompletableFuture[AMFValidationReport] = Validator.validate(model, profileName, messageStyle)

  def loadValidationProfile(path: java.io.File): CompletableFuture[Nothing] = Validator.loadValidationProfile(path)

  def resolveRaml10(unit: BaseUnit) = new Raml10Resolver().resolve(unit)

  def resolveOas20(unit: BaseUnit) = new Oas20Resolver().resolve(unit)

  def resolveAmfGraph(unit: BaseUnit) = new AmfGraphResolver().resolve(unit)
}
