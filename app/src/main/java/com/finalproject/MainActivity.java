package com.finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.finalproject.databinding.ActivityMainBinding;
import com.finalproject.fragments.*;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements DefaultLifecycleObserver {

    // UI variables
    private ActivityMainBinding binding;
    private TextView stopWatchView;
    private ImageButton[] buttons;
    private ImageButton plantBtn, musicBtn, homeBtn, quoteBtn, statsBtn;
    private final ArrayList<Class<? extends Fragment>> buttonClass = new ArrayList<>();

    // ViewModel & Plant Data
    private FocusViewModel focusViewModel;
    private static int plantCount = 0;
    private static final int FOCUS_TIME = 5;
    private static int FOCUS_TOTAL = 0;
    private static int FOCUS_SESSIONS = 0;

    // Notifications and Broadcast
    private static final int NOTIFICATION_REQUEST = 1;
    private PlantReceiver plantReceiver;
    private LocalBroadcastManager localBroadcastManager;
    public static final String CUSTOM_BROADCAST = "com.final project.CUSTOM_BROADCAST";

    // Shared Preferences
    private AccessSharedPref sharedPreferences;

    // State
    private ActivityState activityState;
    private String timeStart, timeEnd;
    private boolean registerReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // a reference to the view model
        focusViewModel = new ViewModelProvider(this).get(FocusViewModel.class);
        // observes changes to application state
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        // permissions are requested if API is greater than 33
        requestPermissions();
        initializeViews();
        initializeLists();
        displayRules();
        createAndRegisterReceiver();
        addHomeFragment(savedInstanceState);
        clickListeners();
        updatePlant();
        updateClock();
    }

    /**
     * Activity State helps to update timers and stop music playback when app is exited.
     */
    public enum ActivityState {
        RESUMED,
        PAUSED,
        STOPPED,
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        super.onPause();
        activityState = ActivityState.PAUSED;
        stopServiceAndTimers();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        super.onStop();
        activityState = ActivityState.STOPPED;
        stopServiceAndTimers();
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        super.onResume();
        createAndRegisterReceiver();
        activityState = ActivityState.RESUMED;
        // retrieves stored time and session data from shared preferences helper class
        int updatedTime = Integer.parseInt(sharedPreferences.getFocusTime());
        int updatedSessions = Integer.parseInt(sharedPreferences.getFocusSessions());
        // if dates match, focus stats are taken from shared preferences
        if (sharedPreferences.compareDates()) {
            if (updatedTime > FOCUS_TOTAL) {
                FOCUS_TOTAL = updatedTime;
                FOCUS_SESSIONS = updatedSessions;
            }
        }
        focusViewModel.updatePlantCount("0");
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        // unregisters custom receiver
        if (localBroadcastManager != null && registerReceiver) {
            localBroadcastManager.unregisterReceiver(plantReceiver);
        }
    }

    /**
     * Tracks changes in _plantCount, which is updated in FocusViewModel.
     *
     * @param _plantCount current plant integer, assists in plant growth in home fragment.
     */
    private static void onChanged(String _plantCount) {
        plantCount = Integer.parseInt(_plantCount);
    }

    /**
     * When MainActivity is paused or stopped, music and focus timers are stopped.
     * Clock-time is also updated to initial time, of 5:00.
     */
    private void stopServiceAndTimers() {
        if (activityState != ActivityState.RESUMED) {
            pauseMediaPlayer();
            focusViewModel.getCountDownTimer().cancel();
            focusViewModel.updateClockTime(timeStart);
            focusViewModel.updatePlantCount("0");
            updateHomeFragment(0);
        }
    }

    /**
     * Stops music playback. Used when app is exited.
     */
    public void pauseMediaPlayer() {
        NotificationManagerCompat.from(getApplicationContext()).cancelAll();
        Intent musicIntent = new Intent(getApplicationContext(), AudioService.class);
        stopService(musicIntent);
    }

    /**
     * Updates the clock's text display based on CountdownTimer in FocusViewModel.
     * Once the time reaches zero, the updated focus session stats are sent to the plant receiver.
     * This is used to track daily focus stats.
     */
    private void updateClock() {
        // if the time changes, stop-watch text updates too
        focusViewModel.getTime().observe(this, _clockTick -> {
            if (_clockTick != null) {
                stopWatchView.setText(_clockTick);
                if (_clockTick.equals(getString(R.string.time_end))) {
                    Intent customIntent = new Intent(MainActivity.CUSTOM_BROADCAST);
                    FOCUS_TOTAL += FOCUS_TIME;
                    FOCUS_SESSIONS++;
                    customIntent.putExtra("update_focus", FOCUS_TOTAL);
                    customIntent.putExtra("focus_sessions", FOCUS_SESSIONS);
                    // stats fragment is updated via the receiver class and shared preferences
                    localBroadcastManager.sendBroadcast(customIntent);
                    updateFragment(StatsFragment.class);
                }
            }
        });
    }

    /**
     * A broadcast is sent to the plant-receiver each time a focus interval passes.
     */
    private void updatePlant() {
        // updates at an interval, used to help show plant growth
        focusViewModel.getSproutTime().observe(this, _sproutTime -> {
            if (_sproutTime != null) {
                // a count is updated within viewModel, is used to update plant image
                focusViewModel.getPlantCount().observe(this, MainActivity::onChanged);
                updateHomeFragment(plantCount);
            }
        });
    }

    /**
     * Creates click listeners for rules, music, home, quotes, and stats image-buttons.
     * These fragments replace the active fragment in the fragment container, via updateFragment.
     * When the plant button is clicked, the stop-watch timer starts.
     */
    private void clickListeners() {
        String[] buttonText = getResources().getStringArray(R.array.button_tooltip);
        // sets tool-tip text
        for (int i = 0; i < buttons.length; i++)
            TooltipCompat.setTooltipText(buttons[i], buttonText[i]);
        // click listeners for fragment buttons
        for (int j = 0; j < buttonClass.size(); j++)
            createClickListener(buttons[j], buttonClass.get(j));

        // leaf button starts a focus session,
        plantBtn.setOnClickListener(v -> {
            String time = stopWatchView.getText().toString();
            // timer can be re-started at 0:00 or 5:00 minutes
            if (time.equals(timeStart) || time.equals(timeEnd)) {
                focusViewModel.startCountDownTimer();
                //focusViewModel.startPlantGrowthTimer();
            }
        });
    }

    /**
     * Helper function to create click listeners.
     * Home fragment is updated with current plant count, to track plant growth.
     *
     * @param button        imageButton click listener is attached to
     * @param fragmentClass fragment to be opened when it is clicked
     */
    private void createClickListener(ImageButton button,
                                     Class<? extends Fragment> fragmentClass) {
        if (button.equals(homeBtn)) {
            homeBtn.setOnClickListener(v -> {
                updateHomeFragment(plantCount);
                animateClick(homeBtn);
            });
        } else {
            button.setOnClickListener(v -> {
                updateFragment(fragmentClass);
                animateClick(button);
            });
        }
    }

    /**
     * Helper function to animate button when clicked. Creates a simple bounce effect.
     *
     * @param button button to be animated
     */
    private void animateClick(ImageButton button) {
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        button.setAnimation(scaleUp);
        button.setAnimation(scaleDown);
    }

    /**
     * Helps to open related fragment when its button is clicked,
     * Including: rules, music, quotes, and stats.
     *
     * @param fragmentClassToChangeTo fragment class which will be active next
     */
    private void updateFragment(Class<? extends Fragment> fragmentClassToChangeTo) {
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.fragment_container,
                        fragmentClassToChangeTo, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * The plant's ID is passed to the home fragment so the display can be updated.
     *
     * @param plantCount current active plant, initially tracked in focusViewModel.
     */
    private void updateHomeFragment(int plantCount) {
        String[] plantNames = getResources().getStringArray(R.array.flower_img);
        // helps ensure plant count remains within bounds
        if (plantCount > plantNames.length - 1) plantCount = 0;
        int plantID = getResources().getIdentifier(plantNames[plantCount], "drawable",
                getPackageName());
        // creating and populating plant bundle
        Bundle plantBundle = new Bundle();
        plantBundle.putInt("plant_id", plantID);
        // fragment is updated with new plant
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, HomeFragment.class, plantBundle)
                .commit();
    }

    /**
     * Adds home fragment to main activity.
     *
     * @param savedInstanceState helps to determine when fragment should be added
     */
    private void addHomeFragment(Bundle savedInstanceState) {
        // adds home fragment to activity
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().
                    setReorderingAllowed(true)
                    .add(R.id.fragment_container, HomeFragment.class, null)
                    .commit();
        }
    }

    /**
     * Registers plant receiver and local broadcast manager,
     * These are used to send focus-session data to stats fragment.
     */
    private void createAndRegisterReceiver() {
        // registers plant receiver so images can be updated
        plantReceiver = new PlantReceiver(getApplicationContext());
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(plantReceiver, new IntentFilter(CUSTOM_BROADCAST));
        registerReceiver = true;
    }

    /**
     * Rules text is displayed on app's first load.
     */
    private void displayRules() {
        boolean appOpened = sharedPreferences.
                getSharedPreferences().getBoolean("first_opened",
                true);
        if (appOpened) {
            try {
                new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                        .setMessage(R.string.rules_text)
                        .setCancelable(true)
                        .show();
                SharedPreferences.Editor editor = sharedPreferences.getSharedPreferences().edit();
                editor.putBoolean("first_opened", false);
                editor.apply(); // Save changes
            } catch (Error err) {
                Log.e("error", "Error displaying rules dialog");
            }
        }
    }

    /**
     * Helper function for click listeners.
     * Buttons are stored in imageButton array, classes are stored in buttonClass Array List.
     */
    private void initializeLists() {
        buttons = new ImageButton[]{ musicBtn, homeBtn, quoteBtn, statsBtn, plantBtn };
        buttonClass.addAll(Arrays.asList(
                MusicFragment.class,
                HomeFragment.class,
                QuoteFragment.class,
                StatsFragment.class
        ));
    }

    /**
     * View binding for components used as click listeners.
     * Time strings, and shared preferences instance are also initialized here.
     */
    private void initializeViews() {
        stopWatchView = binding.textViewStopWatch;
        plantBtn = binding.imageButtonPlantButton;
        homeBtn = binding.imageButtonHomeButton;
        musicBtn = binding.imageButtonMusicButton;
        quoteBtn = binding.imageButtonQuoteButton;
        statsBtn = binding.imageButtonStatsButton;
        // time and shared preferences
        timeStart = getString(R.string.time_start);
        timeEnd = getString(R.string.time_end);
        sharedPreferences = new AccessSharedPref(getApplicationContext());
    }

    /**
     * Requests user permissions for post notifications, if API is greater than 33.
     */
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST);
        }
    }
}