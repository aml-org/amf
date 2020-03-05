package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.AmfScalar
import amf.core.parser.{Annotations, ScalarNode, SearchScope, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorParameter
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, OasTypeParser}
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.{YMap, YMapEntry, YNode}
import ConversionHelpers._

case class AsyncParametersParser(parentId: String, map: YMap)(implicit val ctx: AsyncWebApiContext) {

  def parse(): Seq[Parameter] = {
    map.entries.map(entry => AsyncParameterParser(parentId, Left(entry)).parse())
  }
}

case class AsyncParameterParser(parentId: String, entryOrNode: Either[YMapEntry, YNode])(
    implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {

  private def nameAndAdopt(param: Parameter): Parameter = {
    entryOrNode.left.foreach(entry => param.set(ParameterModel.Name, ScalarNode(entry.key).string()))
    param.adopted(parentId)
  }

  def parse(): Parameter = {
    val map: YMap = entryOrNode
    ctx.link(map) match {
      case Left(fullRef) =>
        handleRef(map, fullRef)
      case Right(_) =>
        val param = Parameter(map)
        nameAndAdopt(param)
        populateParam(param, map)
    }
  }

  def populateParam(param: Parameter, map: YMap): Parameter = {
    parseSchema(map, param)
    map.key("description", ParameterModel.Description in param)
    map.key("location", ParameterModel.Binding in param)

    if (param.binding.isNullOrEmpty) {
      inferAsUriParameter(param)
    }

    AnnotationParser(param, map).parse()
    ctx.closedShape(param.id, map, "parameter")
    param
  }

  private def inferAsUriParameter(param: Parameter) = {
    param.set(ParameterModel.Binding, AmfScalar("path"), Annotations() += SynthesizedField())
  }

  def parseSchema(map: YMap, param: Parameter): Unit = {
    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, shape => shape.withName("schema").adopted(param.id), JSONSchemaDraft7SchemaVersion)
          .parse()
          .foreach { schema =>
            param.set(ParameterModel.Schema, schema, Annotations(entry))
          }
      }
    )
  }

  private def handleRef(map: YMap, fullRef: String): Parameter = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "parameters")
    ctx.declarations
      .findParameter(label, SearchScope.Named)
      .map(param => nameAndAdopt(param.link(label)))
      .getOrElse(remote(fullRef, map))
  }

  private def remote(fullRef: String, map: YMap): Parameter = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(paramNode) =>
        val external = AsyncParameterParser(parentId, Right(paramNode)).parse()
        nameAndAdopt(external.link(fullRef))
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", map)
        nameAndAdopt(ErrorParameter(fullRef, map).link(fullRef))
    }
  }
}
