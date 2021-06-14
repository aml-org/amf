package amf.plugins.document.apicontract.parser.spec.async

import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.internal.annotations.SourceVendor
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.remote.{AsyncApi20, Vendor}
import amf.core.internal.render.BaseEmitters.{EmptyMapEmitter, EntryPartEmitter, ValueEmitter, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.validation.CoreValidations.ResolutionValidation
import amf.plugins.document.apicontract.contexts.emitter.async.AsyncSpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.async.emitters.{
  AsyncApiCreativeWorksEmitter,
  AsyncApiEndpointsEmitter,
  AsyncApiServersEmitter,
  AsyncDeclarationsEmitters
}
import amf.plugins.document.apicontract.parser.spec.common.DeclarationsEmitterWrapper
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.AgnosticShapeEmitterContextAdapter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.document.apicontract.parser.spec.domain.SecurityRequirementsEmitter
import amf.plugins.document.apicontract.parser.spec.oas.emitters.{InfoEmitter, TagsEmitter}
import amf.plugins.domain.apicontract.metamodel.api.WebApiModel
import amf.plugins.domain.apicontract.models.Tag
import amf.plugins.domain.apicontract.models.api.{Api, WebApi}
import amf.plugins.domain.shapes.models.CreativeWork
import org.yaml.model.{YDocument, YNode, YScalar, YType}

import scala.collection.mutable

class AsyncApi20DocumentEmitter(document: BaseUnit)(implicit val spec: AsyncSpecEmitterContext) {

  protected implicit val shapeCtx = AgnosticShapeEmitterContextAdapter(spec)

  def emitWebApi(ordering: SpecOrdering): Seq[EntryEmitter] = {
    val model  = retrieveWebApi()
    val vendor = model.annotations.find(classOf[SourceVendor]).map(_.vendor)
    val api    = WebApiEmitter(model, ordering, vendor, Seq())
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
    Seq(DeclarationsEmitterWrapper(emitters, ordering))

  def versionEntry(b: YDocument.EntryBuilder): Unit =
    b.asyncapi = YNode(YScalar("2.0.0"), YType.Str) // this should not be necessary but for use the same logic

  case class WebApiEmitter(api: Api, ordering: SpecOrdering, vendor: Option[Vendor], references: Seq[BaseUnit]) {
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
