package com.example.myapplication2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PollsDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "polls.db";
    private static final int DATABASE_VERSION = 1;

    public PollsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Polls 테이블 생성
        db.execSQL("CREATE TABLE Polls (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "multiple_choice INTEGER NOT NULL);");

        // Options 테이블 생성
        db.execSQL("CREATE TABLE Options (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "poll_id INTEGER NOT NULL, " +
                "option_text TEXT NOT NULL, " +
                "FOREIGN KEY (poll_id) REFERENCES Polls(id) ON DELETE CASCADE);");

        // Votes 테이블 생성
        db.execSQL("CREATE TABLE Votes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "poll_id INTEGER NOT NULL, " +
                "option_id INTEGER NOT NULL, " +
                "FOREIGN KEY (poll_id) REFERENCES Polls(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (option_id) REFERENCES Options(id) ON DELETE CASCADE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 기존 테이블 삭제
        db.execSQL("DROP TABLE IF EXISTS Votes");
        db.execSQL("DROP TABLE IF EXISTS Options");
        db.execSQL("DROP TABLE IF EXISTS Polls");
        // 새로운 버전의 테이블 생성
        onCreate(db);
    }

    public void deletePoll(long pollId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Polls", "id = ?", new String[]{String.valueOf(pollId)});
        db.delete("Options", "poll_id = ?", new String[]{String.valueOf(pollId)});
        db.close();
    }
}
