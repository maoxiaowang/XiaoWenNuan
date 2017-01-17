package com.example.xiaowennuan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.xiaowennuan.R;
import com.example.xiaowennuan.db.ArticleModel;
import com.example.xiaowennuan.db.ArticleReadModel;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class ArticleActivity extends AppCompatActivity {

    CollapsingToolbarLayout collapsingToolbar;

    //ProgressBar progressBar;

    private final static String TAG = "ArticleActivity";

    private String category;

    private int aId;

    ImageView toolBarImageView;

    private final static int READ = 1;
    private final static int MAIN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Intent intent = getIntent();
        aId = intent.getIntExtra("aid", -1);
        category = intent.getStringExtra("category");

        Toolbar toolbar = (Toolbar) findViewById(R.id.article_toolbar);
        collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.article_collapsing_toolbar_layout);
        toolBarImageView = (ImageView) findViewById(R.id.article_toolbar_image_view);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //progressBar = (ProgressBar) findViewById(R.id.article_progressbar);

        initArticleContent();

    }


    /**
     * 异步初始化
     */
    private Handler initReadHandler = new Handler() {
      public void handleMessage(Message msg) {
          switch (msg.what) {
              case READ:
                  ArrayList<ArticleReadModel> readArrayList = (ArrayList<ArticleReadModel>) msg.obj;
                  ArticleReadModel item = readArrayList.get(0);
                  if (item.image1 != null) {
                      Glide.with(ArticleActivity.this).load(item.image1)
                              .placeholder(R.drawable.placeholder_big).into(toolBarImageView);
                  }
                  // 设置toolbar标题
                  collapsingToolbar.setTitle(item.category);

                  // 初始化WebView
                  final WebView webView = (WebView) findViewById(R.id.article_content_web_view);
                  WebSettings settings = webView.getSettings();
                  settings.setJavaScriptEnabled(true);
                  settings.setDomStorageEnabled(true);
                  //settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                  settings.setAppCacheEnabled(true);
                  settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                  webView.setWebViewClient(new WebViewClient() {
                      // webview加载完成
                      @Override
                      public void onPageFinished(WebView view, String url) {
                          //progressBar.setVisibility(View.GONE);
                      }
                  });

                  webView.loadDataWithBaseURL("http", item.content, "text/html", "utf-8", null);
                  break;
              default:
                  Toast.makeText(ArticleActivity.this, "文章不存在", Toast.LENGTH_SHORT).show();
          }
      }
    };
    private Handler initMainHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MAIN:
                    ArrayList<ArticleModel> arrayList = (ArrayList<ArticleModel>) msg.obj;
                    ArticleModel item = arrayList.get(0);
                    if (item.image1 != null) {
                        Log.d(TAG, item.image1);
                        Glide.with(ArticleActivity.this).load(item.image1)
                                .placeholder(R.drawable.placeholder_big).into(toolBarImageView);
                    }
                    // 设置toolbar标题
                    collapsingToolbar.setTitle(item.category);

                    WebView webView = (WebView) findViewById(R.id.article_content_web_view);
                    WebSettings settings = webView.getSettings();
                    //settings.setJavaScriptEnabled(true);
                    //settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                    settings.setAppCacheEnabled(true);
                    settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    webView.setWebViewClient(new WebViewClient() {
                        // webview加载完成
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            //progressBar.setVisibility(View.GONE);
                        }
                    });
                    webView.loadDataWithBaseURL("http", item.content, "text/html", "utf-8", null);
            }
        }
    };

    private void initArticleContent() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (category) {
                    case "read":
                        List<ArticleReadModel> readList = DataSupport.where("aid = ?", String.valueOf(aId))
                                .find(ArticleReadModel.class);
                        ArrayList<ArticleReadModel> readArrayList = (ArrayList<ArticleReadModel>) readList;
                        Message messageRead = new Message();
                        if (readArrayList.size() == 1) {
                            messageRead.what = READ;
                            messageRead.obj = readArrayList;
                            initReadHandler.sendMessage(messageRead);
                        }
                        break;
                    default:
                        List<ArticleModel> list = DataSupport.where("aid = ?", String.valueOf(aId))
                                .find(ArticleModel.class);
                        ArrayList<ArticleModel> arrayList = (ArrayList<ArticleModel>) list;
                        Message message = new Message();
                        if (arrayList.size() == 1) {
                            message.what = MAIN;
                            message.obj = arrayList;
                            initMainHandler.sendMessage(message);
                        }
                }

            }
        }).start();

        Log.d(TAG, "aid:" + String.valueOf(aId));
        //Log.d(TAG, "size:" + String.valueOf(arrayList.size()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }


}
