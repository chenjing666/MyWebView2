package com.test.mywebview;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import static android.view.KeyEvent.KEYCODE_BACK;

public class MainActivity extends AppCompatActivity {
    private LinearLayout webViewLayout;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webViewLayout = (LinearLayout) findViewById(R.id.web_view);
        webView = new WebView(this);
        webViewLayout.addView(webView);

        webView.loadUrl("http://www.chinaautoid.com/jiejue_1.php");//WebView加载的网页使用loadUrl
        final WebSettings webSettings = webView.getSettings();//获得WebView的设置
        webSettings.setUseWideViewPort(true);// 设置此属性，可任意比例缩放
        webSettings.setLoadWithOverviewMode(true);//适配
        webSettings.setJavaScriptEnabled(true);  //支持js
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);  //设置 缓存模式
        webSettings.setDomStorageEnabled(true);// 开启 DOM storage API 功能
        webSettings.setDatabaseEnabled(true);//开启 database storage API 功能
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);//HTTPS，注意这个是在LOLLIPOP以上才调用的
        }

        webSettings.setAppCacheEnabled(true);//开启 Application Caches 功能
        webSettings.setBlockNetworkImage(true);//关闭加载网络图片，在一开始加载的时候可以设置为true，当加载完网页的时候再设置为false

        //优先使用缓存:
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //缓存模式如下：
        //LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
        //LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
        //LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
        //LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。

        //不使用缓存:
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //加载的进度
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                //获取WebView的标题
                Toast.makeText(MainActivity.this,title,Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
//                return super.onJsAlert(view, url, message, result);
                //Js 弹框
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
                return super.onJsAlert(view, url, "2"+message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("删除");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                b.create().show();
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //需要设置在当前WebView中显示网页，才不会跳到默认的浏览器进行显示
                view.loadUrl(url);//
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                //加载出错了
                Toast.makeText(MainActivity.this,"error"+error,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //加载完成
                webSettings.setBlockNetworkImage(false);
            }
        });
        webView.setDownloadListener(new DownLoadListener());//下载监听


        webView.addJavascriptInterface(new WebAppInterface(this), "WebJs");

        webView.loadUrl("javascript:jsMethod()");//这是WebView最简单的调用JS的方法


    }

    public class WebAppInterface {
        Context mContext;

        public WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void method() {
        }
    }

    private class DownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

        }
    }
    //监听安卓返回键，对webview返回
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.clearCache(true); //清空缓存
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (webViewLayout != null) {
                    webViewLayout.removeView(webView);
                }
                webView.removeAllViews();
                webView.destroy();
            }else {
                webView.removeAllViews();
                webView.destroy();
                if (webViewLayout != null) {
                    webViewLayout.removeView(webView);
                }
            }
            webView = null;
        }
    }

}
