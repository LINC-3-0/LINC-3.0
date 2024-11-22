package mp.p02.home1;

import android.net.Uri;

public class Item {

    private Uri imageUri;
    private String title;
    private String content;
    private boolean isFavorite; // 하트 상태를 저장하는 필드
    private int imageResource;
    private int id; // 아이템의 ID 필드 추가

    // Constructor with Uri
    public Item(int id, Uri imageUri, String title, String content, boolean isFavorite) {
        this.id = id;
        this.imageUri = imageUri;
        this.title = title;
        this.content = content;
        this.isFavorite = isFavorite;
    }

    // Constructor with resource ID
    public Item(int id, int imageResource, String title, String content, boolean isFavorite) {
        this.id = id;
        this.imageResource = imageResource;
        this.title = title;
        this.content = content;
        this.isFavorite = isFavorite;
    }

    // Constructor with Uri (missing field initializations added)
    public Item(Uri imageUri, String title, String content) {
        this.imageUri = imageUri;
        this.title = title;
        this.content = content;
    }

    // Constructor with resource ID (missing field initializations added)
    public Item(int imageResource, String title, String content) {
        this.imageResource = imageResource;
        this.title = title;
        this.content = content;
    }

    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getImageResource() {
        return imageResource;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
