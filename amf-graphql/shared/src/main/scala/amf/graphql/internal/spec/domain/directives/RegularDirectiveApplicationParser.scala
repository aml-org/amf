package amf.graphql.internal.spec.domain.directives

import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{DataNode, DomainElement, ObjectNode}
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel.DefinedBy
import amf.core.internal.parser.domain.Annotations.{inferred, virtual}
import amf.core.internal.parser.domain.SearchScope
import amf.core.internal.render.SpecOrdering.Lexical
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENT, ARGUMENTS, VALUE}
import amf.graphql.internal.spec.parser.syntax.ValueParser
import amf.shapes.internal.spec.common.parser.ApiExtensionsParser
import org.mulesoft.antlrast.ast.Node
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.{YDocument, YMapEntry, YNode}

class RegularDirectiveApplicationParser(override implicit val ctx: GraphQLBaseWebApiContext)
    extends DirectiveApplicationParser
    with ApiExtensionsParser {

  override def appliesTo(node: Node): Boolean = true

  def parse(node: Node, element: DomainElement): Unit = {
    val directiveApplication = parseDirective(node)
    val effective = parseApiExtensionFromDomain(directiveApplication, element).getOrElse(directiveApplication)
    element.withCustomDomainProperty(effective)
  }

  def parseDirective(node: Node): DomainExtension = {
    val directiveApplication = DomainExtension(toAnnotations(node))
    populateDirective(directiveApplication, node)
    directiveApplication
  }

  private def populateDirective(directive: DomainExtension, node: Node): Unit = {
    parseName(directive, node)
    parseDefinedBy(directive, node)
    parseArguments(directive, node)
  }

  def parseApiExtensionFromDomain(domainExtension: DomainExtension, element: DomainElement): Option[DomainExtension] = {
    val (name, entry) = getNamedEntryForExtension(domainExtension)
    parseSemantic(entry, element.meta.`type`.map(_.iri()), Some(ctx.extensionsFacadeBuilder.extensionName(name)))
  }

  private def getNamedEntryForExtension(domainExtension: DomainExtension) = {
    val name = domainExtension.name.value()
    (name, YMapEntry(name, dataNodeToYNode(domainExtension.extension)))
  }
  private def dataNodeToYNode(dataNode: DataNode): YNode = {
    val emit: PartBuilder => Unit = DataNodeEmitter(dataNode, Lexical)(ctx.eh).emit _
    YDocument(emit).node
  }

  protected def parseName(directiveApplication: DomainExtension, node: Node): Unit = {
    val (name, annotations) = findName(node, "AnonymousDirective", "Missing directive name")
    directiveApplication.withName(name, annotations)
  }

  protected def parseArguments(directiveApplication: DomainExtension, node: Node): Unit = {
    // arguments are parsed as the properties of an ObjectNode, which goes in the Extension field in the DomainExtension
    val schema = ObjectNode(virtual())
    collect(node, Seq(ARGUMENTS, ARGUMENT)).foreach { case argument: Node =>
      parseArgument(argument, schema)
    }
    directiveApplication.withExtension(schema)
  }

  protected def parseArgument(n: Node, objectNode: ObjectNode): Unit = {
    val (name, _) = findName(n, "AnonymousDirectiveArgument", "Missing argument name")
    for {
      valueNode   <- pathToNonTerminal(n, Seq(VALUE))
      parsedValue <- ValueParser.parseValue(valueNode)
    } yield {
      objectNode.addProperty(name, parsedValue, toAnnotations(n))
    }
  }

  protected def parseDefinedBy(directiveApplication: DomainExtension, node: Node): Unit = {
    ctx.findAnnotation(directiveApplication.name.value(), SearchScope.All) match {
      case Some(directiveDeclaration) => directiveApplication.setWithoutId(DefinedBy, directiveDeclaration, inferred())
      case None =>
        astError(
          s"Directive ${directiveApplication.name} is not declared",
          toAnnotations(node)
        )
    }
  }
}
