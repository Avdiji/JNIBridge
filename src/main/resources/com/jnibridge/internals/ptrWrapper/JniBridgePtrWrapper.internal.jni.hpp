#pragma once

#include <jni.h>

#include <memory>
#include <string>
#include <type_traits>

${allIncludes}

namespace jnibridge::internal {

    class JniBridgePtrWrapperBase {
        public:
            virtual ~JniBridgePtrWrapperBase() = default;
    };

    /**
     * A pointer wrapper for C++ objects managed across the JNI boundary.
     *
     * Supports:
     * - Raw pointer with optional ownership
     * - std::shared_ptr
     * - std::unique_ptr
     *
     * Ensures that ownership semantics are preserved when converting between pointer types.
     *
     * @tparam T The type of the C++ object being wrapped.
     */
	template<class T>
	class JniBridgePtrWrapper : public JniBridgePtrWrapperBase
	{
		public:
		    JniBridgePtrWrapper(const JniBridgePtrWrapper&) = delete;
            JniBridgePtrWrapper& operator=(const JniBridgePtrWrapper&) = delete;
            JniBridgePtrWrapper(JniBridgePtrWrapper&&) = default;
            JniBridgePtrWrapper& operator=(JniBridgePtrWrapper&&) = default;

			explicit JniBridgePtrWrapper(T* instance, bool owns = false) { _raw = instance; _owns = owns;}
			explicit JniBridgePtrWrapper(std::shared_ptr<T> instance, bool owns = false) { _shared = std::move(instance); _owns = owns; }
			explicit JniBridgePtrWrapper(std::unique_ptr<T> instance, bool owns = false) { _unique = std::move(instance); _owns = owns; }

            template<class U, class = std::enable_if_t<std::is_convertible_v<T*, U*>>>
            JniBridgePtrWrapper<U>* toWrapper() {
                if (_shared) {
                    std::shared_ptr<U> sp = std::static_pointer_cast<U>(_shared);
                    return new JniBridgePtrWrapper<U>(std::move(sp), true);
                }

                if (_unique) {
                    std::unique_ptr<U> up(static_cast<U*>(_unique.release()));
                    return new JniBridgePtrWrapper<U>(std::move(up), false);
                }

                if (_raw) {
                    return new JniBridgePtrWrapper<U>(static_cast<U*>(_raw), false);
                }

                return nullptr;
            }


            /**
             * Destructor.
             * Deletes the raw pointer if it exists and ownership is held.
            */
			~JniBridgePtrWrapper() override { if(_raw && _owns) delete _raw; }

            /**
             * Implicit conversion to a reference to a std::shared_ptr<T>.
             *
             * If the object is currently managed by a raw pointer or unique_ptr and ownership is held,
             * it will be promoted to a shared_ptr.
             *
             * @throws std::runtime_error if conversion is not possible.
             */
			operator std::shared_ptr<T>() {
				if(_shared) { return _shared; }
				if(_raw && _owns) {
					_shared = std::shared_ptr<T>(_raw);
					_raw = nullptr;
					return _shared;
				}
				if(_unique && _owns) {
					_shared = std::move(_unique);
					_unique = nullptr;
					return _shared;
				}
				throw std::runtime_error("Object is in a illegal state, unable to transfer ownership to shared_ptr");
			}

            /**
             * Implicit conversion to a reference to a std::unique_ptr<T>.
             *
             * If the object is managed by a raw pointer and ownership is held,
             * it will be promoted to a unique_ptr.
             *
             * @throws std::runtime_error if conversion is not possible.
             */
			operator std::unique_ptr<T>() {
				if(_unique) { return std::move(_unique); }
				if(_raw && _owns) {
					_unique = std::unique_ptr<T>(_raw);
					_raw = nullptr;
					return std::move(_unique);
				}
				throw std::runtime_error("Object is in a illegal state, unable to transfer ownership to unique_ptr");
			}

            /**
             * Retrieve the raw pointer without altering ownership.
             *
             * @return Pointer to the managed object.
             * @throws std::runtime_error if no object is currently stored.
             */
			T* get() {
				if(_raw) return _raw;
				if(_shared) return _shared.get();
				if(_unique) return _unique.get();
				throw std::runtime_error("No Instance is being persisted in this wrapper");
			}

            const T* get() const { return const_cast<JniBridgePtrWrapper*>(this)->get(); }

            void disableOwnership() { _owns = false; }

		private:
			bool _owns;
			T* _raw = nullptr;
			std::shared_ptr<T> _shared = nullptr;
			std::unique_ptr<T> _unique = nullptr;
	};


    /**
     * Convert a Java object to a JniBridgePtrWrapper pointer.
     *
     * Calls the Java method `getNativeHandle()` on the provided object to retrieve
     * the native pointer (stored as a jlong) and casts it back to the appropriate
     * JniBridgePtrWrapper type.
     *
     * @tparam T The type of the C++ object managed by the wrapper.
     * @param env JNI environment pointer.
     * @param obj Java object containing the native handle.
     * @return Pointer to the JniBridgePtrWrapper<T>.
     */
    template <typename T>
    inline JniBridgePtrWrapper<T>* jobjectToWrapper(JNIEnv* env, jobject obj) {
        jclass cls = env->GetObjectClass(obj);
        jmethodID mid = env->GetMethodID(cls, "getNativeHandle", "()J");

        env->DeleteLocalRef(cls);
        return reinterpret_cast<JniBridgePtrWrapper<T>*>(env->CallLongMethod(obj, mid));
    }

    /**
     * Stores a native wrapper pointer directly into an existing Java object's nativeHandle field.
     *
     * @tparam T  Type of the C++ object managed by JniBridgePtrWrapper.
     * @param env JNI environment pointer.
     * @param object Existing Java object that has a `long nativeHandle` field.
     * @param ptr Pointer to the JniBridgePtrWrapper<T> to store.
     * @return The same Java object for convenience.
     */
    template <typename T>
    inline jobject wrapperToNewJobject(JNIEnv* env, const std::string& jClassName, JniBridgePtrWrapper<T>* ptr) {
        jclass cls = env->FindClass(jClassName.c_str());
        jobject result = env->AllocObject(cls);

        setNativeHandle(env, result, ptr);

        env->DeleteLocalRef(cls);
        return result;
    }

    /**
     * Sets the native handle pointer on a Java object via its `setNativeHandle(long)` method.
     *
     * This utility is used to associate a native C++ wrapper instance (JniBridgePtrWrapper<T>)
     * with a corresponding Java object that exposes a `setNativeHandle(long)` setter.
     * The pointer is cast to a `jlong` and passed to the Java side for storage.
     *
     * @tparam T  Type parameter for the native wrapper.
     * @param env JNI environment pointer, must be valid for the current thread.
     * @param object Java object that declares a `void setNativeHandle(long)` instance method.
     * @param ptr Pointer to the native wrapper that should be stored on the Java side.
     */
    template<typename T>
    inline void setNativeHandle(JNIEnv* env, jobject object, JniBridgePtrWrapper<T>* ptr) {
        jclass cls = env->GetObjectClass(object);

        jmethodID setHandle = env->GetMethodID(cls, "setNativeHandle", "(J)V");
        env->CallVoidMethod(object, setHandle, reinterpret_cast<jlong>(ptr));

        env->DeleteLocalRef(cls);
    }

}  // namespace jnibridge::internal
