package org.mulesoft.lexer

/**
  * Created by emilio.gabeiras on 6/5/17.
  */
object Main {
    def main(args: Array[String]) {
        val l = new CharSequenceLexerInput(
            """Hola que
              | tal 😀
              | Como va
              | """.stripMargin)
         while (l.current != LexerInput.EofChar) {
             println("%d %d = %c %simpleMultiDocument.yaml %simpleMultiDocument.yaml %simpleMultiDocument.yaml" format(l.line, l.column, if (l.current == '\n') 'n' else l.current , l.current, l.lookAhead(1), l.lookAhead(2)))
             l.consume()
         }
    }
}
