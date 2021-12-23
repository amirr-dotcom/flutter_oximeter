
import 'dart:async';

import 'package:flutter/services.dart';


class FlutterOximeter {
  static const MethodChannel _channel = MethodChannel('flutter_oximeter');
  static const EventChannel _detectDataStream = EventChannel('flutter_oximeter_detect_data');
  static const EventChannel _deviceFoundStream = EventChannel('flutter_oximeter_device_found_stream');
  static const EventChannel _scanningStateStream =  EventChannel('flutter_oximeter_device_scanning_state');
  static const EventChannel _connectionStateStream =  EventChannel('flutter_oximeter_device_connection_state');


  Stream get getScanningStateStream => _scanningStateStream.receiveBroadcastStream();
  Stream get getConnectionStateStream => _connectionStateStream.receiveBroadcastStream();



   Stream<OximeterData> get detectedDataStream => _detectDataStream.receiveBroadcastStream().map((element) => OximeterData.fromJson(element));
   Stream<DeviceData> get deviecFoundStream => _deviceFoundStream.receiveBroadcastStream().map((element) => DeviceData.fromJson(element));

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
     await _channel.invokeMethod('startScanDevice');
  }


  void connect({
  required String macAddress,
  required String deviceName,
}) async{
    try{
        await _channel.invokeMethod('connect',[macAddress,deviceName]);


    }
    catch(e) {
      print(e);
    }
  }

  void disConnect({
    required String macAddress,
  }
  ) async{
    try{
        await _channel.invokeMethod('disConnect',[macAddress]);
    }
    catch(e) {
      print(e);
    }
  }





}


class OximeterData {
  int? heartRate;
  int? hrv;
  int? spo2;
  double? perfusionIndex;

  OximeterData(
      {
        this.heartRate,
        this.hrv,
        this.spo2,
        this.perfusionIndex,
      });

  factory OximeterData.fromJson(json) => OximeterData(
    heartRate: (json['heart']??00) as int,
    hrv: (json['hrv']?? 00) as int,
    spo2: (json['spo2']?? 00) as int,
    perfusionIndex: (json['perfusionIndex']?? 0.0) as double,
  );


}


class DeviceData {
  String? macAddress;
  String? deviceName;

  DeviceData(
      {
        this.macAddress,
        this.deviceName,

      });

  factory DeviceData.fromJson(json) => DeviceData(
    macAddress: (json['macAddress']??'') as String,
    deviceName: (json['deviceName']??'') as String,

  );


}
