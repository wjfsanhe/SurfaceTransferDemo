package com.qiyi.framework.surfacetransferdemo;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.qiyi.framework.surfacetransferservice.ISurfaceTransfer;
import com.qiyi.framework.surfacetransferservice.ISurfaceTransferCallback;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
	private final String TAG = "SurfaceDemo.MainActivity";
	private boolean mView1Enable = true;
	private final static String[] REQUIRE_PERMISSIONS = {
			Manifest.permission.CAPTURE_VIDEO_OUTPUT,
			Manifest.permission.CAPTURE_SECURE_VIDEO_OUTPUT,
	};
	//the first init phase
	private IBinder getActivityToken() {
		IBinder binder = null;

		try {
			//Class clazz = Class.forName("com.qiyi.framework.surfacetransferservice.MainActivity");
			//Field token = clazz.getField("mToken");
			Class clazz = Class.forName("android.app.Activity");
			if(MainActivity.this == null) {
				Log.d(TAG, "ACTIVITY IS NULL!!!");
			}
			//Class clazz = activity.getClass();
			Field token = clazz.getDeclaredField("mToken");
			if (token == null) return null;
			//mToken is private field.
			token.setAccessible(true);
			IBinder msg = (IBinder)token.get(MainActivity.this);
			binder = msg;
			Log.e(TAG, "aaaaa ====" +  msg.toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return binder;
	}
	private  SurfaceView mView1 = null;
	private  SurfaceView mView2 = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (PermissionActivity.checkAndRequestPermission(this, REQUIRE_PERMISSIONS)) {
			finish();
			return;
		}
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		mView1 = (SurfaceView) findViewById(R.id.surfaceView);
		mView2 = (SurfaceView) findViewById(R.id.surfaceView2);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		Button toggle = (Button) findViewById(R.id.button);
		Button settings = (Button) findViewById(R.id.setting);
		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//toggle surface change action.
				Log.e(TAG, "settings onClick enter ");
				PackageManager packageManager = getPackageManager();
				Intent intent=new Intent();
				intent =packageManager.getLaunchIntentForPackage("com.qiyi.framework.embeded");
				if(intent==null){
					Log.e(TAG,"settings APP not found!");
					return ;
				}
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				try {
					mSurfaceTransfer.startActivity(intent);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				/*Intent intent = new Intent();
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.setComponent(new ComponentName(
						new String("com.qiyi.settings "), new String("com.qiyi.vr.unityplugin.PluginActivity")));
				startActivity(intent);*/
			}
		});
		toggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//toggle surface change action.
				Log.e(TAG, "toggle onClick enter ");
				if (mSurfaceTransfer == null) return ;
				try {
					mView1Enable = !mView1Enable;
					if (mView1Enable) {
						mSurfaceTransfer.setSurface(null,0,0, 0);
						mSurfaceTransfer.setSurface(mView1.getHolder().getSurface(),mView1.getWidth(), mView1.getHeight(), 0);
						Log.e(TAG, "A Surface Using ");
					} else {
						mSurfaceTransfer.setSurface(null,0,0, 0);
						mSurfaceTransfer.setSurface(mView2.getHolder().getSurface(),mView2.getWidth(),mView2.getHeight(), 0);
						Log.e(TAG, "B Surface Using ");
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		Intent intent = new Intent();
		intent.setAction("com.qiyi.framework.BIND");
		intent.setComponent(new ComponentName("com.qiyi.framework.surfacetransferservice",
				"com.qiyi.framework.surfacetransferservice.AIDLSurfaceTransferService"));
		bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);


	}
	private RectF[] mVirtualRect = new RectF[2];
	private static RectF calcViewScreenLocation(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return new RectF(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			mVirtualRect[0] = calcViewScreenLocation(mView1);
			mVirtualRect[1] = calcViewScreenLocation(mView2);
			Log.i(TAG, "VirtualRect " + mVirtualRect.toString());
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent event){
		// Be sure to call the superclass implementation
		// we determine the the touch point.
		Log.d(TAG,"Event " + event.toString());
		float x = event.getRawX();
		float y = event.getRawY();
		RectF rect = mView1Enable ? mVirtualRect[0] : mVirtualRect[1];

		if (rect.contains(x,y)) {
			MotionEvent cloneEvent = MotionEvent.obtain(event);
			cloneEvent.offsetLocation(-rect.left, -rect.top);
			Log.d(TAG,"Event , convent : " + cloneEvent.toString());
			try {
				mSurfaceTransfer.injectEvent(cloneEvent);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return super.onTouchEvent(event);
	}
	private ISurfaceTransferCallback aidlListener = new ISurfaceTransferCallback.Stub() {
		public synchronized void handleMessage(Message msg) {
			Log.d(TAG, "handleMessage " + msg.toString());
			return ;
		}
	};

	private ISurfaceTransfer mSurfaceTransfer = null;
	private ServiceConnection mServiceConn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Log.d(TAG, "onServiceConnected");
			mSurfaceTransfer = ISurfaceTransfer.Stub.asInterface(service);
			try {
				mSurfaceTransfer.registerListener(aidlListener);
				mSurfaceTransfer.prepareInterface(getActivityToken());
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		public void onServiceDisconnected(ComponentName name) {
			mSurfaceTransfer = null;
		}
	};


	@Override
	protected void onResume() {
		super.onResume();

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
