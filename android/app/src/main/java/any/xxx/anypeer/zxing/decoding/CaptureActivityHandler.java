package any.xxx.anypeer.zxing.decoding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;

import any.xxx.anypeer.zxing.CaptureActivity;
import any.xxx.anypeer.zxing.camera.CameraManager;
import any.xxx.anypeer.zxing.view.ViewfinderResultPointCallback;

import java.util.Vector;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 */
public final class CaptureActivityHandler extends Handler {

	private static final String TAG = CaptureActivityHandler.class
			.getSimpleName();

	private final CaptureActivity activity;
	private final DecodeThread decodeThread;
	private State state;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(CaptureActivity activity,
                                  Vector<BarcodeFormat> decodeFormats, String characterSet) {
		this.activity = activity;
		decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
				new ViewfinderResultPointCallback(activity.getViewfinderView()));
		decodeThread.start();
		state = State.SUCCESS;
		// Start ourselves capturing previews and decoding.
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case any.xxx.anypeer.R.id.auto_focus:
			// Log.d(TAG, "Got auto-focus message");
			// When one auto focus pass finishes, start another. This is the
			// closest thing to
			// continuous AF. It does seem to hunt a bit, but I'm not sure
			// what else to do.
			if (state == State.PREVIEW) {
				CameraManager.get().requestAutoFocus(this, any.xxx.anypeer.R.id.auto_focus);
			}
			break;
		case any.xxx.anypeer.R.id.restart_preview:
			Log.d(TAG, "Got restart preview message");
			restartPreviewAndDecode();
			break;
		case any.xxx.anypeer.R.id.decode_succeeded:
			Log.d(TAG, "Got decode succeeded message");
			state = State.SUCCESS;
			Bundle bundle = message.getData();

			/***********************************************************************/
			Bitmap barcode = bundle == null ? null : (Bitmap) bundle
					.getParcelable(DecodeThread.BARCODE_BITMAP);// ���ñ����߳�

			activity.handleDecode((com.google.zxing.Result) message.obj,
					barcode);// ���ؽ��
			/***********************************************************************/
			break;
		case any.xxx.anypeer.R.id.decode_failed:
			// We're decoding as fast as possible, so when one decode fails,
			// start another.
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					any.xxx.anypeer.R.id.decode);
			break;
		case any.xxx.anypeer.R.id.return_scan_result:
			Log.d(TAG, "Got return scan result message");
			activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
			activity.finish();
			break;
		case any.xxx.anypeer.R.id.launch_product_query:
			Log.d(TAG, "Got product query message");
			String url = (String) message.obj;
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			activity.startActivity(intent);
			break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), any.xxx.anypeer.R.id.quit);
		quit.sendToTarget();
		try {
			decodeThread.join();
		} catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(any.xxx.anypeer.R.id.decode_succeeded);
		removeMessages(any.xxx.anypeer.R.id.decode_failed);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					any.xxx.anypeer.R.id.decode);
			CameraManager.get().requestAutoFocus(this, any.xxx.anypeer.R.id.auto_focus);
			activity.drawViewfinder();
		}
	}

}
