package amf.apicontract.internal.spec.async.emitters.document

import amf.apicontract.client.scala.model.domain.Tag
import amf.apicontract.client.scala.model.domain.api.{Api, WebApi}
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.async.emitters.context.AsyncSpecEmitterContext
import amf.apicontract.internal.spec.async.emitters.domain.{
  AsyncApiCreativeWorksEmitter,
  AsyncApiEndpointsEmitter,
  AsyncApiServersEmitter
}
import amf.apicontract.internal.spec.common.emitter
import amf.apicontract.internal.spec.common.emitter.{AgnosticShapeEmitterContextAdapter, SecurityRequirementsEmitter}
import amf.apicontract.internal.spec.oas.emitter.domain.{InfoEmitter, TagsEmitter}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.internal.annotations.SourceSpec
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.remote.{AsyncApi20, Spec}
import amf.core.internal.render.BaseEmitters.{EmptyMapEmitter, EntryPartEmitter, ValueEmitter, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.validation.CoreValidations.ResolutionValidation
import amf.shapes.client.scala.model.domain.CreativeWork
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.yaml.model.{YDocument, YNode, YScalar, YType}

import scala.collection.mutable

class AsyncApi20DocumentEmitter(document: BaseUnit)(implicit val spec: AsyncSpecEmitterContext) {

  protected implicit val shapeCtx = AgnosticShapeEmitterContextAdapter(spec)

  def emitWebApi(ordering: SpecOrdering): Seq[EntryEmitter] = {
    val model = retrieveWebApi()
    val spec  = model.annotations.find(classOf[SourceSpec]).map(_.spec)
    val api   = WebApiEmitter(model, ordering, spec, Seq())
    api.emitters
  }

  private def retrieveWebApi(): Api = document match {
    case document: Document => document.encodes.asInstanceOf[Api]
    case _ =>
      spec.eh.violation(ResolutionValidation,
                        document.id,
                        None,
                        "BaseUnit doesn't encode a WebApi.",
                        document.position(),
                        document.location())
      WebApi()
  }

  def emitDocument(): YDocument = {
    val doc = document.asInstanceOf[Document]

    val ordering = SpecOrdering.ordering(AsyncApi20, doc.encodes.annotations)

//    val references = ReferencesEmitter(document, ordering)
    val declares =
      wrapDeclarations(AsyncDeclarationsEmitters(doc.declares, ordering, document.references).emitters, ordering)
    val api = emitWebApi(ordering)
//    val extension = extensionEmitter()
//    val usage: Option[ValueEmitter] =
//      doc.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage".asOasExtension, f))

    YDocument {
      _.obj { b =>
        versionEntry(b)
//        traverse(ordering.sorted(api ++ extension ++ usage ++ declares :+ references), b)
        traverse(ordering.sorted(api ++ declares), b)
      }
    }
  }

  def wrapDeclarations(emitters: Seq[EntryEmitter], ordering: SpecOrdering): Seq[EntryEmitter] =
    Seq(emitter.DeclarationsEmitterWrapper(emitters, ordering))

  def versionEntry(b: YDocument.EntryBuilder): Unit =
    b.asyncapi = YNode(YScalar("2.0.0"), YType.Str) // this should not be necessary but for use the same logic

  case class WebApiEmitter(api: Api, ordering: SpecOrdering, vendor: Option[Spec], references: Seq[BaseUnit]) {
    val emitters: Seq[EntryEmitter] = {
      val fs     = api.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      fs.entry(WebApiModel.Identifier).foreach(f => result += ValueEmitter("id", f))

      result += InfoEmitter(fs, ordering)

      fs.entry(WebApiModel.Servers)
        .map(f => result += new AsyncApiServersEmitter(f, ordering))

      fs.entry(WebApiModel.Tags)
        .map(f => result += TagsEmitter("tags", f.array.values.asInstanceOf[Seq[Tag]], ordering))

      fs.entry(WebApiModel.Documentations)
        .map(f => result += new AsyncApiCreativeWorksEmitter(f.arrayValues[CreativeWork].head, ordering))

      fs.entry(WebApiModel.EndPoints) match {
        case Some(f: FieldEntry) => result += new AsyncApiEndpointsEmitter(f, ordering)
        case None                => result += EntryPartEmitter("channels", EmptyMapEmitter())
      }

      fs.entry(WebApiModel.Security).map(f => result += SecurityRequirementsEmitter("security", f, ordering))

      result ++= AnnotationsEmitter(api, ordering).emitters
      ordering.sorted(result)
    }
  }
}
