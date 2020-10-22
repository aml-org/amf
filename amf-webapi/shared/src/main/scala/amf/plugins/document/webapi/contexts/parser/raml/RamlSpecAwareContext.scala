package amf.plugins.document.webapi.contexts.parser.raml

import amf.core.Root
import amf.core.model.domain.{DomainElement, Shape}
import amf.plugins.document.webapi.contexts.{SpecAwareContext, SpecVersionFactory}
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.raml.{Raml08DocumentParser, Raml10DocumentParser, RamlDocumentParser}
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.security.{SecurityScheme, WithSettings}
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait RamlSpecAwareContext extends SpecAwareContext {}

abstract class RamlSpecVersionFactory(implicit val ctx: RamlWebApiContext) extends SpecVersionFactory {

  override def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser =
    RamlSecuritySchemeParser.apply

  def securitySettingsParser: (YNode, String, DomainElement with WithSettings) => RamlSecuritySettingsParser

  def operationParser: (YMapEntry, String, Boolean) => RamlOperationParser = RamlOperationParser.apply

  def endPointParser
    : (YMapEntry, (String) => EndPoint, Option[EndPoint], ListBuffer[EndPoint], Boolean) => RamlEndpointParser

  def parameterParser: (YMapEntry, Parameter => Unit, Boolean) => RamlParameterParser

  def responseParser: (YMapEntry, Response => Unit, Boolean) => RamlResponseParser

  def requestParser: (YMap, () => Request, Boolean) => RamlRequestParser

  def documentParser: (Root) => RamlDocumentParser

  def typeParser: (YMapEntry, Shape => Unit, Boolean, DefaultType) => RamlTypeParser

  def payloadParser: (YMapEntry, String, Boolean) => RamlPayloadParser
}

class Raml10VersionFactory(implicit override val ctx: RamlWebApiContext) extends RamlSpecVersionFactory {

  override def endPointParser
    : (YMapEntry, String => EndPoint, Option[EndPoint], mutable.ListBuffer[EndPoint], Boolean) => RamlEndpointParser =
    Raml10EndpointParser.apply

  override def securitySettingsParser: (YNode, String, DomainElement with WithSettings) => RamlSecuritySettingsParser =
    (node, typeValue, element) => new Raml10SecuritySettingsParser(node, typeValue, element)

  override def parameterParser: (YMapEntry, Parameter => Unit, Boolean) => RamlParameterParser =
    Raml10ParameterParser.apply

  override def responseParser: (YMapEntry, Response => Unit, Boolean) => RamlResponseParser =
    Raml10ResponseParser.apply

  override def requestParser: (YMap, () => Request, Boolean) => RamlRequestParser = Raml10RequestParser.apply

  override def documentParser: (Root) => RamlDocumentParser = Raml10DocumentParser.apply

  override def typeParser: (YMapEntry, Shape => Unit, Boolean, DefaultType) => RamlTypeParser =
    (entry, f, isAnnotation, default) => Raml10TypeParser(entry, f, TypeInfo(isAnnotation = isAnnotation), default)

  override def payloadParser: (YMapEntry, String, Boolean) => RamlPayloadParser =
    Raml10PayloadParser.apply
}

class Raml08VersionFactory(implicit override val ctx: RamlWebApiContext) extends RamlSpecVersionFactory {

  override def endPointParser
    : (YMapEntry, String => EndPoint, Option[EndPoint], mutable.ListBuffer[EndPoint], Boolean) => RamlEndpointParser =
    Raml08EndpointParser.apply

  override def securitySettingsParser: (YNode, String, DomainElement with WithSettings) => RamlSecuritySettingsParser =
    RamlSecuritySettingsParser.apply

  override def parameterParser: (YMapEntry, (Parameter) => Unit, Boolean) => RamlParameterParser =
    Raml08ParameterParser.apply

  override def responseParser: (YMapEntry, Response => Unit, Boolean) => RamlResponseParser =
    Raml08ResponseParser.apply

  override def requestParser: (YMap, () => Request, Boolean) => RamlRequestParser = Raml08RequestParser.apply

  override def documentParser: (Root) => RamlDocumentParser = Raml08DocumentParser.apply

  override def typeParser: (YMapEntry, Shape => Unit, Boolean, DefaultType) => RamlTypeParser = Raml08TypeParser.apply

  override def payloadParser: (YMapEntry, String, Boolean) => RamlPayloadParser =
    Raml08PayloadParser.apply

}
