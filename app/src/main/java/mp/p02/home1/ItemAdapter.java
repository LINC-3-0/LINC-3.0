package mp.p02.home1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private Context context;
    private ItemDatabaseHelper dbHelper;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList != null ? itemList : new ArrayList<>(); // null 체크 및 초기화
        this.dbHelper = new ItemDatabaseHelper(context); // 데이터베이스 헬퍼 초기화
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
        final Item item = itemList.get(position);

        // 아이템의 제목과 설명 설정
        holder.itemTitle.setText(item.getTitle());
        holder.itemDescription.setText(item.getContent());

        // 이미지가 있으면 설정, 없으면 기본 리소스 사용
        if (item.getImageUri() != null) {
            holder.itemImage.setImageURI(item.getImageUri());
        } else {
            holder.itemImage.setImageResource(item.getImageResource());
        }

        // 하트 버튼 초기 상태 설정
        holder.heartButton.setImageResource(item.isFavorite() ? R.drawable.heart_fill : R.drawable.heart_empty);

        // 하트 버튼 클릭 이벤트 처리
        holder.heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFavorite = item.isFavorite();
                item.setFavorite(!isFavorite);

                // 하트 상태를 데이터베이스에 업데이트
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dbHelper.updateHeartState(item.getId(), !isFavorite); // updateHeartState 호출
                    }
                }).start();

                // UI 업데이트
                holder.heartButton.setImageResource(item.isFavorite() ? R.drawable.heart_fill : R.drawable.heart_empty);
            }
        });

        // 아이템 이미지를 클릭하면 상세 페이지로 이동
        holder.itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ItemDetailActivity.class);

                // item_id와 함께 제목, 내용, 이미지 정보를 전달
                intent.putExtra("item_id", item.getId()); // item_id 전달
                intent.putExtra("item_title", item.getTitle());
                intent.putExtra("item_content", item.getContent());

                if (item.getImageUri() != null) {
                    intent.putExtra("item_image_uri", item.getImageUri().toString());
                } else {
                    intent.putExtra("item_image_resource", item.getImageResource());
                }

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0; // null 체크 및 안전한 접근
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public ImageView itemImage;
        public TextView itemTitle;
        public TextView itemDescription;
        public ImageButton heartButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            heartButton = itemView.findViewById(R.id.heartButton);
        }
    }
}
