package com.example.myrunningtracker;

import android.icu.text.IDNA;

public class Information {

    public static int ACTIVITY_ID = 0;
    public static double PERSONAL_BEST;

    public Information() {

    }

    // setter and getter methods to retrieve ACTIVITY_ID
    // RunActivity : id = 1
    // GoalRunActivity : id = 0
    public void setActivityId(int i) {
        ACTIVITY_ID = i;
    }

    public int getActivityId() {
        return ACTIVITY_ID;
    }

    public void setPersonalBest(double i) { PERSONAL_BEST = i; }

    public double getPersonalBest() {
        return PERSONAL_BEST;
    }

}
