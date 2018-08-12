package com.jmorph.samaple;

import com.jmorph.transformer.FieldTransformerContract;

public class DepartmentIdToNameTransformer implements FieldTransformerContract<Integer, String> {
    @Override
    public String transform(Integer integer) {
        return integer == 2 ? "Tester" : "Developer";
    }

    @Override
    public Integer reverseTransform(String s) {
        return s.equals("Tester") ? 2 : 1;
    }
}
