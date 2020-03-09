package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{LexicalInformation, SynthesizedField}
import amf.core.metamodel.domain.ExternalSourceElementModel
import amf.core.model.domain.{AmfArray, AmfScalar, Annotation, DataNode}
import amf.core.parser.errorhandler.WarningOnlyHandler
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.plugins.document.webapi.annotations.ParsedJSONExample
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.{RamlWebApiContext, RamlWebApiContextType}
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher.{JSONSchema, XMLSchema}
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorNamedExample
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, DataNodeParser, SpecParserOps}
import amf.plugins.document.webapi.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.metamodel.common.ExamplesField
import amf.plugins.domain.shapes.models.{AnyShape, Example, ExemplifiedDomainElement, ScalarShape}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ParserSideValidations.{
  ExamplesMustBeAMap,
  ExclusivePropertiesSpecification,
  InvalidFragmentType
}
import org.mulesoft.lexer.Position
import org.yaml.model.YNode.MutRef
import org.yaml.model._
import org.yaml.parser.JsonParser
import org.yaml.render.YamlRender

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class OasResponseExamplesParser(entry: YMapEntry)(implicit ctx: WebApiContext) {
  def parse(): Seq[Example] = {
    val results = ListBuffer[Example]()
    entry.value
      .as[YMap]
      .regex(".*/.*")
      .map(e => results += OasResponseExampleParser(e).parse())

    results
  }
}

case class OasExamplesParser(map: YMap, exemplifiedDomainElement: ExemplifiedDomainElement)(
    implicit ctx: WebApiContext) {
  def parse(): Unit = {
    (map.key("example"), map.key("examples")) match {
      case (Some(exampleEntry), None) =>
        val examples = List(parseExample(exampleEntry.value))
        exemplifiedDomainElement.set(ExamplesField.Examples,
                                     AmfArray(examples, Annotations(exampleEntry)),
                                     Annotations(exampleEntry))
      case (None, Some(examplesEntry)) =>
        val examples = Oas3NamedExamplesParser(examplesEntry, exemplifiedDomainElement.id).parse()
        exemplifiedDomainElement.set(ExamplesField.Examples,
                                     AmfArray(examples, Annotations(examplesEntry.value)),
                                     Annotations(examplesEntry))
      case (Some(_), Some(_)) =>
        ctx.eh.violation(
          ExclusivePropertiesSpecification,
          exemplifiedDomainElement.id,
          s"Properties 'example' and 'examples' are exclusive and cannot be declared together",
          map
        )
      case _ => // ignore
    }
  }

  private def parseExample(yNode: YNode) = {
    val example = Example(yNode).adopted(exemplifiedDomainElement.id)
    RamlExampleValueAsString(yNode, example, Oas3ExampleOptions).populate()
  }
}

case class Oas3NamedExamplesParser(entry: YMapEntry, parentId: String)(implicit ctx: WebApiContext) {
  def parse(): Seq[Example] = {
    entry.value
      .as[YMap]
      .entries
      .map(e => Oas3NameExampleParser(e, parentId, Oas3ExampleOptions).parse())
  }
}

case class OasResponseExampleParser(yMapEntry: YMapEntry)(implicit ctx: WebApiContext) {
  def parse(): Example = {
    val example = Example(yMapEntry)
      .set(ExampleModel.MediaType, yMapEntry.key.as[YScalar].text)
    RamlExampleValueAsString(yMapEntry.value, example, ExampleOptions(strictDefault = false, quiet = true)).populate()
  }
}

case class RamlExamplesParser(map: YMap,
                              singleExampleKey: String,
                              multipleExamplesKey: String,
                              exemplified: ExemplifiedDomainElement,
                              options: ExampleOptions)(implicit ctx: WebApiContext) {
  def parse(): Unit = {
    if (map.key(singleExampleKey).isDefined && map.key(multipleExamplesKey).isDefined && exemplified.id.nonEmpty) {
      ctx.eh.violation(
        ExclusivePropertiesSpecification,
        exemplified.id,
        s"Properties '$singleExampleKey' and '$multipleExamplesKey' are exclusive and cannot be declared together",
        map
      )
    }
    val examples = RamlMultipleExampleParser(multipleExamplesKey, map, exemplified.withExample, options).parse() ++
      RamlSingleExampleParser(singleExampleKey, map, exemplified.withExample, options).parse()

    map
      .key(multipleExamplesKey)
      .orElse(map.key(singleExampleKey)) match {
      case Some(e) =>
        exemplified.set(ExamplesField.Examples, AmfArray(examples), Annotations(e))
      case _ if examples.nonEmpty =>
        exemplified.set(ExamplesField.Examples, AmfArray(examples))
      case _ => // ignore
    }
  }
}

case class RamlMultipleExampleParser(key: String,
                                     map: YMap,
                                     producer: Option[String] => Example,
                                     options: ExampleOptions)(implicit ctx: WebApiContext) {
  def parse(): Seq[Example] = {
    val examples = ListBuffer[Example]()

    map.key(key).foreach { entry =>
      ctx.link(entry.value) match {
        case Left(s) =>
          examples += ctx.declarations.findNamedExampleOrError(entry.value)(s).link(s)

        case Right(node) =>
          node.tagType match {
            case YType.Map =>
              examples ++= node.as[YMap].entries.map(RamlNamedExampleParser(_, producer, options).parse())
            case YType.Null => // ignore
            case YType.Str
                if node.toString().matches("<<.*>>") && ctx
                  .asInstanceOf[RamlWebApiContext]
                  .contextType != RamlWebApiContextType.DEFAULT => // Ignore
            case _ =>
              ctx.eh.violation(
                ExamplesMustBeAMap,
                "",
                s"Property '$key' should be a map",
                entry
              )
          }
      }
    }
    examples
  }
}

case class RamlNamedExampleParser(entry: YMapEntry, producer: Option[String] => Example, options: ExampleOptions)(
    implicit ctx: WebApiContext) {
  def parse(): Example = {
    val name           = ScalarNode(entry.key)
    val simpleProducer = () => producer(Some(name.text().toString))
    val example: Example = ctx.link(entry.value) match {
      case Left(s) =>
        ctx.declarations
          .findNamedExample(s)
          .map(e => e.link(s).asInstanceOf[Example])
          .getOrElse(RamlSingleExampleValueParser(entry, simpleProducer, options).parse())
      case Right(_) => RamlSingleExampleValueParser(entry, simpleProducer, options).parse()
    }
    example.set(ExampleModel.Name, name.text(), Annotations(entry))
  }
}

case class RamlSingleExampleParser(key: String,
                                   map: YMap,
                                   producer: Option[String] => Example,
                                   options: ExampleOptions)(implicit ctx: WebApiContext) {
  def parse(): Option[Example] = {
    val newProducer = () => producer(None)
    map.key(key).flatMap { entry =>
      ctx.link(entry.value) match {
        case Left(s) =>
          ctx.declarations
            .findNamedExample(s,
                              Some(
                                errMsg =>
                                  ctx.eh.violation(
                                    InvalidFragmentType,
                                    s,
                                    errMsg,
                                    entry.value
                                )))
            .map(e => e.link(s).asInstanceOf[Example])
        case Right(node) =>
          node.tagType match {
            case YType.Map =>
              Option(RamlSingleExampleValueParser(entry, newProducer, options).parse())
            case YType.Null => None
            case _ => // example can be any type or scalar value, like string int datetime etc. We will handle all like strings in this stage
              Option(
                RamlExampleValueAsString(node, newProducer().add(Annotations(entry.value)), options)
                  .populate())
          }
      }
    }
  }
}

case class RamlSingleExampleValueParser(entry: YMapEntry, producer: () => Example, options: ExampleOptions)(
    implicit ctx: WebApiContext)
    extends SpecParserOps {
  def parse(): Example = {
    val example = producer().add(Annotations(entry))

    entry.value.tagType match {
      case YType.Map =>
        val map = entry.value.as[YMap]

        if (map.key("value").nonEmpty) {
          map.key("displayName", (ExampleModel.DisplayName in example).allowingAnnotations)
          map.key("description", (ExampleModel.Description in example).allowingAnnotations)
          map.key("strict", (ExampleModel.Strict in example).allowingAnnotations)

          map
            .key("value")
            .foreach { entry =>
              RamlExampleValueAsString(entry.value, example, options).populate()
            }

          AnnotationParser(example, map, List(VocabularyMappings.example)).parse()

          if (ctx.vendor.isRaml) ctx.closedShape(example.id, map, "example")
        } else RamlExampleValueAsString(entry.value, example, options).populate()
      case YType.Null => // ignore
      case _          => RamlExampleValueAsString(entry.value, example, options).populate()
    }

    example
  }
}

case class Oas3NameExampleParser(entry: YMapEntry, parentId: String, options: ExampleOptions)(
    implicit ctx: WebApiContext)
    extends SpecParserOps {
  def parse(): Example = {
    val map = entry.value.as[YMap]

    ctx.link(map) match {
      case Left(fullRef) => parseLink(fullRef, map).add(Annotations(entry))
      case Right(_)      => Oas3ExampleValueParser(map, newExample(map), options).parse()
    }
  }

  private val keyName = ScalarNode(entry.key)

  private def setName(e: Example): Example = e.set(ExampleModel.Name, keyName.string())

  private def newExample(ast: YPart): Example =
    setName(Example(entry)).adopted(parentId)

  private def parseLink(fullRef: String, map: YMap) = {
    val name = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "examples")
    ctx.declarations
      .findExample(name, SearchScope.All)
      .map(found => setName(found.link(name)))
      .getOrElse {
        ctx.obtainRemoteYNode(fullRef) match {
          case Some(exampleNode) =>
            Oas3ExampleValueParser(exampleNode.as[YMap], newExample(exampleNode), options).parse()
          case None =>
            ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find example reference $fullRef", map)
            val errorExample = setName(ErrorNamedExample(name, map).link(name)).adopted(parentId)
            errorExample
        }
      }
  }
}

case class Oas3ExampleValueParser(map: YMap, example: Example, options: ExampleOptions)(implicit ctx: WebApiContext)
    extends SpecParserOps {
  def parse(): Example = {
    map.key("summary", (ExampleModel.Summary in example).allowingAnnotations)
    map.key("description", (ExampleModel.Description in example).allowingAnnotations)
    map.key("externalValue", (ExampleModel.ExternalValue in example).allowingAnnotations)

    example.withStrict(options.strictDefault)

    map
      .key("value")
      .foreach { entry =>
        RamlExampleValueAsString(entry.value, example, options).populate()
      }

    AnnotationParser(example, map, List(VocabularyMappings.example)).parse()

    ctx.closedShape(example.id, map, "example")
    example
  }
}

case class RamlExampleValueAsString(node: YNode, example: Example, options: ExampleOptions)(
    implicit ctx: WebApiContext) {
  def populate(): Example = {
    if (example.fields.entry(ExampleModel.Strict).isEmpty) {
      example.set(ExampleModel.Strict, AmfScalar(options.strictDefault), Annotations() += SynthesizedField())
    }

    val (targetNode, mutTarget) = node match {
      case mut: MutRef =>
        ctx.declarations.fragments
          .get(mut.origValue.asInstanceOf[YScalar].text)
          .foreach { e =>
            example.withReference(e.encoded.id)
            example.set(ExternalSourceElementModel.Location, e.location.getOrElse(ctx.loc))
          }
        (mut.target.getOrElse(node), true)
      case _ =>
        (node, false) // render always (even if xml) for | multiline strings. (If set scalar.text we lose the token)

    }

    node.toOption[YScalar] match {
      case Some(_) if node.tagType == YType.Null =>
        example.set(ExampleModel.Raw, AmfScalar("null", Annotations.valueNode(node)), Annotations.valueNode(node))
      case Some(scalar) =>
        example.set(ExampleModel.Raw, AmfScalar(scalar.text, Annotations.valueNode(node)), Annotations.valueNode(node))
      case _ =>
        example.set(ExampleModel.Raw,
                    AmfScalar(YamlRender.render(targetNode), Annotations.valueNode(node)),
                    Annotations.valueNode(node))

    }

    val result = NodeDataNodeParser(targetNode, example.id, options.quiet, mutTarget, options.isScalar).parse()

    result.dataNode.foreach { dataNode =>
      // If this example comes from a 08 param with type string, we force this to be a string
      example.set(ExampleModel.StructuredValue, dataNode, Annotations(node))
    }

    example
  }
}

case class NodeDataNodeParser(node: YNode,
                              parentId: String,
                              quiet: Boolean,
                              fromExternal: Boolean = false,
                              isScalar: Boolean = false)(implicit ctx: WebApiContext) {
  def parse(): DataNodeParserResult = {
    val errorHandler             = if (quiet) WarningOnlyHandler(ctx.eh) else ctx.eh
    var jsonText: Option[String] = None
    val exampleNode: Option[YNode] = node.toOption[YScalar] match {
      case Some(scalar) if scalar.mark.isInstanceOf[QuotedMark] => Some(node)
      case Some(_) if isScalar                                  => Some(node)
      case Some(scalar) if JSONSchema.unapply(scalar.text).isDefined =>
        jsonText = Some(scalar.text)
        node
          .toOption[YScalar]
          .map { scalar =>
            val parser =
              if (!fromExternal)
                JsonParser.withSource(scalar.text,
                                      scalar.sourceName,
                                      Position(node.range.lineFrom, node.range.columnFrom))(errorHandler)
              else JsonParser.withSource(scalar.text, scalar.sourceName)
            parser.document().node
          }
      case Some(scalar) if XMLSchema.unapply(scalar.text).isDefined => None
      case _                                                        => Some(node) // return default node for xml too.
    }

    errorHandler match {
      case wh: WarningOnlyHandler if wh.hasRegister && jsonText.isDefined =>
        parseDataNode(exampleNode, jsonText.map(ParsedJSONExample(_)).toSeq)
      case wh: WarningOnlyHandler if wh.hasRegister => DataNodeParserResult(exampleNode, None)
      case _                                        => parseDataNode(exampleNode, jsonText.map(ParsedJSONExample(_)).toSeq)

    }
  }

  private def parseDataNode(exampleNode: Option[YNode], ann: Seq[Annotation] = Seq()) = {
    val dataNode = exampleNode.map { ex =>
      val dataNode = DataNodeParser(ex, parent = Some(parentId)).parse()
      dataNode.annotations.reject(_.isInstanceOf[LexicalInformation])
      dataNode.annotations += LexicalInformation(Range(ex.value.range))
      ann.foreach { a =>
        dataNode.annotations += a
      }

      dataNode
    }
    DataNodeParserResult(exampleNode, dataNode)
  }
}

case class DataNodeParserResult(exampleNode: Option[YNode], dataNode: Option[DataNode]) {}

case class ExampleOptions(strictDefault: Boolean, quiet: Boolean, isScalar: Boolean = false) {
  def checkScalar(shape: AnyShape): ExampleOptions = shape match {
    case _: ScalarShape =>
      ExampleOptions(strictDefault, quiet, isScalar = true)
    case _ => this
  }
}

object DefaultExampleOptions extends ExampleOptions(true, false, false)

object Oas3ExampleOptions extends ExampleOptions(strictDefault = true, quiet = true)
