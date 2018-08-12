package com.jmorph.samaple;

import com.jmorph.transformer.FieldTransformerContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MillisToDateStringTransformer implements FieldTransformerContract<Long, String> {

    private final String DATE_FORMAT = "dd MMM yyyy";

    @Override
    public String transform(Long millis) {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date(millis));
    }

    @Override
    public Long reverseTransform(String date) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(date).getTime();
        } catch (ParseException e) {
            return (long) -1;
        }
    }
}
