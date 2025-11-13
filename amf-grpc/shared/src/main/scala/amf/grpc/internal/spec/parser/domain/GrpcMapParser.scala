package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.{IDENTIFIER, KEY_TYPE, MAP_NAME}
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.{NodeShapeModel, ScalarShapeModel}
import org.mulesoft.antlrast.ast.{ASTNode, Node}

case class GrpcMapParser(ast: Node)(implicit ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  private val propertyMap: PropertyShape = PropertyShape(toAnnotations(ast))

  def parse(setterFn: PropertyShape => Unit): PropertyShape = {
    parseName()
    setterFn(propertyMap)
    parseFields()
    propertyMap
  }

  def parseName(): Unit = withName(ast, MAP_NAME, propertyMap)

  def parseFieldName(field: ASTNode): String = {
    path(field, Seq(MAP_NAME, IDENTIFIER)) match {
      case Some(f) =>
        withOptTerminal(f) {
          case Some(t) =>
            t.value
          case _ =>
            astError("missing mandatory Proto3 map field name", toAnnotations(field))
            ""
        }
      case _ =>
        astError("missing mandatory Proto3 map field name", toAnnotations(field))
        ""
    }
  }

  private def parseFields(): Any = {
    val ann           = toAnnotations(ast)
    val defaultScalar = ScalarShape(ann)
    defaultScalar set (XsdTypes.xsdString.iri(), ann) as ScalarShapeModel.DataType
    val range          = parseFieldRange(ast).getOrElse(defaultScalar)
    val key            = parseFieldRange(ast, KEY_TYPE).getOrElse(defaultScalar)
    val mapValueSchema = NodeShape(ann)
    mapValueSchema set range as NodeShapeModel.AdditionalPropertiesSchema
    mapValueSchema set key as NodeShapeModel.AdditionalPropertiesKeySchema

    val order = parseFieldNumber(ast).getOrElse(0)
    val name  = parseFieldName(ast)
    propertyMap.withName(name, ann)
    propertyMap.set(PropertyShapeModel.SerializationOrder, AmfScalar(order, ann), ann)
    propertyMap.set(PropertyShapeModel.Range, mapValueSchema, ann)
  }
}
