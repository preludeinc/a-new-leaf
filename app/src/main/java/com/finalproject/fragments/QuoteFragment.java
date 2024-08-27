package com.finalproject.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.finalproject.AccessSharedPref;
import com.finalproject.databinding.FragmentQuoteBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class QuoteFragment extends Fragment {
    private Context context;
    private String quote, author;
    private TextView quoteText, authorText;
    private static int requestCount = 0;
    private static final int MAX_REQUESTS = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // binding
        FragmentQuoteBinding binding =
                FragmentQuoteBinding.inflate(getLayoutInflater(), container, false);
        View quoteView = binding.getRoot();
        quoteText = binding.quoteText;
        authorText = binding.quoteAuthor;
        context = quoteView.getContext();
        requestCount++;
        getQuoteData();
        return quoteView;
    }

    /**
     * Fetches a new random quote, taken from zen quotes.io, using Volley API
     */
    private void getQuoteData() {
        // shared preferences
        AccessSharedPref sharedPref = new AccessSharedPref(requireContext());
        // a new API call is made if fewer than five requests have been made
        // otherwise, stored values are displayed
        if (requestCount <= MAX_REQUESTS) {
            JsonArrayRequest jsonArrayRequest = getJsonArrayRequest(sharedPref);
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(jsonArrayRequest);
        } else {
            String retrieveQuote = sharedPref.retrieveData("quote");
            String retrieveAuthor = sharedPref.retrieveData("author");
            setQuoteText(retrieveQuote, retrieveAuthor);
        }
    }

    /**
     * Fetches data from zen quotes.io, using a GET request via Volley.
     * <p>
     * The JSONArray is parsed into objects, then the fragment's display is updated.
     *
     * @param sharedPref an instance of shared preferences for the project taken from helper class
     * @return a JSONArrayRequest, added to Volley's requestQueue.
     */
    @NonNull
    private JsonArrayRequest getJsonArrayRequest(AccessSharedPref sharedPref) {
        String url = "https://zenquotes.io/api/random";
        // creates a new response listener based on request method, url, and jsonArray
        return new JsonArrayRequest(Request.Method.GET, url, null, jsonArray -> {
            try {
                JSONObject responseObj = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    responseObj = jsonArray.getJSONObject(i);
                }
                if (responseObj != null) {
                    quote = responseObj.getString("q");
                    author = responseObj.getString("a");
                    sharedPref.storeQuote(quote, author);
                    setQuoteText(quote, author);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, volleyError -> Log.e("Error", "Error parsing data"));
    }

    /**
     * Displays formatted quote text.
     *
     * @param quote  recently fetched or stored quote, from shared preferences
     * @param author recently fetched or stored author
     */
    public void setQuoteText(String quote, String author) {
        String formattedQuote = "\"" + quote + "\"";
        String formattedAuthor = "~" + author;
        quoteText.setText(formattedQuote);
        authorText.setText(formattedAuthor);
    }
}
