package mp.p02.home1;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import mp.p02.home1.databinding.ActivityBannerContentBinding;

public class BannerContentActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActivityBannerContentBinding binding; // ViewBinding 클래스 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 바인딩 객체 초기화
        binding = ActivityBannerContentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // UI 요소들 초기화
        drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.navigationView;
        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;

        // Toolbar 설정 및 뒤로가기 버튼 처리
        Toolbar toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                finish(); // 뒤로가기 버튼 클릭 시 액티비티 종료
            }
        });

        // 메뉴 이미지 클릭 시 사이드바 열기/닫기
        binding.menuImage.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 네비게이션 메뉴 아이템 클릭 처리 (수정, 삭제, 신고)
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_edit) {
                    // "수정" 메뉴 클릭 시 처리
                    Toast.makeText(BannerContentActivity.this, "수정 메뉴 클릭됨", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_delete) {
                    // "삭제" 메뉴 클릭 시 처리
                    Toast.makeText(BannerContentActivity.this, "삭제 메뉴 클릭됨", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_report) {
                    // "신고" 메뉴 클릭 시 처리
                    Toast.makeText(BannerContentActivity.this, "신고 메뉴 클릭됨", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BannerContentActivity.this, "알 수 없는 메뉴 아이템", Toast.LENGTH_SHORT).show();
                }

                // 네비게이션 드로어를 닫기
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // 데이터 설정 (서버나 데이터베이스에서 실제 데이터 불러올 수 있음)
        binding.productImageView.setImageResource(R.drawable.dog1);
        binding.productNameTextView.setText("Awesome Product");
        binding.productPriceTextView.setText("$199");
        binding.productDescriptionTextView.setText("This is an amazing product that offers great value. It is highly recommended for anyone looking for quality and reliability.");

        // 구매 버튼 클릭 리스너
        binding.buyButton.setOnClickListener(v ->
                Toast.makeText(BannerContentActivity.this, "Product Purchased!", Toast.LENGTH_SHORT).show()
        );

        // 관심목록에 추가 버튼 클릭 리스너
        binding.addToWishlistButton.setOnClickListener(v ->
                Toast.makeText(BannerContentActivity.this, "Added to Wishlist!", Toast.LENGTH_SHORT).show()
        );

        // 하단바 아이템 클릭 리스너 (현재 동작 없음)
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> true);
    }
}
