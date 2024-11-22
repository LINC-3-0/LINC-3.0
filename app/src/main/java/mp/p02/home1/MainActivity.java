package mp.p02.home1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final int WRITE_POST_REQUEST_CODE = 1;
    private static final int BANNER_IMAGE_CHANGE_INTERVAL = 4000; // 4초
    private ImageView bannerImageView;
    private Handler handler = new Handler();
    private int currentImageIndex = 0;

    // 배너에 사용할 이미지 배열
    private int[] bannerImages = {R.drawable.dog1, R.drawable.dog2, R.drawable.dog3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set click listener for the banner image
        bannerImageView = findViewById(R.id.bannerImageView);
        bannerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to BannerContentActivity
                Intent intent = new Intent(MainActivity.this, BannerContentActivity.class);
                startActivity(intent);
            }
        });

        // 시작 시 배너 이미지 변경 시작
        startBannerImageSwitching();

        // Set click listeners for each image
        ImageView image1 = findViewById(R.id.button1);
        ImageView image2 = findViewById(R.id.button2);
        ImageView image3 = findViewById(R.id.button3);
        ImageView image4 = findViewById(R.id.button4);

        // Image click listeners
        image1.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Image 1 Clicked!", Toast.LENGTH_SHORT).show());
        image2.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Image 2 Clicked!", Toast.LENGTH_SHORT).show());
        image3.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Image 3 Clicked!", Toast.LENGTH_SHORT).show());
        image4.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Image 4 Clicked!", Toast.LENGTH_SHORT).show());

        // FloatingActionButton click listener
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            // Switch to WritePostActivity
            Intent intent = new Intent(MainActivity.this, WritePostActivity.class);
            startActivityForResult(intent, WRITE_POST_REQUEST_CODE);
        });

        // Recent Items Button click listener
        Button recentButton = findViewById(R.id.recent_button);
        recentButton.setOnClickListener(v -> {
            // Switch to RecentItemsActivity
            Intent intent = new Intent(MainActivity.this, RecentItemsActivity.class);
            startActivity(intent);
        });

        // "전체보기" Button click listener
        Button viewAllButton = findViewById(R.id.view_all_button);
        viewAllButton.setOnClickListener(v -> {
            // Switch to RecentItemsActivity
            Intent intent = new Intent(MainActivity.this, RecentItemsActivity.class);
            startActivity(intent);
        });
    }

    private void startBannerImageSwitching() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 이미지 변경
                bannerImageView.setImageResource(bannerImages[currentImageIndex]);
                currentImageIndex = (currentImageIndex + 1) % bannerImages.length; // 다음 이미지 인덱스로 전환

                // 일정 시간 후 다시 실행
                handler.postDelayed(this, BANNER_IMAGE_CHANGE_INTERVAL);
            }
        }, BANNER_IMAGE_CHANGE_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 종료될 때 핸들러 콜백 제거
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WRITE_POST_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // WritePostActivity로부터 전달된 데이터 받기
            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");
            String imageUriString = data.getStringExtra("imageUri");

            // 로그로 데이터 확인
            Log.d("MainActivity", "Title: " + title);
            Log.d("MainActivity", "Content: " + content);
            Log.d("MainActivity", "ImageUri: " + imageUriString);

            // Null 값 체크 및 처리
            if (title == null || content == null) {
                Toast.makeText(this, "데이터가 올바르게 전달되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 데이터를 RecentItemsActivity로 전달
            Intent intent = new Intent(MainActivity.this, RecentItemsActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            intent.putExtra("imageUri", imageUriString);
            startActivity(intent);
        } else {
            Log.d("MainActivity", "onActivityResult: 데이터가 전달되지 않음.");
        }
    }
}
