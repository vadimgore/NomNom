package com.vadimoe.nomnom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;


public class RecipeViewerActivity extends ActionBarActivity {

    final static String TAG ="RecipeViewerActivity";

    TextView mRecipeName;
    TextView mRecipeIngredients;
    ImageView mRecipeImage;
    WebView mWebView;
    String mRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_viewer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecipeName = (TextView) findViewById(R.id.recipe_name);
        mRecipeIngredients = (TextView) findViewById(R.id.recipe_ingredients);
        mRecipeImage = (ImageView) findViewById(R.id.recipe_image);
        mWebView = (WebView) findViewById(R.id.webview);

        mRecipe = getIntent().getStringExtra("recipe");

        try {
            JSONObject jsonRecipe = new JSONObject(mRecipe);
            String name = jsonRecipe.getString("label");
            String ingredients = formatIngredients(jsonRecipe.getString("ingredientLines"));
            String urlImage = jsonRecipe.getString("image");
            String urlSource = jsonRecipe.getString("url");

            mRecipeName.setText(name);
            mRecipeIngredients.setText(ingredients);
            new ImageLoader(mRecipeImage).execute(urlImage);
            //mWebView.getSettings().setBuiltInZoomControls(true);
            //mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            mWebView.loadUrl(urlSource);

        } catch (JSONException e) {
            Log.i(TAG, "JSONException: " + e.getMessage());
        } catch (Exception e) {
            Log.i(TAG, "Exception: " + e.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipe_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String formatIngredients(String ingredients) {

        StringBuilder formatted_ingredients = new StringBuilder();
        try {

            JSONArray jsonArray = new JSONArray(ingredients);
            for (int i = 0; i < jsonArray.length(); i++) {
                formatted_ingredients.append(jsonArray.getString(i));
                formatted_ingredients.append("\n");
            }
        } catch (JSONException e) {
            Log.i(TAG, "Exception: " + e.getMessage());
        }

        return formatted_ingredients.toString();
    }
}
