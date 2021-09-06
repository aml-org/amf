package amf.apicontract.client.scala.transform

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.internal.spec.common.transformation.ExtendsHelper
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.DataNode
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Spec
import org.yaml.model.YMapEntry

/**
  * Temporally object to respect new domain internfaces. Probably this will be agroupated at some Domain Element client logic.
  */
object AbstractElementTransformer {

  def asEndpoint(unit: BaseUnit,
                 rt: ResourceType,
                 spec: Spec = Spec.RAML10,
                 errorHandler: AMFErrorHandler = UnhandledErrorHandler): EndPoint = {
    rt.linkTarget match {
      case Some(_) =>
        val resourceType = rt.effectiveLinkTarget().asInstanceOf[ResourceType]
        asEndpoint(unit, resourceType, spec, errorHandler)
      case _ =>
        Option(rt.dataNode)
          .map { dataNode =>
            val extendsHelper = ExtendsHelper(spec, keepEditingInfo = false, errorHandler)
            extendsHelper.asEndpoint(unit, dataNode, rt.annotations, rt.name.value(), rt.id)
          }
          .getOrElse(EndPoint())
    }
  }

  def entryAsEndpoint(unit: BaseUnit,
                      rt: ResourceType,
                      node: DataNode,
                      entry: YMapEntry,
                      errorHandler: AMFErrorHandler = UnhandledErrorHandler,
                      spec: Spec = Spec.RAML10): EndPoint = {
    val helper = ExtendsHelper(spec, keepEditingInfo = false, errorHandler)
    helper.entryAsEndpoint(unit, node, rt.name.option().getOrElse(""), rt.id, entry)
  }

  /** Get this trait as an operation. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asOperation(unit: BaseUnit,
                  tr: Trait,
                  spec: Spec = Spec.RAML10,
                  errorHandler: AMFErrorHandler = UnhandledErrorHandler): Operation = {
    tr.linkTarget match {
      case Some(_) =>
        val value = tr.effectiveLinkTarget().asInstanceOf[Trait]
        asOperation(unit, value, spec, errorHandler)
      case _ =>
        Option(tr.dataNode)
          .map { dataNode =>
            val extendsHelper = ExtendsHelper(spec, keepEditingInfo = false, errorHandler)
            extendsHelper.asOperation(
              dataNode,
              unit,
              tr.name.option().getOrElse(""),
              tr.annotations,
              tr.id
            )
          }
          .getOrElse(Operation())
    }
  }

  def entryAsOperation[T <: BaseUnit](unit: T,
                                      tr: Trait,
                                      entry: YMapEntry,
                                      spec: Spec = Spec.RAML10,
                                      errorHandler: AMFErrorHandler = UnhandledErrorHandler): Operation = {
    val extendsHelper = ExtendsHelper(spec, keepEditingInfo = false, errorHandler)
    extendsHelper.entryAsOperation(unit, tr.name.option().getOrElse(""), tr.id, entry)
  }
}
