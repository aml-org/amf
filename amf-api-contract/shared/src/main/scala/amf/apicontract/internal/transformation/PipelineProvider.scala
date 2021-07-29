package amf.apicontract.internal.transformation

import amf.core.client.scala.transform.TransformationPipeline
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec._

private[amf] object PipelineProvider {

  private[amf] val editing: Map[Spec, () => TransformationPipeline] = Map(
    RAML08  -> (() => Raml08EditingPipeline()),
    RAML10  -> (() => Raml10EditingPipeline()),
    OAS20   -> (() => Oas20EditingPipeline()),
    OAS30   -> (() => Oas3EditingPipeline()),
    ASYNC20 -> (() => Async20EditingPipeline()),
    AMF     -> (() => AmfEditingPipeline())
  )

  private[amf] val default: Map[Spec, () => TransformationPipeline] = Map(
    RAML08  -> (() => Raml08TransformationPipeline()),
    RAML10  -> (() => Raml10TransformationPipeline()),
    OAS20   -> (() => Oas20TransformationPipeline()),
    OAS30   -> (() => Oas30TransformationPipeline()),
    ASYNC20 -> (() => Async20TransformationPipeline()),
    AMF     -> (() => AmfTransformationPipeline())
  )

  private[amf] val cache: Map[Spec, () => TransformationPipeline] = Map(
    RAML08  -> (() => Raml08CachePipeline()),
    RAML10  -> (() => Raml10CachePipeline()),
    OAS20   -> (() => Oas20CachePipeline()),
    OAS30   -> (() => Oas3CachePipeline()),
    ASYNC20 -> (() => Async20CachePipeline()),
    AMF     -> (() => AmfEditingPipeline(urlShortening = true))
  )

  def getEditingPipelines(vendors: Spec*): Seq[(String, TransformationPipeline)] = {
    vendors.toSeq.map { vendor =>
      (vendor.id, editing(vendor)())
    }
  }

  def getCachePipelines(vendors: Spec*): Seq[(String, TransformationPipeline)] = {
    vendors.toSeq.map { vendor =>
      (vendor.id, cache(vendor)())
    }
  }

  def getDefaultPipelines(vendors: Spec*): Seq[(String, TransformationPipeline)] = {
    vendors.toSeq.map { vendor =>
      (vendor.id, default(vendor)())
    }
  }
}
