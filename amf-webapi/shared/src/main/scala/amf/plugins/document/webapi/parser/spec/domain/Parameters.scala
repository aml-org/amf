package amf.plugins.document.webapi.parser.spec.domain

import amf.plugins.document.webapi.annotations.{DefaultPayload, EndPointParameter}
import amf.plugins.domain.webapi.models.{Parameter, Payload}
import org.yaml.model.YMap

case class Parameters(query: Seq[Parameter] = Nil,
                      path: Seq[Parameter] = Nil,
                      header: Seq[Parameter] = Nil,
                      baseUri08: Seq[Parameter] = Nil,
                      body: Option[Payload] = None) {
  def merge(inner: Parameters): Parameters = {
    Parameters(merge(query, inner.query),
               merge(path, inner.path),
               merge(header, inner.header),
               merge(baseUri08, inner.baseUri08),
               merge(body, inner.body))
  }

  def add(inner: Parameters): Parameters = {
    Parameters(add(query, inner.query),
               add(path, inner.path),
               add(header, inner.header),
               add(baseUri08, inner.baseUri08),
               add(body, inner.body))
  }

  private def merge(global: Option[Payload], inner: Option[Payload]): Option[Payload] =
    inner.map(_.add(DefaultPayload())).orElse(global.map(_.copy()))

  private def add(global: Option[Payload], inner: Option[Payload]): Option[Payload] =
    inner.map(_.add(DefaultPayload())).orElse(global.map(_.copy()))

  private def merge(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
    val globalMap = global.map(p => p.name.value() -> p).toMap
    val innerMap  = inner.map(p => p.name.value()  -> p).toMap

    (globalMap ++ innerMap).values.toSeq
  }

  private def add(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
    val globalMap = global.map(p => p.name.value() -> p).toMap
    val innerMap  = inner.map(p => p.name.value()  -> p).toMap

    (globalMap ++ innerMap).values.toSeq
  }

  def nonEmpty: Boolean = query.nonEmpty || path.nonEmpty || header.nonEmpty || body.isDefined
}

object Parameters {
  def classified(path: String, params: Seq[Parameter], payload: Option[Payload] = None): Parameters = {
    var uriParams: Seq[Parameter]  = Nil
    var pathParams: Seq[Parameter] = Nil
    params.filter(_.isPath).foreach { param =>
      if (path.contains(s"{${param.name.value()}}"))
        pathParams ++= Seq(param)
      else uriParams ++= Seq(param)
    }
    Parameters(params.filter(_.isQuery) ++ pathParams, Nil, params.filter(_.isHeader), uriParams, payload)
  }
}

case class OasParameter(parameter: Parameter, payload: Payload, ast: Option[YMap] = None) {
  def isFormData: Boolean = parameter.isForm
  def isBody: Boolean     = parameter.isBody
  def isQuery: Boolean    = parameter.isQuery
  def isPath: Boolean     = parameter.isPath
  def isHeader: Boolean   = parameter.isHeader
}

object OasParameter {
  def apply(ast: YMap): OasParameter = OasParameter(Parameter(ast), Payload(ast), Some(ast))
}
