package amf.maker

import amf.builder.EndPointBuilder
import amf.model.EndPoint
import amf.parser.ASTNode
import amf.remote.Vendor

import scala.util.matching.Regex

/**
  * Created by martin.gutierrez on 7/10/17.
  */
class EndPointMaker(content: ASTNode[_], vendor: Vendor) extends ListMaker[EndPoint](vendor) {

  override def make: EndPoint           = ???
  override def makeList: List[EndPoint] = extractFromChildren(content).map(_.build).toList

  def extractFromChildren(endPoint: ASTNode[_], parent: Option[EndPointBuilder] = None): Seq[EndPointBuilder] = {
    endPointPathEntries(endPoint)
      .map(getBuilder(_, parent))
  }

  private def getBuilder(entry: ASTNode[_], parent: Option[EndPointBuilder]): EndPointBuilder = {
    val builder = builders.endPoint
    val key     = entry.child(0).content
    val value   = entry.child(1)
    builder
      .withPath(key)
      .withName(findValue(value, "displayName"))
      .withDescription(findValue(value, "description"))
      .withChildren(extractFromChildren(value, Some(builder)).toList)
  }

  private def endPointPathEntries(endPoint: ASTNode[_]): Seq[ASTNode[_]] = {
    endPoint.children
      .flatMap {
        case child if EndPointMaker.path.unapplySeq(child.child(0).content).isDefined => Some(child)
        case _                                                                        => None
      }
  }
}

object EndPointMaker {
  val path: Regex = "/.*".r

  def apply(node: ASTNode[_], vendor: Vendor): EndPointMaker = new EndPointMaker(node, vendor)
}
