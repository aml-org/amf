package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.ScalarNode
import amf.core.client.scala.vocabulary.Namespace
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.ScalarShape
import org.mulesoft.antlrast.ast.{Node, Terminal}

class GraphQLNestedEnumParser(enumTypeDef: Node)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  val enum = ScalarShape(toAnnotations(enumTypeDef)).withDataType(Namespace.XsdTypes.xsdString.iri())

  def parse(parentId: String): ScalarShape = {
    parseName()
    enum.adopted(parentId)
    parseValues()
    enum
  }

  private def parseName(): Unit = {
    val name = findName(enumTypeDef, "AnonymousEnum", "Missing enumeration type name", enum.id)
    enum.withName(name)
  }

  private def parseValues(): Unit = {
    val values = valuesFrom(Seq(ENUM_VALUES_DEFINITION, ENUM_VALUE_DEFINITION, ENUM_VALUE, NAME))
    val keywordValues = valuesFrom(Seq(ENUM_VALUES_DEFINITION, ENUM_VALUE_DEFINITION, ENUM_VALUE, NAME, KEYWORD))
    enum.withValues(values ++ keywordValues)
  }

  private def valuesFrom(path: Seq[String]): Seq[ScalarNode] = {
    val values = collect(enumTypeDef, path) collect {
      case n: Node if n.children.head.isInstanceOf[Terminal] => n.children.head.asInstanceOf[Terminal]
    }
    values.map { t =>
      val s = ScalarNode(t.value, Some(XsdTypes.xsdString.iri()), toAnnotations(t)).withName(t.value)
      s.adopted(enum.id)
    }
  }
}
