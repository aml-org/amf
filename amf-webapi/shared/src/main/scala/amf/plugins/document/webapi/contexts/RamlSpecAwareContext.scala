package amf.plugins.document.webapi.contexts

import amf.core.Root
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.raml.{Raml08DocumentParser, Raml10DocumentParser, RamlDocumentParser}
import amf.plugins.domain.webapi.models._
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait RamlSpecAwareContext extends SpecAwareContext {}

trait SpecVersionFactory {}

trait RamlSpecVersionFactory extends SpecVersionFactory {

  def operationParser: (YMapEntry, (String) => Operation, Boolean) => RamlOperationParser

  def endPointParser
    : (YMapEntry, (String) => EndPoint, Option[EndPoint], ListBuffer[EndPoint], Boolean) => RamlEndpointParser
  def parameterParser: (YMapEntry, String => Parameter) => RamlParameterParser

  def responseParser: (YMapEntry, (String) => Response) => RamlResponseParser
  def requestParser: (YMap, () => Request) => RamlRequestParser

  def documentParser: (Root) => RamlDocumentParser
}

class Raml10VersionFactory(implicit val ctx: RamlWebApiContext) extends RamlSpecVersionFactory {

  override def operationParser: (YMapEntry, (String) => Operation, Boolean) => RamlOperationParser =
    Raml10OperationParser.apply

  override def endPointParser
    : (YMapEntry, String => EndPoint, Option[EndPoint], mutable.ListBuffer[EndPoint], Boolean) => RamlEndpointParser =
    Raml10EndpointParser.apply

  override def parameterParser: (YMapEntry, (String) => Parameter) => RamlParameterParser = Raml10ParameterParser.apply

  override def responseParser: (YMapEntry, (String) => Response) => RamlResponseParser = Raml10ResponseParser.apply

  override def requestParser: (YMap, () => Request) => RamlRequestParser = Raml10RequestParser.apply

  override def documentParser: (Root) => RamlDocumentParser = Raml10DocumentParser.apply
}

class Raml08VersionFactory(implicit val ctx: RamlWebApiContext) extends RamlSpecVersionFactory {

  override def operationParser: (YMapEntry, (String) => Operation, Boolean) => RamlOperationParser =
    Raml08OperationParser.apply

  override def endPointParser
    : (YMapEntry, String => EndPoint, Option[EndPoint], mutable.ListBuffer[EndPoint], Boolean) => RamlEndpointParser =
    Raml08EndpointParser.apply

  override def parameterParser: (YMapEntry, (String) => Parameter) => RamlParameterParser = Raml08ParameterParser.apply

  override def responseParser: (YMapEntry, (String) => Response) => RamlResponseParser = Raml08ResponseParser.apply

  override def requestParser: (YMap, () => Request) => RamlRequestParser = Raml08RequestParser.apply

  override def documentParser: (Root) => RamlDocumentParser = Raml08DocumentParser.apply
}
