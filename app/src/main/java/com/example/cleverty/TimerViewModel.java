package com.example.cleverty;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TimerViewModel extends ViewModel {

    // ================= START: SINGLETON FACTORY =================
    private static volatile TimerViewModel INSTANCE;

    public static class TimerViewModelFactory implements ViewModelProvider.Factory {
        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (INSTANCE == null) {
                synchronized (TimerViewModel.class) {
                    if (INSTANCE == null) {
                        INSTANCE = new TimerViewModel();
                    }
                }
            }
            return (T) INSTANCE;
        }
    }
    // ================== END: SINGLETON FACTORY ==================

    // --- Enums for State Management ---
    public enum TimerState { FOCUS, SHORT_BREAK, LONG_BREAK }
    public enum TimerStatus { RUNNING, PAUSED, STOPPED }

    // --- Timer Durations ---
    private long focusTimeInMillis = 25 * 60 * 1000;
    private long shortBreakTimeInMillis = 5 * 60 * 1000;
    private long longBreakTimeInMillis = 15 * 60 * 1000;

    private CountDownTimer countDownTimer;

    // --- LiveData objects to be observed by the UI ---
    private final MutableLiveData<Long> _timeLeftInMillis = new MutableLiveData<>();
    public final LiveData<Long> timeLeftInMillis = _timeLeftInMillis;

    private final MutableLiveData<TimerState> _currentSession = new MutableLiveData<>(TimerState.FOCUS);
    public final LiveData<TimerState> currentSession = _currentSession;

    private final MutableLiveData<TimerStatus> _timerStatus = new MutableLiveData<>(TimerStatus.STOPPED);
    public final LiveData<TimerStatus> timerStatus = _timerStatus;

    private final MutableLiveData<Integer> _focusSessionsCompleted = new MutableLiveData<>(0);
    public final LiveData<Integer> focusSessionsCompleted = _focusSessionsCompleted;

    // --- Constructor ---
    private TimerViewModel() {
        // Initialize with default focus time
        _timeLeftInMillis.setValue(focusTimeInMillis);
    }

    // --- Public methods to be called by the Activity ---

    public void onStartPauseClicked(Context context) {
        TimerStatus status = _timerStatus.getValue();
        if (status == TimerStatus.RUNNING) {
            pauseTimer();
        } else { // Handles both PAUSED and STOPPED cases
            startTimer(context);
        }
    }

    public void applySettings(long focusMinutes, long breakMinutes) {
        focusTimeInMillis = focusMinutes * 60 * 1000;
        shortBreakTimeInMillis = breakMinutes * 60 * 1000;
        if (_timerStatus.getValue() == TimerStatus.STOPPED) {
            resetTimer();
        }
    }

    // In TimerViewModel.java

    public void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        _timerStatus.setValue(TimerStatus.STOPPED);
        _currentSession.setValue(TimerState.FOCUS);
        _timeLeftInMillis.setValue(focusTimeInMillis);
        _focusSessionsCompleted.setValue(0);
    }


    public void skipSession(Context context) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        playNotificationSound(context);

        if (_currentSession.getValue() == TimerState.FOCUS) {
            int currentCount = _focusSessionsCompleted.getValue() != null ? _focusSessionsCompleted.getValue() : 0;
            currentCount++;
            _focusSessionsCompleted.setValue(currentCount);

            if (currentCount > 0 && currentCount % 4 == 0) {
                _currentSession.setValue(TimerState.LONG_BREAK);
                _timeLeftInMillis.setValue(longBreakTimeInMillis);
            } else {
                _currentSession.setValue(TimerState.SHORT_BREAK);
                _timeLeftInMillis.setValue(shortBreakTimeInMillis);
            }
        } else { // If it was a break, next is always focus
            _currentSession.setValue(TimerState.FOCUS);
            _timeLeftInMillis.setValue(focusTimeInMillis);
        }
        _timerStatus.setValue(TimerStatus.STOPPED);
        startTimer(context); // Automatically start the next session
    }

    // --- Private Timer Logic ---
    private void startTimer(Context context) {
        _timerStatus.setValue(TimerStatus.RUNNING);
        long timeToCount = _timeLeftInMillis.getValue();

        countDownTimer = new CountDownTimer(timeToCount, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                _timeLeftInMillis.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                skipSession(context); // Pass context to play sound on finish
            }
        }.start();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        _timerStatus.setValue(TimerStatus.PAUSED);
    }

    // --- Sound Helper Method ---
    private void playNotificationSound(Context context) {
        try {
            Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(context.getApplicationContext(), notificationSoundUri);
            ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Helper & Cleanup Methods ---
    public long getCurrentSessionTotalTime() {
        TimerState session = _currentSession.getValue();
        if (session == TimerState.FOCUS) return focusTimeInMillis;
        if (session == TimerState.SHORT_BREAK) return shortBreakTimeInMillis;
        return longBreakTimeInMillis;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
