package amf.plugins.document.webapi.validation

import amf.core.model.document.{BaseUnit, DeclaresModel, PayloadFragment}
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{DataNode, ObjectNode, Shape}
import amf.core.remote.Platform
import amf.core.validation.ValidationCandidate
import amf.core.vocabulary.Namespace
import amf.plugins.domain.shapes.models.{NodeShape, UnionShape}
import amf.plugins.domain.webapi.metamodel.security.SecuritySchemeModel
import amf.plugins.domain.webapi.metamodel.{ParameterModel, PayloadModel, RequestModel}
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.{Parameter, Payload, Request}

class ShapeFacetsCandidatesCollector(model: BaseUnit, platform: Platform) {

  def validate(): Future[Seq[AMFValidationResult]] = {
    val shapesWithFacets = findShapesWithFacets()
    shapesWithFacets map {
      case (shape, facetDefinitons) =>
        val effectiveShape = shape match {
          case _ if shape.isLink && shape.linkTarget.isDefined => shape.linkTarget.get.asInstanceOf[Shape]
          case _                                               => shape
        }
        val facetsPayload = toFacetsPayload(effectiveShape)
        val facetsShape   = toFacetsDefinitionShape(effectiveShape, facetDefinitons)
        val fragment      = PayloadFragment(facetsPayload, "application/yaml")
        ValidationCandidate(facetsShape, fragment)
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

    elements map {
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
      case _                                                           => false
    } map (_.get)
  }

  // We collect the facet instances and build a data node with them
  protected def toFacetsPayload(shape: Shape): DataNode = {
    val payload = ObjectNode(shape.annotations).withId(shape.id)
    shape.customShapeProperties.foreach { extension =>
      payload.addProperty(extension.definedBy.name.value(), extension.extension, extension.annotations)
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
      var counter     = 0
      val anyOfShapes = facetDefinitions.map { facetsMap =>
        val facetsUnionShape = NodeShape().withId(shape.id + "Shape" + counter)
        counter += 1
        facetsUnionShape.withProperties(facetsMap.values.toSeq)
      }
      facetsShape.withAnyOf(anyOfShapes)
    }
  }
}

object ShapeFacetsCandidatesCollector {
  def apply(model: BaseUnit, platform: Platform): Seq[ValidationCandidate] =
    new ShapeFacetsCandidatesCollector(model, platform).collect()
}
