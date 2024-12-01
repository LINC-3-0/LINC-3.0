package mp.p02.home1;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private Context context;
    private ItemDatabaseHelper dbHelper;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.dbHelper = new ItemDatabaseHelper(context);
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

        // 아이템의 제목과 내용 설정
        holder.itemTitle.setText(item.getTitle());
        holder.itemDescription.setText(item.getContent());

        // 이미지 설정
        if (item.getImageUri() != null) {
            holder.itemImage.setImageURI(item.getImageUri());
        } else {
            // 기본 이미지 설정
            holder.itemImage.setImageResource(R.drawable.button1);
        }

        // 하트 버튼 초기 상태 설정
        holder.heartButton.setImageResource(item.isFavorite() ? R.drawable.heart_fill : R.drawable.heart_empty);

        // 하트 버튼 클릭 이벤트
        holder.heartButton.setOnClickListener(v -> {
            boolean isFavorite = item.isFavorite();
            item.setFavorite(!isFavorite);

            // 데이터베이스 업데이트
            new Thread(() -> dbHelper.updateHeartState(item.getId(), !isFavorite)).start();

            // UI 업데이트
            holder.heartButton.setImageResource(item.isFavorite() ? R.drawable.heart_fill : R.drawable.heart_empty);
        });

        // 아이템 클릭 이벤트 -> 상세 페이지로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ItemDetailActivity.class);
            intent.putExtra("item_id", item.getId());
            intent.putExtra("item_title", item.getTitle());
            intent.putExtra("item_content", item.getContent());
            if (item.getImageUri() != null) {
                intent.putExtra("item_image_uri", item.getImageUri().toString());
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public void updateItems(List<Item> newItems) {
        this.itemList = newItems;
        notifyDataSetChanged();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle;
        TextView itemDescription;
        ImageButton heartButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            heartButton = itemView.findViewById(R.id.heartButton);
        }
    }
}
