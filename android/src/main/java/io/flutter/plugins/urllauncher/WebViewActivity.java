// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.urllauncher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Browser;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.util.HashMap;
import java.util.Map;

/*  Launches WebView activity */
public class WebViewActivity extends Activity {

  /*
   * Use this to trigger a BroadcastReceiver inside WebViewActivity
   * that will request the current instance to finish.
   * */
  public static String ACTION_CLOSE = "close action";

  private final BroadcastReceiver broadcastReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();
          if (ACTION_CLOSE.equals(action)) {
            finish();
          }
        }
      };

  private final WebViewClient webViewClient =
      new WebViewClient() {

        /*
         * This method is deprecated in API 24. Still overridden to support
         * earlier Android versions.
         */
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
          if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            view.loadUrl(url);
            return false;
          }
          return super.shouldOverrideUrlLoading(view, url);
        }

        @RequiresApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.loadUrl(request.getUrl().toString());
          }
          return false;
        }

          @Override
          public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error)
          {
              handleSslError(view, handler, error);
          }

          private void handleSslError(WebView view, final SslErrorHandler handler, SslError error) {
              Context context = view.getContext();
              android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(context);
//        String message = context.getString(R.string.ssl_error_message_default);
              String message = "SSL Certificate error.";
              int errorType = error.getPrimaryError();
              switch (errorType) {
                  case SslError.SSL_UNTRUSTED:
//            message = context.getString(R.string.ssl_error_message_untrusted);
                      message = "The certificate authority is not trusted.";
                      break;
                  case SslError.SSL_EXPIRED:
//            message = context.getString(R.string.ssl_error_message_expired);
                      message = "The certificate has expired.";
                      break;
                  case SslError.SSL_IDMISMATCH:
//            message = context.getString(R.string.ssl_error_message_id_mismatch);
                      message = "The certificate Hostname mismatch.";
                      break;
                  case SslError.SSL_NOTYETVALID:
//            message = context.getString(R.string.ssl_error_message_not_valid);
                      message = "The certificate is not yet valid.";
                      break;
              }
//        message += " " + context.getString(R.string.ssl_error_wether_continue);
              message += " Do you want to continue anyway?";
//        dialogBuilder.setTitle(R.string.ssl_error_title);
              dialogBuilder.setTitle("SSL Certificate Error");
              dialogBuilder.setMessage(message);

//        dialogBuilder.setPositiveButton(R.string.string_continue, new DialogInterface.OnClickListener() {
              dialogBuilder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                      handler.proceed();
                  }
              });
//        dialogBuilder.setNegativeButton(R.string.string_cancel, new DialogInterface.OnClickListener() {
              dialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                      handler.cancel();
                  }
              });
              android.app.AlertDialog dialog = dialogBuilder.create();
              dialog.show();
          }
      };

  private WebView webview;

  private IntentFilter closeIntentFilter = new IntentFilter(ACTION_CLOSE);

  // Verifies that a url opened by `Window.open` has a secure url.
  private class FlutterWebChromeClient extends WebChromeClient {
    @Override
    public boolean onCreateWindow(
        final WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
      final WebViewClient webViewClient =
          new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(
                @NonNull WebView view, @NonNull WebResourceRequest request) {
              webview.loadUrl(request.getUrl().toString());
              return true;
            }

            /*
             * This method is deprecated in API 24. Still overridden to support
             * earlier Android versions.
             */
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
              webview.loadUrl(url);
              return true;
            }

              @Override
              public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error)
              {
                  handleSslError(view, handler, error);
              }

              private void handleSslError(WebView view, final SslErrorHandler handler, SslError error) {
                  Context context = view.getContext();
                  android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(context);
//        String message = context.getString(R.string.ssl_error_message_default);
                  String message = "SSL Certificate error.";
                  int errorType = error.getPrimaryError();
                  switch (errorType) {
                      case SslError.SSL_UNTRUSTED:
//            message = context.getString(R.string.ssl_error_message_untrusted);
                          message = "The certificate authority is not trusted.";
                          break;
                      case SslError.SSL_EXPIRED:
//            message = context.getString(R.string.ssl_error_message_expired);
                          message = "The certificate has expired.";
                          break;
                      case SslError.SSL_IDMISMATCH:
//            message = context.getString(R.string.ssl_error_message_id_mismatch);
                          message = "The certificate Hostname mismatch.";
                          break;
                      case SslError.SSL_NOTYETVALID:
//            message = context.getString(R.string.ssl_error_message_not_valid);
                          message = "The certificate is not yet valid.";
                          break;
                  }
//        message += " " + context.getString(R.string.ssl_error_wether_continue);
                  message += " Do you want to continue anyway?";
//        dialogBuilder.setTitle(R.string.ssl_error_title);
                  dialogBuilder.setTitle("SSL Certificate Error");
                  dialogBuilder.setMessage(message);

//        dialogBuilder.setPositiveButton(R.string.string_continue, new DialogInterface.OnClickListener() {
                  dialogBuilder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          handler.proceed();
                      }
                  });
//        dialogBuilder.setNegativeButton(R.string.string_cancel, new DialogInterface.OnClickListener() {
                  dialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          handler.cancel();
                      }
                  });
                  android.app.AlertDialog dialog = dialogBuilder.create();
                  dialog.show();
              }
          };

      final WebView newWebView = new WebView(webview.getContext());
      newWebView.setWebViewClient(webViewClient);

      final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
      transport.setWebView(newWebView);
      resultMsg.sendToTarget();

      return true;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    webview = new WebView(this);
    setContentView(webview);
    // Get the Intent that started this activity and extract the string
    final Intent intent = getIntent();
    final String url = intent.getStringExtra(URL_EXTRA);
    final boolean enableJavaScript = intent.getBooleanExtra(ENABLE_JS_EXTRA, false);
    final boolean enableDomStorage = intent.getBooleanExtra(ENABLE_DOM_EXTRA, false);
    final Bundle headersBundle = intent.getBundleExtra(Browser.EXTRA_HEADERS);

    final Map<String, String> headersMap = extractHeaders(headersBundle);
    webview.loadUrl(url, headersMap);

    webview.getSettings().setJavaScriptEnabled(enableJavaScript);
    webview.getSettings().setDomStorageEnabled(enableDomStorage);

    // Open new urls inside the webview itself.
    webview.setWebViewClient(webViewClient);

    // Multi windows is set with FlutterWebChromeClient by default to handle internal bug: b/159892679.
    webview.getSettings().setSupportMultipleWindows(true);
    webview.setWebChromeClient(new FlutterWebChromeClient());

    // Register receiver that may finish this Activity.
    registerReceiver(broadcastReceiver, closeIntentFilter);
  }

  private Map<String, String> extractHeaders(Bundle headersBundle) {
    final Map<String, String> headersMap = new HashMap<>();
    for (String key : headersBundle.keySet()) {
      final String value = headersBundle.getString(key);
      headersMap.put(key, value);
    }
    return headersMap;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(broadcastReceiver);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
      webview.goBack();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  private static String URL_EXTRA = "url";
  private static String ENABLE_JS_EXTRA = "enableJavaScript";
  private static String ENABLE_DOM_EXTRA = "enableDomStorage";

  /* Hides the constants used to forward data to the Activity instance. */
  public static Intent createIntent(
      Context context,
      String url,
      boolean enableJavaScript,
      boolean enableDomStorage,
      Bundle headersBundle) {
    return new Intent(context, WebViewActivity.class)
        .putExtra(URL_EXTRA, url)
        .putExtra(ENABLE_JS_EXTRA, enableJavaScript)
        .putExtra(ENABLE_DOM_EXTRA, enableDomStorage)
        .putExtra(Browser.EXTRA_HEADERS, headersBundle);
  }
}
