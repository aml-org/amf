package amf.apicontract.internal.transformation.compatibility.oas

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.{Operation, Payload}
import amf.apicontract.internal.annotations.FormBodyParameter
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{Shape, AmfScalar}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.shapes.client.scala.model.domain.{FileShape, NodeShape}
import amf.core.internal.parser.domain.Annotations

/** To represent a method with file upload:
  *
  * in RAML: \- You define a body with multipart/form-data and a property of type: file \- You can specify acceptable
  * file types directly with fileTypes: ['application/xml']
  *
  * In OAS 2.0 you need a parameter of type: file and in: formData with a consumes: - multipart/form-data, to correctly
  * emit this the FormBodyParameter annotation is needed
  */
case class FixFilePayloads() extends TransformationStep() {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[WebApi] => fixFields(doc.encodes.asInstanceOf[WebApi])
      case _                                                 => // ignore
    }
    model
  }

  private val fileTypes = Seq("multipart/form-data", "application/x-www-form-urlencoded")

  private def fixFields(api: WebApi): Unit = {
    api.endPoints.foreach { endPoint =>
      endPoint.operations.foreach { operation =>
        if (hasFileContentTypes(operation)) {
          val requestPayloads  = operation.requests.flatMap(_.payloads)
          val responsePayloads = operation.responses.flatMap(_.payloads)
          val payloads         = requestPayloads ++ responsePayloads
          payloads.foreach(checkFormData)
          payloads.map(_.schema).foreach(checkSchemaParameters)
        }
      }
    }
  }

  /** in RAML you can declare multiple payload body schemas in the same operation in OAS 2.o in particular if you have a
    * file upload (in: formData, type: file) you can't have another in:body that's why we only transform the payload if
    * it only has a file content type and nothing else
    */
  private def hasFileContentTypes(operation: Operation): Boolean = {
    val contentTypes = operation.accepts.map(_.value())
    contentTypes.nonEmpty && contentTypes.forall(fileTypes.contains)
  }

  /** if the schema has a file parameter, add the FormBodyParameter annotation to the payload to emit it correctly (in
    * PayloadAsParameterEmitter class)
    */
  private def checkFormData(payload: Payload): Unit = payload.schema match {
    case ns: NodeShape =>
      val ranges       = ns.properties.map(_.range)
      val hasFileParam = ranges.size == 1 && ranges.exists(_.isInstanceOf[FileShape])
      if (hasFileParam) {
        payload.set(NameFieldSchema.Name, AmfScalar("formData"), Annotations.inferred())
        payload.add(FormBodyParameter())
        // in RAML you can set name and displayName property but in OAS you only have name, we remove displayName
        ns.properties.foreach(p => p.range.fields.removeField(ShapeModel.DisplayName))
      }
    case _ => // ignore
  }

  /** in RAML you can create parameters with different names but same schemas, in OAS 2.0 emission we use the schema
    * name as name, so we need to duplicate the schema and change its name if we find this case
    */
  private def checkSchemaParameters(s: Shape): Unit = s match {
    case ns: NodeShape if hasDuplicatedRanges(ns.properties) => normalizeRanges(ns.properties)
    case _                                                   => // ignore
  }

  private def hasDuplicatedRanges(props: Seq[PropertyShape]): Boolean = {
    val ranges = props.map(_.range).map(_.name.value())
    ranges.distinct.length != ranges.length
  }

  private def normalizeRanges(props: Seq[PropertyShape]): Unit = {
    props.foreach { prop =>
      val newRange = prop.range
        .copyShape()
        .set(NameFieldSchema.Name, AmfScalar(prop.name.value()), Annotations.inferred())
      prop.withRange(newRange)
    }
  }
}
