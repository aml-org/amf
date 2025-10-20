package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.{NamedDomainElement, Shape}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.{ONE_OF_FIELD, ONE_OF_NAME}
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import org.mulesoft.antlrast.ast.Node

import scala.collection.mutable

case class GrpcOneOfParser(ast: Node)(implicit context: GrpcWebApiContext) extends GrpcASTParserHelper {

  private val base: AnyShape = AnyShape(toAnnotations(ast))

//  def parse(setterFn: AnyShape => Unit): AnyShape = {
//    innerParse()
//    setterFn(base)
//    base
//  }

  def parseAsProperty(setterFn: PropertyShape => Unit): PropertyShape = {
    innerParse()
    val property =
      PropertyShape(toAnnotations(ast)).withName(base.name.value(), base.name.annotations()).withRange(base)
    setterFn(property)
    property
  }

  private def innerParse(): Unit = {
    parseName(base)
    parseMembers()
  }

  private def parseMembers(): Unit = {
    val members: mutable.Buffer[Shape] = mutable.Buffer()
    collect(ast, Seq(ONE_OF_FIELD)).map { case (oneOfField: Node) =>
      GrpcFieldParser(oneOfField).parse(property => {
        val shape = NodeShape(toAnnotations(oneOfField))
        shape.withProperties(Seq(property))
        members.append(shape)
      })
    }
    base.withXone(members)
  }

  protected def parseName(element: NamedDomainElement): Unit = withName(ast, ONE_OF_NAME, element)
}
