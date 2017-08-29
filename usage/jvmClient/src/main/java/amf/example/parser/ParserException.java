package amf.example.parser;

/**
  * User Exception used to exemplify how to handle amf exception.
 */
public class ParserException extends Exception {
    ParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
