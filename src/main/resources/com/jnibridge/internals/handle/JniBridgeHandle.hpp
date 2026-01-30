#pragma once

#include <jni.h>

#include <memory>
#include <string>
#include <variant>
#include <stdexcept>
#include <vector>

${internal_includes}

namespace jnibridge::internal {

    /**
     * Throws a JniBridgeException on the Java side from native code.
     *
     * <p>Sets a pending Java exception via JNI. The caller must return immediately
     * after invoking this method.</p>
     *
     * @param env     JNI environment for the current thread
     * @param message exception message
     */
    inline void throwJniBridgeExceptionJava(JNIEnv *env, const std::string& message) {
        env->ThrowNew(env->FindClass("com/jnibridge/exception/JniBridgeException"), message.c_str());
    }

    /**
     * Returns a safe default value for JNI methods after a Java exception
     * has been thrown.
     *
     * The returned value satisfies the native method signature but is
     * semantically meaningless. It must not be observed by Java code
     * while an exception is pending.
     *
     * @tparam T
     *         JNI return type.
     */
    template <typename T>
    static inline T jniDefaultReturn();

    template <>
    inline void jniDefaultReturn<void>() { }
    template <>
    inline jboolean jniDefaultReturn<jboolean>() { return JNI_FALSE; }
    template <>
    inline jint jniDefaultReturn<jint>() { return 0; }
    template <>
    inline jlong jniDefaultReturn<jlong>() { return 0; }
    template <>
    inline jshort jniDefaultReturn<jshort>() { return 0; }
    template <>
    inline jbyte jniDefaultReturn<jbyte>() { return 0; }
    template <>
    inline jchar jniDefaultReturn<jchar>() { return 0; }
    template <>
    inline jfloat jniDefaultReturn<jfloat>() { return 0.0f; }
    template <>
    inline jdouble jniDefaultReturn<jdouble>() { return 0.0; }
    template <typename T>
    inline T jniDefaultReturn() { return nullptr; } // objects, arrays, etc.

    /**
     * Base exception type for errors raised by the JNI bridge.
     *
     * Use this exception for bridge-level failures such as.
     */
    class JniBridgeError : public std::runtime_error {
    public:

        enum class Code {
            Unknown, // unknown exception causes...
            DestroyedHandle, // When the Handle has already been destroyed.
            NotASharedPtr, // When the wrapped type is supposed to be a sharedPtr but isn't.
            NotAUniquePtr, // When the wrapped type is supposed to be a uniquePtr but isn't.
            PassingUniquePtr, // When theres an attempt to pass a uniquePtr to a function.
            CorruptHandleStorage, // When the Handle-storage is corrupted.
            InvalidCallingInstance, // The native handle must not be invalid.
            InvalidEnumImplementation // If the passed enum does not implement toInt and fromInt methods.
        };

        /**
         * Creates a bridge exception with the given message and optional code.
         *
         * @param message Human-readable error message.
         * @param code Optional machine-readable category.
         */
        explicit JniBridgeError(const Code code = Code::Unknown)
            : std::runtime_error(getMessageFromCode(code)) {}

    private:
        /**
         * This function provides a stable, user-facing explanation of bridge-level failures.
         *
         * @param code The JNIBridge error code describing the failure category.
         * @return A human-readable error message corresponding to the given error code.
         */
        static const std::string getMessageFromCode(const Code &code) {
            switch(code) {
                case Code::DestroyedHandle: return "The underlying JNIBridge-handle has been destroyed or was never initialized.";
                case Code::CorruptHandleStorage: return "The underlying JNIBridge-handle has been corrupted.";
                case Code::NotASharedPtr: return "Illegal handle state: shared ownership expected.";
                case Code::NotAUniquePtr: return "Illegal handle state: unique ownership expected.";
                case Code::PassingUniquePtr: return "JNIBridge does not allow passing std::unique_ptr to other functions.";
                case Code::InvalidCallingInstance: return "Cannot perform the operation. Native Handle might be null/destroyed/invalid.";
                case Code::InvalidEnumImplementation: return "Bridged enums must implement toInt and fromInt functions.";

                case Code::Unknown:
                default: return "An unknown JNIBridge-Error has occurred.";
            }
        }
    };

    /**
     * Base class for all native handles.
     *
     * Each Java object created by JNIBridge stores a pointer to a BaseHandle
     * instance, which manages access to the underlying native object.
     */
    class BaseHandle {
    public:
        virtual ~BaseHandle() = default;

        /**
         * Describes how a native instance is stored and owned by a Handle.
         *
         * - RawOwned    : Handle owns a raw pointer and deletes it on destruction
         * - RawBorrowed : Handle stores a non-owning raw pointer
         * - Shared      : Handle stores a std::shared_ptr<T>
         * - Unique      : Handle stores a std::unique_ptr<T>
         */
        enum StorageStrategy {
            RawOwned,
            RawBorrowed,
            Shared,
            Unique
        };
    };

    /**
     * Type-specific handle implementation.
     *
     * Wraps an instance of type T using one of several ownership strategies
     * (raw pointer, shared_ptr, unique_ptr).
     *
     * @tparam T Native type being wrapped.
     */
    template<class T>
    class Handle : public BaseHandle {
    public:

        // store either rawPtr, sharedPtr, uniquePtr
        using ErasedUnique = std::unique_ptr<void, void(*)(void*)>;
        using Store = std::variant<T*, std::shared_ptr<T>, ErasedUnique>;

        /**
         * Destructor.
         *
         * Deletes the wrapped instance only if it is stored as a
         * raw pointer with RawOwned ownership.
         *
         * Shared and unique ownership are released automatically.
         */
        ~Handle() {
            if(_strategy == StorageStrategy::RawOwned) {
                delete std::get<T*>(_store);
            }
        }

        /**
         * Constructs a handle from a raw pointer.
         *
         * @param instance Pointer to an instance of T.
         * @param owns Whether the handle owns the pointer and is responsible for deleting it.
         */
        explicit Handle(T* instance, bool owns) : _store(instance), _strategy(owns ? StorageStrategy::RawOwned : StorageStrategy::RawBorrowed){}

        /**
         * Constructs a handle from a shared pointer.
         *
         * @param sharedInstance Shared ownership of the instance.
         */
        explicit Handle(std::shared_ptr<T> sharedInstance) : _store(std::move(sharedInstance)), _strategy(StorageStrategy::Shared){}

        /**
         * Constructs a handle from a unique pointer.
         *
         * @param uniqueInstance Unique ownership of the instance.
         */
        explicit Handle(std::unique_ptr<T> u)
          : _store(ErasedUnique(u.release(), [](void* p){ delete static_cast<T*>(p); })),
            _strategy(StorageStrategy::Unique) {}

        /**
         * Retrieves the wrapped instance as a raw pointer.
         *
         * @return Pointer to the wrapped instance.
         *
         * @throws std::logic_error if the instance cannot be retrieved
         *         (should never occur unless the handle is corrupted).
         */
        T* get() const {
            switch(_strategy) {
                case StorageStrategy::RawOwned:
                case StorageStrategy::RawBorrowed:
                    return std::get<T*>(_store);

                case StorageStrategy::Shared:
                    return std::get<std::shared_ptr<T>>(_store).get();

                case StorageStrategy::Unique:
                    return static_cast<T*>(std::get<ErasedUnique>(_store).get());
            }
            throw JniBridgeError(JniBridgeError::Code::CorruptHandleStorage);
        }

        std::shared_ptr<T> getShared(JNIEnv *env) const {
            try {
                if(_strategy != StorageStrategy::Shared) {
                    throwJniBridgeExceptionJava(env, "Function expectes a std::shared_ptr");
                    return nullptr;
                }
                return std::get<std::shared_ptr<T>>(_store);
            } catch(const std::exception &e) {
                throwJniBridgeExceptionJava(env, "Unable to access instance. nativeHandle might be null or invalid.");
                return nullptr;
            }
        }

        /**
         * Attempts to cast the wrapped instance to another type.
         *
         * @tparam X Target type.
         * @return Pointer to X if the cast succeeds, otherwise nullptr.
         *
         * @throws std::logic_error if the wrapped instance cannot be accessed.
         */
        template<class X, typename U = T>
        typename std::enable_if<std::is_polymorphic<U>::value, X*>::type
        getAs(JNIEnv *env) const {
            try {
                T* original = get();
                return dynamic_cast<X*>(original);
            } catch(const std::exception &e) {
                throwJniBridgeExceptionJava(env, "Unable to access instance. nativeHandle might be null or invalid.");
                return nullptr;
            }
        }

        /**
         * Retrieves the wrapped instance as a shared_ptr<X>.
         *
         * @tparam X Target type.
         * @return Shared pointer to X if the cast succeeds.
         * @throws std::logic_error if the wrapped instance cannot be accessed.
         * @throws std::logic_error if the instance is not stored as a shared_ptr<T>.
         */
        template<class X, typename U = T>
        typename std::enable_if<std::is_polymorphic<U>::value, std::shared_ptr<X>>::type
        getAsShared(JNIEnv *env) const {
            try {
                if(_strategy != StorageStrategy::Shared) {
                    throwJniBridgeExceptionJava(env, "Function expectes a std::shared_ptr");
                    return nullptr;
                }

                std::shared_ptr<T> original = std::get<std::shared_ptr<T>>(_store);
                return std::dynamic_pointer_cast<X>(original);

            } catch(const std::exception &e) {
                throwJniBridgeExceptionJava(env, "Unable to access instance. nativeHandle might be null or invalid.");
                return nullptr;
            }
        }

        /**
         * Retrieves the wrapped instance as a unique_ptr<X>.
         *
         * @tparam X Target type.
         * @throws std::logic_error if the instance is not stored as a unique_ptr<T>.
         * @note Ownership is NOT transferred; this function is intended for inspection only.
         */
        template<class X>
        std::unique_ptr<X> getAsUnique(JNIEnv *env) const {
            try {
                throwJniBridgeExceptionJava(env, "JniBridge does not support passing std::unique_ptr");
                return nullptr;

            } catch(const std::exception &e) {
                throwJniBridgeExceptionJava(env, "Unable to access instance. nativeHandle might be null or invalid.");
                return nullptr;
            }
        }

    private:
        Store _store;
        StorageStrategy _strategy;
    };

    /**
     * Assigns a native handle to a Java object by calling setNativeHandle(long).
     *
     * @param env JNI environment.
     * @param object Java object receiving the handle.
     * @param ptr Native handle pointer.
     */
    inline void setNativeHandle(JNIEnv* env, jobject object, BaseHandle* ptr) {
        jclass cls = env->GetObjectClass(object);

        jmethodID setHandle = env->GetMethodID(cls, "setNativeHandle", "(J)V");
        env->CallVoidMethod(object, setHandle, reinterpret_cast<jlong>(ptr));

        if(env->ExceptionCheck()) { env->DeleteLocalRef(cls); }
        env->DeleteLocalRef(cls);
    }

    /**
     * Retrieves the native handle stored in a Java object.
     *
     * @param env JNI environment.
     * @param obj Java object containing the native handle.
     * @return Native handle pointer as jlong.
     */
    inline jlong getHandle(JNIEnv* env, jobject obj) {
        if(!obj) { return 0; }

        jclass cls = env->GetObjectClass(obj);
        jmethodID mid = env->GetMethodID(cls, "getNativeHandle", "()J");

        jlong handle = env->CallLongMethod(obj, mid);

        if(env->ExceptionCheck()) {
            env->DeleteLocalRef(cls);
            return jniDefaultReturn<jlong>();
        }

        env->DeleteLocalRef(cls);
        return handle;
    }

    /**
     * Creates a Java object and associates it with a native handle.
     *
     * @param env JNI environment.
     * @param jClassName Fully-qualified Java class name.
     * @param handle Native handle pointer.
     * @return Newly allocated Java object.
     */
    inline jobject jobjectFromBaseHandle(JNIEnv *env, const std::string& jClassName, BaseHandle* handle) {
        jclass cls = env->FindClass(jClassName.c_str());
        jobject result = env->AllocObject(cls);

        setNativeHandle(env, result, handle);

        if(env->ExceptionCheck()) {
            env->DeleteLocalRef(cls);
            return jniDefaultReturn<jobject>();
        }

        env->DeleteLocalRef(cls);
        return result;
    }

    /**
     * @brief Captures a pending Java exception and stores it for later handling.
     *
     * Retrieves the currently pending Java exception (if any), clears the JVM
     * exception state, and stores the exception as a JNI global reference in
     * the provided vector.
     *
     * This function is intended to be called immediately after a JNI call that
     * may throw, when native execution needs to continue.
     *
     * @param env JNI environment pointer for the current thread.
     * @param pending Vector used to collect captured exceptions.
     *
     * @return true if an jthrowable has been captured, else false;
     *
     * @warning
     * This function clears the JVM exception state. If the captured exception is
     * not rethrown later, it will be silently swallowed.
     *
     * @note use this function in conjunction with throwPendingJException
     */
    inline bool capturePendingJException(JNIEnv *env, std::vector<jthrowable> &pending) {
        // check if exception occurred return if not
        jthrowable localEx = env->ExceptionOccurred();
        if (!localEx) { return false; }

        // clear jvm of pending exceptions
        env->ExceptionClear();

        // add pending exception to the vector
        jthrowable globalEx = (jthrowable) env->NewGlobalRef(localEx);
        env->DeleteLocalRef(localEx);
        if (!globalEx) { return false; }

        pending.push_back(globalEx);
        return true;

    }

    /**
     * @brief Throws the first captured Java exception and releases all stored references.
     *
     * Throws the first Java exception previously captured via
     * @c capturePendingJException() and deletes all stored JNI global references.
     *
     * This function is intended to be called once at the end of a JNI method
     * (e.g., in a cleanup or finally-style section).
     *
     * @param env JNI environment pointer for the current thread.
     * @param pending Vector containing captured Java exceptions as global references.
     *
     * @note
     * Only one Java exception may be pending when returning to Java. Any
     * additional collected exceptions are discarded.
     *
     * @warning
     * After calling @c Throw(), no further normal JNI calls should be made.
     */
    inline void throwPendingJException(JNIEnv *env, std::vector<jthrowable> &pending) {
        if (pending.empty()) { return; }

        // add all java exceptions as suppressed to the first exception in the list.
        jthrowable primaryException = pending[0];
        jclass throwableCls = env->FindClass("java/lang/Throwable");
        jmethodID addSuppressed = env->GetMethodID(throwableCls, "addSuppressed", "(Ljava/lang/Throwable;)V");

        for(size_t i = 1; i < pending.size(); ++i) {
            env->CallVoidMethod(primaryException, addSuppressed, pending[i]);
        }

        // cleanup global refs
        env->Throw(primaryException);
        for(jthrowable globalEx : pending) { env->DeleteGlobalRef(globalEx); }
        pending.clear();
    }

}  // namespace jnibridge::internal
