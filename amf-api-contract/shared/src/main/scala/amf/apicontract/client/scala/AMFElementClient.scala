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
import amf.core.internal.remote.Spec
import amf.shapes.client.scala.ShapesElementClient
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.client.scala.render.{JsonSchemaShapeRenderer, RamlShapeRenderer}
import org.yaml.model.{YMapEntry, YNode}

class AMFElementClient private[amf] (override protected val configuration: AMFConfiguration)
    extends ShapesElementClient(configuration) {

  override def getConfiguration: AMFConfiguration = configuration

  override def renderElement(element: DomainElement, references: Seq[BaseUnit] = Nil): YNode = {
    if (element.isInstanceOf[DialectDomainElement]) super.renderElement(element)
    else AMFElementRenderer.renderElement(element, configuration).asInstanceOf[SyamlParsedDocument].document.node
  }

  /** Get this resource type as an endpoint. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asEndpoint(unit: BaseUnit, rt: ResourceType, spec: Spec = Spec.RAML10): EndPoint =
    AbstractElementTransformer.asEndpoint(unit, rt, spec, configuration.errorHandlerProvider.errorHandler())

  /** Get this trait as an operation. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asOperation(unit: BaseUnit, tr: Trait, spec: Spec = Spec.RAML10): Operation =
    AbstractElementTransformer.asOperation(unit, tr, spec, configuration.errorHandlerProvider.errorHandler())

  def entryAsEndpoint(unit: BaseUnit,
                      rt: ResourceType,
                      node: DataNode,
                      entry: YMapEntry,
                      spec: Spec = Spec.RAML10): EndPoint =
    AbstractElementTransformer.entryAsEndpoint(unit,
                                               rt,
                                               node,
                                               entry,
                                               configuration.errorHandlerProvider.errorHandler(),
                                               spec)

  def entryAsOperation(unit: BaseUnit, tr: Trait, entry: YMapEntry, spec: Spec = Spec.RAML10): Operation =
    AbstractElementTransformer.entryAsOperation(unit,
                                                tr,
                                                entry,
                                                spec,
                                                configuration.errorHandlerProvider.errorHandler())
}
