

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_oximeter/flutter_oximeter.dart';


void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  FlutterOximeter oxi=FlutterOximeter();

  @override
  void initState() {
    super.initState();
    initPlatformState();


    // _streamSubscription =
    //     FlutterOximeter.instance.startDiscovery().listen((r) {
    //       setState(() {
    //         final existingIndex = results.indexWhere(
    //                 (element) => element.device.address == r.device.address);
    //         if (existingIndex >= 0)
    //           results[existingIndex] = r;
    //         else
    //           results.add(r);
    //       });
    //     });

  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await FlutterOximeter.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('Running on: $_platformVersion\n'),
              // StreamBuilder<Object>(
              //   stream: oxi.detectDataStream.receiveBroadcastStream(),
              //   builder: (context, snapshot) {
              //     return Text('dfsdfd');
              //   }
              // ),


              StreamBuilder<OximeterData>(
                stream: oxi.detectedDataStream,
                builder: (context, snapshot) {
                  return Column(
                    children: [
                      Text(snapshot.data!.spo2.toString()),
                      Text(snapshot.data!.heartRate.toString()),
                      Text(snapshot.data!.hrv.toString()),

                    ],
                  );
                }
              ),
              TextButton(
                onPressed: () async{

                  oxi.startScanDevice();
                },
                child: Text('Start Scan'),
              ),

              TextButton(
                onPressed: () async{

                  oxi.connect(macAddress: 'ff',deviceName: 'djjd');
                },
                child: Text('Connect'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
