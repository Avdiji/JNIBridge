    inline jnibridge::internal::JniBridgePtrWrapper<${cType}>*
    jlong_to_${cTypeUnderscore}(jlong nativeHandle)
    {
${longToWrapperBody}
    }

//    inline std::string ${cTypeUnderscore}_to_jclassName(
//        jnibridge::internal::JniBridgePtrWrapper<${cType}> cType)
//    {
//${wrapperToJClassNameBody}
//    }