package com.jnibridge.nativeaccess;

public abstract class Pointer implements IPointer {

    private long nativeHandle;

    @Override
    public long getNativeHandle() { return nativeHandle; }

    @Override
    public void setNativeHandle(final long handle) {
        nativeHandle = handle;
    }

    @Override
    @SuppressWarnings("removal")
    protected void finalize() { destruct(); }

}
