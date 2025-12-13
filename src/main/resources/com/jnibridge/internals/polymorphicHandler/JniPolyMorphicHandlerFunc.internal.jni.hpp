    inline ${cType}*
    jlong_to_${cTypeUnderscore}(jlong nativeHandle)
    {
${longToWrapperBody}
    }

    inline std::shared_ptr<${cType}>
    jlong_to_shared_${cTypeUnderscore}(jlong nativeHandle)
    {
${longToWrapperBody}
    }


    inline std::unique_ptr<${cType}>
    jlong_to_unique_${cTypeUnderscore}(jlong nativeHandle)
    {
${longToWrapperBody}
    }


