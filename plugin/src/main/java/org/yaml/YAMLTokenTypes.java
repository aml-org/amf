package org.yaml;

/**
 * @author: oleg
 */
public interface YAMLTokenTypes {
  YamlElementType COMMENT = new YamlElementType("comment");
  YamlElementType WHITESPACE = new YamlElementType("whitespace");
  YamlElementType INDENT = new YamlElementType("indent");
  YamlElementType EOL = new YamlElementType("Eol");

  YamlElementType LBRACE = new YamlElementType("{");
  YamlElementType RBRACE = new YamlElementType("}");
  YamlElementType LBRACKET = new YamlElementType("[");
  YamlElementType RBRACKET = new YamlElementType("]");
  YamlElementType COMMA = new YamlElementType(",");
  YamlElementType COLON = new YamlElementType(":");
  YamlElementType QUESTION = new YamlElementType("?");

  YamlElementType DOCUMENT_MARKER = new YamlElementType("---");
  YamlElementType DOCUMENT_END = new YamlElementType("...");
  YamlElementType SEQUENCE_MARKER = new YamlElementType("-");

  YamlElementType TAG = new YamlElementType("tag");

  YamlElementType SCALAR_KEY = new YamlElementType("scalar key");
  YamlElementType TEXT = new YamlElementType("text");

  YamlElementType SCALAR_STRING = new YamlElementType("scalar string");
  YamlElementType SCALAR_DSTRING = new YamlElementType("scalar dstring");

  YamlElementType SCALAR_LIST = new YamlElementType("scalar list");
  YamlElementType SCALAR_TEXT = new YamlElementType("scalar text");
}
