
import 'package:location/location.dart';

class LocationService{

  Location location = Location();

  late bool _serviceEnabled;

  enableGPS() async{
    _serviceEnabled = await location.serviceEnabled();
    print('here'+_serviceEnabled.toString());
    if (_serviceEnabled) {

    }
    else{
      _serviceEnabled = await location.requestService();
    }

    // _permissionGranted = await location.hasPermission();
    // if (_permissionGranted == PermissionStatus.denied) {
    //   _permissionGranted = await location.requestPermission();
    //   if (_permissionGranted != PermissionStatus.granted) {
    //     return;
    //   }
    // }
    return _serviceEnabled;
  }








}