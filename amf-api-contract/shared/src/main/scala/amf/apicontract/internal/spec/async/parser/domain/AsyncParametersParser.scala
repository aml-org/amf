package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.internal.metamodel.domain.ParameterModel
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorParameter
import amf.apicontract.internal.spec.common.parser.{SpecParserOps, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.spec.common.JSONSchemaDraft7SchemaVersion
import amf.shapes.internal.spec.common.parser.{AnnotationParser, YMapEntryLike}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import org.yaml.model.YMap

case class AsyncParametersParser(parentId: String, map: YMap)(implicit val ctx: AsyncWebApiContext) {

  def parse(): Seq[Parameter] = {
    map.entries.map(entry => AsyncParameterParser(parentId, YMapEntryLike(entry)).parse())
  }
}

case class AsyncParameterParser(parentId: String, entryLike: YMapEntryLike)(implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {

  private def nameAndAdopt(param: Parameter): Parameter = {
    entryLike.key.foreach(k => param.setWithoutId(ParameterModel.Name, ScalarNode(k).string(), Annotations(k)))
    param.add(entryLike.annotations)
  }

  def parse(): Parameter = {
    val map: YMap = entryLike.asMap
    ctx.link(map) match {
      case Left(fullRef) =>
        handleRef(map, fullRef)
      case Right(_) =>
        val param = Parameter()
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

    AnnotationParser(param, map)(WebApiShapeParserContextAdapter(ctx)).parse()
    ctx.closedShape(param, map, "parameter")
    param
  }

  private def inferAsUriParameter(param: Parameter) = {
    param.setWithoutId(ParameterModel.Binding, AmfScalar("path"), Annotations.synthesized())
  }

  def parseSchema(map: YMap, param: Parameter): Unit = {
    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, shape => shape.withName("schema"), JSONSchemaDraft7SchemaVersion)(
          WebApiShapeParserContextAdapter(ctx))
          .parse()
          .foreach { schema =>
            param.setWithoutId(ParameterModel.Schema, schema, Annotations(entry))
          }
      }
    )
  }

  private def handleRef(map: YMap, fullRef: String): Parameter = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "parameters")
    ctx.declarations
      .findParameter(label, SearchScope.Named)
      .map(param =>
        nameAndAdopt(param.link(AmfScalar(label), Annotations(entryLike.value), Annotations.synthesized())))
      .getOrElse(remote(fullRef, map))
  }

  private def remote(fullRef: String, map: YMap): Parameter = {
    ctx.obtainRemoteYNode(fullRef) match {
      case Some(paramNode) =>
        val external = AsyncParameterParser(parentId, YMapEntryLike(paramNode)).parse()
        nameAndAdopt(external.link(AmfScalar(fullRef), Annotations(map), Annotations.synthesized()))
      case None =>
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", map.location)
        nameAndAdopt(ErrorParameter(fullRef, map).link(fullRef, annotations = Annotations(map)))
    }
  }
}
