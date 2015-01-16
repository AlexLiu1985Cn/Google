package com.efun.lib.pay;

import com.android.vending.billing.IInAppBillingService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

public class PayActivity extends FragmentActivity {

	private static final String BILLING_BIND_SERVEICE_TAG = "com.android.vending.billing.InAppBillingService.BIND";
	private IInAppBillingService mService;

	private ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
		}
	};

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.efun_lib_pay_layout);
		Intent serviceIntent = new Intent(BILLING_BIND_SERVEICE_TAG);
		serviceIntent.setPackage("com.android.vending");
		bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
