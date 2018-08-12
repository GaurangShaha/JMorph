package com.jmorph.transformer;

public interface FieldTransformerContract<SOURCE, TARGET> {
    TARGET transform(SOURCE source);

    SOURCE reverseTransform(TARGET target);
}
