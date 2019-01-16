package any.xxx.anypeer.moudle.common;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import any.xxx.anypeer.R;
import any.xxx.anypeer.widget.photoview.PhotoView;

public class PhotoActivity extends AppCompatActivity {

    public static final String PIC = "pic";

    private ImageView img_back;
    private PhotoView photo_view;
    private String pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        pic = getIntent().getStringExtra(PIC);

        if (TextUtils.isEmpty(pic)) {
            finish();
            return;
        }

        photo_view = findViewById(R.id.photo_view);
        photo_view.setImageURI(Uri.parse(pic));

        img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.ly_root).setBackgroundResource(android.R.color.transparent);
        findViewById(R.id.txt_title).setVisibility(View.GONE);
    }
}
