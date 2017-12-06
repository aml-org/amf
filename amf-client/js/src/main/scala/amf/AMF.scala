package amf

import amf.core.validation.AMFValidationReport
import amf.model.document.BaseUnit
import amf.plugins.document.Vocabularies
import amf.plugins.document.vocabularies.spec.Dialect

import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("AMF")
object AMF {

  def init(): Promise[Any] = {
    amf.plugins.document.WebApi.register()
    amf.plugins.document.Vocabularies.register()
    amf.plugins.features.AMFValidation.register()
    amf.Core.init()
  }

  def raml10Parser(): Raml10Parser = new Raml10Parser()

  def raml10Generator(): Raml10Generator = new Raml10Generator()

  def oas20Parser(): Oas20Parser = new Oas20Parser()

  def oas20Generator(): Oas20Generator = new Oas20Generator()

  def amfGraphParser(): AmfGraphParser = new AmfGraphParser()

  def amfGraphGenerator(): AmfGraphGenerator = new AmfGraphGenerator()

  def validate(model: BaseUnit, profileName: String, messageStyle: String = "AMF"): Promise[AMFValidationReport] =
    Core.validate(model, profileName, messageStyle)

  def loadValidationProfile(url: String): Promise[String] = Core.loadValidationProfile(url)

  def registerNamespace(alias: String, prefix: String): Boolean = Core.registerNamespace(alias, prefix)

  def registerDialect(url: String): Promise[Dialect] = Vocabularies.registerDialect(url)

  def resolveRaml10(unit: BaseUnit) = new Raml10Resolver().resolve(unit)

  def resolveOas20(unit: BaseUnit) = new Oas20Resolver().resolve(unit)

  def resolveAmfGraph(unit: BaseUnit) = new AmfGraphResolver().resolve(unit)
}
