import 'package:flutter/services.dart';

class EpubKitty {
  
  static const MethodChannel _channel = const MethodChannel('epub_kitty');

  /// @param identifier unique key for epub
  /// @param themeColor 
  /// @param scrollDirection
  /// @param allowSharing
  static void setConfig(String identifier, String themeColor, String scrollDirection, bool allowSharing) async {
    Map<String,dynamic> agrs = {
      "identifier": identifier,
      "themeColor": themeColor,
      "scrollDirection": scrollDirection,
      "allowSharing": allowSharing
    };
    await _channel.invokeMethod('setConfig', agrs);
  }

  /// @param bookPath the local path in cache
  static void open(String bookPath) async {
    Map<String,dynamic> agrs = {
      "bookPath": bookPath
    };
    await _channel.invokeMethod('open', agrs);
  }

  /// @param bookPath the local path in cache
  /// @param string as json location
  /// {"bookId":"0d51478b-52a5-46e1-aa2f-523a9b5720ba","href":"/index_split_006.html","created":1592382265249,"locations":{"cfi":"epubcfi(/0!/4/2[filepos18888]/2/2/2/1:0)"},"title":""}
  static void openWithLocation(String bookPath, String location) async {
    Map<String,dynamic> agrs = {
      "bookPath": bookPath,
      "location": location
    };
    await _channel.invokeMethod('openWithLocation', agrs);
  }
}
