package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.{NamedDomainElement, Shape}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.{ONE_OF_FIELD, ONE_OF_NAME}
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import org.mulesoft.antlrast.ast.Node

import scala.collection.mutable

case class GrpcOneOfParser(ast: Node)(implicit context: GrpcWebApiContext) extends GrpcASTParserHelper {
  private val ann: Annotations = toAnnotations(ast)
  private val base: AnyShape = AnyShape(ann)

  def parseAsProperty(setterFn: PropertyShape => Unit): PropertyShape = {
    innerParse()
    val property = PropertyShape(ann).withName(base.name.value(), ann)
    property.setWithoutId(PropertyShapeModel.Range, base, ann)
    setterFn(property)
    property
  }

  private def innerParse(): Unit = {
    parseName(base)
    parseMembers()
  }

  private def parseMembers(): Unit = {
    val members: mutable.Buffer[Shape] = mutable.Buffer()
    collect(ast, Seq(ONE_OF_FIELD)).map { case oneOfField: Node =>
      GrpcFieldParser(oneOfField).parse(property => {
        val ann = toAnnotations(oneOfField)
        val shape = NodeShape(ann)
        shape set (Seq(property), ann) as NodeShapeModel.Properties
        members.append(shape)
      })
    }
    base set (members, ann) as ShapeModel.Xone
  }

  protected def parseName(element: NamedDomainElement): Unit = withName(ast, ONE_OF_NAME, element)
}
