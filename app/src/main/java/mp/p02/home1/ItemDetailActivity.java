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

        Toolbar toolbar = findViewById(R.id.top_app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        itemImageView = findViewById(R.id.itemImageView);
        itemTitleTextView = findViewById(R.id.itemTitleTextView);
        itemContentTextView = findViewById(R.id.itemContentTextView);
        drawerLayout = findViewById(R.id.drawer_layout);
        menuImage = findViewById(R.id.menu_image);
        navigationView = findViewById(R.id.navigation_view);

        Intent intent = getIntent();
        itemId = intent.getIntExtra("item_id", -1);

        if (itemId == -1 || !databaseHelper.doesItemExist(itemId)) {
            Log.e("ItemDetailActivity", "Invalid or non-existent item ID received: " + itemId);
            Toast.makeText(this, "Invalid or non-existent item ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = intent.getStringExtra("item_title");
        String content = intent.getStringExtra("item_content");
        String imageUriString = intent.getStringExtra("item_image_uri");

        itemTitleTextView.setText(title);
        itemContentTextView.setText(content);

        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            itemImageView.setImageURI(imageUri);
        }

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
