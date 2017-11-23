package amf.plugins.document.webapi.validation

import amf.framework.model.document.{BaseUnit, DeclaresModel}
import amf.domain.extensions.{CustomDomainProperty, DataNode, ObjectNode}
import amf.domain.security.SecurityScheme
import amf.domain.{Parameter, Payload, Request}
import amf.framework.validation.AMFValidationResult
import amf.metadata.domain.security.SecuritySchemeModel
import amf.metadata.domain.{ParameterModel, PayloadModel, RequestModel}
import amf.metadata.shape.ShapeModel
import amf.remote.Platform
import amf.shape.{NodeShape, Shape, UnionShape}
import amf.vocabulary.Namespace

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ShapeFacetsValidation(model: BaseUnit, platform: Platform) {

  def validate(): Future[Seq[AMFValidationResult]] = {
    val shapesWithFacets = findShapesWithFacets()
    val listResults = shapesWithFacets map { case (shape: Shape, facetDefinitons: Seq[Shape#FacetsMap]) =>
        validateFacets(shape, facetDefinitons)
    }

    // Finally we collect all the results
    Future.sequence(listResults).map(_.flatten)
  }

  protected def findShapesWithFacets(): Seq[(Shape, Seq[Shape#FacetsMap])] = {
    val typesWithShapes = Seq(
      PayloadModel.`type`.head.iri(),
      ParameterModel.`type`.head.iri(),
      RequestModel.`type`.head.iri(),
      SecuritySchemeModel.`type`.head.iri(),
      (Namespace.Document + "DomainProperty").iri()
    )
    val encodedShapes = model.findBy { domainElement =>
      domainElement.getTypeIds().exists(typesWithShapes.contains(_))
    }

    val declaredShapes: Seq[Shape] = model match {
      case withDeclarations: DeclaresModel =>
        withDeclarations.declares.filter(_.isInstanceOf[Shape]).map(_.asInstanceOf[Shape])
      case _ => Seq.empty
    }

    val elements = encodedShapes ++ declaredShapes

    elements map  {
      case payload: Payload =>
        Option(payload.schema) match {
          case Some(shape) =>
            Some((shape, shape.collectCustomShapePropertyDefinitions(onlyInherited = true)))
          case _ =>
            None
        }
      case parameter: Parameter =>
        Option(parameter.schema) match {
          case Some(shape) =>
            Some((shape, shape.collectCustomShapePropertyDefinitions(onlyInherited = true)))
          case _ =>
            None
        }
      case request: Request =>
        Option(request.queryString) match {
          case Some(shape) =>
            Some((shape, shape.collectCustomShapePropertyDefinitions(onlyInherited = true)))
          case _ =>
            None
        }

      case securityScheme: SecurityScheme =>
        Option(securityScheme.queryString) match {
          case Some(shape) =>
            Some((shape, shape.collectCustomShapePropertyDefinitions(onlyInherited = true)))
          case _ =>
            None
        }

      case customDomainProperty: CustomDomainProperty =>
        Option(customDomainProperty.schema) match {
          case Some(shape) =>
            Some((shape, shape.collectCustomShapePropertyDefinitions(onlyInherited = true)))
          case _ =>
            None
        }

      case shape: Shape => Some((shape, shape.collectCustomShapePropertyDefinitions(onlyInherited = true)))

      case _ => None

    } filter {
      case Some((shape: Shape, facetDefinitons: Seq[Shape#FacetsMap])) => facetDefinitons.nonEmpty
      case _ => false
    } map(_.get)
  }

  // Create a data node with the facets and a shape with the facet definitions.
  // Validate as a payload afterwards.
  protected def validateFacets(shape: Shape, facetDefinitons: Seq[Shape#FacetsMap]): Future[Seq[AMFValidationResult]] = {
    val facetsPayload = toFacetsPayload(shape)
    val facetsShape = toFacetsDefinitionShape(shape, facetDefinitons)
    PayloadValidation(platform, facetsShape).validate(facetsPayload) map { report =>
      if (report.conforms) {
        Seq.empty
      } else {
        report.results.map { result =>
          result.copy(targetProperty = Some(ShapeModel.CustomShapeProperties.value.iri()))
        }
      }
    }
  }


  // We collect the facet instances and build a data node with them
  protected def toFacetsPayload(shape: Shape): DataNode = {
    val payload = ObjectNode(shape.annotations).withId(shape.id)
    shape.customShapeProperties.foreach { extension =>
      payload.addProperty(extension.definedBy.name, extension.extension, extension.annotations)
    }
    payload
  }

  // Get all the facet definitions and create a shape only with those property shapes
  // This can be a single node or a union depending on if we have a single map or multiple facet maps.
  protected def toFacetsDefinitionShape(shape: Shape, facetDefinitions: Seq[Shape#FacetsMap]): Shape = {
    if (facetDefinitions.length == 1) {
      val facetsShape = NodeShape().withId(shape.id + "Shape")
      facetsShape.withProperties(facetDefinitions.head.values.toSeq)
    } else {
      val facetsShape = UnionShape().withId(shape.id + "Shape")
      var counter = 0
      val anyOfShapes = facetDefinitions.map { facetsMap =>
        val facetsUnionShape = NodeShape().withId(shape.id + "Shape" + counter)
        counter += 1
        facetsUnionShape.withProperties(facetsMap.values.toSeq)
      }
      facetsShape.withAnyOf(anyOfShapes)
    }
  }
}

object ShapeFacetsValidation {
  def apply(model: BaseUnit, platform: Platform) = new ShapeFacetsValidation(model, platform)
}