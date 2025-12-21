#pragma once

#include <jni.h>

#include <memory>
#include <string>
#include <variant>
#include <stdexcept>

${allIncludes}

namespace jnibridge::internal {

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

    /**
     * Base class for all native handles.
     *
     * Each Java object created by JNIBridge stores a pointer to a BaseHandle
     * instance, which manages access to the underlying native object.
     */
    class BaseHandle {
    public:
        virtual ~BaseHandle() = default;
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
            throw std::logic_error("JniBridge-Handle can not return a wrapped instance.");
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
            if(_strategy == StorageStrategy::Shared) {
                std::shared_ptr<T> original = std::get<std::shared_ptr<T>>(_store);
                return std::dynamic_pointer_cast<X>(original);
            }
            throw std::logic_error("The JNI-wrapped type is not an instance of std::shared_ptr<T>.")
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
            if (_strategy == StorageStrategy::Unique) {
                // Cannot transfer ownership from const handle safely
                throw std::logic_error("Accessing unique_ptr<X> by value is not supported.");
            }
            throw std::logic_error("JNI-wrapped instance is not stored as std::unique_ptr<T>.");
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
        jclass cls = env->GetObjectClass(obj);
        jmethodID mid = env->GetMethodID(cls, "getNativeHandle", "()J");

        jlong handle = env->CallLongMethod(obj, mid);
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

}  // namespace jnibridge::internal
