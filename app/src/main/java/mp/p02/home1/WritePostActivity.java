package mp.p02.home1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

import java.io.IOException;

public class WritePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText editTextTitle;
    private EditText editTextContent;
    private ImageView imageView;
    private Button buttonSelectImage;
    private Uri imageUri;
    private ItemDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        dbHelper = new ItemDatabaseHelper(this); // SQLite 헬퍼 초기화

        // UI 초기화
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        imageView = findViewById(R.id.imageView);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);

        // 이미지 선택 버튼
        buttonSelectImage.setOnClickListener(v -> openImageChooser());

        // 완료 버튼
        Button buttonComplete = findViewById(R.id.buttonComplete);
        buttonComplete.setOnClickListener(v -> handleSubmit());

        // 뒤로가기 버튼
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "이미지 선택"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSubmit() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (!title.isEmpty() && !content.isEmpty()) {
            Item newItem = new Item(imageUri, title, content);
            dbHelper.addItem(newItem); // SQLite에 데이터 저장

            // 반환 데이터 설정
            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);
            if (imageUri != null) {
                resultIntent.putExtra("imageUri", imageUri.toString());
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
