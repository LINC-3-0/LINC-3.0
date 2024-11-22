package mp.p02.home1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RecentItemsActivity extends AppCompatActivity {

    private static final int WRITE_POST_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private ItemDatabaseHelper dbHelper; // SQLite 데이터베이스 헬퍼 인스턴스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_items);

        dbHelper = new ItemDatabaseHelper(this); // SQLite 데이터베이스 헬퍼 초기화

        // 아이템 리스트 초기화
        itemList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 비동기로 SQLite에서 저장된 아이템을 불러옴
        loadItems();

        // 기존에 추가된 아이템을 Intent로 받아옴
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String imageUriString = intent.getStringExtra("imageUri");

        // 로그 추가
        Log.d("RecentItemsActivity", "Title: " + title);
        Log.d("RecentItemsActivity", "Content: " + content);
        Log.d("RecentItemsActivity", "ImageUri: " + imageUriString);

        if (title != null && content != null) {
            int imageResource = R.drawable.button1; // Default image
            Item newItem;

            if (imageUriString != null) {
                Uri imageUri = Uri.parse(imageUriString);
                newItem = new Item(imageUri, title, content);
            } else {
                newItem = new Item(imageResource, title, content);
            }

            // 여기서 itemList가 null이 아닌지 확인하고 null이라면 빈 ArrayList로 초기화합니다.
            if (itemList == null) {
                itemList = new ArrayList<>();
            }

            // SQLite에 추가된 아이템이 이미 존재하는지 확인 후 추가
            if (!itemList.contains(newItem)) {
                itemList.add(0, newItem); // List 상단에 추가
                addItem(newItem); // 비동기로 SQLite 데이터베이스에 아이템 저장
            }
        }

        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);

        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecentItemsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button recentButton = findViewById(R.id.recent_button);
        recentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the Recent Items button action
            }
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView appName = findViewById(R.id.app_name);
        appName.setText("AppName");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecentItemsActivity.this, WritePostActivity.class);
                startActivityForResult(intent, WRITE_POST_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WRITE_POST_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");
            String imageUriString = data.getStringExtra("imageUri");

            int imageResource = R.drawable.button1; // Default image

            Item newItem;

            if (imageUriString != null) {
                Uri imageUri = Uri.parse(imageUriString);
                newItem = new Item(imageUri, title, content);
            } else {
                newItem = new Item(imageResource, title, content);
            }

            // 새로운 아이템이 리스트에 존재하지 않는 경우에만 추가
            if (!itemList.contains(newItem)) {
                itemList.add(0, newItem); // List 상단에 추가
                addItem(newItem); // 비동기로 SQLite 데이터베이스에 아이템 저장

                itemAdapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0); // 추가된 항목으로 스크롤
            }
        }
    }

    private void loadItems() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                itemList = dbHelper.getAllItems();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itemAdapter = new ItemAdapter(RecentItemsActivity.this, itemList);
                        recyclerView.setAdapter(itemAdapter);
                    }
                });
            }
        }).start();
    }

    private void addItem(final Item item) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                dbHelper.addItem(item);
            }
        }).start();
    }
}
