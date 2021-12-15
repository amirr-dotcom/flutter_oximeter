package com.devnation.flutter_oximeter;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// import com.digidoctor.android.R;
import com.inuker.bluetooth.library.Code;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.vphealthy.oximetersdk.OxiOprateManager;
import com.vphealthy.oximetersdk.listener.base.IConnectResponse;
import com.vphealthy.oximetersdk.listener.base.INotifyResponse;

import java.util.LinkedList;
import java.util.List;

public class ViaOximeterScanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final static String TAG = ViaOximeterScanActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    ListView mListView;
    LinkedList<SearchResult> mBleList = new LinkedList<>();
    LinkedList<String> mBleListStr = new LinkedList<>();
    ScanListAdapter mScanListAdapter;
    Button btnStartScan, btnStopScan;
    ImageView ivScan;
    private Activity mActivity = ViaOximeterScanActivity.this;
    private BluetoothAdapter mBluetoothAdapter;
    ImageView deviceNotFound, ivGoBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_via_oximeter_scan);
//        deviceNotFound = findViewById(R.id.imageView13);
//        ivGoBack = findViewById(R.id.ivBack);

        checkScanPermission();
        initAdapter();
    }

    private void checkScanPermission() {

        if (Build.VERSION.SDK_INT >= 23) {
            //校验是否已具有模糊定位权限
            if (ContextCompat.checkSelfPermission(mActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请模糊定位权限
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0x10);
            }
        }
    }

    private void initAdapter() {

//        mListView = findViewById(R.id.lay_listview);
//        btnStartScan = findViewById(R.id.btnStartScan);
//        btnStopScan = findViewById(R.id.btnStopScan);
//        btnStopScan = findViewById(R.id.btnStopScan);
//        ivScan = findViewById(R.id.ivScan);
        mScanListAdapter = new ScanListAdapter(getApplicationContext(), mBleList);
        mListView.setAdapter(mScanListAdapter);
        mListView.setOnItemClickListener(this);
        mScanListAdapter.notifyDataSetChanged();

        btnStartScan.setOnClickListener(view -> ViaOximeterScanActivity.this.startScans());
        btnStopScan.setOnClickListener(view -> ViaOximeterScanActivity.this.stopScans());
        ivGoBack.setOnClickListener(v -> ViaOximeterScanActivity.this.goBack());

        startScans();

        final BluetoothManager bluetoothManager;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        // ivScan.setOnClickListener(view -> startActivity(new Intent(mActivity, DeviceConnectScanActivity.class)));

    }

    private void goBack() {
        finish();
    }

    @Override
    public void onBackPressed() {
        stopScans();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        startScans();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScans();
    }

    public void startScans() {
     //   Toast.makeText(mActivity, getString(R.string.seraching_for_device), Toast.LENGTH_SHORT).show();

        mBleListStr.clear();
        mBleList.clear();
        mScanListAdapter.notifyDataSetChanged();
        startScan();
    }

    public void stopScans() {
        stopScan();
        if (mBleList.isEmpty()) {
            deviceNotFound.setVisibility(View.VISIBLE);
        }
    }


    private void startScan() {
        OxiOprateManager.getMangerInstance(getApplicationContext()).startScanDevice(new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Log.i(TAG, "onSearchStarted");
            }

            @Override
            public void onDeviceFounded(SearchResult searchResult) {
                String address = searchResult.getAddress();
                deviceNotFound.setVisibility(View.GONE);
                Log.i(TAG, "onDeviceFounded:" + address);
                if (!mBleListStr.contains(address)) {
                    mBleListStr.add(address);
                    mBleList.add(searchResult);
                    mScanListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSearchStopped() {
                Log.i(TAG, "onSearchStopped");
            }

            @Override
            public void onSearchCanceled() {
                Log.i(TAG, "onSearchCanceled");
            }
        });
    }

    private void stopScan() {
        OxiOprateManager.getMangerInstance(getApplicationContext()).stopScanDevice();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SearchResult searchResult = mBleList.get(position);
        connect(searchResult);
    }

    private void connect(final SearchResult searchResult) {
        Log.d(TAG, "connect:address: " + searchResult.getAddress());
        Log.d(TAG, "connect:name: " + searchResult.getName());

        OxiOprateManager.getMangerInstance(getApplicationContext()).connectDevice(searchResult.getAddress(), searchResult.getName(), new IConnectResponse() {
            @Override
            public void connectState(int code, BleGattProfile bleGattProfile, boolean isUpdateModel) {
                if (code == Code.REQUEST_SUCCESS) {
                    //蓝牙与设备的连接状态
                    Log.i(TAG, "Connection Success");
                } else {
                    Log.i(TAG, "Connection Failure");
                }
            }
        }, new INotifyResponse() {
            @Override
            public void notifyState(int state) {
                if (state == Code.REQUEST_SUCCESS) {
                    Log.i(TAG, "Notify Success");
                   // Intent intent = new Intent(ViaOximeterScanActivity.this, DataActivity.class);
//                    intent.putExtra("mac", searchResult.getAddress());
//                    intent.putExtra("show", ViaOximeterScanActivity.this.getIntent().getStringExtra("show"));
//                    ViaOximeterScanActivity.this.startActivity(intent);
                } else {
                    Log.i(TAG, "Notify Fail");
                }
            }
        });
    }

    public static class ScanListAdapter extends BaseAdapter {

        List<SearchResult> itemData;
        private LayoutInflater mLayoutInflater;

        public ScanListAdapter(Context mContext, List<SearchResult> itemData) {
            this.itemData = itemData;
            this.mLayoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            if (itemData != null) {
                return itemData.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return itemData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ScanItemHolder mScanItemHolder;
            if (convertView == null) {
                mScanItemHolder = new ScanItemHolder();
//                convertView = mLayoutInflater.inflate(R.layout.item_connect_device, null);
//                mScanItemHolder.mBleAddress = convertView.findViewById(R.id.ble_list_address);
//                mScanItemHolder.mBleName = convertView.findViewById(R.id.ble_list_name);
//                mScanItemHolder.mBleRssi = convertView.findViewById(R.id.ble_list_rssi);
                convertView.setTag(mScanItemHolder);
            } else {
                mScanItemHolder = (ScanItemHolder) convertView.getTag();
            }
            SearchResult bleDevice = itemData.get(position);
            mScanItemHolder.mBleName.setText(bleDevice.getName());
            mScanItemHolder.mBleAddress.setText(bleDevice.getAddress());
            mScanItemHolder.mBleRssi.setText("");
            return convertView;
        }

        class ScanItemHolder {
            TextView mBleName;
            TextView mBleAddress;
            TextView mBleRssi;
        }
    }
}