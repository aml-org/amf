package amf.plugins.document.webapi.parser.spec.domain

import amf.plugins.document.webapi.annotations.{DefaultPayload, EndPointParameter}
import amf.plugins.domain.webapi.models.{Parameter, Payload}
import org.yaml.model.YMap

case class Parameters(query: Seq[Parameter] = Nil,
                      path: Seq[Parameter] = Nil,
                      header: Seq[Parameter] = Nil,
                      body: Option[Payload] = None) {
  def merge(inner: Parameters): Parameters = {
    Parameters(merge(query, inner.query),
               merge(path, inner.path),
               merge(header, inner.header),
               merge(body, inner.body))
  }

  def add(inner: Parameters): Parameters = {
    Parameters(add(query, inner.query), add(path, inner.path), add(header, inner.header), add(body, inner.body))
  }

  private def merge(global: Option[Payload], inner: Option[Payload]): Option[Payload] =
    inner.map(_.add(DefaultPayload())).orElse(global.map(_.copy()))

  private def add(global: Option[Payload], inner: Option[Payload]): Option[Payload] =
    inner.map(_.add(DefaultPayload())).orElse(global.map(_.copy()))

  private def merge(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
    val globalMap = global.map(p => p.name -> p).toMap
    val innerMap  = inner.map(p => p.name  -> p).toMap

    (globalMap ++ innerMap).values.toSeq
  }

  private def add(global: Seq[Parameter], inner: Seq[Parameter]): Seq[Parameter] = {
    val globalMap = global.map(p => p.name -> p).toMap
    val innerMap  = inner.map(p => p.name  -> p).toMap

    (globalMap ++ innerMap).values.toSeq
  }

  def nonEmpty: Boolean = query.nonEmpty || path.nonEmpty || header.nonEmpty || body.isDefined
}

object Parameters {
  def classified(params: Seq[Parameter], payload: Option[Payload] = None): Parameters = {
    Parameters(params.filter(_.isQuery), params.filter(_.isPath), params.filter(_.isHeader), payload)
  }
}

case class OasParameter(parameter: Parameter, payload: Payload) {
  def isBody: Boolean   = parameter.isBody
  def isQuery: Boolean  = parameter.isQuery
  def isPath: Boolean   = parameter.isPath
  def isHeader: Boolean = parameter.isHeader
}

object OasParameter {
  def apply(ast: YMap): OasParameter = OasParameter(Parameter(ast), Payload(ast))
}
