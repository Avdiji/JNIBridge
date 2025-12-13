#pragma once

#include <jni.h>

#include <memory>
#include <string>
#include <variant>

${allIncludes}

namespace jnibridge::internal {

    enum StorageStrategy {
        RawOwned,
        RawBorrowed,
        Shared,
        Unique
    };

    class BaseHandle {
        public:
            virtual ~BaseHandle() = default;
            virtual StorageStrategy strategy() const noexcept;
    };

    template<class T>
    class Handle : public BaseHandle {
        public:
            using Store = std::variant<T*, std::shared_ptr<T>, std::unique_ptr<T>>;

            ~Handle() {
                if(_strategy == StorageStrategy::RawOwned) {
                    delete std::get<T*>(_store);
                }
            }

            StorageStrategy strategy() const noexcept override {
                return _strategy;
            }

            explicit Handle(T* instance, bool owns) : _store(instance), _strategy(owns ? StorageStrategy::RawOwned : StorageStrategy::RawBorrowed){}
            explicit Handle(std::shared_ptr<T> sharedInstance) : _store(std::move(sharedInstance)), _strategy(StorageStrategy::Shared){}
            explicit Handle(std::unique_ptr<T> uniqueInstance) : _store(std::move(uniqueInstance)), _strategy(StorageStrategy::Unique){}


            T* get() const noexcept{
                switch(_strategy) {
                    case StorageStrategy::RawOwned:
                    case StorageStrategy::RawBorrowed:
                        return std::get<T*>(_store);

                    case StorageStrategy::Shared:
                        return std::get<std::shared_ptr<T>>(_store).get();

                    case StorageStrategy::Unique:
                        return std::get<std::unique_ptr<T>>(_store).get();
                }
                return nullptr;
            }

            template<class X>
            X* getAs() const noexcept{
                T* original = get();
                return dynamic_cast<X*>(original);
            }

            template<class X>
            std::shared_ptr<X> getAsShared() const noexcept{
                if(_strategy == StorageStrategy::Shared) {
                    std::shared_ptr<T> original = std::get<std::shared_ptr<T>>(_store);
                    return std::dynamic_pointer_cast<X>(original);
                }
                return nullptr;
            }

            template<class X>
            std::shared_ptr<X> getAsUnique() const noexcept{
                if(_strategy == StorageStrategy::Unique) {
                    std::unique_ptr<T> original = std::get<std::unique_ptr<T>>(_store);
                    return std::dynamic_pointer_cast<X>(original);
                }
                return nullptr;
            }

        private:
            Store _store;
            StorageStrategy _strategy;
    };


    inline void setNativeHandle(JNIEnv* env, jobject object, BaseHandle* ptr) {
        jclass cls = env->GetObjectClass(object);

        jmethodID setHandle = env->GetMethodID(cls, "setNativeHandle", "(J)V");
        env->CallVoidMethod(object, setHandle, reinterpret_cast<jlong>(ptr));

        env->DeleteLocalRef(cls);
    }

    inline jlong getHandle(JNIEnv* env, jobject obj) {
        jclass cls = env->GetObjectClass(obj);
        jmethodID mid = env->GetMethodID(cls, "getNativeHandle", "()J");

        jlong handle = env->CallLongMethod(obj, mid);
        env->DeleteLocalRef(cls);
        return handle;
    }

    inline jobject jobjectFromBaseHandle(JNIEnv *env, const std::string& jClassName, BaseHandle* handle) {
        jclass cls = env->FindClass(jClassName.c_str());
        jobject result = env->AllocObject(cls);

        setNativeHandle(env, result, handle);

        env->DeleteLocalRef(cls);
        return result;
    }

}  // namespace jnibridge::internal
