package amf.graphqlfederation.internal.spec.transformation

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.federation.ShapeFederationMetadata
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel.{FederationMetadata, IsStub}
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel.{Provides, Requires}
import amf.graphqlfederation.internal.spec.transformation.introspection.directives.{DomainExtensionSetter, FederationDirectiveApplicationsBuilder, FederationDirectiveDeclarations}
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape, UnionShape}
import amf.shapes.internal.domain.metamodel.NodeShapeModel.Keys

case class FederationModelToDomainExtension() extends TransformationStep {

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case doc: Document =>
        val declarations = FederationDirectiveDeclarations.extractFrom(doc)
        val builder      = FederationDirectiveApplicationsBuilder(declarations)
        val setExtension = DomainExtensionSetter(builder)

        propagateExtensions(doc, setExtension)

        doc.declares.foreach {
          case node: NodeShape =>
            setExtension
              .fromKeysIn(node)
              .fromShareableIn(node)
              .fromInaccessibleIn(node)

            node.properties.foreach { prop =>
              setExtension
                .fromProvidesIn(prop)
                .fromRequiresIn(prop)
                .fromOverrideIn(prop)
                .fromInaccessibleIn(prop)
                .fromShareableIn(prop)
                .fromExternalIn(prop)
              removeFields(prop)(Provides, Requires, IsStub, FederationMetadata)
            }

            node.operations.foreach { operation =>
              setExtension.fromInaccessibleIn(operation)

              operation.requests.flatMap(_.queryParameters).foreach { parameter =>
                setExtension.fromInaccessibleIn(parameter)
                removeFields(parameter)(FederationMetadata)
              }

              removeFields(operation)(FederationMetadata)
            }

            removeFields(node)(IsStub, Keys, FederationMetadata)

          case scalar: ScalarShape =>
            setExtension.fromInaccessibleIn(scalar)

            scalar.values.foreach { value =>
              setExtension.fromInaccessibleIn(value)
              removeFields(value)(FederationMetadata)
            }

            removeFields(scalar)(FederationMetadata)

          case union: UnionShape =>
            setExtension.fromInaccessibleIn(union)
            removeFields(union)(FederationMetadata)

          case cdp: CustomDomainProperty =>
            cdp.schema match {
              case n: NodeShape =>
                n.properties.foreach { prop =>
                  setExtension.fromInaccessibleIn(prop)
                  removeFields(prop)(FederationMetadata)
                }
              case _ => // skip
            }

          case _ => // skip
        }

      case _ => // skip
    }
    model
  }

  private def propagateExtensions(doc: Document, setExtension: DomainExtensionSetter): Unit = {
    doc.encodes match {
      case api: Api => api.endPoints.foreach { endpoint =>
        endpoint.operations.foreach { operation =>
          operation.requests.foreach { request =>
            request.queryParameters.foreach { parameter =>
              propagateForQueryParameters(parameter, setExtension)
            }
          }
        }
      }
    }
  }

  private def propagateForQueryParameters(parameter: Parameter, setExtension: DomainExtensionSetter): Unit = {
    setExtension
      .fromInaccessibleIn(parameter)
  }

  private def removeFields(e: DomainElement)(fields: Field*): Unit = fields.foreach(f => e.fields.removeField(f))

}
