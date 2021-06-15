package amf.plugins.document.apicontract.parser.spec.jsonschema

import amf.core.internal.parser.YMapOps
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import org.yaml.model.{YMap, YNode, YSequence, YType}

import java.net.URI
import scala.annotation.tailrec
import scala.util.Try

trait ReferenceResolver {
  def resolve(reference: String, index: Map[String, YMapEntryLike]): Option[YMapEntryLike]
}

object FragmentTraversingResolver extends ReferenceResolver {

  override def resolve(reference: String, index: Map[String, YMapEntryLike]): Option[YMapEntryLike] =
    Try(new URI(reference)).toOption.flatMap { uri =>
      val fragment = uri.getRawFragment
      val baseUri  = reference.stripSuffix(s"#$fragment")
      val baseNode = index.get(baseUri)
      baseNode.flatMap { node =>
        val keys = keysOfFragment(fragment)
        keys.flatMap(traverseFragmentKeys(node, _))
      }
    }

  @tailrec
  private def traverseFragmentKeys(entry: YMapEntryLike, keys: List[String]): Option[YMapEntryLike] =
    keys.headOption match {
      case Some(key) =>
        val value = entry.value
        val newEntry = value.tag.tagType match {
          case YType.Map =>
            value.asOption[YMap].flatMap(_.key(key)).map(YMapEntryLike(_))
          case YType.Seq =>
            nodeFromSeq(key, value.asOption[YSequence]).map(YMapEntryLike(_))
          case _ => None
        }
        newEntry match {
          case Some(result) => traverseFragmentKeys(result, keys.tail)
          case None         => None
        }
      case None => Some(entry)
    }

  private def nodeFromSeq(key: String, currentSeq: Option[YSequence]): Option[YNode] =
    for {
      index <- Try(key.toInt).toOption
      seq   <- currentSeq
      node  <- seq.nodes.lift(index)
    } yield node

  private def keysOfFragment(fragment: String): Option[List[String]] = {
    if (fragment.isEmpty) Some(Nil)
    else if (fragment.startsWith("/")) Some(fragment.stripPrefix("/").split("/").toList)
    else None
  }
}
