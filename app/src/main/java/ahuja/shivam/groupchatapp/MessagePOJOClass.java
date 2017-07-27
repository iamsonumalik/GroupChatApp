package ahuja.shivam.groupchatapp;

/**
 * Created by sourabh on 25/7/17.
 */

public class MessagePOJOClass {
    private String text;
    private String name;
    private String photoUrl;

    public MessagePOJOClass() {
    }

    public MessagePOJOClass(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
