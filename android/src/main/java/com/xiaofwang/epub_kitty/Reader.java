package com.xiaofwang.epub_kitty;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.Config;
import com.folioreader.FolioReader;
import com.folioreader.model.HighLight;
import com.folioreader.model.locators.ReadLocator;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.util.AppUtil;
import com.folioreader.util.OnHighlightListener;
import com.folioreader.util.ReadLocatorListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.readium.r2.shared.Locations;

public class Reader implements OnHighlightListener, FolioReader.OnClosedListener {

  private ReaderConfig readerConfig;
  public FolioReader folioReader;
  private Context context;
  public MethodChannel.Result result;
  public EventChannel.EventSink pageEventSink;
  private BinaryMessenger messenger;
  private String identifier;
  private String custId;

  Reader(Context context, BinaryMessenger messenger, ReaderConfig config, String identifier, String custId) {
    this.context = context;
    this.identifier = identifier;
    this.custId = custId;
    readerConfig = config;
    getHighlightsAndSave();

    folioReader = FolioReader.get()
      .setOnHighlightListener(this)
      .setReadLocatorListener(this)
      .setOnClosedListener(this);
  }

  public void open(String bookPath) {
    ReadLocator readLocator;
    SharedPreferences preferences = context.getSharedPreferences(this.custId, Context.MODE_PRIVATE);
    String jsonString = preferences.getString(this.identifier, null);
    
    if (jsonString != null) {
      try {
        JSONObject jsonObj = new JSONObject(jsonString);
        Locations locations = new Locations();
        locations.setCfi(jsonObj.optString("cfi"));
        readLocator = new ReadLocator(jsonObj.optString("bookId"), jsonObj.optString("href"), jsonObj.optLong("created"), locations);
        folioReader.setReadLocator(readLocator);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    } else {
      folioReader.setReadLocator(null);
    }
    folioReader.setConfig(readerConfig.config, true).openBook(bookPath);
  }

  public void close(){
    folioReader.close();
  }

  private void getHighlightsAndSave() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        ArrayList<HighLight> highlightList = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
          highlightList = objectMapper.readValue(
              loadAssetTextAsString("highlights/highlights_data.json"),
              new TypeReference<List<HighlightData>>() {});
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (highlightList == null) {
          folioReader.saveReceivedHighLights(highlightList, new OnSaveHighlight() {
            @Override
            public void onFinished() {
            }
          });
        }
      }
    }).start();
  }

  private String loadAssetTextAsString(String name) {
    if (name != null) {
      BufferedReader in = null;
      try {
        StringBuilder buf = new StringBuilder();
        InputStream is = context.getAssets().open(name);
        in = new BufferedReader(new InputStreamReader(is));
        String str;
        boolean isFirst = true;
        while ((str = in.readLine()) != null) {
          if (isFirst)
            isFirst = false;
          else
            buf.append('\n');
          buf.append(str);
        }
        return buf.toString();
      } catch (IOException e) {
        Log.e("Reader", "Error opening asset " + name);
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            Log.e("Reader", "Error closing asset " + name);
          }
        }
      }
    }
    return null;
  }

  @Override
  public void onFolioReaderClosed() {
  }

  @Override
  public void onHighlight(HighLight highlight, HighLight.HighLightAction type) {
  }

}