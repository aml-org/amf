package amf.plugins.document.webapi.parser.spec.common.emitters.factory

import amf.core.emitter.PartEmitter
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.remote.Vendor
import amf.plugins.domain.shapes.models.{CreativeWork, Example}
import amf.plugins.domain.webapi.models._
import amf.plugins.domain.webapi.models.bindings.{ChannelBindings, MessageBindings, OperationBindings, ServerBindings}
import amf.plugins.domain.webapi.models.security.{ParametrizedSecurityScheme, SecurityRequirement, SecurityScheme}
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}

trait DomainElementEmitterFactory {

  def emitter(e: DomainElement): Option[PartEmitter] = e match {
    case s: Shape                      => typeEmitter(s)
    case e: Response                   => responseEmitter(e)
    case p: Parameter                  => parameterEmitter(p)
    case p: Payload                    => payloadEmitter(p)
    case e: Example                    => exampleEmitter(e)
    case t: TemplatedLink              => templatedLinkEmitter(t)
    case c: Callback                   => callbackEmitter(c)
    case r: Request                    => requestEmitter(r)
    case m: Message                    => messageEmitter(m)
    case i: CorrelationId              => correlationIdEmitter(i)
    case m: MessageBindings            => messageBindingsEmitter(m)
    case o: OperationBindings          => operationBindingsEmitter(o)
    case c: ChannelBindings            => channelBindingsEmitter(c)
    case s: ServerBindings             => serverBindingsEmitter(s)
    case t: Trait                      => traitEmitter(t)
    case r: ResourceType               => resourceTypeEmitter(r)
    case o: Operation                  => operationEmitter(o)
    case s: SecurityScheme             => securitySchemeEmitter(s)
    case s: ParametrizedSecurityScheme => parametrizedSecuritySchemeEmitter(s)
    case s: SecurityRequirement        => securityRequirementEmitter(s)
    case c: CreativeWork               => creativeWorkEmitter(c)
    case e: EndPoint                   => endpointEmitter(e)
    case s: Server                     => serverEmitter(s)
    case l: License                    => licenseEmitter(l)
    case o: Organization               => organizationEmitter(o)
    case t: Tag                        => tagEmitter(t)
    case c: CustomDomainProperty       => customDomainPropertyEmitter(c)
    case _                             => None
  }

  def typeEmitter(s: Shape): Option[PartEmitter]                                            = None
  def responseEmitter(e: Response): Option[PartEmitter]                                     = None
  def parameterEmitter(p: Parameter): Option[PartEmitter]                                   = None
  def payloadEmitter(p: Payload): Option[PartEmitter]                                       = None
  def exampleEmitter(example: Example): Option[PartEmitter]                                 = None
  def templatedLinkEmitter(link: TemplatedLink): Option[PartEmitter]                        = None
  def callbackEmitter(callback: Callback): Option[PartEmitter]                              = None
  def requestEmitter(r: Request): Option[PartEmitter]                                       = None
  def messageEmitter(m: Message): Option[PartEmitter]                                       = None
  def correlationIdEmitter(i: CorrelationId): Option[PartEmitter]                           = None
  def messageBindingsEmitter(m: MessageBindings): Option[PartEmitter]                       = None
  def operationBindingsEmitter(o: OperationBindings): Option[PartEmitter]                   = None
  def channelBindingsEmitter(c: ChannelBindings): Option[PartEmitter]                       = None
  def serverBindingsEmitter(s: ServerBindings): Option[PartEmitter]                         = None
  def traitEmitter(t: Trait): Option[PartEmitter]                                           = None
  def resourceTypeEmitter(t: ResourceType): Option[PartEmitter]                             = None
  def operationEmitter(o: Operation): Option[PartEmitter]                                   = None
  def securitySchemeEmitter(s: SecurityScheme): Option[PartEmitter]                         = None
  def parametrizedSecuritySchemeEmitter(s: ParametrizedSecurityScheme): Option[PartEmitter] = None
  def securityRequirementEmitter(s: SecurityRequirement): Option[PartEmitter]               = None
  def creativeWorkEmitter(c: CreativeWork): Option[PartEmitter]                             = None
  def endpointEmitter(e: EndPoint): Option[PartEmitter]                                     = None
  def serverEmitter(s: Server): Option[PartEmitter]                                         = None
  def licenseEmitter(l: License): Option[PartEmitter]                                       = None
  def organizationEmitter(o: Organization): Option[PartEmitter]                             = None
  def tagEmitter(t: Tag): Option[PartEmitter]                                               = None
  def customDomainPropertyEmitter(t: CustomDomainProperty): Option[PartEmitter]             = None
}

object DomainElementEmitterFactory {
  def apply(vendor: Vendor, eh: ErrorHandler): Option[DomainElementEmitterFactory] = vendor match {
    case Vendor.RAML08  => Some(Raml08EmitterFactory(eh))
    case Vendor.RAML10  => Some(Raml10EmitterFactory(eh))
    case Vendor.OAS20   => Some(Oas20EmitterFactory(eh))
    case Vendor.OAS30   => Some(Oas30EmitterFactory(eh))
    case Vendor.ASYNC20 => Some(AsyncEmitterFactory(eh))
    case _              => None
  }
}
