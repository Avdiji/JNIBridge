#pragma once

#include <jni.h>

#include <memory>
#include <string>
#include <variant>
#include <stdexcept>

${internal_includes}

namespace jnibridge::internal {

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
            CSelfMustNotBeNull // The wrapped value of the calling handle must never be null.
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
                case Code::CSelfMustNotBeNull: return "Cannot perform the operation because the native instance is null.";
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
        using Store = std::variant<T*, std::shared_ptr<T>, std::unique_ptr<T>>;

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
        explicit Handle(std::unique_ptr<T> uniqueInstance) : _store(std::move(uniqueInstance)), _strategy(StorageStrategy::Unique){}

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
                    return std::get<std::unique_ptr<T>>(_store).get();
            }
            throw JniBridgeError(JniBridgeError::Code::CorruptHandleStorage);
        }

        /**
         * Attempts to cast the wrapped instance to another type.
         *
         * @tparam X Target type.
         * @return Pointer to X if the cast succeeds, otherwise nullptr.
         *
         * @throws std::logic_error if the wrapped instance cannot be accessed.
         */
        template<class X>
        X* getAs() const {
            T* original = get();
            return dynamic_cast<X*>(original);
        }

        /**
         * Retrieves the wrapped instance as a shared_ptr<X>.
         *
         * @tparam X Target type.
         * @return Shared pointer to X if the cast succeeds.
         * @throws std::logic_error if the wrapped instance cannot be accessed.
         * @throws std::logic_error if the instance is not stored as a shared_ptr<T>.
         */
        template<class X>
        std::shared_ptr<X> getAsShared() const {
            if(_strategy != StorageStrategy::Shared) {
                throw JniBridgeError(JniBridgeError::Code::NotASharedPtr);
            }

            std::shared_ptr<T> original = std::get<std::shared_ptr<T>>(_store);
            return std::dynamic_pointer_cast<X>(original);
        }

        /**
         * Retrieves the wrapped instance as a unique_ptr<X>.
         *
         * @tparam X Target type.
         * @throws std::logic_error if the instance is not stored as a unique_ptr<T>.
         * @note Ownership is NOT transferred; this function is intended for inspection only.
         */
        template<class X>
        std::unique_ptr<X> getAsUnique() const {
            if (_strategy != StorageStrategy::Unique) {
                throw JniBridgeError(JniBridgeError::Code::NotAUniquePtr);
            }
            throw JniBridgeError(JniBridgeError::Code::PassingUniquePtr);
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
        if(handle == -1) { throw JniBridgeError(JniBridgeError::Code::DestroyedHandle); }

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

        env->DeleteLocalRef(cls);
        return result;
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

}  // namespace jnibridge::internal
