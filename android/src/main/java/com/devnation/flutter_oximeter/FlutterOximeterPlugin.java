package com.devnation.flutter_oximeter;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;


import com.inuker.bluetooth.library.Code;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.vphealthy.oximetersdk.OxiOprateManager;
import com.vphealthy.oximetersdk.listener.base.IBleWriteResponse;
import com.vphealthy.oximetersdk.listener.base.INotifyResponse;
import com.vphealthy.oximetersdk.listener.data.OnACKDataListener;
import com.vphealthy.oximetersdk.model.data.AckData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterOximeterPlugin
 */
public class FlutterOximeterPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    // private Synth synth;
    OxiOprateManager oximeter = OxiOprateManager.getMangerInstance(App.context);

    private EventChannel detectDataEventChannel;
    private EventChannel.EventSink detectDataSink;

    private EventChannel deviceFoundEventChannel;
    private EventChannel.EventSink deviceFoundSink;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_oximeter");

        channel.setMethodCallHandler(this);
        //  synth = new Synth();


        detectDataEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_oximeter_detect_data");
        detectDataEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, EventChannel.EventSink eventSink) {
                detectDataSink = eventSink;
            }

            @Override
            public void onCancel(Object listener) {
                detectDataSink = null;
            }
        });


        deviceFoundEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_oximeter_device_found_stream");
        deviceFoundEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, EventChannel.EventSink eventSink) {
                deviceFoundSink = eventSink;

            }

            @Override
            public void onCancel(Object listener) {
                deviceFoundSink = null;
            }
        });
    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("startScanDevice")) {
            try {


                Log.i("TAG", "Yessssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" + call.method.toString());
//        ArrayList arguments = (ArrayList) call.arguments;
//        Log.d("Connect Array", arguments.toString());
//        Log.d("Connect Array", arguments.get(0).toString());
                startScanDevice();

                //  eventChannelSink.success("Yessssssssssssssssssssssssssss Dataaaaaaaaaaaaa");
                result.success(1);
            } catch (Exception ex) {

                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        } else if (call.method.equals("disConnect")) {
            try {

                //  synth.disConnect();
                //    result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        } else if (call.method.equals("connect")) {
            try {
                ArrayList arguments = (ArrayList) call.arguments;
                connectOximeter(arguments.get(0).toString(),arguments.get(1).toString());
                //    result.success(1);
            } catch (Exception ex) {
                result.error("1", ex.getMessage(), ex.getStackTrace());
            }
        } else {
            result.notImplemented();
        }
    }


    void startScanDevice() {


        oximeter.startScanDevice(new SearchResponse() {

            @Override
            public void onSearchStarted() {

            }

            @Override
            public void onDeviceFounded(SearchResult searchResult) {

                if (deviceFoundSink != null) {
                    deviceFoundSink.success(searchResult);
                }
                Log.i("Device Found", searchResult.toString());
            }

            @Override
            public void onSearchStopped() {

            }

            @Override
            public void onSearchCanceled() {

            }

        });

    }


    void connectOximeter(String macAddress,String deviceName) {
        oximeter.connectDevice(macAddress, deviceName, (i, bleGattProfile, b) -> Log.i("Connect Stateb", String.valueOf(b)),
                new INotifyResponse() {
                    @Override
                    public void notifyState(int i) {
                        Log.i("Notify State", String.valueOf(i));

                        OxiOprateManager.getMangerInstance(App.context).startListenTestData(getBleWriteResponse(), new OnACKDataListener() {
                            @Override
                            public void onDataChange(AckData ackData) {

                            }
                        });
                    }
                }
        );

    }

    private IBleWriteResponse getBleWriteResponse() {
        return state -> {
            if (state == Code.REQUEST_SUCCESS) {
                Log.i("TAG", "write success");
                listenDetectResult();
            } else {
                Log.i("TAG", "write fail");
            }
        };
    }

    private void listenDetectResult() {

        OxiOprateManager.getMangerInstance(App.context).setOnDetectDataListener(detectData -> {



            Map<String, Object> discoveryResult = new HashMap<>();
            discoveryResult.put("heart", detectData.getHeart());
            discoveryResult.put("spo2", detectData.getSpo2h());
            discoveryResult.put("hrv", detectData.getHrv());

            Log.i("Detected data", discoveryResult.toString());

            if (detectDataSink != null) {
                detectDataSink.success(discoveryResult);
            }
        });
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
