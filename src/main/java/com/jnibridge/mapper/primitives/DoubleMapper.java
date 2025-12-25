package com.jnibridge.mapper.primitives;

import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for simple doubles.
 */
@Mapping(
        cType = "double",
        jniType = "jdouble",
        inPath = "com/jnibridge/mappings/primitives/PrimitiveMapper.in.mapping",
        outPath = "com/jnibridge/mappings/primitives/PrimitiveMapper.out.mapping"
)
public class DoubleMapper implements TypeMapper {
}
