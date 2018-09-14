// ISurfaceTransferCallback.aidl
package com.qiyi.framework.surfacetransferservice;
import android.os.Message;

// Declare any non-default types here with import statements

interface ISurfaceTransferCallback {
    void handleMessage(in Message msg);
}
