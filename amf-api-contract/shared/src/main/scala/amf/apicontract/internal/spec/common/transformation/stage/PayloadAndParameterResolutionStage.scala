package amf.apicontract.internal.spec.common.transformation.stage

import amf.apicontract.client.scala.model.domain._
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.common.validation._
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.transform.stages.TransformationStep
import amf.shapes.client.scala.domain.models.{AnyShape, Example, ExampleTracking}
import amf.shapes.internal.domain.metamodel.common.ExamplesField

/**
  * Propagate examples defined in parameters and payloads onto their corresponding shape so they are validated
  * in the examples validation phase
  * Only necessary for OAS 3.0 spec
  */
class PayloadAndParameterResolutionStage(profile: ProfileName) extends TransformationStep() {

  private type SchemaContainerWithId = SchemaContainer with AmfObject

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit =
    if (appliesTo(profile)) resolveExamples(model)
    else model

  protected def appliesTo(profile: ProfileName) = profile match {
    case Oas30Profile | AmfProfile => true
    case _                         => false
  }

  def resolveExamples(model: BaseUnit): BaseUnit = {
    model match {
      case doc: Document if doc.encodes.isInstanceOf[Api] =>
        val webApiContainers   = searchPayloadAndParams(doc.encodes.asInstanceOf[Api])
        val declaredContainers = searchDeclarations(doc)
        (webApiContainers ++ declaredContainers).foreach(setExamplesInSchema)
      case _ =>
    }
    model
  }

  private def searchDeclarations(doc: Document): Seq[SchemaContainerWithId] =
    doc.declares.collect {
      case param: Parameter => param.payloads :+ param
      case req: Request     => req.payloads
      case res: Response    => res.payloads
    }.flatten

  def searchPayloadAndParams(baseApi: Api): Seq[SchemaContainerWithId] = {
    baseApi.endPoints.flatMap { endpoint =>
      val paramSchemas    = endpoint.parameters.flatMap(_.payloads)
      val endpointSchemas = traverseEndpoint(endpoint)
      paramSchemas ++ endpointSchemas ++ endpoint.parameters
    }
  }

  private def traverseEndpoint(endpoint: EndPoint): Seq[SchemaContainerWithId] = {
    endpoint.operations.flatMap { op =>
      val responseSchemas = op.responses.flatMap(_.payloads)
      val requestSchemas = Option(op.request) match {
        case Some(req) => reqSchemas(req)
        case None      => Nil
      }
      responseSchemas ++ requestSchemas
    }
  }

  private def reqSchemas(req: Request): Seq[SchemaContainerWithId] = {
    val reqParams = req.uriParameters ++ req.queryParameters ++ req.cookieParameters
    req.payloads ++ reqParams.flatMap(_.payloads) ++ reqParams
  }

  private def setExamplesInSchema(container: SchemaContainerWithId): Unit =
    container.schema match {
      case shape: AnyShape =>
        container.examples.foreach { example =>
          if (!shape.examples.exists(_.id == example.id)) {
            example.add(ExampleTracking.tracked(container.id, example, None))
            addExampleToShape(shape, example)
            container.removeExamples()
          }
        }
      case _ =>
    }

  protected def addExampleToShape(shape: AnyShape, example: Example): Unit = {
    shape.setArrayWithoutId(ExamplesField.Examples, shape.examples ++ Seq(example))
  }
}

class RamlCompatiblePayloadAndParameterResolutionStage(profile: ProfileName)
    extends PayloadAndParameterResolutionStage(profile) {

  override protected def appliesTo(profile: ProfileName): Boolean = profile match {
    case Raml10Profile | Raml08Profile => true
    case _                             => false
  }

  override protected def addExampleToShape(shape: AnyShape, example: Example): Unit =
    shape.effectiveLinkTarget() match {
      case linkedShape: AnyShape => super.addExampleToShape(linkedShape, example)
      case _                     => // ignore
    }
}
