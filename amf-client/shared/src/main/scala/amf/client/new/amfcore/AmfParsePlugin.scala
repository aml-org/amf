package amf.client.`new`.amfcore

import amf.core.model.document.BaseUnit
import amf.core.remote.Vendor
import amf.core.validation.AMFValidationReport
import org.yaml.model.YDocument

import scala.concurrent.Future

sealed trait AmfPlugin[T] extends Ordering[AmfPlugin[_]] {
  val id: String
  def apply(element: T): Boolean
  // test for collisions?
  def priority: PluginPriority //?

  override def compare(x: AmfPlugin[_], y: AmfPlugin[_]): Int = {
    x.priority.priority compareTo (y.priority.priority)
  }
}

trait AmfParsePlugin extends AmfPlugin[YDocument] {
  def parse: Future[BaseUnit]
  val supportedVendors: Seq[Vendor]
  def apply(element: YDocument, vendor: Vendor) = supportedVendors.contains(vendor) && apply(element)
}

trait AmfResolvePlugin extends AmfPlugin[BaseUnit] {
  def resolve: BaseUnit
}

trait AmfValidatePlugin extends AmfPlugin[BaseUnit] {
  def validate: AMFValidationReport
}

sealed case class PluginPriority(priority: Int) {}

object HighPriority extends PluginPriority(1)

object NormalPriority extends PluginPriority(2)

object LowPriority extends PluginPriority(3)
