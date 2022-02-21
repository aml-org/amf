package amf.apiinstance.client.scala.model.domain

import amf.apiinstance.internal.metamodel.domain.UpstreamServiceModel
import amf.apiinstance.internal.metamodel.domain.UpstreamServiceModel._
import amf.core.client.scala.model.domain.{DomainElement, NamedDomainElement}
import amf.core.internal.metamodel.{Field, Obj}
import amf.core.internal.parser.domain.{Annotations, Fields}
import org.yaml.model.{YMap, YNode}

case class UpstreamService(fields: Fields, annotations: Annotations) extends DomainElement with NamedDomainElement {
  override def meta: Obj = UpstreamServiceModel

  def hosts: Seq[String] = fields.field(Host)
  def withHosts(ports: Seq[String]): UpstreamService.this.type = set(Host, ports)

  def routes: Seq[Route] = fields.field(UpstreamServiceModel.Route)
  def withRoutes(routes: Seq[Route]): UpstreamService.this.type = setArray(UpstreamServiceModel.Route, routes)
  def withRoute(route: Route): UpstreamService.this.type = {
    val newRoutes = routes ++ Seq(route)
    withRoutes(newRoutes)
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override private[amf] def componentId = s"/upstream/${name}"

  override protected def nameField: Field = UpstreamServiceModel.Name
}

object UpstreamService {
  def apply(): UpstreamService = apply(Annotations())

  def apply(ast: YMap): UpstreamService = apply(Annotations(ast))

  def apply(node: YNode): UpstreamService = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): UpstreamService = UpstreamService(Fields(), annotations)
}