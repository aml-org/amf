package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.{Parameter, Server}
import amf.apicontract.internal.metamodel.domain.{ParameterModel, ServerModel}
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.{ErrorServer, ErrorServerVariable}
import amf.apicontract.internal.spec.oas.parser.domain.OasLikeServerVariableParser
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.metamodel.domain.ExternalSourceElementModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.utils.IdCounter
import amf.core.internal.validation.CoreValidations
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.internal.domain.metamodel.common.ExamplesField
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.ExamplesMustBeASeq
import org.yaml.model.{YMap, YMapEntry, YNode, YSequence, YType}

case class Async20ServerVariableParser(entry: YMapEntryLike, parent: String)(implicit override val ctx: AsyncWebApiContext)
    extends OasLikeServerVariableParser(entry, parent)(ctx) {

  override protected def parseMap(variable: Parameter, map: YMap): Unit = {
    super.parseMap(variable, map)
    map.key(
      "examples",
      examplesEntry => {
        examplesEntry.value.tagType match {
          case YType.Seq =>
            val idCounter = new IdCounter()
            val examples = examplesEntry.value.as[YSequence].nodes.map { node =>
              Example(node)
                .withName(idCounter.genId("example"), Annotations.synthesized())
                .setWithoutId(
                  ExternalSourceElementModel.Raw,
                  AmfScalar(node.asScalar.map(_.text).getOrElse(node.toString), Annotations(node)),
                  Annotations.inferred()
                )
            }
            variable.fields.setWithoutId(
              ExamplesField.Examples,
              AmfArray(examples, Annotations(examplesEntry.value)),
              Annotations(examplesEntry)
            )
          case _ =>
            ctx.violation(ExamplesMustBeASeq, variable, "Examples facet must be an array of strings")
        }
      }
    )
  }
}
class Async24ServerVariableParser(override val entry: YMapEntryLike, override val parent: String)(implicit override val ctx: AsyncWebApiContext)
  extends Async20ServerVariableParser(entry, parent)(ctx) {
  override def parse(): Parameter = {
    val map: YMap = entry.asMap
    ctx.link(map) match {
      case Left(fullRef) => handleRef(fullRef)
      case Right(_)      => super.parse()
    }
  }

  private def handleRef(fullRef: String): Parameter = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "serverVariables")
    ctx.declarations
      .findServerVariable(label, SearchScope.Named)
      .map(serverVariables => nameAndAdopt(generateLink(label, serverVariables, entry), entry.key))
      .getOrElse(remote(fullRef, entry))
  }

  private def remote(fullRef: String, entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext): Parameter = {
    ctx.navigateToRemoteYNode(fullRef) match {
      case Some(result) =>
        val serverVariableNode = result.remoteNode
        val external = Async20ServerVariableParser(YMapEntryLike(serverVariableNode), parent)(result.context).parse()
        nameAndAdopt(
          external.link(AmfScalar(fullRef), entryLike.annotations, Annotations.synthesized()),
          entryLike.key
        )
      case None =>
        ctx.eh.violation(
          CoreValidations.UnresolvedReference,
          "",
          s"Cannot find link reference $fullRef",
          entryLike.asMap.location
        )
        val errorServerVariable = ErrorServerVariable(fullRef, entryLike.asMap)
        nameAndAdopt(errorServerVariable.link(fullRef, errorServerVariable.annotations), entryLike.key)
    }
  }

  private def generateLink(label: String, effectiveTarget: Parameter, entryLike: YMapEntryLike): Parameter = {
    val serverVariable = Parameter(entryLike.annotations)
    val hash   = s"${serverVariable.id}$label".hashCode
    serverVariable
      .withId(s"${serverVariable.id}/link-$hash")
      .withLinkTarget(effectiveTarget)
      .withLinkLabel(label, Annotations(entryLike.value))
  }

  def nameAndAdopt(serverVariable: Parameter, key: Option[YNode]): Parameter = {
    key foreach { k =>
      serverVariable.setWithoutId(ParameterModel.Name, ScalarNode(k).string(), Annotations(k))
    }
    serverVariable
  }
}
