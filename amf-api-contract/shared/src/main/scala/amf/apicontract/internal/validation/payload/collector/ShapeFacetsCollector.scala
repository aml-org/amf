package amf.apicontract.internal.validation.payload.collector

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{Parameter, Payload, Request}
import amf.apicontract.internal.metamodel.domain.security.SecuritySchemeModel
import amf.apicontract.internal.metamodel.domain.{ParameterModel, PayloadModel, RequestModel}
import amf.core.client.scala.model.document.PayloadFragment
import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.vocabulary.Namespace
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.validation.ValidationCandidate
import amf.shapes.client.scala.domain.models.UnionShape

// RAML centric as it collects custom facets from the RAML "facets" node.
object ShapeFacetsCollector extends ValidationCandidateCollector {

  private val typesWithShapes = Set(
    PayloadModel.`type`.head.iri(),
    ParameterModel.`type`.head.iri(),
    RequestModel.`type`.head.iri(),
    SecuritySchemeModel.`type`.head.iri(),
    (Namespace.Document + "DomainProperty").iri(),
    ShapeModel.`type`.head.iri()
  )

  override def collect(element: AmfElement): Seq[ValidationCandidate] = {
    val shapeWithFacets: Option[(Shape, Seq[Shape#FacetsMap])] = findShapeWithFacets(element)
    shapeWithFacets match {
      case Some((shape: Shape, facetDefinitions: Seq[Shape#FacetsMap])) =>
        // TODO: we can think that reference resolution should already have run by now.
        val effectiveShape = shape match {
          case _ if shape.isLink && shape.linkTarget.isDefined => shape.linkTarget.get.asInstanceOf[Shape]
          case _                                               => shape
        }
        val facetsPayload = toFacetsPayload(effectiveShape)
        val facetsShape   = toFacetsDefinitionShape(effectiveShape, facetDefinitions)
        val fragment      = PayloadFragment(facetsPayload, "application/yaml")
        List(ValidationCandidate(facetsShape, fragment))
      case None => Nil
    }
  }

  protected def findShapeWithFacets(element: AmfElement): Option[(Shape, Seq[Shape#FacetsMap])] = {
    element match {
      case e: DomainElement if e.graph.types().exists(typesWithShapes.contains) =>
        collectCustomFacetsFromShape(e).filter {
          case (_: Shape, facetDefinitions: Seq[Shape#FacetsMap]) => facetDefinitions.exists(_.nonEmpty)
        }
      case _ => None
    }
  }

  private def collectCustomFacetsFromShape(element: DomainElement): Option[(Shape, Seq[Shape#FacetsMap])] = {
    element match {
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

    }
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
