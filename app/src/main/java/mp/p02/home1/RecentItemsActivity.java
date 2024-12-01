package mp.p02.home1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class RecentItemsActivity extends AppCompatActivity {

    private static final int WRITE_POST_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private ItemDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_items);

        dbHelper = new ItemDatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // SQLite에서 데이터 로드
        loadItems();

        // FloatingActionButton으로 글 작성 화면으로 이동
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(RecentItemsActivity.this, WritePostActivity.class);
            startActivityForResult(intent, WRITE_POST_REQUEST_CODE);
        });

        // 홈 버튼 클릭 이벤트
        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecentItemsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // 뒤로 가기 버튼 클릭 이벤트
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadItems() {
        itemList = dbHelper.getAllItems();
        itemAdapter = new ItemAdapter(this, itemList);
        recyclerView.setAdapter(itemAdapter);
    }

    private void refreshItemList() {
        List<Item> updatedItems = dbHelper.getAllItems();
        itemAdapter.updateItems(updatedItems); // 어댑터 데이터 업데이트
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshItemList(); // 화면 재진입 시 데이터 갱신
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WRITE_POST_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");
            String imageUriString = data.getStringExtra("imageUri");

            if (title != null && content != null) {
                Item newItem;
                if (imageUriString != null) {
                    Uri imageUri = Uri.parse(imageUriString);
                    newItem = new Item(imageUri, title, content);
                } else {
                    newItem = new Item(R.drawable.button1, title, content);
                }

                dbHelper.addItem(newItem); // SQLite에 저장
                refreshItemList(); // 데이터 새로고침
                recyclerView.scrollToPosition(0); // 상단으로 스크롤
            }
        }
    }
}
