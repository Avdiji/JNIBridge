#pragma once

#include "${internalHandlePath}"
#include <jni.h>

namespace jnibridge::internal {

${exceptionFunc}

    /**
     * Determines the default Java exception class for a given C++ exception.
     *
     * If no explicit exception mapping exists, this function selects a Java
     * exception type based on the dynamic type of the C++ exception.
     *
     * Unknown exception types fall back to {@code java.lang.Exception}.
     *
     * @param env the active JNI environment.
     * @param e the C++ exception to classify.
     * @return a {@code jclass} representing the selected Java exception type.
     */
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


    /**
     * Creates a Java exception object corresponding to a given C++ exception.
     * If a cause is provided, it is attached via javas {@code Throwable.initCause}.
     *
     * @param env the active JNI environment
     * @param e the C++ exception to translate
     * @param cause an optional Java throwable representing the exception cause
     * @return a newly constructed Java {@code Throwable}
     */
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

    /**
     * Handles a C++ exception and all nested exceptions by translating them
     * into a Java exception chain.
     *
     * Nested C++ exceptions (created via {@code std::throw_with_nested})
     * are recursively unwrapped and translated, preserving the original
     * exception hierarchy using Java exception causes.
     *
     * @param env the active JNI environment
     * @param e the C++ exception to handle
     * @return a Java {@code Throwable} mirroring the C++ exception hierarchy
     */
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