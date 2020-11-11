package amf.plugins.domain.webapi.resolution.stages

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.AmfObject
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.metamodel.common.ExamplesField
import amf.plugins.domain.shapes.models.{AnyShape, Example, ExampleTracking}
import amf.plugins.domain.webapi.models._
import amf._
import amf.plugins.domain.webapi.models.api.{Api, WebApi}

/**
  * Propagate examples defined in parameters and payloads onto their corresponding shape so they are validated
  * in the examples validation phase
  * Only necessary for OAS 3.0 spec
  */
class PayloadAndParameterResolutionStage(profile: ProfileName)(override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage() {

  private type SchemaContainerWithId = SchemaContainer with AmfObject

  override def resolve[T <: BaseUnit](model: T): T =
    if (appliesTo(profile)) resolveExamples(model).asInstanceOf[T]
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

class RamlCompatiblePayloadAndParameterResolutionStage(profile: ProfileName)(implicit errorHandler: ErrorHandler)
    extends PayloadAndParameterResolutionStage(profile)(errorHandler) {

  override protected def appliesTo(profile: ProfileName): Boolean = profile match {
    case RamlProfile | Raml10Profile | Raml08Profile => true
    case _                                           => false
  }

  override protected def addExampleToShape(shape: AnyShape, example: Example): Unit =
    shape.effectiveLinkTarget() match {
      case linkedShape: AnyShape => super.addExampleToShape(linkedShape, example)
      case _                     => // ignore
    }
}
