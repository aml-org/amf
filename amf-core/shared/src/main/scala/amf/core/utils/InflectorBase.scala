package amf.core.utils

import java.util.regex.{Matcher, Pattern}

import scala.collection.mutable.ListBuffer

object InflectorBase {

  case class Container(rule: String, replacement: String)

  private val singulars  = ListBuffer[Container]()
  private val plurals    = ListBuffer[Container]()
  private val irregulars = ListBuffer[Container]()

  addPlural("$", "s")
  addPlural("s$", "s")
  addPlural("(ax|test)is$", "$1es")
  addPlural("(octop|vir)us$", "$1i")
  addPlural("(alias|status)$", "$1es")
  addPlural("(bu)s$", "$1ses")
  addPlural("(buffal|tomat)o$", "$1oes")
  addPlural("([ti])um$", "$1a")
  addPlural("sis$", "ses")
  addPlural("(?:([^f])fe|([lr])f)$", "$1$2ves")
  addPlural("(hive)$", "$1s")
  addPlural("([^aeiouy]|qu)y$", "$1ies")
  addPlural("(x|ch|ss|sh)$", "$1es")
  addPlural("(matr|vert|ind)(?:ix|ex)$", "$1ices")
  addPlural("([m|l])ouse$", "$1ice")
  addPlural("^(ox)$", "$1en")
  addPlural("(quiz)$", "$1zes")

  addSingular("s$", "")
  addSingular("(n)ews$", "$1ews")
  addSingular("([ti])a$", "$1um")
  addSingular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1sis")
  addSingular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)sis$", "$1sis")
  addSingular("(^analy)ses$", "$1sis")
  addSingular("([^f])ves$", "$1fe")
  addSingular("(hive)s$", "$1")
  addSingular("(tive)s$", "$1")
  addSingular("([lr])ves$", "$1f")
  addSingular("([^aeiouy]|qu)ies$", "$1y")
  addSingular("(s)eries$", "$1eries")
  addSingular("(m)ovies$", "$1ovie")
  addSingular("(x|ch|ss|sh)es$", "$1")
  addSingular("([m|l])ice$", "$1ouse")
  addSingular("(bus)es$", "$1")
  addSingular("(bus)$", "$1")
  addSingular("(o)es$", "$1")
  addSingular("(shoe)s$", "$1")
  addSingular("(cris|ax|test)es$", "$1is")
  addSingular("(cris|ax|test)is$", "$1is")
  addSingular("(octop|vir)i$", "$1us")
  addSingular("(alias|status)es$", "$1")
  addSingular("(alias|status)$", "$1")
  addSingular("^(ox)en", "$1")
  addSingular("(vert|ind)ices$", "$1ex")
  addSingular("(matr)ices$", "$1ix")
  addSingular("(quiz)zes$", "$1")
  addSingular("(database)s$", "$1")

  addIrregular("person", "people")
  addIrregular("man", "men")
  addIrregular("child", "children")
  addIrregular("sex", "sexes")
  addIrregular("move", "moves")
  addIrregular("foot", "feet")
  addIrregular("tooth", "teeth")

  private val uncountables = List("equipment", "information", "rice", "money", "species", "series", "fish", "sheep")

  private def addPlural(rule: String, replacement: String): Unit = plurals += Container(rule, replacement)

  private def addSingular(rule: String, replacement: String): Unit = singulars += Container(rule, replacement)

  private def addIrregular(rule: String, replacement: String): Unit = irregulars += Container(rule, replacement)

  implicit class Inflector(val word: String) {

    /**
      * Replaces a found pattern in a word and returns a transformed word.
      *
      * @return Replaces a found pattern in a word and returns a transformed word. Null is pattern does not match.
      */
    private def gsub(word: String, rule: String, replacement: String): Option[String] = {
      val pattern: Pattern = Pattern.compile(rule, Pattern.CASE_INSENSITIVE)
      val matcher: Matcher = pattern.matcher(word)
      if (matcher.find()) Some(matcher.replaceFirst(replacement)) else None
    }

    private def findGSubInList(list: Seq[Container], word: String): Option[String] = {
      list
        .find(p => {
          gsub(word, p.rule, p.replacement).isDefined
        })
        .flatMap(p => gsub(word, p.rule, p.replacement))
    }

    def pluralize: String = {

      if (uncountables.contains(word)) word
      else {
        irregulars
          .find(i => i.rule.equalsIgnoreCase(word))
          .map(_.replacement)
          .orElse(findGSubInList(plurals, word))
          .getOrElse(word)
      }
    }

    def singularize: String = {

      if (uncountables.contains(word)) word
      else {
        irregulars
          .find(i => i.replacement.equalsIgnoreCase(word))
          .map(_.rule)
          .orElse(findGSubInList(singulars, word))
          .getOrElse(word)
      }
    }

    def camelToScoreSing(sign: String = "_"): String = {

      val upper = ListBuffer[Integer]()
      val bytes = word.getBytes()

      bytes.foreach(b => {
        if (b < 97 || b > 122) upper += bytes.indexOf(b)
      })

      val b = new StringBuffer(word)
      upper.foreach(index => {
        b.insert(index, sign)
      })

      b.toString.toLowerCase()
    }

    /**
      * Generates a camel case version of a phrase from dash.
      *
      * @param capitalizeFirstChar set to true if first character needs to be capitalized, false if not.
      * @return camel case version of dash.
      */
    def camelize(capitalizeFirstChar: Boolean = false): String = {

      val split = word.split("_")
      val tail = split.tail.map { x =>
        x.head.toUpper + x.tail
      }
      if (capitalizeFirstChar) split.head.capitalize + tail.mkString else split.head + tail.mkString
    }

    def decapitalize: String = {
      if (word.nonEmpty) {
        val c = word.toCharArray
        c.head.toLower + c.tail.mkString
      } else word
    }

  }
}