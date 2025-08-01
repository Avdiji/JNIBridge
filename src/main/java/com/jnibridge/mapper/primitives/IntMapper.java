package com.jnibridge.mapper.primitives;

import com.jnibridge.annotations.typemapping.Mapping;
import com.jnibridge.mapper.TypeMapper;

/**
 * Mapper for simple integers.
 */
@Mapping(
        cType = "int",
        jniType = "jint",
        inPath = "com/jnibridge/mappings/primitives/IntMapper.in.mapping",
        outPath = "com/jnibridge/mappings/primitives/IntMapper.out.mapping"
)
public class IntMapper implements TypeMapper {}
