package amf.apicontract.internal.transformation.stages

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, FieldsFilter}
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject}
import amf.core.client.scala.transform.TransformationStep
import amf.core.client.scala.traversal.iterator.InstanceCollector
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDArray, JsonLDElement, JsonLDObject}
import org.mulesoft.common.client.lexical.PositionRange

class VirtualElementLexicalStage(val profile: ProfileName, val keepEditingInfo: Boolean)
    extends TransformationStep()
    with PlatformSecrets {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case d: Document =>
        d.iterator(fieldsFilter = FieldsFilter.All, visited = InstanceCollector())
          .toStream
          .filter(isVirtual)
          .foreach(addLexicalToVirtualElement)
        model
      case _ => model
    }
  }

  private def isVirtual(e: AmfElement): Boolean =
    e.annotations.isVirtual && e.annotations.lexical() == PositionRange.NONE

  private def addLexicalToVirtualElement(element: AmfElement): Unit = {
    val children: Seq[AmfElement] = getChildren(element)
    if (children.nonEmpty) {
      val virtualChildren = children.filter(isVirtual)
      virtualChildren.foreach(addLexicalToVirtualElement) // depth first search
      val ranges         = children.map(_.annotations.lexical())
      val effectiveRange = ranges.filterNot(_ == PositionRange.NONE).sortWith(_.start < _.start)
      if (effectiveRange.nonEmpty) {
        val range = PositionRange(effectiveRange.head.start, effectiveRange.last.end)
        element.annotations += LexicalInformation(range)
      }
    }
  }

  private def getChildren(element: AmfElement): Seq[AmfElement] = element match {
    case AmfArray(values, _)          => values
    case amfObject: AmfObject         => amfObject.fields.fields().map(_.value.value).toSeq
    case jsonLDElement: JsonLDElement => getJsonLDElementChildren(jsonLDElement)
    case _                            => Nil
  }

  private def getJsonLDElementChildren(jsonLDElement: JsonLDElement): Seq[AmfElement] = jsonLDElement match {
    case array: JsonLDArray            => array.values
    case JsonLDObject(fields, _, _, _) => fields.fields().map(_.value.value).toSeq
    case _                             => Nil
  }
}
