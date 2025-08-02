package com.jnibridge.mapper.primitives;

import com.jnibridge.annotations.typemapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

@Mapping(
        cType = "short",
        jniType = "jshort",
        inPath = "com/jnibridge/mappings/primitives/PrimitiveMapper.in.mapping",
        outPath = "com/jnibridge/mappings/primitives/PrimitiveMapper.out.mapping"
)
public class ShortMapper implements TypeMapper {
}
