package com.efun.lib.pay;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.vending.billing.IInAppBillingService;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class PayActivity extends FragmentActivity {

	private static final String BILLING_BIND_SERVEICE_TAG = "com.android.vending.billing.InAppBillingService.BIND";
	private IInAppBillingService mService;
	private String mPayone = "";
	private String mPayTwo = "";
	private String mToken = "";

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
		findViewById(R.id.getSkuInfo).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						Log.d("alex", PayActivity.class + ": get sku info start!!");
						Bundle skuDetails = null;
						ArrayList<String> skuList = new ArrayList<String>();
						skuList.add("payone");
						skuList.add("paytwo");
						Bundle querySkus = new Bundle();
						querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
						try {
							skuDetails = mService.getSkuDetails(3,
									getPackageName(), "inapp", querySkus);
							int response = skuDetails.getInt("RESPONSE_CODE");
							if (response == 0) {
								ArrayList<String> responseList = skuDetails
										.getStringArrayList("DETAILS_LIST");

								for (String thisResponse : responseList) {
									JSONObject object = new JSONObject(
											thisResponse);
									String sku = object.getString("productId");
									String price = object.getString("price");
									if (sku.equals("payone"))
										mPayone = price;
									else if (sku.equals("paytwo"))
										mPayTwo = price;
								}
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}).start();
			}
		});
		findViewById(R.id.startPay).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String sku = "payone";
				try {
					Bundle buyIntentBundle = mService.getBuyIntent(3,
							getPackageName(), sku, "inapp",
							"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
					if (0 == buyIntentBundle.getInt("RESPONSE_CODE")) {
						PendingIntent pendingIntent = buyIntentBundle
								.getParcelable("BUY_INTENT");
						startIntentSenderForResult(
								pendingIntent.getIntentSender(), 1001,
								new Intent(), Integer.valueOf(0),
								Integer.valueOf(0), Integer.valueOf(0));

					}
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (SendIntentException e) {
					e.printStackTrace();
				}
			}
		});
		findViewById(R.id.getPurchase).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						try {
							Bundle ownedItems = mService.getPurchases(3,
									getPackageName(), "inapp", null);
							int response = ownedItems.getInt("RESPONSE_CODE");
							if (response == 0) {
								ArrayList<String> ownedSkus = ownedItems
										.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
								ArrayList<String> purchaseDataList = ownedItems
										.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
								ArrayList<String> signatureList = ownedItems
										.getStringArrayList("INAPP_DATA_SIGNATURE");
								String continuationToken = ownedItems
										.getString("INAPP_CONTINUATION_TOKEN");
								for (int i = 0; i < purchaseDataList.size(); ++i) {
									String purchaseData = purchaseDataList
											.get(i);
									String signature = signatureList.get(i);
									String sku = ownedSkus.get(i);

									// do something with this purchase
									// information
									// e.g. display the updated list of products
									// owned by user
								}
								// **********************************
								String token = ownedItems
										.getString("INAPP_CONTINUATION_TOKEN");
								if (token != null) {
									ownedItems = mService.getPurchases(3,
											getPackageName(), "inapp", null);
								}
								// **************/
								// if continuationToken != null, call
								// getPurchases again
								// and pass in the token to retrieve more items
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}

					}
				});
		
		findViewById(R.id.consumePurchase).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							int response = mService.consumePurchase(3, getPackageName(), mToken);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			unbindService(mServiceConn);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1001) {
			int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
			String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
			String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

			if (resultCode == RESULT_OK) {
				try {
					JSONObject jo = new JSONObject(purchaseData);
					String sku = jo.getString("productId");
					mToken = jo.getString("purchaseToken");
					// alert("You have bought the " + sku
					// + ". Excellent choice, adventurer!");
				} catch (JSONException e) {
					// alert("Failed to parse purchase data.");
					e.printStackTrace();
				}
			}
		}
	}

}
