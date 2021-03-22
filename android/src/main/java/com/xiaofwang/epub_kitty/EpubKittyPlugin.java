package com.xiaofwang.epub_kitty;

import android.util.Log;

import android.app.Activity;
import android.content.Context;

import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** EpubKittyPlugin */
public class EpubKittyPlugin implements MethodCallHandler {

  private Reader reader;
  private ReaderConfig config;

  static private Activity activity;
  static private Context context;
  static BinaryMessenger messenger;
  private EventChannel.EventSink pageEventSink;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    Log.e("Reader", " ---------> registerWith");
    context = registrar.context();
    activity = registrar.activity();
    messenger = registrar.messenger();
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "epubChannel");
    final EventChannel e = new EventChannel(messenger, "pageChannel").setStreamHandler(new EventChannel.StreamHandler() {
      @Override
      public void onListen(Object o, EventChannel.EventSink eventSink) {
        Log.e("Reader", "onListen");
        pageEventSink = eventSink;
      }
      @Override
      public void onCancel(Object o) {
      }
    });
    channel.setMethodCallHandler(new EpubKittyPlugin());
    Log.e("Reader", " ---------> ok");
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("setConfig")) {
      Map<String,Object> arguments = (Map<String, Object>) call.arguments;
      String identifier = arguments.get("identifier").toString();
      String themeColor = arguments.get("themeColor").toString();
      String scrollDirection = arguments.get("scrollDirection").toString();
      Boolean allowSharing = Boolean.parseBoolean(arguments.get("allowSharing").toString());
      config = new ReaderConfig(context,identifier,themeColor,scrollDirection,allowSharing);

    } else if (call.method.equals("openWithLocation")) {
      Map<String,Object> arguments = (Map<String, Object>) call.arguments;
      String bookPath = arguments.get("bookPath").toString();
      String location = arguments.get("location").toString();
      reader = new Reader(context,messenger,config,pageEventSink);
      reader.openWithLocation(bookPath, location);

    } else if (call.method.equals("open")) {
      Map<String,Object> arguments = (Map<String, Object>) call.arguments;
      String bookPath = arguments.get("bookPath").toString();
      reader = new Reader(context,messenger,config);
      reader.open(bookPath);

    } else if (call.method.equals("close")) {
      reader.close();
    
    } else {
      result.notImplemented();
    }
  }
}
