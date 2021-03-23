package com.xiaofwang.epub_kitty;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.Config;
import com.folioreader.FolioReader;
import com.folioreader.model.locators.ReadLocator;
import com.folioreader.util.AppUtil;
import com.folioreader.util.ReadLocatorListener;

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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.readium.r2.shared.Locations;

/** EpubKittyPlugin */
public class EpubKittyPlugin implements MethodCallHandler, ReadLocatorListener {

  private Reader reader;
  private ReaderConfig config;

  static private Activity activity;
  static private Context context;
  static BinaryMessenger messenger;
  private EventChannel.EventSink pageEventSink;
  private static final String PAGE_CHANNEL = "com.xiaofwang.epub_reader/page";

  private String identifier;
  private String custId;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
	  context = registrar.context();
    activity = registrar.activity();
    messenger = registrar.messenger();
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "epub_kitty");
    setPageHandler(registrar.messenger());
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
      this.identifier = arguments.get("identifier").toString();
      this.custId = arguments.get("custId").toString();
      reader = new Reader(context, messenger, config, this.identifier, this.custId);
      reader.open(bookPath);

    } else if (call.method.equals("close")) {
      reader.close();
    
    } else {
      result.notImplemented();
    }
  }

  private void setPageHandler(BinaryMessenger messenger){
    new EventChannel(messenger,PAGE_CHANNEL).setStreamHandler(new EventChannel.StreamHandler() {
      @Override
      public void onListen(Object o, EventChannel.EventSink eventSink) {
        pageEventSink = eventSink;
      }
      @Override
      public void onCancel(Object o) {
      }
    });
  }

    @Override
  public void saveReadLocator(ReadLocator readLocator) {

    String bookId = readLocator.getBookId();
    String cfi = readLocator.getLocations().getCfi();
    long created = readLocator.getCreated();
    String href = readLocator.getHref();

    Log.e("readLocator", "bookId: "+readLocator.getBookId());
    Log.e("readLocator", "cfi: "+readLocator.getLocations().getCfi());
    Log.e("readLocator", "created: "+readLocator.getCreated());
    Log.e("readLocator", "href: "+readLocator.getHref());
    Log.e("readLocator", "json: "+readLocator.toJson());

    JSONObject obj = new JSONObject();
    try {
      obj.put("bookId", bookId);
      obj.put("cfi", cfi);
      obj.put("created", created);
      obj.put("href", href);

    } catch (JSONException e) {
      e.printStackTrace();
    }

    SharedPreferences preferences = context.getSharedPreferences(this.custId, Context.MODE_PRIVATE);
    SharedPreferences.Editor edit = preferences.edit();
    edit.putString(this.identifier, obj.toString());
    edit.apply();
    
    if (pageEventSink != null){
      Log.e("readLocator", "pageEventSink != null");
      Log.e("readLocator", readLocator.toJson());
      pageEventSink.success(readLocator.toJson());
    }
  }
}