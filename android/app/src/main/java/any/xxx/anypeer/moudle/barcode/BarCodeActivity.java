package any.xxx.anypeer.moudle.barcode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.util.Utils;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;

public class BarCodeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code);

        findViewById(R.id.img_back).setVisibility(View.VISIBLE);
        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
        	final int qrcodeType = intent.getIntExtra(Utils.QRCODETYPE, Utils.QrCodeType.Null_Address.ordinal());
	        String qrcodeString = "Hello World";
        	if (qrcodeType == Utils.QrCodeType.Carrier_Address.ordinal()) {
				//Carrier Address
		        qrcodeString = intent.getStringExtra(Utils.ADDRESS);
		        ((TextView) findViewById(R.id.txt_title)).setText(R.string.my_carrier_address);
	        }
	        else if (qrcodeType == Utils.QrCodeType.Wallet_Address.ordinal()) {
		        //Wallet's receipt Address
		        qrcodeString = intent.getStringExtra(Utils.ADDRESS);
		        ((TextView) findViewById(R.id.txt_title)).setText(R.string.transfer_address_text);
		        findViewById(R.id.qrcodeString).setVisibility(View.VISIBLE);
		        ((TextView) findViewById(R.id.qrcodeString)).setText(qrcodeString);
	        }

	        ImageView mQrcode = findViewById(R.id.qrcode);

	        Bitmap bitmap = QRCodeEncoder.syncEncodeQRCode(qrcodeString, getQrcWidth(), Color.parseColor("#000000"));
	        if (bitmap == null) {
		        bitmap = QRCodeEncoder.syncEncodeQRCode(qrcodeString, getQrcWidth(), Color.parseColor("#000000"));
	        }

	        Glide.with(this).asBitmap().load(bitmap).into(mQrcode);

			final String finalQrcodeString = qrcodeString;
			mQrcode.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					String qrcodeString = finalQrcodeString;
					if (qrcodeType == Utils.QrCodeType.Carrier_Address.ordinal()) {
						//Carrier Address
						String[] args = finalQrcodeString.split("\\s+");
						if (args.length > 0) {
							qrcodeString = args[0];
						}
					}

					if (clipboard != null) {
						clipboard.setText(qrcodeString);
						Toast.makeText(BarCodeActivity.this, getString(R.string.content_copyed), Toast.LENGTH_LONG).show();
					}

					return false;
				}
			});
        }
    }

	private int getQrcWidth() {
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		return point.x * 6 / 10;
	}
}
