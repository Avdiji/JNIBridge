package com.jnibridge.mapper.primitives;

import com.jnibridge.annotations.typemapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

@Mapping(
        cType = "float",
        jniType = "jfloat",
        inPath = "com/jnibridge/mappings/primitives/PrimitiveMapper.in.mapping",
        outPath = "com/jnibridge/mappings/primitives/PrimitiveMapper.out.mapping"
)
public class FloatMapper implements TypeMapper {
}
