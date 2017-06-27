package org.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.yaml.YamlElementGenerator;
import org.yaml.YAMLTokenTypes;
import org.yaml.psi.YAMLFile;
import org.yaml.psi.YAMLKeyValue;
import org.yaml.psi.YAMLMapping;

/**
 * @author oleg
 */
public class YAMLHashImpl extends YAMLMappingImpl implements YAMLMapping {
  public YAMLHashImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  protected void addNewKey(@NotNull YAMLKeyValue key) {
    PsiElement anchor = null;
    for (PsiElement child = getLastChild(); child != null; child = child.getPrevSibling()) {
      final IElementType type = child.getNode().getElementType();
      if (type == YAMLTokenTypes.COMMA || type == YAMLTokenTypes.LBRACE) {
        anchor = child;
      }
    }
    
    addAfter(key, anchor);

    final YAMLFile dummyFile = YamlElementGenerator.apply(getProject()).createDummyYamlWithText("{,}");
    final PsiElement comma = dummyFile.findElementAt(1);
    assert comma != null && comma.getNode().getElementType() == YAMLTokenTypes.COMMA;
    
    addAfter(comma, key);
  }

  @Override
  public String toString() {
    return "YAML hash";
  }
}