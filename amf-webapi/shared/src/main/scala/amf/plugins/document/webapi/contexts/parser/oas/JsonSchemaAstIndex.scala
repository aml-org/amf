package amf.plugins.document.webapi.contexts.parser.oas

import org.yaml.model._
import amf.core.parser._
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.utils.AliasCounter
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.validations.ParserSideValidations.ExeededMaxYamlReferences

import scala.collection.mutable

object JsonSchemaAstIndex {
  def apply(root: YNode)(implicit ctx: WebApiContext) =
    new JsonSchemaAstIndex(root, AliasCounter(ctx.options.getMaxYamlReferences))
  def apply(root: YNode, refsCounter: AliasCounter)(implicit ctx: WebApiContext) =
    new JsonSchemaAstIndex(root, refsCounter)
}

class JsonSchemaAstIndex(root: YNode, val refsCounter: AliasCounter)(implicit val ctx: WebApiContext) {

  private val index: mutable.Map[String, Either[YNode, YMapEntry]] = mutable.Map.empty

  init()
  def init(): Unit = root.to[YMap] match {
    case Right(value) => indexMap(value)
    case _            => // ignore
  }

  private def indexMap(m: YMap): Unit = {
    val maybeEntry = m.key("id").orElse(m.key("$id"))
    // root base id
    val idOpt = maybeEntry.flatMap(_.value.asScalar.map(_.text))
    idOpt.foreach { id =>
      index.put(id, maybeEntry.map(Right(_)).getOrElse(Left(m)))
    }
    index.put("/", Left(m))
    m.entries.filter(e => !e.value.asScalar.exists(p => p.text == "id" || p.text == "$id")).foreach { e =>
      index(e.key.as[YScalar].text, e.value, "", idOpt, maybeEntry)
    }
  }

  def getNodeAndEntry(ref: String): Option[Either[YNode, YMapEntry]] = index.get(ref)

  private def index(key: String,
                    node: YNode,
                    path: String,
                    lastId: Option[String],
                    maybeEntry: Option[YMapEntry]): Unit = {

    if (refsCounter.exceedsThreshold(node)) {
      ctx.violation(
        ExeededMaxYamlReferences,
        lastId.getOrElse(""),
        "Exceeded maximum yaml references threshold"
      )
    } else {
      lastId.foreach { li =>
        index.put(li + "/" + key, maybeEntry.map(Right(_)).getOrElse(Left(node)))
      }
      val newPath = if (path.nonEmpty) path + "/" + key else key
      index.put(newPath, maybeEntry.map(Right(_)).getOrElse(Left(node)))
      node.tagType match {
        case YType.Map =>
          val m     = node.as[YMap]
          val idOpt = m.key("id").orElse(m.key("$id")).flatMap(_.value.asScalar.map(_.text))
          val newId = idOpt.map { newId =>
            if (newId.startsWith("#")) lastId.getOrElse("") + newId
            else newId
          // todo: id with uri htt[://example.com/last => http://example.com/newId
          }
          newId.foreach { index.put(_, maybeEntry.map(Right(_)).getOrElse(Left(node))) }
          indexMap(newPath, newId, m)
        case YType.Seq =>
          val s = node.as[YSequence]
          indexSequence(newPath, lastId, s)
        case _ =>
      }
    }
  }

  private def indexSequence(path: String, lastId: Option[String], seq: YSequence): Unit = {
    seq.nodes.zipWithIndex.foreach { case (n, i) => index(i.toString, n, path, lastId, None) }
  }

  private def indexMap(path: String, lastId: Option[String], map: YMap): Unit = {
    map.entries.filter(e => !e.value.asScalar.exists(p => p.text == "id" || p.text == "$id")).foreach { e =>
      index(e.key.as[YScalar].text, e.value, path, lastId, Some(e))
    }
  }
}
