package com.example.kim.life;

import java.text.Collator;
import java.util.Comparator;

import android.graphics.drawable.Drawable;

public class ListData {
    /**
     * 리스트 정보를 담고 있을 객체 생성
     */
    // 사진 경로
    private String mPhoto;
    // 제목
    private String mTitle;
    // 날짜
    private String mDate;

    public String getmPhoto() { return mPhoto; }
    public String getmTitle() { return mTitle; }
    public String getmDate() { return mDate; }

    public ListData(String photo, String title, String date) {
        mPhoto = photo;
        mTitle = title;
        mDate = date;
    }

}