package mp.p02.home1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class ItemDetailActivity extends AppCompatActivity {

    private ImageView itemImageView;
    private TextView itemTitleTextView;
    private TextView itemContentTextView;
    private DrawerLayout drawerLayout;
    private ImageView menuImage;
    private ItemDatabaseHelper databaseHelper;
    private int itemId;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        databaseHelper = new ItemDatabaseHelper(this);

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.top_app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // View 초기화
        itemImageView = findViewById(R.id.itemImageView);
        itemTitleTextView = findViewById(R.id.itemTitleTextView);
        itemContentTextView = findViewById(R.id.itemContentTextView);
        drawerLayout = findViewById(R.id.drawer_layout);
        menuImage = findViewById(R.id.menu_image);
        navigationView = findViewById(R.id.navigation_view);

        // Intent로 데이터 받기
        Intent intent = getIntent();
        itemId = intent.getIntExtra("item_id", -1);

        if (itemId == -1 || !databaseHelper.doesItemExist(itemId)) {
            Log.e("ItemDetailActivity", "Invalid or non-existent item ID received: " + itemId);
            Toast.makeText(this, "Item does not exist or ID is invalid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = intent.getStringExtra("item_title");
        String content = intent.getStringExtra("item_content");
        String imageUriString = intent.getStringExtra("item_image_uri");

        // 디버깅 로그
        Log.d("ItemDetailActivity", "Item ID: " + itemId);
        Log.d("ItemDetailActivity", "Title: " + title);
        Log.d("ItemDetailActivity", "Content: " + content);
        Log.d("ItemDetailActivity", "Image URI: " + imageUriString);

        // 데이터 표시
        itemTitleTextView.setText(title != null ? title : "No Title");
        itemContentTextView.setText(content != null ? content : "No Content");

        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            itemImageView.setImageURI(imageUri);
        } else {
            // 기본 이미지 설정
            itemImageView.setImageResource(R.drawable.button1);
        }

        // 메뉴 버튼 클릭 이벤트
        menuImage.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int navItemId = item.getItemId();
            if (navItemId == R.id.nav_edit) {
                editItem();
            } else if (navItemId == R.id.nav_delete) {
                deleteItem();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void deleteItem() {
        Log.d("ItemDetailActivity", "Attempting to delete item with ID: " + itemId);
        if (databaseHelper.deleteItem(itemId)) {
            Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("deleted_item_id", itemId);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete the item.", Toast.LENGTH_SHORT).show();
        }
    }

    private void editItem() {
        Toast.makeText(this, "Edit functionality is not yet implemented.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
