package ahuja.shivam.groupchatapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

/**
 * Created by sourabh on 25/7/17.
 */

public class MessageAdapter extends ArrayAdapter<MessagePOJOClass> {
    private SharedPreferences msharedPreferences;

    public MessageAdapter(Context context, int resource, List<MessagePOJOClass> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_layout, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView othersmessageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView othersNameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView usersSentMsg = (TextView) convertView.findViewById(R.id.usersMsg);
        TextView authorusername = (TextView) convertView.findViewById(R.id.username);
        ImageView usersSentImage = (ImageView) convertView.findViewById(R.id.userphotoImageView);
        msharedPreferences = getContext().getSharedPreferences("USER", getContext().MODE_PRIVATE);
        MessagePOJOClass message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            othersmessageTextView.setVisibility(View.GONE);
            usersSentMsg.setVisibility(View.GONE);
            if (message.getName().trim().equals(msharedPreferences.getString("username", "ANONYMOUS").trim())) {

                authorusername.setVisibility(View.VISIBLE);
                usersSentImage.setVisibility(View.VISIBLE);
                othersNameTextView.setVisibility(View.GONE);
                photoImageView.setVisibility(View.GONE);
                Glide.with(usersSentImage.getContext())
                        .load(message.getPhotoUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(usersSentImage);
                authorusername.setText(message.getName());

            } else {
                othersNameTextView.setVisibility(View.VISIBLE);
                othersNameTextView.setText(message.getName());
                photoImageView.setVisibility(View.VISIBLE);
                authorusername.setVisibility(View.GONE);
                usersSentImage.setVisibility(View.GONE);
                Glide.with(photoImageView.getContext())
                        .load(message.getPhotoUrl())
                        .into(photoImageView);
            }
        } else {
            photoImageView.setVisibility(View.GONE);
            usersSentImage.setVisibility(View.GONE);
            if (message.getName().trim().equals(msharedPreferences.getString("username", "ANONYMOUS").trim())) {
                othersNameTextView.setVisibility(View.GONE);
                othersmessageTextView.setVisibility(View.GONE);
                usersSentMsg.setVisibility(View.VISIBLE);
                authorusername.setVisibility(View.VISIBLE);
                usersSentMsg.setText(message.getText());
                authorusername.setText(message.getName());
            } else {
                authorusername.setVisibility(View.GONE);
                othersNameTextView.setVisibility(View.VISIBLE);
                othersmessageTextView.setVisibility(View.VISIBLE);
                usersSentMsg.setVisibility(View.GONE);
                othersmessageTextView.setText(message.getText());
                othersNameTextView.setText(message.getName());

            }
        }
        return convertView;
    }


}
