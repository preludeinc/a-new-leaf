package com.finalproject;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * These methods help to update and retrieve data stored in the FocusData Shared Preferences folder.
 */
public class AccessSharedPref {

    private final Context context;
    private final Gson gson;

    public AccessSharedPref(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    /**
     * Returns shared preferences instance used for focus data
     *
     * @return shared preferences instance specific to this project.
     */
    public SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("FocusData", Context.MODE_PRIVATE);
    }

    /**
     * Returns today's date in String format.
     *
     * @return copy of today's date
     */
    public String getDate() {
        String date = new SimpleDateFormat("dd-MM-yyyy",
                Locale.getDefault()).format(new Date());
        return gson.toJson(date);
    }

    /**
     * Helper function which stores focus stats in shared preferences after a session has ended.
     *
     * @param focusTotal   current number of minutes focused
     * @param focusSession current number of focus sessions
     */
    public void storeFocusStats(int focusTotal, int focusSession) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString("stored_date", getDate());
        editor.putString("updated_focus_time", String.valueOf(focusTotal));
        editor.putString("updated_focus_sessions", String.valueOf(focusSession));
        editor.apply();
    }

    /**
     * Helper function to store quote data in shared preferences from quotes fragment.
     * There is some repeated code between this and the previous function,
     * decided to leave them as two separate functions for clarity.
     *
     * @param quote a random quote, taken from Zenquotes.io
     * @param author author of this quote
     */
    public void storeQuote(String quote, String author) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString("quote", quote);
        editor.putString("author", author);
        editor.apply();
    }

    /**
     * Getter for specified parameter, stored in shared preferences
     *
     * @param dataToRetrieve parameter to be retrieved
     * @return related stored value
     */
    public String retrieveData(String dataToRetrieve) {
        return getSharedPreferences().getString(dataToRetrieve, "");
    }

    /**
     * Getter for stored date
     *
     * @return date stored in shared preferences
     */
    public String getStoredDate() {
        return retrieveData("stored_date");
    }

    /**
     * Getter for stored time
     *
     * @return date stored in shared preferences
     */
    public String getFocusTime() {
        return retrieveData("updated_focus_time");
    }

    /**
     * Getter for focus session count
     *
     * @return stored focus session count
     */
    public String getFocusSessions() {
        return retrieveData("updated_focus_sessions");
    }

    /**
     * Removes JSON formatting for stored date
     *
     * @return stored date, in simplified string form
     */
    public String formatStoredDate() {
        return getStoredDate().replace("\\", "")
                .replace("\"", "");
    }

    /**
     * Removes formatting for current date
     *
     * @return today's date
     */
    public String formatCurrentDate() {
        return getDate().replace("\"", "");
    }

    /**
     * Compares stored date to current date, used in stats fragment and main activity
     * to update stats display
     *
     * @return boolean indicating whether dates match.
     */
    public boolean compareDates() {
        String storedDate = formatStoredDate();
        String currentDate = formatCurrentDate();
        return storedDate.equals(currentDate);
    }
}
