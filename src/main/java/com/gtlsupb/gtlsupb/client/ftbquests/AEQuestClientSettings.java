package com.gtlsupb.gtlsupb.client.ftbquests;

import java.util.ArrayList;
import java.util.List;

public class AEQuestClientSettings {

    private static boolean detectionEnabled = true;
    private static boolean rewardEnabled = true;
    private static int interval = 40;

    private static int submitCount = 1;
    private static final int SUBMIT_COUNT_MIN = 1;
    private static final int SUBMIT_COUNT_MAX = 100000;

    private static final List<Runnable> refreshers = new ArrayList<>();

    public static void update(boolean detection, boolean reward, int intervalTicks) {
        detectionEnabled = detection;
        rewardEnabled = reward;
        interval = intervalTicks;
        refreshTitles();
    }

    public static boolean isDetectionEnabled() {
        return detectionEnabled;
    }

    public static boolean isRewardEnabled() {
        return rewardEnabled;
    }

    public static int getInterval() {
        return interval;
    }

    public static int intervalStep() {
        return 10;
    }

    public static void toggleDetection() {
        detectionEnabled = !detectionEnabled;
        refreshTitles();
    }

    public static void toggleReward() {
        rewardEnabled = !rewardEnabled;
        refreshTitles();
    }

    public static void stepInterval(int delta) {
        interval = Math.max(10, Math.min(200, interval + delta));
        refreshTitles();
    }

    public static int getSubmitCount() {
        return submitCount;
    }

    public static void stepSubmitCount(int delta) {
        setSubmitCount(submitCount + delta);
    }

    public static void setSubmitCount(int value) {
        int clamped = Math.max(SUBMIT_COUNT_MIN, Math.min(SUBMIT_COUNT_MAX, value));
        if (clamped == submitCount) return;
        submitCount = clamped;
        refreshTitles();
    }

    public static void addTitlesRefresher(Runnable r) {
        if (r == null || refreshers.contains(r)) return;
        refreshers.add(r);
    }

    public static void removeTitlesRefresher(Runnable r) {
        refreshers.remove(r);
    }

    private static void refreshTitles() {
        for (Runnable r : new ArrayList<>(refreshers)) {
            try {
                r.run();
            } catch (Throwable ignored) {

            }
        }
    }
}
