package com.example.myapplication2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ArrayList<Post> postList;
    private FloatingActionButton fab;
    private AppBarConfiguration appBarConfiguration;

    private PollsDBHelper dbHelper;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new PollsDBHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, position -> {
            Intent intent = new Intent(MainActivity.this, PollDetailActivity.class);
            intent.putExtra("POLL_ID", postList.get(position).getId());
            activityResultLauncher.launch(intent);
        }, this);
        recyclerView.setAdapter(postAdapter);

        setupActivityResultLauncher();

        loadPollsFromDatabase();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> activityResultLauncher.launch(new Intent(MainActivity.this, NewPollActivity.class)));

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_chat, R.id.navigation_notifications, R.id.navigation_dashboard)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);
        } else {
            Log.e("MainActivity", "NavHostFragment is null");
        }
    }

    private void setupActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        long pollId = data.getLongExtra("pollId", -1);
                        boolean isDeleted = data.getBooleanExtra("isDeleted", false);

                        if (isDeleted) {
                            int indexToRemove = -1;
                            for (int i = 0; i < postList.size(); i++) {
                                if (postList.get(i).getId() == pollId) {
                                    indexToRemove = i;
                                    break;
                                }
                            }
                            if (indexToRemove != -1) {
                                postList.remove(indexToRemove);
                                postAdapter.notifyItemRemoved(indexToRemove);
                            }
                        } else if (pollId != -1) {
                            // 수정된 투표 처리
                            int indexToUpdate = -1;
                            for (int i = 0; i < postList.size(); i++) {
                                if (postList.get(i).getId() == pollId) {
                                    indexToUpdate = i;
                                    break;
                                }
                            }

                            if (indexToUpdate != -1) {
                                // 기존 투표 업데이트
                                String title = data.getStringExtra("title");
                                String description = data.getStringExtra("description");
                                ArrayList<String> options = data.getStringArrayListExtra("options");
                                boolean isMultipleChoice = data.getBooleanExtra("isMultipleChoice", false);

                                Post updatedPost = postList.get(indexToUpdate);
                                updatedPost.setTitle(title);
                                updatedPost.setDescription(description);
                                updatedPost.setOptions(options);
                                updatedPost.setMultipleChoice(isMultipleChoice);
                                postAdapter.notifyItemChanged(indexToUpdate);
                            } else {
                                // 새 투표 추가
                                String title = data.getStringExtra("title");
                                String description = data.getStringExtra("description");
                                ArrayList<String> options = data.getStringArrayListExtra("options");
                                boolean isMultipleChoice = data.getBooleanExtra("isMultipleChoice", false);

                                Post newPost = new Post(pollId, title, description, options, isMultipleChoice);
                                postList.add(newPost);
                                postAdapter.notifyItemInserted(postList.size() - 1);
                            }
                        }
                    }
                });
    }


    private void loadPollsFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("Polls", null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            postList.clear();
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                Post post = new Post(id, title, description);
                postList.add(post);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            insertDefaultPolls();
            loadPollsFromDatabase();
        }
        postAdapter.notifyDataSetChanged();
        db.close();
    }

    private void insertDefaultPolls() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues pollValues = new ContentValues();
        pollValues.put("title", "팝업스토어 일정");
        pollValues.put("description", "헬로키티 팝업스토어 갈까요 말까요?");
        pollValues.put("multiple_choice", 0);
        long pollId = db.insert("Polls", null, pollValues);

        insertDefaultOptions(db, pollId);

        ContentValues pollValues2 = new ContentValues();
        pollValues2.put("title", "축제 이벤트");
        pollValues2.put("description", "이번 주말에 열리는 축제에 참여하세요!");
        pollValues2.put("multiple_choice", 0);
        long pollId2 = db.insert("Polls", null, pollValues2);

        insertDefaultOptions(db, pollId2);
        db.close();
    }

    private void insertDefaultOptions(SQLiteDatabase db, long pollId) {
        ContentValues option1 = new ContentValues();
        option1.put("poll_id", pollId);
        option1.put("option_text", "가자");
        db.insert("Options", null, option1);

        ContentValues option2 = new ContentValues();
        option2.put("poll_id", pollId);
        option2.put("option_text", "가지말자");
        db.insert("Options", null, option2);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
