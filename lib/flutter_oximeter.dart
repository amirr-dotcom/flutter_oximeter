
import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_bluetooth_serial/flutter_bluetooth_serial.dart';
import 'package:flutter_oximeter/location_servive.dart';

import 'package:fluttertoast/fluttertoast.dart';
import 'package:permission_handler/permission_handler.dart';

class FlutterOximeter {
  static const MethodChannel _channel = MethodChannel('flutter_oximeter');
  static const EventChannel _detectDataStream = EventChannel('flutter_oximeter_detect_data');
  static const EventChannel deviceFoundStream = EventChannel('flutter_oximeter_device_found_stream');




   Stream<OximeterData> get detectedDataStream => _detectDataStream.receiveBroadcastStream().map((element) => OximeterData.fromJson(element));

  // listenToData(){
  //   _detectDataStream.receiveBroadcastStream().listen((event) {
  //
  //     print('MyData'+event.toString());
  //     print('My'+event['spo2'].toString());
  //     print('My'+OximeterData.fromJson( Map<String, dynamic>.from(event)).toString());
  //     print('My'+OximeterData.fromJson(Map<String, dynamic>.from(event)).heartRate.toString());
  //   });
  // }



  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }



   void startScanDevice() async{

     bool locationEnable=await LocationService().enableGPS();
     await FlutterBluetoothSerial.instance.requestEnable();

     if(locationEnable){
       bool bluetoothEnable=(await FlutterBluetoothSerial.instance.isEnabled)??false;

       if(bluetoothEnable){
         try{
           await _channel.invokeMethod('startScanDevice');
         }
         catch(e) {
           _alertToast(e);
         }
       }
       else{
         _alertToast( 'Please enable bluetooth to use this feature');
       }

     }
     else{
       _alertToast( 'Please enable location to use this feature');
     }
     await Permission.location.request();

  }


  void connect({
  required String macAddress,
  required String deviceName,
}) async{
    try{
      await _channel.invokeMethod('connect',[macAddress,deviceName]);
    }
    catch(e) {

    }
  }



  void _alertToast(msg){
    Fluttertoast.showToast(
        msg: msg,
        toastLength: Toast.LENGTH_SHORT,
        gravity: ToastGravity.BOTTOM,
        timeInSecForIosWeb: 1,
        fontSize: 16.0
    );
  }



}


class OximeterData {
  int? heartRate;
  int? hrv;
  int? spo2;

  OximeterData(
      {
        this.heartRate,
        this.hrv,
        this.spo2,
      });

  factory OximeterData.fromJson(json) => OximeterData(
    heartRate: json['heart'] as int,
    hrv: json['hrv'] as int,
    spo2: json['spo2'] as int,
  );


}
