package amf.plugins.document.webapi.contexts
import org.yaml.model._
import amf.core.parser._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.RefCounter
import amf.validations.ParserSideValidations.ExeededMaxYamlReferences

import scala.collection.mutable

class JsonSchemaAstIndex(root: YNode)(implicit val ctx: WebApiContext) {

  private val index: mutable.Map[String, YNode] = mutable.Map.empty
  private val refsCounter: RefCounter           = RefCounter(ctx)

  init()
  def init(): Unit = root.to[YMap] match {
    case Right(value) => indexMap(value)
    case _            => // ignore
  }

  private def indexMap(m: YMap): Unit = {
    // root base id
    val idOpt = m.key("id").orElse(m.key("$id")).flatMap(_.value.asScalar.map(_.text))
    idOpt.foreach { id =>
      index.put(id, m)
    }
    index.put("/", m)
    m.entries.filter(e => !e.value.asScalar.exists(p => p.text == "id" || p.text == "$id")).foreach { e =>
      index(e.key.as[YScalar].text, e.value, "", idOpt)
    }
  }

  def getNode(ref: String): Option[YNode] = index.get(ref)

  private def index(key: String, node: YNode, path: String, lastId: Option[String]): Unit = {

    if (refsCounter.exceedsThreshold(node)) {
      ctx.violation(
        ExeededMaxYamlReferences,
        lastId.getOrElse(""),
        "Exceeded maximum yaml references threshold"
      )
    } else {
      lastId.foreach { li =>
        index.put(li + "/" + key, node)
      }
      val newPath = if (path.nonEmpty) path + "/" + key else key
      index.put(newPath, node)
      node.tagType match {
        case YType.Map =>
          val m     = node.as[YMap]
          val idOpt = m.key("id").orElse(m.key("$id")).flatMap(_.value.asScalar.map(_.text))
          val newId = idOpt.map { newId =>
            if (newId.startsWith("#")) lastId.getOrElse("") + newId
            else newId
          // todo: id with uri htt[://example.com/last => http://example.com/newId
          }
          newId.foreach { index.put(_, node) }
          indexMap(newPath, newId, m)
        case YType.Seq =>
          val s = node.as[YSequence]
          indexSequence(newPath, lastId, s)
        case _ =>
      }
    }
  }

  private def indexSequence(path: String, lastId: Option[String], seq: YSequence): Unit = {
    seq.nodes.zipWithIndex.foreach { case (n, i) => index(i.toString, n, path, lastId) }
  }

  private def indexMap(path: String, lastId: Option[String], map: YMap): Unit = {
    map.entries.filter(e => !e.value.asScalar.exists(p => p.text == "id" || p.text == "$id")).foreach { e =>
      index(e.key.as[YScalar].text, e.value, path, lastId)
    }
  }
}
