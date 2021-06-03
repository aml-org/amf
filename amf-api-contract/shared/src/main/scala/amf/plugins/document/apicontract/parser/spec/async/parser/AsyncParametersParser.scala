package amf.plugins.document.apicontract.parser.spec.async.parser

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.AmfScalar
import amf.core.parser.{Annotations, ScalarNode, SearchScope, YMapOps}
import amf.plugins.document.apicontract.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.OasDefinitions
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.ErrorParameter
import amf.plugins.document.apicontract.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.document.apicontract.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, OasTypeParser}
import amf.plugins.domain.apicontract.metamodel.ParameterModel
import amf.plugins.domain.apicontract.models.Parameter
import amf.plugins.features.validation.CoreValidations
import org.yaml.model.YMap

case class AsyncParametersParser(parentId: String, map: YMap)(implicit val ctx: AsyncWebApiContext) {

  def parse(): Seq[Parameter] = {
    map.entries.map(entry => AsyncParameterParser(parentId, YMapEntryLike(entry)).parse())
  }
}

case class AsyncParameterParser(parentId: String, entryLike: YMapEntryLike)(implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {

  private def nameAndAdopt(param: Parameter): Parameter = {
    entryLike.key.foreach(k => param.set(ParameterModel.Name, ScalarNode(k).string(), Annotations(k)))
    param.adopted(parentId).add(entryLike.annotations)
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
    ctx.closedShape(param.id, map, "parameter")
    param
  }

  private def inferAsUriParameter(param: Parameter) = {
    param.set(ParameterModel.Binding, AmfScalar("path"), Annotations.synthesized())
  }

  def parseSchema(map: YMap, param: Parameter): Unit = {
    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, shape => shape.withName("schema").adopted(param.id), JSONSchemaDraft7SchemaVersion)(
          WebApiShapeParserContextAdapter(ctx))
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
        ctx.eh.violation(CoreValidations.UnresolvedReference, "", s"Cannot find link reference $fullRef", map)
        nameAndAdopt(ErrorParameter(fullRef, map).link(fullRef, annotations = Annotations(map)))
    }
  }
}
