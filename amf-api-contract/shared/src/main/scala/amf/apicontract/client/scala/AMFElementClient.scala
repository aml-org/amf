package amf.apicontract.client.scala

import amf.aml.client.scala.AMLElementClient
import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.client.scala.transform.AbstractElementTransformer
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DataNode, DomainElement}
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.client.scala.render.AMFElementRenderer
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.client.scala.render.{JsonSchemaShapeRenderer, RamlShapeRenderer}
import org.yaml.model.{YMapEntry, YNode}

class AMFElementClient private[amf] (override protected val configuration: AMFConfiguration)
    extends AMLElementClient(configuration) {

  override def getConfiguration: AMFConfiguration = configuration

  def toJsonSchema(element: AnyShape): String    = JsonSchemaShapeRenderer.toJsonSchema(element, configuration)
  def buildJsonSchema(element: AnyShape): String = JsonSchemaShapeRenderer.buildJsonSchema(element, configuration)

  def toRamlDatatype(element: AnyShape): String = RamlShapeRenderer.toRamlDatatype(element, configuration)

  override def renderElement(element: DomainElement, references: Seq[BaseUnit] = Nil): YNode = {
    if (element.isInstanceOf[DialectDomainElement]) super.renderElement(element)
    else AMFElementRenderer.renderElement(element, configuration).asInstanceOf[SyamlParsedDocument].document.node
  }

  /** Get this resource type as an endpoint. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asEndpoint[T <: BaseUnit](unit: T, rt: ResourceType, profile: ProfileName = Raml10Profile): EndPoint =
    AbstractElementTransformer.asEndpoint(unit, rt, profile, configuration.errorHandlerProvider.errorHandler())

  /** Get this trait as an operation. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asOperation[T <: BaseUnit](unit: T, tr: Trait, profile: ProfileName = Raml10Profile): Operation =
    AbstractElementTransformer.asOperation(unit, tr, profile, configuration.errorHandlerProvider.errorHandler())

  def entryAsEndpoint[T <: BaseUnit](unit: T,
                                     rt: ResourceType,
                                     node: DataNode,
                                     entry: YMapEntry,
                                     profile: ProfileName = Raml10Profile): EndPoint =
    AbstractElementTransformer.entryAsEndpoint(unit,
                                               rt,
                                               node,
                                               entry,
                                               configuration.errorHandlerProvider.errorHandler(),
                                               profile)

  def entryAsOperation[T <: BaseUnit](unit: T,
                                      tr: Trait,
                                      entry: YMapEntry,
                                      profile: ProfileName = Raml10Profile): Operation =
    AbstractElementTransformer.entryAsOperation(unit,
                                                tr,
                                                entry,
                                                profile,
                                                configuration.errorHandlerProvider.errorHandler())
}
