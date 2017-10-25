package amf.spec.domain

import amf.domain.{Annotations, CreativeWork, Operation, Response}
import amf.metadata.domain.OperationModel.Method
import amf.metadata.domain.{DomainElementModel, OperationModel}
import amf.model.AmfArray
import amf.parser.{YMapOps, YValueOps}
import amf.spec.Declarations
import amf.spec.common.{AnnotationParser, ArrayNode, ValueNode}
import amf.spec.declaration.OasCreativeWorkParser
import amf.spec.raml.RamlSyntax
import org.yaml.model.{YMap, YMapEntry, YScalar}

import scala.collection.mutable

/**
  *
  */
case class RamlOperationParser(entry: YMapEntry, producer: (String) => Operation, declarations: Declarations)
    extends RamlSyntax {

  def parse(): Operation = {

    val method = entry.key.value.toScalar.text

    val operation = producer(method).add(Annotations(entry))
    operation.set(Method, ValueNode(entry.key).string())

    entry.value.value match {
      // Empty operation
      case s: YScalar if s.text == "" => operation

      // Regular operation
      case map: YMap =>
        validateClosedShape(operation.id, map, "operation")

        map.key("displayName", entry => {
          val value = ValueNode(entry.value)
          operation.set(OperationModel.Name, value.string(), Annotations(entry))
        })

        map.key("description", entry => {
          val value = ValueNode(entry.value)
          operation.set(OperationModel.Description, value.string(), Annotations(entry))
        })

        map.key("(deprecated)", entry => {
          val value = ValueNode(entry.value)
          operation.set(OperationModel.Deprecated, value.boolean(), Annotations(entry))
        })

        map.key("(summary)", entry => {
          val value = ValueNode(entry.value)
          operation.set(OperationModel.Summary, value.string(), Annotations(entry))
        })

        map.key(
          "(externalDocs)",
          entry => {
            val creativeWork: CreativeWork = OasCreativeWorkParser(entry.value.value.toMap).parse()
            operation.set(OperationModel.Documentation, creativeWork, Annotations(entry))
          }
        )

        map.key(
          "protocols",
          entry => {
            val value = ArrayNode(entry.value.value.toSequence)
            operation.set(OperationModel.Schemes, value.strings(), Annotations(entry))
          }
        )

        map.key("(consumes)", entry => {
          val value = ArrayNode(entry.value.value.toSequence)
          operation.set(OperationModel.Accepts, value.strings(), Annotations(entry))
        })

        map.key("(produces)", entry => {
          val value = ArrayNode(entry.value.value.toSequence)
          operation.set(OperationModel.ContentType, value.strings(), Annotations(entry))
        })

        map.key(
          "is",
          entry => {
            val traits = entry.value.value.toSequence.nodes.map(value => {
              ParametrizedDeclarationParser(value.value, operation.withTrait, declarations.traits).parse()
            })
            if (traits.nonEmpty) operation.setArray(DomainElementModel.Extends, traits, Annotations(entry))
          }
        )

        RamlRequestParser(map, () => operation.withRequest(), declarations)
          .parse()
          .map(operation.set(OperationModel.Request, _))

        map.key(
          "responses",
          entry => {
            entry.value.value.toMap.regex(
              "\\d{3}",
              entries => {
                val responses = mutable.ListBuffer[Response]()
                entries.foreach(entry => {
                  responses += RamlResponseParser(entry, operation.withResponse, declarations).parse()
                })
                operation.set(OperationModel.Responses,
                              AmfArray(responses, Annotations(entry.value)),
                              Annotations(entry))
              }
            )
          }
        )

        map.key(
          "securedBy",
          entry => {
            // TODO check for empty array for resolution ?
            val securedBy = entry.value.value.toSequence.nodes
              .map(s => RamlParametrizedSecuritySchemeParser(s, operation.withSecurity, declarations).parse())

            operation.set(OperationModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
          }
        )

        AnnotationParser(() => operation, map).parse()

        operation
    }
  }
}
