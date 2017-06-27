package org.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.yaml.YamlElementGenerator;
import org.yaml.YAMLUtil;
import org.yaml.psi.YAMLKeyValue;

public class YAMLBlockMappingImpl extends YAMLMappingImpl {
  public YAMLBlockMappingImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void addNewKey(@NotNull YAMLKeyValue key) {
    final int indent = YAMLUtil.getIndentToThisElement(this);

    final YamlElementGenerator generator = YamlElementGenerator.apply(getProject());
    add(generator.createEol());
    if (indent > 0) {
      add(generator.createIndent(indent));
    }
    add(key);
  }
}
