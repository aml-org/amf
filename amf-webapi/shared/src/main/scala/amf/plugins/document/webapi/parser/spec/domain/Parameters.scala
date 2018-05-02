package amf.plugins.document.webapi.parser.spec.domain

import amf.plugins.domain.webapi.models.{Parameter, Payload}
import org.yaml.model.YNode

case class Parameters(query: Seq[Parameter] = Nil,
                      path: Seq[Parameter] = Nil,
                      header: Seq[Parameter] = Nil,
                      baseUri08: Seq[Parameter] = Nil,
                      body: Seq[Payload] = Nil) {
  def merge(inner: Parameters): Parameters = {
    Parameters(
      mergeParams(query, inner.query),
      mergeParams(path, inner.path),
      mergeParams(header, inner.header),
      mergeParams(baseUri08, inner.baseUri08),
      mergePayloads(body, inner.body)
    )
  }

  def add(inner: Parameters): Parameters = {
    Parameters(
      addParams(query, inner.query),
      addParams(path, inner.path),
      addParams(header, inner.header),
      addParams(baseUri08, inner.baseUri08),
      addPayloads(body, inner.body)
    )
  }

  private def mergeParams(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
    val globalMap = global.map(p => p.name.value() -> p).toMap
    val innerMap  = inner.map(p => p.name.value()  -> p).toMap

    (globalMap ++ innerMap).values.toSeq
  }

  private def mergePayloads(global: Seq[Payload], inner: Seq[Payload]): Seq[Payload] = inner

  private def addParams(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
    val globalMap = global.map(p => p.name.value() -> p).toMap
    val innerMap  = inner.map(p => p.name.value()  -> p).toMap

    (globalMap ++ innerMap).values.toSeq
  }

  private def addPayloads(global: Seq[Payload], inner: Seq[Payload]): Seq[Payload] = global ++ inner

  def nonEmpty: Boolean = query.nonEmpty || path.nonEmpty || header.nonEmpty || body.nonEmpty
}

object Parameters {
  def classified(path: String, params: Seq[Parameter], payloads: Seq[Payload] = Nil): Parameters = {
    var uriParams: Seq[Parameter]  = Nil
    var pathParams: Seq[Parameter] = Nil
    params.filter(_.isPath).foreach { param =>
      if (path.contains(s"{${param.name.value()}}"))
        pathParams ++= Seq(param)
      else uriParams ++= Seq(param)
    }
    Parameters(params.filter(_.isQuery) ++ pathParams, Nil, params.filter(_.isHeader), uriParams, payloads)
  }
}

case class OasParameter(parameter: Parameter, payload: Payload, ast: Option[YNode] = None) {
  def isFormData: Boolean = parameter.isForm
  def isBody: Boolean     = parameter.isBody
  def isQuery: Boolean    = parameter.isQuery
  def isPath: Boolean     = parameter.isPath
  def isHeader: Boolean   = parameter.isHeader

  def hasInvalidBinding: Boolean = !isFormData && !isBody && !isQuery && !isPath && !isHeader
}

object OasParameter {
  def apply(node: YNode): OasParameter = OasParameter(Parameter(node), Payload(node), Some(node))
}
