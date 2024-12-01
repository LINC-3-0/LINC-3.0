package com.example.myapplication2;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PollDetailActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_EDIT_POLL = 1;
    private TextView titleTextView, descriptionTextView;
    private LinearLayout optionsLayout, commentsLayout;
    private EditText commentInput;
    private ArrayList<Integer> voteCounts;
    private ArrayList<CheckBox> optionViews;
    private ArrayList<TextView> percentageViews;
    private ArrayList<ProgressBar> progressBars;
    private int totalVotes;
    private boolean isMultipleChoice;

    private PollsDBHelper dbHelper;
    private long pollId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_detail);

        dbHelper = new PollsDBHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        titleTextView = findViewById(R.id.pollTitle);
        descriptionTextView = findViewById(R.id.pollDescription);
        optionsLayout = findViewById(R.id.optionsLayout);
        commentsLayout = findViewById(R.id.commentsLayout);
        commentInput = findViewById(R.id.commentInput);

        Button submitVoteButton = findViewById(R.id.submitVoteButton);
        Button resetVoteButton = findViewById(R.id.resetVoteButton);
        Button submitCommentButton = findViewById(R.id.submitCommentButton);

        submitVoteButton.setOnClickListener(v -> handleVote());
        resetVoteButton.setOnClickListener(v -> resetVotes());
        submitCommentButton.setOnClickListener(v -> handleComment());

        loadPollData();
    }

    private void setupOptions(ArrayList<String> options) {
        voteCounts = new ArrayList<>();
        optionViews = new ArrayList<>();
        percentageViews = new ArrayList<>();
        progressBars = new ArrayList<>();

        for (String option : options) {
            voteCounts.add(0);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(option);
            optionViews.add(checkBox);

            // 중복투표가 허용되지 않는 경우 단일 선택만 가능하도록 설정
            if (!isMultipleChoice) {
                checkBox.setOnClickListener(view -> {
                    for (CheckBox cb : optionViews) {
                        if (cb != checkBox) {
                            cb.setChecked(false);
                        }
                    }
                });
            }

            optionsLayout.addView(checkBox);

            TextView percentageView = new TextView(this);
            percentageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            percentageView.setText("0%");
            percentageView.setVisibility(View.GONE);
            percentageViews.add(percentageView);

            ProgressBar optionProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            optionProgressBar.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            optionProgressBar.setMax(100);
            optionProgressBar.setProgress(0);
            optionProgressBar.setVisibility(View.GONE);
            progressBars.add(optionProgressBar);

            optionsLayout.addView(percentageView);
            optionsLayout.addView(optionProgressBar);
        }
    }



    private void loadPollData() {
        Intent intent = getIntent();
        if (intent != null) {
            pollId = intent.getLongExtra("POLL_ID", -1);

            if (pollId != -1) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                Cursor pollCursor = db.query("Polls", null, "id = ?",
                        new String[]{String.valueOf(pollId)}, null, null, null);

                if (pollCursor.moveToFirst()) {
                    String title = pollCursor.getString(pollCursor.getColumnIndexOrThrow("title"));
                    String description = pollCursor.getString(pollCursor.getColumnIndexOrThrow("description"));
                    isMultipleChoice = pollCursor.getInt(pollCursor.getColumnIndexOrThrow("multiple_choice")) > 0;

                    titleTextView.setText(title != null ? title : "제목 없음");
                    descriptionTextView.setText(description != null ? description : "설명 없음");

                    Cursor optionsCursor = db.query("Options", null, "poll_id = ?",
                            new String[]{String.valueOf(pollId)}, null, null, null);

                    ArrayList<String> options = new ArrayList<>();
                    while (optionsCursor.moveToNext()) {
                        options.add(optionsCursor.getString(optionsCursor.getColumnIndexOrThrow("option_text")));
                    }
                    optionsCursor.close();

                    setupOptions(options);
                }
                pollCursor.close();
                db.close();
            }
        } else {
            Toast.makeText(this, "인텐트 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_poll_detail, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SpannableString s = new SpannableString(menuItem.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
            menuItem.setTitle(s);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            Toast.makeText(this, "새로고침", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_revise) {
            Intent intent = new Intent(this, NewPollActivity.class);
            intent.putExtra("title", titleTextView.getText().toString());
            intent.putExtra("description", descriptionTextView.getText().toString());
            intent.putExtra("options", getOptionsFromViews());
            intent.putExtra("isMultipleChoice", isMultipleChoice);
            intent.putExtra("isEditMode", true);
            intent.putExtra("POLL_ID", pollId);  // 수정된 투표 ID 전달
            startActivityForResult(intent, REQUEST_CODE_EDIT_POLL);
            return true;
        } else if (id == R.id.action_delete) {
            deletePoll();
            return true;
        } else if (id == R.id.action_share) {
            sharePoll();
            return true;
        } else if (id == R.id.action_message) {
            Toast.makeText(this, "채팅", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void deletePoll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Votes", "poll_id = ?", new String[]{String.valueOf(pollId)});
        db.delete("Options", "poll_id = ?", new String[]{String.valueOf(pollId)});
        db.delete("Polls", "id = ?", new String[]{String.valueOf(pollId)});
        db.close();

        Toast.makeText(this, "투표가 삭제되었습니다.", Toast.LENGTH_SHORT).show();

        // 결과를 반환하고 액티비티를 종료
        Intent resultIntent = new Intent();
        resultIntent.putExtra("pollId", pollId);
        resultIntent.putExtra("isDeleted", true); // 삭제됨을 나타내는 플래그 추가
        setResult(RESULT_OK, resultIntent);
        finish();
    }



    private ArrayList<String> getOptionsFromViews() {
        ArrayList<String> options = new ArrayList<>();
        for (View optionView : optionViews) {
            if (optionView instanceof CheckBox) {
                options.add(((TextView) optionView).getText().toString());
            }
        }
        return options;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_POLL && resultCode == RESULT_OK) {
            String updatedTitle = data.getStringExtra("title");
            String updatedDescription = data.getStringExtra("description");
            ArrayList<String> updatedOptions = data.getStringArrayListExtra("options");
            isMultipleChoice = data.getBooleanExtra("isMultipleChoice", false);

            titleTextView.setText(updatedTitle);
            descriptionTextView.setText(updatedDescription);
            updateOptions(updatedOptions);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", updatedTitle);
            resultIntent.putExtra("description", updatedDescription);
            resultIntent.putStringArrayListExtra("options", updatedOptions);
            resultIntent.putExtra("isMultipleChoice", isMultipleChoice);
            setResult(RESULT_OK, resultIntent);

            // 수정된 옵션을 데이터베이스에 저장
            updatePollInDatabase(updatedTitle, updatedDescription, updatedOptions, isMultipleChoice);
        }
    }

    private void updatePollInDatabase(String title, String description, ArrayList<String> options, boolean isMultipleChoice) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Update Polls Table
        ContentValues pollValues = new ContentValues();
        pollValues.put("title", title);
        pollValues.put("description", description);
        pollValues.put("multiple_choice", isMultipleChoice ? 1 : 0);
        db.update("Polls", pollValues, "id = ?", new String[]{String.valueOf(pollId)});

        // Remove old options and add updated ones
        db.delete("Options", "poll_id = ?", new String[]{String.valueOf(pollId)});
        for (String option : options) {
            ContentValues optionValues = new ContentValues();
            optionValues.put("poll_id", pollId);
            optionValues.put("option_text", option);
            db.insert("Options", null, optionValues);
        }

        db.close();
    }



    private void updateOptions(ArrayList<String> updatedOptions) {
        optionsLayout.removeAllViews();
        voteCounts.clear();
        optionViews.clear();
        percentageViews.clear();
        progressBars.clear();

        for (String option : updatedOptions) {
            voteCounts.add(0);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(option);
            optionViews.add(checkBox);
            optionsLayout.addView(checkBox);

            TextView percentageView = new TextView(this);
            percentageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            percentageView.setText("0%");
            percentageView.setVisibility(View.GONE);
            percentageViews.add(percentageView);

            ProgressBar optionProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            optionProgressBar.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            optionProgressBar.setMax(100);
            optionProgressBar.setProgress(0);
            optionProgressBar.setVisibility(View.GONE);
            progressBars.add(optionProgressBar);

            optionsLayout.addView(percentageView);
            optionsLayout.addView(optionProgressBar);
        }
    }

    private void sharePoll() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareMessage = titleTextView.getText().toString() + "\n\n" + descriptionTextView.getText().toString();
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "공유할 앱을 선택하세요"));
    }

    private void handleVote() {
        totalVotes = 0;
        ArrayList<Integer> selectedOptions = new ArrayList<>();

        for (int i = 0; i < optionViews.size(); i++) {
            CheckBox optionView = (CheckBox) optionViews.get(i); // CheckBox로 캐스팅
            if (optionView.isChecked()) {
                selectedOptions.add(i);
                voteCounts.set(i, voteCounts.get(i) + 1);
                totalVotes++;
            }
        }

        // 단일 선택 모드일 때 선택된 옵션이 하나 이상인 경우 첫 번째 옵션만 허용
        if (!isMultipleChoice && selectedOptions.size() > 1) {
            for (int i = 0; i < optionViews.size(); i++) {
                ((CheckBox) optionViews.get(i)).setChecked(i == selectedOptions.get(0));
            }
        }

        updateResults();
        saveVotesToDatabase();
    }



    private void saveVotesToDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Votes", "poll_id = ?", new String[]{String.valueOf(pollId)});

        for (int i = 0; i < optionViews.size(); i++) {
            if (((CheckBox) optionViews.get(i)).isChecked()) {
                ContentValues voteValues = new ContentValues();
                voteValues.put("poll_id", pollId);
                voteValues.put("option_id", i + 1);
                db.insert("Votes", null, voteValues);
            }
        }

        db.close();
    }

    private void resetVotes() {
        for (View optionView : optionViews) {
            ((CheckBox) optionView).setChecked(false);
        }
        totalVotes = 0;
        for (int i = 0; i < voteCounts.size(); i++) {
            voteCounts.set(i, 0);
            percentageViews.get(i).setText("0%");
            percentageViews.get(i).setVisibility(View.GONE);
            progressBars.get(i).setProgress(0);
            progressBars.get(i).setVisibility(View.GONE);
        }

        resetVotesInDatabase();
    }

    private void resetVotesInDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Votes", "poll_id = ?", new String[]{String.valueOf(pollId)});
        db.close();
    }

    private void updateResults() {
        if (totalVotes > 0) {
            for (int i = 0; i < optionViews.size(); i++) {
                int voteCount = voteCounts.get(i);
                int percent = (voteCount * 100) / totalVotes;
                TextView percentageView = percentageViews.get(i);
                ProgressBar optionProgressBar = progressBars.get(i);
                percentageView.setText(((TextView) optionViews.get(i)).getText() + ": " + percent + "%");
                optionProgressBar.setProgress(percent);
                percentageView.setVisibility(View.VISIBLE);
                optionProgressBar.setVisibility(View.VISIBLE);
            }
        } else {
            for (int i = 0; i < percentageViews.size(); i++) {
                percentageViews.get(i).setVisibility(View.GONE);
                progressBars.get(i).setVisibility(View.GONE);
            }
        }
    }

    private void handleComment() {
        String commentText = commentInput.getText().toString().trim();
        if (!commentText.isEmpty()) {
            View commentView = getLayoutInflater().inflate(R.layout.item_comment, null);
            ImageView profileImageView = commentView.findViewById(R.id.profileImageView);
            TextView commentAuthorTextView = commentView.findViewById(R.id.commentAuthorTextView);
            TextView commentTextView = commentView.findViewById(R.id.commentTextView);
            TextView commentTimeTextView = commentView.findViewById(R.id.commentTimeTextView);
            ImageButton deleteCommentButton = commentView.findViewById(R.id.deleteCommentButton);
            ImageButton likeCommentButton = commentView.findViewById(R.id.likeCommentButton);

            profileImageView.setImageResource(android.R.drawable.sym_def_app_icon);
            commentAuthorTextView.setText("사용자 이름");
            commentTextView.setText(commentText);
            commentTimeTextView.setText(new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault()).format(new Date()));

            deleteCommentButton.setOnClickListener(v -> commentsLayout.removeView(commentView));
            likeCommentButton.setTag(false);
            likeCommentButton.setImageResource(android.R.drawable.btn_star_big_off);
            likeCommentButton.setOnClickListener(v -> showLikeConfirmationDialog(likeCommentButton));

            commentsLayout.addView(commentView);
            commentInput.setText("");
        } else {
            Toast.makeText(this, "댓글을 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLikeConfirmationDialog(ImageButton likeButton) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("이 댓글을 공감하시겠습니까?")
                .setPositiveButton("확인", (dialog, id) -> {
                    likeButton.setImageResource(android.R.drawable.btn_star_big_on);
                    likeButton.setTag(true);
                    dialog.dismiss();
                })
                .setNegativeButton("취소", (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}