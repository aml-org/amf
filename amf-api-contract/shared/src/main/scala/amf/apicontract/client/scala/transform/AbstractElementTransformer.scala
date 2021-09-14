package amf.apicontract.client.scala.transform

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.internal.spec.common.transformation.ExtendsHelper
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.DataNode
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.YMapEntry

/**
  * Temporally object to respect new domain internfaces. Probably this will be agroupated at some Domain Element client logic.
  */
object AbstractElementTransformer {

  def asEndpoint[T <: BaseUnit](unit: T,
                                rt: ResourceType,
                                configuration: AMFGraphConfiguration,
                                profile: ProfileName = Raml10Profile,
                                errorHandler: AMFErrorHandler = UnhandledErrorHandler): EndPoint = {
    rt.linkTarget match {
      case Some(_) =>
        val resourceType = rt.effectiveLinkTarget().asInstanceOf[ResourceType]
        asEndpoint(unit, resourceType, configuration, profile, errorHandler)
      case _ =>
        Option(rt.dataNode)
          .map { dataNode =>
            val extendsHelper = ExtendsHelper(profile, keepEditingInfo = false, errorHandler)
            extendsHelper.asEndpoint(unit, dataNode, rt.annotations, rt.name.value(), rt.id, configuration)
          }
          .getOrElse(EndPoint())
    }
  }

  def entryAsEndpoint[T <: BaseUnit](unit: T,
                                     rt: ResourceType,
                                     node: DataNode,
                                     entry: YMapEntry,
                                     configuration: AMFGraphConfiguration,
                                     errorHandler: AMFErrorHandler = UnhandledErrorHandler,
                                     profile: ProfileName = Raml10Profile): EndPoint = {
    val helper = ExtendsHelper(profile, keepEditingInfo = false, errorHandler)
    helper.entryAsEndpoint(unit, node, rt.name.option().getOrElse(""), rt.id, entry, configuration)
  }

  /** Get this trait as an operation. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asOperation[T <: BaseUnit](unit: T,
                                 tr: Trait,
                                 configuration: AMFGraphConfiguration,
                                 profile: ProfileName = Raml10Profile,
                                 errorHandler: AMFErrorHandler = UnhandledErrorHandler): Operation = {
    tr.linkTarget match {
      case Some(_) =>
        val value = tr.effectiveLinkTarget().asInstanceOf[Trait]
        asOperation(unit, value, configuration, profile, errorHandler)
      case _ =>
        Option(tr.dataNode)
          .map { dataNode =>
            val extendsHelper = ExtendsHelper(profile, keepEditingInfo = false, errorHandler)
            extendsHelper.asOperation(
              dataNode,
              unit,
              tr.name.option().getOrElse(""),
              tr.annotations,
              tr.id,
              configuration
            )
          }
          .getOrElse(Operation())
    }
  }

  def entryAsOperation[T <: BaseUnit](unit: T,
                                      tr: Trait,
                                      entry: YMapEntry,
                                      configuration: AMFGraphConfiguration,
                                      profile: ProfileName = Raml10Profile,
                                      errorHandler: AMFErrorHandler = UnhandledErrorHandler,
                                      ): Operation = {
    val extendsHelper = ExtendsHelper(profile, keepEditingInfo = false, errorHandler)
    extendsHelper.entryAsOperation(unit, tr.name.option().getOrElse(""), tr.id, entry, configuration)
  }
}
