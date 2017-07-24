package amf.graph

import amf.builder._
import amf.common.AMFAST
import amf.common.AMFToken.{Entry, MapToken, SequenceToken, StringToken}
import amf.common.Strings.strings
import amf.compiler.Root
import amf.document.BaseUnit
import amf.metadata.document.DocumentModel
import amf.metadata.domain._
import amf.metadata.Type.{Array, Scalar, Str}
import amf.metadata.{Field, Obj, Type}
import amf.vocabulary.Namespace

/**
  * AMF Graph parser
  */
object GraphParser {

  def parse(root: Root): BaseUnit = {
    val ast = root.ast > MapToken

    val ctx = context(ast)
    val t   = typed(ast, ctx)
    parse(ast, ctx, t).asInstanceOf[BaseUnit]
  }

  private def typed(ast: AMFAST, ctx: GraphContext): Obj = types(ctx.expand(types(ast).head))

  private def parse(ast: AMFAST, ctx: GraphContext, t: Obj): Any = {
    val builder  = builders(t)()
    val children = ast.children
    t.fields.foreach(f => {
      val k = ctx.reduce(f.value)
      children.find(key(k)) match {
        case Some(entry) => traverse(ctx, builder, f, value(f.`type`, entry.last))
        case _           =>
      }
    })
    builder.build
  }

  private def value(t: Type, node: AMFAST): AMFAST = {
    node.`type` match {
      case SequenceToken =>
        t match {
          case Array(_) => node
          case _        => value(t, node.head)
        }
      case MapToken =>
        t match {
          case Scalar(_) => node.children.find(key("@value")).get.last
          case _         => node
        }
      case _ => node
    }
  }

  private def traverse(ctx: GraphContext, builder: Builder, f: Field, node: AMFAST) = {
    f.`type` match {
      case _: Obj => builder.set(f, parse(node, ctx, typed(node, ctx)))
      case Str    => builder.set(f, node.content.unquote)
    }
  }

  private def context(ast: AMFAST): GraphContext = {
    ast.children.find(key("@context")) match {
      case Some(t) =>
        MapGraphContext(
          (t > MapToken).children
            .map((entry) => {
              entry.head.content.unquote -> Namespace(entry.last.content.unquote)
            })
            .toMap)
      case _ => EmptyGraphContext
    }
  }

  private def types(ast: AMFAST): Seq[String] = {
    ast.children.find(key("@type")) match {
      case Some(t) => (t > SequenceToken).children.map(_.content.unquote)
      case _       => throw new Exception(s"No @type declaration on node $ast")
    }
  }

  /** Find entry with matching key. */
  private def key(key: String)(n: AMFAST): Boolean = (n is Entry) && (n > StringToken) ? key

  /** Object Type builders. */
  private val builders: Map[Obj, () => Builder] = Map(
    DocumentModel     -> DocumentBuilder.apply,
    WebApiModel       -> WebApiBuilder.apply,
    OrganizationModel -> OrganizationBuilder.apply,
    LicenseModel      -> LicenseBuilder.apply,
    CreativeWorkModel -> CreativeWorkBuilder.apply,
    EndPointModel     -> EndPointBuilder.apply,
    OperationModel    -> OperationBuilder.apply
  )

  private val types: Map[String, Obj] = builders.keys.map(t => t.`type`.head.iri() -> t).toMap
}
