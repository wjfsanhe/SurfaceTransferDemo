// ISurfaceTransfer.aidl
package com.qiyi.framework.surfacetransferservice;
import android.view.Surface;
import com.qiyi.framework.surfacetransferservice.ISurfaceTransferCallback;
import android.os.IBinder;
import android.view.MotionEvent;

// Declare any non-default types here with import statements

interface ISurfaceTransfer {
    int startActivity(in Intent intent);
    int setSurface(in Surface surface, int width, int height, int density);
    int registerListener(in ISurfaceTransferCallback callback);
    int prepareInterface(in IBinder token);
    int injectEvent(in MotionEvent event);
}
