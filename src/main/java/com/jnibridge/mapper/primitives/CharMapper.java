package com.jnibridge.mapper.primitives;

import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for simple chars.
 */
@Mapping(
        cType = "char",
        jniType = "jchar",
        inPath = "com/jnibridge/mappings/primitives/PrimitiveMapper.in.mapping",
        outPath = "com/jnibridge/mappings/primitives/PrimitiveMapper.out.mapping"
)
public class CharMapper implements TypeMapper {
}
