package amf.plugins.document.webapi.parser.spec.jsonschema

import java.net.URI

import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import org.yaml.model.{YNode, YType}

trait ResolutionScope {

  protected val path: String

  def resolve(key: String, node: YMapEntryLike): Seq[(String, YMapEntryLike)]

  def getNext(entryLike: YMapEntryLike): ResolutionScope =
    entryLike.key
      .flatMap(_.asScalar)
      .map(_.text)
      .map(key => getInstance(path + "/" + key))
      .getOrElse(this)

  protected def getInstance(path: String): ResolutionScope
}

case class LexicalResolutionScope(protected val path: String = "") extends ResolutionScope {

  override def resolve(key: String, node: YMapEntryLike): Seq[(String, YMapEntryLike)] = {
    val pathToKey = s"$path/$key"
    Seq((pathToKey, node))
  }

  override protected def getInstance(path: String): ResolutionScope = LexicalResolutionScope(path)
}

abstract class CommonDraft4To7ResolutionScope(private val baseUri: URI, protected val path: String = "")
    extends ResolutionScope {
  override def getNext(entryLike: YMapEntryLike): ResolutionScope = {
    val optionalIdNode      = getIdKey(entryLike)
    val optionalIdToResolve = optionalIdNode.flatMap(_.asScalar).map(_.text)
    optionalIdToResolve match {
      case Some(id) if hasFragment(formatUri(id)) => super.getNext(entryLike)
      case Some(id) =>
        resolveIdValue(id).map(resolved => getInstanceWithNewBase(resolved)).getOrElse(super.getNext(entryLike))
      case _ => super.getNext(entryLike)
    }
  }

  private def hasFragment(unresolvedId: String) =
    try {
      Option(new URI(unresolvedId).getFragment).isDefined
    } catch {
      case _: Throwable => false
    }

  override def resolve(key: String, entryLike: YMapEntryLike): Seq[(String, YMapEntryLike)] = {
    val currentEntry = resolveIdValue(formatFragment(s"$path/$key")).map(uri => (uri.toString, entryLike)).toSeq
    currentEntry ++ entryFromIdNode(entryLike)
  }

  protected def entryFromIdNode(entryLike: YMapEntryLike): Seq[(String, YMapEntryLike)] = {
    lookAheadForId(entryLike)
      .flatMap { resolveIdValue }
      .map { uri =>
        (formatUri(uri.toString), entryLike)
      }.toSeq
  }

  protected def lookAheadForId(entryLike: YMapEntryLike): Option[String] = {
    val optionalIdNode = getIdKey(entryLike)
    optionalIdNode.flatMap(_.asScalar).map(_.text)
  }

  protected def resolveIdValue(id: String): Option[URI] =
    try {
      Some(baseUri.resolve(id))
    } catch {
      case _: Throwable => None
    }

  protected def getIdKey(entryLike: YMapEntryLike): Option[YNode] = {
    entryLike.value.tagType match {
      case YType.Map => getIdFromMap(entryLike.asMap.map)
      case _         => None
    }
  }

  protected def getIdFromMap(map: Map[YNode, YNode]): Option[YNode]

  protected def formatUri(uri: String): String = if (uri.endsWith("#")) uri.dropRight(1) else uri

  protected def formatFragment(fragment: String): String = if (fragment.startsWith("#")) fragment else s"#$fragment"

  protected def getInstanceWithNewBase(path: URI): ResolutionScope
}

case class Draft4ResolutionScope(private val baseUri: URI, override val path: String = "")
    extends CommonDraft4To7ResolutionScope(baseUri, path) {
  protected def getIdFromMap(map: Map[YNode, YNode]): Option[YNode] = map.get("id")
  override protected def getInstance(path: String): ResolutionScope = Draft4ResolutionScope(baseUri, path)
  protected def getInstanceWithNewBase(path: URI): ResolutionScope  = Draft4ResolutionScope(path)
}

case class Draft7ResolutionScope(private val baseUri: URI, override val path: String = "")
    extends CommonDraft4To7ResolutionScope(baseUri, path) {
  protected def getIdFromMap(map: Map[YNode, YNode]): Option[YNode] = map.get("$id")
  override protected def getInstance(path: String): ResolutionScope = Draft7ResolutionScope(baseUri, path)
  protected def getInstanceWithNewBase(path: URI): ResolutionScope  = Draft7ResolutionScope(path)
}

case class Draft2019ResolutionScope(private val baseUri: URI, override val path: String = "")
    extends CommonDraft4To7ResolutionScope(baseUri, path) {

  protected def getIdFromMap(map: Map[YNode, YNode]): Option[YNode]         = map.get("$id")
  override protected def getInstanceWithNewBase(path: URI): ResolutionScope = Draft2019ResolutionScope(path)
  override protected def getInstance(path: String): ResolutionScope         = Draft2019ResolutionScope(baseUri, path)

  override protected def entryFromIdNode(entryLike: YMapEntryLike): Seq[(String, YMapEntryLike)] = {
    val nextOrSameBaseUriId = lookAheadForId(entryLike).flatMap { resolveIdValue }.getOrElse(this.baseUri)
    val optionalAnchor      = lookAheadForAnchor(entryLike)

    val result = optionalAnchor match {
      case Some(anchor) =>
        val entryFromId =
          if (isDifferentThanBase(nextOrSameBaseUriId)) Some(creatNodeFrom(nextOrSameBaseUriId, entryLike)) else None
        entryFromId.toSeq ++ resolveAnchor(s"#$anchor", nextOrSameBaseUriId).map(uri => (uri.toString, entryLike)).toSeq
      case _ if isDifferentThanBase(nextOrSameBaseUriId) => Some(creatNodeFrom(nextOrSameBaseUriId, entryLike)).toSeq
      case _                                         => Seq()
    }
    result
  }

  private def isDifferentThanBase(nextOrSameBaseUriId: URI) = !nextOrSameBaseUriId.equals(baseUri)
  private def creatNodeFrom(nextBaseUri: URI, entryLike: YMapEntryLike) = (formatUri(nextBaseUri.toString), entryLike)

  private def lookAheadForAnchor(entryLike: YMapEntryLike): Option[String] = {
    val optionalAnchorNode = getAnchorKey(entryLike)
    optionalAnchorNode.flatMap(_.asScalar).map(_.text)
  }

  private def getAnchorKey(entryLike: YMapEntryLike): Option[YNode] = {
    entryLike.value.tagType match {
      case YType.Map => entryLike.asMap.map.get("$anchor")
      case _         => None
    }
  }

  protected def resolveAnchor(anchor: String, baseUri: URI): Option[URI] =
    try {
      Some(baseUri.resolve(anchor))
    } catch {
      case _: Throwable => None
    }
}