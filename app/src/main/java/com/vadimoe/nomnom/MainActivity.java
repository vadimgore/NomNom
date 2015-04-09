package com.vadimoe.nomnom;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity implements AccelerometerListener, AsyncTaskListener {

    final static String TAG ="NomNom Main Activity";
    final static String EDAMAM_SEARCH_API = "https://api.edamam.com/search";
    final static String APP_ID = "8fd8d674";
    final static String API_KEY = "8a9950dfddae1d65091be2bc07cde612";

    RadioButton mBreakfast;
    RadioButton mLunch;
    RadioButton mDinner;

    String mMeal;

    final String[] diet_filters = {
          "balanced",
          "high-protein",
          "low-carb",
          "low-fat",
          "high-fiber",
          "low-sodium"
    };

    final String[] health_filters = {
          "vegetarian",
          "vegan",
          "gluten-free",
          "dairy-free",
          "soy-free",
          "fish-free",
          "shellfish-free",
          "tree-nut-free",
          "egg-free",
          "peanut-free"
    };

    SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBreakfast = (RadioButton) findViewById(R.id.breakfast);
        mLunch = (RadioButton) findViewById(R.id.lunch);
        mDinner = (RadioButton) findViewById(R.id.dinner);
        mMeal = "dinner";

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }

    public void onAccelerationChanged(float x, float y, float z) {
        // TODO Auto-generated method stub

    }

    public void onShake(float force) {
        // Do your stuff here
        String[] ingredients = {mMeal};
        findRecipe(ingredients);
    }

    public void onRadioButtonClicked(View view) {
        // Is the view now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.breakfast:
                if (checked)
                    mMeal = "breakfast";
                break;
            case R.id.lunch:
                if (checked)
                    mMeal = "lunch";
                break;
            case R.id.dinner:
                if (checked)
                    mMeal = "dinner";
                break;
            case R.id.desert:
                if (checked)
                    mMeal = "desert";
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        DialogFragment dialog = null;

        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_diet:
                dialog = new DietDialogFragment();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_diet_settings));
                return true;
            case R.id.action_health:
                dialog = new HealthDialogFragment();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_health_settings));
                return true;
            case R.id.action_calories:
                dialog = new CaloriesDialogFragment();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_calories_settings));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(getBaseContext(), "onResume Accelerometer Started",
        //        Toast.LENGTH_SHORT).show();

        //Check device supported Accelerometer sensor or not
        if (AccelerometerManager.isSupported(this)) {

            //Start Accelerometer Listening
            AccelerometerManager.startListening(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isListening()) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening();

            //Toast.makeText(getBaseContext(), "onStop Accelerometer Stopped",
            //        Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Sensor", "Service  destroy");

        //Check device supported Accelerometer senssor or not
        if (AccelerometerManager.isListening()) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening();

            //Toast.makeText(getBaseContext(), "onDestroy Accelerometer Stopped",
            //        Toast.LENGTH_LONG).show();
        }

    }

    private void findRecipe(String[] ingredient_filters) {

        Log.i(TAG, "Get recipe for " + ingredient_filters);
        String ingredients = "";
        for (int i = 0; i < ingredient_filters.length; i++) {
            if (i == 0)
                ingredients += ingredient_filters[i];
            else
                ingredients += "+" + ingredient_filters[i];
        }

        new HttpGetter(this, this,
            "Please wait why we are searching for a recipe")
            .execute(
                    EDAMAM_SEARCH_API,
                    "app_id=" + APP_ID,
                    "app_key=" + API_KEY,
                    "q=" + ingredients
            );
    }

    @Override
    public void onTaskComplete(String httpResponse) {
        if (httpResponse.equals(""))
            return;

        String[] saved_diet_preferences = mSharedPref.getString(
                getString(R.string.dialog_diet_settings), "").split(",");
        String[] saved_health_preferences = mSharedPref.getString(
                getString(R.string.dialog_health_settings), "").split(",");
        Integer calories_count = mSharedPref.getInt(
                getString(R.string.dialog_calories_settings), 0)*1000;

        String[] diet_preferences = null;
        if (!saved_diet_preferences[0].equals("")) {
            diet_preferences = new String[saved_diet_preferences.length];
            for (int i = 0; i < saved_diet_preferences.length; i++) {
                Integer item = Integer.parseInt(saved_diet_preferences[i]);
                diet_preferences[i] = diet_filters[item];
            }
        }

        String[] health_preferences = null;
        if (!saved_health_preferences[0].equals("")) {
            health_preferences = new String[saved_health_preferences.length];
            for (int i = 0; i < saved_health_preferences.length; i++) {
                Integer item = Integer.parseInt(saved_health_preferences[i]);
                health_preferences[i] = health_filters[item];
            }
        }

        try {
            JSONObject jsonResponse = new JSONObject(httpResponse);
            JSONArray recipes =  jsonResponse.getJSONArray("hits");
            boolean bRecipeMatches = true;
            for (int i = 0; i < recipes.length(); i++) {
                JSONObject jsonRecipe = recipes.getJSONObject(i).getJSONObject("recipe");
                String dietLabels = jsonRecipe.get("dietLabels").toString().toLowerCase();
                String healthLabels = jsonRecipe.get("healthLabels").toString().toLowerCase();
                String calCount = jsonRecipe.get("calories").toString();
                bRecipeMatches = true;

                // Check if recipe matches diet filters
                if (diet_preferences != null) {
                    for (String f : diet_preferences) {
                        if (!dietLabels.contains(f)) {
                            bRecipeMatches = false;
                            break;
                        }
                    }
                }

                if (!bRecipeMatches)
                    // The recipe does not match diet filters, go to next
                    continue;

                // Check if recipe matches health filters
                if (health_preferences != null) {
                    for (String f : health_preferences) {
                        if (!healthLabels.contains(f)) {
                            bRecipeMatches = false;
                            break;
                        }
                    }
                }

                if (bRecipeMatches) {
                    // We found the recipe that matches our filters
                    String name = jsonRecipe.getString("label");
                    Log.i(TAG, "Recipe = " + name);
                    //Toast.makeText(getBaseContext(), "Found recipe: " + name,
                    //        Toast.LENGTH_LONG).show();
                    Intent recipeViewIntent = new Intent(this, RecipeViewerActivity.class);
                    recipeViewIntent.putExtra("recipe", jsonRecipe.toString());
                    startActivity(recipeViewIntent);
                    break;
                }
            }

            if (!bRecipeMatches) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("No recipes matching your diet and health criteria found")
                        .setTitle("Nom Nom")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }


        }
        catch (JSONException e) {
            // Handle exception
            Log.i(TAG, "JSONException:" + e.getMessage());
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MainFragment extends Fragment {

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
