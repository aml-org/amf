package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.{FIELD_NAME, FIELD_OPTION, FIELD_OPTIONS_ELEMENTS}
import amf.shapes.client.scala.model.domain.UnresolvedShape
import org.mulesoft.antlrast.ast.Node

case class GrpcFieldParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  val ann: Annotations             = toAnnotations(ast)
  val propertyShape: PropertyShape = PropertyShape(ann)

  def parse(setterFn: PropertyShape => Unit = _ => ()): PropertyShape = {
    parseFieldName()
    setterFn(propertyShape)
    parseFieldNumber()
    parseFieldRange()
    parseOptions()
    propertyShape
  }

  def parseOptions(): Unit = {
    collect(ast, Seq(FIELD_OPTIONS_ELEMENTS, FIELD_OPTION)).foreach { case n: Node =>
      GrpcOptionParser(n).parse { ex =>
        val extensions = propertyShape.customDomainProperties :+ ex
        propertyShape.set(PropertyShapeModel.CustomDomainProperties, AmfArray(extensions, ann), ann)
      }
    }
  }

  def parseFieldName(): Unit = {
    withName(ast, FIELD_NAME, propertyShape)
  }

  def parseFieldNumber(): Unit = {
    parseFieldNumber(ast) match {
      case Some(order) => propertyShape.setWithoutId(PropertyShapeModel.SerializationOrder, AmfScalar(order, ann), ann)
      case None        => astError("missing Protobuf3 field number", propertyShape.annotations)
    }
  }

  def parseFieldRange(): Unit = {
    parseFieldRange(ast) match {
      case Some(range) => propertyShape.setWithoutId(PropertyShapeModel.Range, range, ann)
      case _           => astError("missing Protobuf3 field type", propertyShape.annotations)
    }
  }

}
