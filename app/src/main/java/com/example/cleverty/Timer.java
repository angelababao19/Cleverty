package com.example.cleverty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class Timer extends AppCompatActivity {

    // --- UI Elements ---
    private TextView timerText, sessionStatusText, sessionCountText;
    private Button startPauseButton, resetButton, skipButton, saveSettingsButton;
    private CircularProgressIndicator progressCircular;
    private LinearLayout customizationLayout;
    private TextInputEditText focusTimeInput, breakTimeInput;
    private BottomNavigationView bottomNav;

    // --- ViewModel ---
    private TimerViewModel timerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        // --- Get the SINGLETON ViewModel instance using the factory ---
        timerViewModel = new ViewModelProvider(this, new TimerViewModel.TimerViewModelFactory()).get(TimerViewModel.class);

        initializeViews();
        setupClickListeners();
        observeViewModel(); // This is the key method to link UI and ViewModel
    }

    private void initializeViews() {
        timerText = findViewById(R.id.timer_text);
        sessionStatusText = findViewById(R.id.session_status_text);
        sessionCountText = findViewById(R.id.session_count_text);
        startPauseButton = findViewById(R.id.start_pause_button);
        resetButton = findViewById(R.id.reset_button);
        skipButton = findViewById(R.id.skip_button);
        saveSettingsButton = findViewById(R.id.save_settings_button);
        progressCircular = findViewById(R.id.progress_circular);
        customizationLayout = findViewById(R.id.customization_layout);
        focusTimeInput = findViewById(R.id.focus_time_input);
        breakTimeInput = findViewById(R.id.break_time_input);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupClickListeners() {
        // --- Pass button clicks and the Activity Context to the ViewModel ---
        startPauseButton.setOnClickListener(v -> timerViewModel.onStartPauseClicked(this));
        resetButton.setOnClickListener(v -> timerViewModel.resetTimer());
        skipButton.setOnClickListener(v -> timerViewModel.skipSession(this));

        saveSettingsButton.setOnClickListener(v -> {
            try {
                long focusMinutes = Long.parseLong(focusTimeInput.getText().toString());
                long breakMinutes = Long.parseLong(breakTimeInput.getText().toString());
                timerViewModel.applySettings(focusMinutes, breakMinutes);
                Toast.makeText(this, "Settings Applied!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Bottom Navigation ---
        bottomNav.setSelectedItemId(R.id.nav_timer);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_timer) {
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Homepage.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(getApplicationContext(), Account.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void observeViewModel() {
        // --- Observe LiveData for TIME changes ---
        timerViewModel.timeLeftInMillis.observe(this, millis -> {
            int minutes = (int) (millis / 1000) / 60;
            int seconds = (int) (millis / 1000) % 60;
            String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            timerText.setText(timeFormatted);

            long totalTime = timerViewModel.getCurrentSessionTotalTime();
            if (totalTime > 0) {
                progressCircular.setProgress((int) (100 * millis / totalTime));
            }
        });

        // --- Observe LiveData for TIMER STATUS changes ---
        timerViewModel.timerStatus.observe(this, status -> {
            switch (status) {
                case RUNNING:
                    startPauseButton.setText("PAUSE");
                    customizationLayout.setVisibility(View.GONE);
                    break;
                case PAUSED:
                    startPauseButton.setText("RESUME");
                    customizationLayout.setVisibility(View.GONE);
                    break;
                case STOPPED:
                    startPauseButton.setText("START");
                    customizationLayout.setVisibility(View.VISIBLE);
                    break;
            }
        });

        // --- Observe LiveData for SESSION TYPE changes ---
        timerViewModel.currentSession.observe(this, session -> {
            switch (session) {
                case FOCUS:
                    sessionStatusText.setText("Focus");
                    break;
                case SHORT_BREAK:
                    sessionStatusText.setText("Short Break");
                    break;
                case LONG_BREAK:
                    sessionStatusText.setText("Long Break");
                    break;
            }
        });

        // --- Observe LiveData for SESSION COUNT changes ---
        timerViewModel.focusSessionsCompleted.observe(this, count -> {
            String sessionText = getString(R.string.session_count_format, count);
            sessionCountText.setText(sessionText);
        });

    }
}
