package com.vadimoe.nomnom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity implements AccelerometerListener {

    final static String TAG ="NomNom Main Activity";
    final static String EDAMAM_SEARCH_API = "https://api.edamam.com/search";
    final static String APP_ID = "8fd8d674";
    final static String API_KEY = "8a9950dfddae1d65091be2bc07cde612";

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

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    public void onAccelerationChanged(float x, float y, float z) {
        // TODO Auto-generated method stub

    }

    public void onShake(float force) {

        try {
            //Toast.makeText(getBaseContext(), "Shake Detected", Toast.LENGTH_LONG).show();

            // Do your stuff here
            String[] ingredients = {"chicken", "rice"};
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
            
            String recipe = getRecipe(ingredients, diet_preferences, health_preferences, calories_count);
            Log.i(TAG, "Recipe = " + recipe);

            JSONObject jsonRecipe = new JSONObject(recipe);

            String name = jsonRecipe.getString("label");
            // Called when Motion Detected
            Toast.makeText(getBaseContext(), name, Toast.LENGTH_LONG).show();
        }
        catch (JSONException e) {
            Log.i(TAG, "JSONException:" + e.getMessage());
        }
        catch (Exception e) {
            Log.i(TAG, "Exception:" + e.getMessage());
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
        Toast.makeText(getBaseContext(), "onResume Accelerometer Started",
                Toast.LENGTH_SHORT).show();

        //Check device supported Accelerometer senssor or not
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

            Toast.makeText(getBaseContext(), "onStop Accelerometer Stopped",
                    Toast.LENGTH_SHORT).show();
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

            Toast.makeText(getBaseContext(), "onDestroy Accelerometer Stopped",
                    Toast.LENGTH_LONG).show();
        }

    }

    private String getRecipe(String[] ingredient_filters, String[] diet_filters,
                             String[] health_filters, int calories_filter) {

        Log.i(TAG, "Get recipe for " + ingredient_filters);
        String ingredients = "";
        for (int i = 0; i < ingredient_filters.length; i++) {
            if (i == 0)
                ingredients += ingredient_filters[i];
            else
                ingredients += "+" + ingredient_filters[i];
        }

        String recipe = "";
        try {
            // Get full Concierge profile from iFashion
            String httpResponse = new HttpGetter(this,
                    "Please wait why we are searching for a recipe")
                    .execute(
                        EDAMAM_SEARCH_API,
                        "app_id=" + APP_ID,
                        "app_key=" + API_KEY,
                        "q=" + ingredients
                    ).get();

            if (!httpResponse.equals("")) {
                try {
                    JSONObject jsonResponse = new JSONObject(httpResponse);
                    JSONArray recipes =  jsonResponse.getJSONArray("hits");
                    for (int i = 0; i < recipes.length(); i++) {
                        JSONObject jsonRecipe = recipes.getJSONObject(i).getJSONObject("recipe");
                        String dietLabels = jsonRecipe.get("dietLabels").toString().toLowerCase();
                        String healthLabels = jsonRecipe.get("healthLabels").toString().toLowerCase();
                        String calCount = jsonRecipe.get("calories").toString();

                        boolean bRecipeMatches = true;

                        // Check if recipe matches diet filters
                        if (diet_filters != null) {
                            for (String f : diet_filters) {
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
                        if (health_filters != null) {
                            for (String f : health_filters) {
                                if (!healthLabels.contains(f)) {
                                    bRecipeMatches = false;
                                    break;
                                }
                            }
                        }

                        if (!bRecipeMatches)
                            // The recipe does not match diet filters, go to next
                            continue;


                        // We found the recipe that matches our filters
                        recipe = jsonRecipe.toString();
                    }
                }
                catch (JSONException e) {
                    // Handle exception
                    Log.i(TAG, "JSONException:" + e.getMessage());
                }
            }

        } catch (InterruptedException e) {
            // Handle exception
            Log.i(TAG, "InterruptedException:" + e.getMessage());
        } catch (ExecutionException e) {
            // Handle exception
            Log.i(TAG, "ExecutionException:" + e.getMessage());
        }

        Log.i(TAG, "Recipe =" + recipe);


        return recipe;
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
