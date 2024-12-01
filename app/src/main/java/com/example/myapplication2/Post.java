package com.example.myapplication2;

import java.util.ArrayList;

public class Post {
    private long id;  // 투표 ID
    private String title;
    private String description;
    private ArrayList<String> options;  // 선택지 목록
    private boolean isMultipleChoice;  // 중복 투표 허용 여부

    // 기존에 있는 생성자
    public Post(long id, String title, String description) {
        this(id, title, description, new ArrayList<>(), false);
    }

    // 기본 선택지를 가지는 생성자
    public Post(String title, String description) {
        this(0, title, description);
    }

    // 모든 속성을 포함하는 생성자
    public Post(long id, String title, String description, ArrayList<String> options, boolean isMultipleChoice) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.options = options;
        this.isMultipleChoice = isMultipleChoice;
    }

    // ID를 위한 getter와 setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public boolean isMultipleChoice() {
        return isMultipleChoice;
    }

    public void setMultipleChoice(boolean multipleChoice) {
        isMultipleChoice = multipleChoice;
    }
}
