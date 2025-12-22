#pragma once

#include "${internalHandlePath}"
#include <jni.h>

namespace jnibridge::internal {

${exceptionFunc}

    static jclass getDefaultExceptionClass(JNIEnv *env, const std::exception& e) {
        if (dynamic_cast<const std::invalid_argument*>(&e)) {
            return env->FindClass("java/lang/IllegalArgumentException");
        }
        else if (dynamic_cast<const std::out_of_range*>(&e)) {
            return env->FindClass("java/lang/IndexOutOfBoundsException");
        }
        else if(dynamic_cast<const std::logic_error*>(&e)) {
            return env->FindClass("java/lang/IllegalStateException");
        }
        else if (dynamic_cast<const std::runtime_error*>(&e)) {
            return env->FindClass("java/lang/RuntimeException");
        }
        return env->FindClass("java/lang/Exception");
    }


    static jthrowable createJavaException(JNIEnv *env, const std::exception& e, jthrowable cause = nullptr) {
        // extract the right JavaException-class from the corresponding C++ exception.
        jclass exClass = getMappedExceptionClass(env, e);
        exClass = exClass ? exClass : getDefaultExceptionClass(env, e);

        // exception constructor
        jstring message = env->NewStringUTF(e.what());
        jmethodID constructor = env->GetMethodID(exClass, "<init>", "(Ljava/lang/String;)V");

        if(cause) {
            // construct exception with cause
            jmethodID initCause = env->GetMethodID(exClass, "initCause", "(Ljava/lang/Throwable;)Ljava/lang/Throwable;");
            jobject exWithCause = static_cast<jthrowable>(env->NewObject(exClass, constructor, message));
            env->CallVoidMethod(exWithCause, initCause, cause);
            return static_cast<jthrowable>(exWithCause);
        }
        // construct exception with no cause
        return static_cast<jthrowable>(env->NewObject(exClass, constructor, message));
    }

    static jthrowable handleException(JNIEnv *env, const std::exception& e) {
        try {
            std::rethrow_if_nested(e);
        } catch(std::exception &nested) {
            jthrowable cause = static_cast<jthrowable>(handleException(env, nested));
            return createJavaException(env, e, cause);
        }
        return createJavaException(env, e);
    }

} // namespace jnibridge::internal