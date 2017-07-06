package org.mulesoft.lexer

import scala.annotation.StaticAnnotation

/**
  * An Annotation to specify that a given lexer function will fail (return false) without changing the lexer state
  * (For the time being is just a hint, we can use it in the future to optimize the lexer)
  */
final class failfast extends StaticAnnotation
