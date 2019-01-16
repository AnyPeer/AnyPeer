package any.xxx.anypeer.moudle.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.EMMessage;
import any.xxx.anypeer.bean.VoiceMessageBody;
import any.xxx.anypeer.chatbean.ChatMessage;

public class VoicePlayClickListener implements View.OnClickListener {

    private ChatMessage message;
    private String filePath;
    private ImageView voiceIconView;

    private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    Activity activity;
    private BaseAdapter adapter;

    public static boolean isPlaying = false;
    public static VoicePlayClickListener currentPlayListener = null;

    VoicePlayClickListener(ChatMessage message, ImageView v, BaseAdapter adapter, Activity activity) {
        this.message = message;
        filePath = message.getFilePath();
        this.adapter = adapter;
        voiceIconView = v;
        this.activity = activity;
    }

    void stopPlayVoice() {
        voiceAnimation.stop();
        if (ChatMessage.Direct.getMsgDirect(message.getDirect()) == ChatMessage.Direct.RECEIVE) {
            voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
        } else {
            voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
        }
        // stop play voice
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
        ((ChatActivity) activity).playMsgId = null;
        adapter.notifyDataSetChanged();
    }

    void playVoice(String filePath) {
        if (!(new File(filePath).exists())) {
            return;
        }
        ((ChatActivity) activity).playMsgId = message.getMsgId();
        AudioManager audioManager = (AudioManager) activity
                .getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = new MediaPlayer();
//		if (EMChatManager.getInstance().getChatOptions().getUseSpeaker()) {
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
//		} else {
//			audioManager.setSpeakerphoneOn(false);// 关闭扬声器
//			// 把声音设定成Earpiece（听筒）出来，设定为正在通话中
//			audioManager.setMode(AudioManager.MODE_IN_CALL);
//			mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
//		}
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    stopPlayVoice(); // stop animation
                }

            });
            isPlaying = true;
            currentPlayListener = this;
            mediaPlayer.start();
            showAnimation();
        } catch (Exception e) {
        }
    }

    // show the voice playing animation
    private void showAnimation() {
        // play voice, and start animation
        if (ChatMessage.Direct.getMsgDirect(message.getDirect()) == ChatMessage.Direct.RECEIVE) {
            voiceIconView.setImageResource(R.drawable.voice_from_icon);
        } else {
            voiceIconView.setImageResource(R.drawable.voice_to_icon);
        }
        voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
        voiceAnimation.start();
    }

    @Override
    public void onClick(View v) {
//        String st = activity.getResources().getString(R.string.Is_download_voice_click_later);
        if (isPlaying) {
            if (((ChatActivity) activity).playMsgId != null && ((ChatActivity) activity).playMsgId.equals(message.getMsgId())) {
                currentPlayListener.stopPlayVoice();
                return;
            }
            currentPlayListener.stopPlayVoice();
        }

        playVoice(filePath);
    }
}
