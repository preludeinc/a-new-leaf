package com.finalproject;

import android.app.Application;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * This View Model class manages clock-tick execution and helps to update plant images.
 */
public class FocusViewModel extends AndroidViewModel {
    private static final long CLOCK_TICK = 1000;
    private static final long FOCUS_TIME = (long) 5.00;
    private static final int FLOWER_ARR_SIZE = 3;
    private int plantCount = 0;
    private final long focusDuration = TimeUnit.MINUTES.toMillis(FOCUS_TIME);
    private CountDownTimer countDownTimer;
    private final MutableLiveData<String> _clockTime = new MutableLiveData<>();
    private final MutableLiveData<String> _plantCount = new MutableLiveData<>();
    private final MutableLiveData<String> _sproutTime = new MutableLiveData<>();

    public FocusViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Getter for _clockTime
     *
     * @return _clockTime, time in seconds
     */
    public LiveData<String> getTime() {
        return _clockTime;
    }

    /**
     * Getter for _plantCount
     *
     * @return _plantCount, used to track position in current plant's drawable array.
     */
    public LiveData<String> getPlantCount() {
        return _plantCount;
    }

    /**
     * Getter for _sproutTime
     *
     * @return _sproutTime, the plant growth interval.
     */
    public LiveData<String> getSproutTime() {
        return _sproutTime;
    }

    /**
     * Getter for countdownTimer
     *
     * @return countDownTimer, helps to cancel timer when user exits the app
     */
    public CountDownTimer getCountDownTimer() {
        return countDownTimer;
    }

    /**
     * Uses TimeUnit class to convert milliseconds into minutes.
     *
     * @param millis current time, in milliseconds
     * @return current number of minutes
     */
    public long getMinutes(long millis) {
        // ms are converted to min and second
        return TimeUnit.MILLISECONDS.toMinutes(millis);
    }

    /**
     * Uses TimeUnit class to convert milliseconds into seconds.
     *
     * @param millis current time, in milliseconds
     * @return current number of seconds
     */
    public long getSeconds(long millis) {
        return TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
    }

    /**
     * Setter for mutable _clockTime, based on countdown-timer.
     *
     * @param tickTime formatted String representing current count-down time.
     */
    public void updateClockTime(String tickTime) {
        if (tickTime != null) {
            _clockTime.setValue(tickTime);
        }
    }

    /**
     * Setter for _plantCount, helps to track plant growth.
     *
     * @param numPlant increments at a set interval currently, about every minute.
     */
    public void updatePlantCount(String numPlant) {
        if (numPlant != null) {
            _plantCount.setValue(numPlant);
        }
    }

    /**
     * Setter for mutable _sproutTime, based on second count-down timer.
     *
     * @param growthTime String representing an interval of about once per minute.
     */
    public void updateSproutTime(String growthTime) {
        if (growthTime != null) {
            _sproutTime.setValue(growthTime);
        }
    }

    /**
     * Starts a five minute timer when the leaf button is pressed.
     * <p>
     * Milliseconds are converted to minutes and seconds, and formatted as a string.
     **/
    public void startCountDownTimer() {
        countDownTimer = new CountDownTimer(focusDuration, CLOCK_TICK) {
            @Override
            public void onTick(long millis) {
                // ms are converted to min and second
                long minutes = getMinutes(millis);
                long seconds = getSeconds(millis);
                // formats time as a string, used to update clock-text
                String sTime = String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds);
                updateClockTime(sTime);

                String[] growthTime = { "3:50", "2:00", "0:30" };
                for (String timeCheck: growthTime) {
                    if (sTime.equals(timeCheck)) {
                        if (plantCount < FLOWER_ARR_SIZE) {
                            plantCount++;
                            updatePlantCount(String.valueOf(plantCount));
                            String updatePlant = "plant" + plantCount;
                            updateSproutTime(updatePlant);
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                cancel();
                updatePlantCount("0");
            }
        }.start();
    }
}
