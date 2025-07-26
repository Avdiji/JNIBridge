package com.jnibridge.mapper.primitives;

import com.jnibridge.annotations.TypeMapping;
import com.jnibridge.mapper.TypeMapper;

@TypeMapping(
        cType = "int",
        jniType = "int",
        inPath = "com/jnibridge/mappings/primitives/IntMapper.in.mapping",
        outPath = "com/jnibridge/mappings/primitives/IntMapper.out.mapping"
)
public class IntMapper implements TypeMapper {}
