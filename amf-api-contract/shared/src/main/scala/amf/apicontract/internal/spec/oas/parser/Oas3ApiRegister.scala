package amf.apicontract.internal.spec.oas.parser

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{Callback, Parameter, Request, Response, TemplatedLink}
import amf.apicontract.internal.spec.common.parser.{WebApiContext, WebApiRegister}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.document.Module
import amf.core.client.scala.model.domain.{DomainElement, NamedDomainElement, Shape}
import amf.core.internal.remote.Spec
import amf.shapes.client.scala.model.domain.Example

class Oas3ApiRegister()(implicit ctx: WebApiContext) extends WebApiRegister {

  override def onCollect(alias: String, unit: BaseUnit): Unit = unit match {
    case module: Module if isOas3Component(module) =>
      indexModule(alias, module) { (module, declarations) =>
        zipWithComponentKey(module.declares).foreach(tuple => declarations += (tuple._1, tuple._2))
      }
    case _ => super.onCollect(alias, unit)
  }

  private def zipWithComponentKey(declared: Seq[DomainElement]) = declared
    .map(declared => (declared, componentMapping(declared)))
    .collect { case (declared, Some(indexKey)) => (indexKey, declared) }

  private def isOas3Component(module: Module) = module.processingData.sourceSpec.option().contains(Spec.OAS30.id)

  private def componentMapping(element: DomainElement) = element match {
    case shape: Shape                                 => Some(withComponentPrefix("schemas", shape))
    case scheme: SecurityScheme                       => Some(withComponentPrefix("securitySchemes", scheme))
    case request: Request                             => Some(withComponentPrefix("requestBodies", request))
    case parameter: Parameter if !isHeader(parameter) => Some(withComponentPrefix("parameters", parameter))
    case link: TemplatedLink                          => Some(withComponentPrefix("links", link))
    case header: Parameter                            => Some(withComponentPrefix("headers", header))
    case example: Example                             => Some(withComponentPrefix("examples", example))
    case callback: Callback                           => Some(withComponentPrefix("callbacks", callback))
    case response: Response                           => Some(withComponentPrefix("responses", response))
    case _                                            => None
  }

  private def withComponentPrefix(component: String, element: NamedDomainElement) =
    s"/components/$component/${element.name.value()}"

  private def isHeader(parameter: Parameter) = {
    parameter.binding.option().contains("header")
  }
}
