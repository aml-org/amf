package amf.sfdc.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.sfdc.internal.spec.context.GraphQLWebApiContext
import amf.sfdc.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.sfdc.internal.spec.parser.syntax.TokenTypes.{ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION}
import amf.sfdc.internal.spec.context.GraphQLWebApiContext
import amf.sfdc.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.sfdc.internal.spec.parser.syntax.TokenTypes.{ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION}
import org.mulesoft.antlrast.ast.Node

case class GraphQLFieldParser(ast: Node)(implicit val ctx: GraphQLWebApiContext) extends GraphQLASTParserHelper {
  val property = PropertyShape(toAnnotations(ast))

  def parse(adopt: PropertyShape => Unit): PropertyShape = {
    parseName()
    adopt(property)
    parseDescription()
    parseArguments()
    // pareOutputType @TODO
    property
  }

  private def parseName(): Unit = {
    property.withName(findName(ast, "AnonymousField", "", "Missing name for field"))
  }

  private def parseDescription(): Unit = {
    findDescription(ast).map(_.value).foreach(property.withDescription)
  }

  private def parseArguments() = {
    collect(ast, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).foreach { case argument: Node =>
      parseArgument(argument)
    }
  }

  private def parseArgument(n: Node) = {
    val name = findName(n, "AnonymousInputType", property.id, "Missing input type name" )
    val description = findDescription(n)
    val shape = parseType(n, property.id)
    // add parameters to propertyShape model @TODO
  }
}
