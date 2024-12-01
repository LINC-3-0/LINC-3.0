package com.example.myapplication2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class NewPollActivity extends AppCompatActivity {

    private EditText newPollTitle, newPollDescription;
    private LinearLayout optionsLayout;
    private CheckBox multipleChoiceCheckBox;
    private boolean isEditMode = false;

    private PollsDBHelper dbHelper;
    private long pollId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_post);

        dbHelper = new PollsDBHelper(this);

        newPollTitle = findViewById(R.id.newPollTitle);
        newPollDescription = findViewById(R.id.newPollDescription);
        optionsLayout = findViewById(R.id.optionsLayout);
        multipleChoiceCheckBox = findViewById(R.id.multipleChoiceCheckBox);

        Button addOptionButton = findViewById(R.id.addOptionButton);
        addOptionButton.setOnClickListener(v -> addOptionField());

        Button submitPollButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        submitPollButton.setOnClickListener(v -> submitPoll());
        cancelButton.setOnClickListener(v -> finish());

        // 수정 모드인지 확인하고, 수정 모드라면 기존 데이터를 폼에 채우기
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("isEditMode", false)) {
            isEditMode = true;
            pollId = intent.getLongExtra("POLL_ID", -1);
            loadPollData(intent);
        }
    }

    // 기존 폴 데이터를 폼에 로드하는 메서드
    private void loadPollData(Intent intent) {
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        ArrayList<String> options = intent.getStringArrayListExtra("options");
        boolean isMultipleChoice = intent.getBooleanExtra("isMultipleChoice", false);

        newPollTitle.setText(title);
        newPollDescription.setText(description);
        multipleChoiceCheckBox.setChecked(isMultipleChoice);

        if (options != null) {
            for (String option : options) {
                addOptionField(option);
            }
        }
    }

    // 새로운 옵션 필드를 추가하는 메서드
    private void addOptionField() {
        addOptionField("");
    }

    // 옵션 필드에 텍스트를 미리 설정하여 추가하는 메서드
    private void addOptionField(String optionText) {
        LinearLayout optionLayout = new LinearLayout(this);
        optionLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        optionLayout.setOrientation(LinearLayout.HORIZONTAL);

        EditText newOption = new EditText(this);
        newOption.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        newOption.setText(optionText);
        optionLayout.addView(newOption);

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(android.R.drawable.ic_delete);
        deleteButton.setOnClickListener(v -> optionsLayout.removeView(optionLayout));
        optionLayout.addView(deleteButton);

        optionsLayout.addView(optionLayout);
    }

    // 폴 생성을 완료하고 결과를 반환하는 메서드
    private void submitPoll() {
        String title = newPollTitle.getText().toString().trim();
        String description = newPollDescription.getText().toString().trim();
        ArrayList<String> options = new ArrayList<>();
        for (int i = 0; i < optionsLayout.getChildCount(); i++) {
            View child = optionsLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                EditText optionEditText = (EditText) ((LinearLayout) child).getChildAt(0);
                if (!optionEditText.getText().toString().trim().isEmpty()) {
                    options.add(optionEditText.getText().toString().trim());
                }
            }
        }
        // 중복 선택 가능 여부 확인
        boolean isMultipleChoice = multipleChoiceCheckBox.isChecked();

        // 제목 또는 옵션이 없는 경우 처리
        if (title.isEmpty() || options.isEmpty()) {
            Toast.makeText(this, "제목과 최소 하나의 옵션이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 데이터베이스에 저장할 때 ContentValues 사용
        ContentValues pollValues = new ContentValues();
        pollValues.put("title", title);
        pollValues.put("description", description);
        pollValues.put("multiple_choice", isMultipleChoice ? 1 : 0);

        if (isEditMode && pollId != -1) {
            // 기존 투표 데이터 업데이트
            db.update("Polls", pollValues, "id = ?", new String[]{String.valueOf(pollId)});
            db.delete("Options", "poll_id = ?", new String[]{String.valueOf(pollId)});
        } else {
            // 새로운 투표 데이터 추가
            pollId = db.insert("Polls", null, pollValues);
        }

        // 옵션 데이터 저장
        for (String option : options) {
            ContentValues optionValues = new ContentValues();
            optionValues.put("poll_id", pollId);
            optionValues.put("option_text", option);
            db.insert("Options", null, optionValues);
        }

        db.close();

        // 데이터 반환 및 액티비티 종료
        Intent resultIntent = new Intent();
        resultIntent.putExtra("pollId", pollId);
        resultIntent.putExtra("title", title);
        resultIntent.putExtra("description", description);
        resultIntent.putStringArrayListExtra("options", options);
        resultIntent.putExtra("isMultipleChoice", isMultipleChoice);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
