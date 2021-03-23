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
import io.flutter.plugin.common.EventChannel;

/** EpubKittyPlugin */
public class EpubKittyPlugin implements MethodCallHandler {

  private Reader reader;
  private ReaderConfig config;

  static private Activity activity;
  static private Context context;
  static BinaryMessenger messenger;
  static EventChannel.EventSink pageEventSink;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
	  context = registrar.context();
    activity = registrar.activity();
    messenger = registrar.messenger();
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "epub_kitty");
    new EventChannel(registrar.messenger(), "com.xiaofwang.epub_reader/page").setStreamHandler(new EventChannel.StreamHandler() {
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
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
	
    if (call.method.equals("setConfig")) {
      Map<String,Object> arguments = (Map<String, Object>) call.arguments;
      String identifier = arguments.get("identifier").toString();
      String themeColor = arguments.get("themeColor").toString();
      String scrollDirection = arguments.get("scrollDirection").toString();
      Boolean allowSharing = Boolean.parseBoolean(arguments.get("allowSharing").toString());
      config = new ReaderConfig(context, identifier, themeColor, scrollDirection, allowSharing);

    } else if (call.method.equals("open")) {
      Map<String,Object> arguments = (Map<String, Object>) call.arguments;
      String bookPath = arguments.get("bookPath").toString();
      String identifier = arguments.get("identifier").toString();
      String custId = arguments.get("custId").toString();
      reader = new Reader(context, messenger, config, identifier, custId, pageEventSink);
      reader.open(bookPath);

    } else if (call.method.equals("close")) {
      reader.close();
    
    } else {
      result.notImplemented();
    }
  }
}