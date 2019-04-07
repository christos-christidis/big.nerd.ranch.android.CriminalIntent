package com.bignerdranch.criminalintent;

import java.util.Date;
import java.util.UUID;

class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;

    // SOS: according to wiki, the probability of getting 2 same UUIDs when I've generated 103 trillion
    // of them is 1 in a billion! But it's not 0. So there should be better alternatives. Here, I'm
    // leaving this as is since I'm following the book's instructions.
    Crime() {
        mId = UUID.randomUUID();
        mDate = new Date();     // returns current date
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }
}
