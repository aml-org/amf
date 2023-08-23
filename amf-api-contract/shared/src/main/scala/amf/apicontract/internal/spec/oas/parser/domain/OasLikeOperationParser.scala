package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.Operation
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.metamodel.domain.OperationModel.Method
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.DuplicatedOperationId
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.shapes.internal.spec.common.parser.{AnnotationParser, OasLikeCreativeWorkParser}
import org.yaml.model._

abstract class OasLikeOperationParser(entry: YMapEntry, adopt: Operation => Operation)(implicit
    val ctx: OasLikeWebApiContext
) extends SpecParserOps {

  protected def entryKey: AmfScalar = ScalarNode(entry.key).string()

  protected val closedShapeName = "operation"

  def parse(): Operation = {
    val operation: Operation = Operation(Annotations(entry))
    operation.setWithoutId(Method, entryKey, Annotations.inferred())
    adopt(operation)

    val map = entry.value.as[YMap]

    ctx.closedShape(operation, map, closedShapeName)

    map.key("operationId").foreach { entry =>
      val operationId = entry.value.toString()
      if (!ctx.registerOperationId(operationId))
        ctx.eh
          .violation(DuplicatedOperationId, operation, s"Duplicated operation id '$operationId'", entry.value.location)
    }

    parseOperationId(map, operation)

    map.key("description", OperationModel.Description in operation)
    map.key("summary", OperationModel.Summary in operation)
    map.key(
      "externalDocs",
      OperationModel.Documentation in operation using (OasLikeCreativeWorkParser.parse(_, operation.id))
    )

    AnnotationParser(operation, map).parseOrphanNode("responses")
    AnnotationParser(operation, map).parse()

    operation
  }

  def parseOperationId(map: YMap, operation: Operation): Unit = {
    map.key("operationId", OperationModel.Name in operation)
    map.key("operationId", OperationModel.OperationId in operation)
  }
}
