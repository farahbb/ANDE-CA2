package com.example.tripsavvy_studio_2b03_2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchResults extends AppCompatActivity {
    private LinearLayout scrollViewContent;
    private List<Place> originalSearchResults;

    private static final int FILTER_OPTIONS_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searched);
        Intent intent = getIntent();
        int userId = intent.getIntExtra("userId", -1);
        Log.d("userid","userid:"+userId);

        String searchQuery = intent.getStringExtra("searchQuery");
        TextView searchedTerm = findViewById(R.id.searchedTerm);
        searchedTerm.setText("Searched Results: "+searchQuery);




        originalSearchResults = (List<Place>) intent.getSerializableExtra("searchResults");
        List<Place> searchResults = (List<Place>) intent.getSerializableExtra("searchResults");
        scrollViewContent = findViewById(R.id.scrollViewContent);
        populateScrollView(searchResults);
        ImageButton filterButton = findViewById(R.id.filterButton);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(0);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Intent intenth = new Intent(SearchResults.this, Home.class);
                        intenth.putExtra("userId", userId);
                        startActivity(intenth);
                        return true;

                    case R.id.action_favourites:
                        Intent intentf = new Intent(SearchResults.this, Favorites.class);
                        intentf.putExtra("userId", userId);
                        startActivity(intentf);
                        return true;

                    case R.id.action_profile:
                        Intent intentp = new Intent(SearchResults.this, Profile.class);
                        intentp.putExtra("userId", userId);
                        startActivity(intentp);
                        return true;

                    case R.id.action_store:
                        startActivity(new Intent(SearchResults.this, Store.class));
                        return true;

                    default:
                        return false;
                }

            }

        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the FilterOptions activity for result
                Intent intent = new Intent(SearchResults.this, FilterOptions.class);
                startActivityForResult(intent, FILTER_OPTIONS_REQUEST_CODE);
            }
        });
        bottomNavigationView.setSelectedItemId(0);
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = getIntent();
        String searchQuery = intent.getStringExtra("searchQuery");
        DatabaseHandler dbHandler = new DatabaseHandler(this);



        if (requestCode == FILTER_OPTIONS_REQUEST_CODE && resultCode == RESULT_OK) {

            // Retrieve selected filter options from the result intent
            ArrayList<String> selectedCategories = data.getStringArrayListExtra("selectedCategories");

            TextView filterCat = findViewById(R.id.filterCats);
            String joinedCategories = TextUtils.join(", ", selectedCategories);
            filterCat.setText("Filters: " + joinedCategories);
            Log.d("SearchPlaces", "Selection: " + selectedCategories.toString());
            Log.d("SearchPlaces", "Query: " + searchQuery);
            // Apply the filter options to your search results
            List<Place> filteredResults = dbHandler.searchPlaces(searchQuery,selectedCategories);

            // Update your UI with the filtered results
            populateScrollView(filteredResults);
        }
    }


    private void populateScrollView(List<Place> places) {
        scrollViewContent.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Place place : places) {
            // Inflate the layout for each place
            View placeView = inflater.inflate(R.layout.place_item, scrollViewContent, false);

            // Find views in the inflated layout
            ImageView imageView = placeView.findViewById(R.id.imageView);
            TextView textView = placeView.findViewById(R.id.placeName);
            ImageButton favoriteButton = placeView.findViewById(R.id.buttonFavorite2);
            favoriteButton.setTag(place.getPlaceId());

            boolean isFavorite = isPlaceFavorite(place.getPlaceId());
            favoriteButton.setImageResource(isFavorite ? R.drawable.fav_buttonpressed : R.drawable.imgbutton_fav);

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toggle the favorite state when the button is clicked
                    toggleFavorite(place.getPlaceId(), favoriteButton);
                }
            });

            // Use Picasso to load the image from the URL
            Picasso.get().load(place.getImageUrl()).into(imageView);
            textView.setText(place.getName() + "\n📍" + "distance" + " km\nDetails");

            // Add the inflated layout to the ScrollView
            scrollViewContent.addView(placeView);
        }
    }

    private boolean isPlaceFavorite(int placeId) {
        return getSharedPreferences("Favorites", MODE_PRIVATE)
                .getBoolean(getFavoriteKey(placeId), false);
    }

    private void toggleFavorite(int placeId, ImageButton favoriteButton) {
        boolean isFavorite = !isPlaceFavorite(placeId);
        favoriteButton.setImageResource(isFavorite ? R.drawable.fav_buttonpressed : R.drawable.imgbutton_fav);

        getSharedPreferences("Favorites", MODE_PRIVATE)
                .edit()
                .putBoolean(getFavoriteKey(placeId), isFavorite)
                .apply();
    }

    private String getFavoriteKey(int placeId) {
        return "favorite_" + placeId;
    }

    private List<Place> filterResults(List<String> selectedCategories) {
        List<Place> filteredResults = new ArrayList<>();

        // Implement your filtering logic here based on the selected categories
        for (Place place : originalSearchResults) {
            if (selectedCategories.contains(place.getPlacecat())) {
                filteredResults.add(place);
            }
        }

        return filteredResults;
    }

}
