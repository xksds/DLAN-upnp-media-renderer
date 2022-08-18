package com.test.dlna;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.test.dlna.service.Action;
import com.test.dlna.service.dmr.DLNAPlayer;

import java.io.IOException;

public class LocalPlayerActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    private static final int MEDIA_PLAYER_BUFFERING_UPDATE = 4001;
    private static final int MEDIA_PLAYER_COMPLETION = 4002;
    private static final int MEDIA_PLAYER_ERROR = 4003;
    private static final int MEDIA_PLAYER_INFO = 4004;
    private static final int MEDIA_PLAYER_PREPARED = 4005;
    private static final int MEDIA_PLAYER_PROGRESS_UPDATE = 4006;
    private static final int MEDIA_PLAYER_VIDEO_SIZE_CHANGED = 4007;
    private static final int MEDIA_PLAYER_VOLUME_CHANGED = 4008;
    private static final int MEDIA_PLAYER_HIDDEN_CONTROL = 4009;

    private SurfaceHolder mSurfaceHolder;
    private static final String TAG = LocalPlayerActivity.class.getName();

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    MediaPlayer mMediaPlayer;
    String playURI;

    private AudioManager mAudioManager;
    private TextView mTextViewTime;
    private SeekBar mSeekBarProgress;
    private TextView mTextViewLength;
    private ImageButton mPauseButton;
    private ProgressBar mProgressBarPreparing;
    private TextView mTextProgress;
    private TextView mTextInfo;
    private RelativeLayout mBufferLayout;
    private LinearLayout mLayoutBottom;
    private RelativeLayout mLayoutTop;
    private TextView mVideoTitle;
    private Button mLeftButton;
    private Button mRightButton;
    private ImageView mSound;
    private SeekBar mSeekBarSound;
    private boolean isMute;
    private int mBackCount;

    private static DLNAPlayer.RemoterListener mMediaListener;
    public static void setMediaListener(DLNAPlayer.RemoterListener mediaListener) {
        mMediaListener = mediaListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlna_player);
        mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        surfaceView = (SurfaceView) findViewById(R.id.gplayer_surfaceview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
        mMediaPlayer.setOnErrorListener(mErrorListener);
        mMediaPlayer.setOnInfoListener(mInfoListener);
        mMediaPlayer.setOnPreparedListener(mPreparedListener);
        mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
        mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);

        initControl();

        Intent intent = getIntent();
        playURI = intent.getStringExtra("playURI");
        if (!TextUtils.isEmpty(playURI)) {
            setUri(playURI);
        }
        setTitle(intent);
        registerBroadcast();
    }

    private void setTitle(Intent intent) {
        String name = intent.getStringExtra("name");
        if (!TextUtils.isEmpty(name)) {
            mVideoTitle.setText(name);
        }
    }

    private void initControl() {
        mBufferLayout = (RelativeLayout) findViewById(R.id.buffer_info);
        mProgressBarPreparing = (ProgressBar) findViewById(R.id.player_prepairing);
        mTextProgress = (TextView) findViewById(R.id.prepare_progress);
        mTextInfo = (TextView) findViewById(R.id.info);

        mLayoutTop = (RelativeLayout) findViewById(R.id.layout_top);
        mVideoTitle = (TextView) findViewById(R.id.video_title);
        mLeftButton = (Button) findViewById(R.id.topBar_back);
        mRightButton = (Button) findViewById(R.id.topBar_list_switch);
        mLeftButton.setOnClickListener(this);
        mRightButton.setOnClickListener(this);

        mTextViewTime = (TextView) findViewById(R.id.current_time);
        mTextViewLength = (TextView) findViewById(R.id.totle_time);
        mPauseButton = (ImageButton) findViewById(R.id.play);
        mPauseButton.setOnClickListener(this);
        mLayoutBottom = (LinearLayout) findViewById(R.id.layout_control);
        mTextProgress = (TextView) findViewById(R.id.prepare_progress);
        mTextInfo = (TextView) findViewById(R.id.info);

        mSeekBarProgress = (SeekBar) findViewById(R.id.seekBar_progress);
        mSeekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int id = seekBar.getId();
                switch (id) {
                    case R.id.seekBar_progress:
                        seekTo(seekBar.getProgress());
                        break;
                    default:
                        break;
                }
            }
        });

        mSound = (ImageView) findViewById(R.id.sound);
        mSound.setOnClickListener(this);
        mSeekBarSound = (SeekBar) findViewById(R.id.seekBar_sound);
        mSeekBarSound.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mSeekBarSound.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        mSeekBarSound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        playURI = intent.getStringExtra("playURI");
        if (!TextUtils.isEmpty(playURI)) {
            setUri(playURI);
        }
        setTitle(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (null != mMediaPlayer)
            mMediaPlayer.pause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        exit();
        unregisterBroadcast();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mBackCount > 0) {
                exit();
            } else {
                mBackCount++;
                Toast.makeText(this, R.string.player_exit, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (null != mMediaPlayer) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (null != mMediaListener) {
            mMediaListener.stop();
            mMediaListener = null;
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.topBar_back:
                exit();
                break;
            case R.id.sound:
                isMute = !isMute;
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, isMute);
                if (isMute) {
                    mSound.setImageResource(R.drawable.phone_480_sound_mute);
                } else {
                    mSound.setImageResource(R.drawable.phone_480_sound_on);
                }
                break;
            case R.id.play: {
                doPauseResume();
                break;
            }
            default:
                break;
        }
    }

    private void updatePausePlay() {
        if(null != mMediaPlayer || null == mPauseButton)
            return;
        boolean isPlaying = mMediaPlayer.isPlaying();

        int resource = isPlaying ? R.drawable.button_pause
                : R.drawable.button_play;
        mPauseButton.setBackgroundResource(resource);
    }

    private void doPauseResume() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (null != mMediaListener) {
                mMediaListener.pause();
            }
        } else {
            mMediaPlayer.start();
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 200);

            if (null != mMediaListener) {
                mMediaListener.start();
            }
        }
        updatePausePlay();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            int visibility = mLayoutTop.getVisibility();
            if (visibility != View.VISIBLE) {
                mLayoutTop.setVisibility(View.VISIBLE);
                mLayoutBottom.setVisibility(View.VISIBLE);
            } else {
                mLayoutTop.setVisibility(View.GONE);
                mLayoutBottom.setVisibility(View.GONE);
            }
        }
        return false;
    }

    public  int getAudioSessionId() {
        if (null != mMediaPlayer) return  mMediaPlayer.getAudioSessionId();
        return 1;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(TAG, "surfaceChanged Called");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG, "surfaceCreated Called");
        if (null == mSurfaceHolder && null != mMediaPlayer)
            mMediaPlayer.setDisplay(holder);

        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG, "surfaceDestroyed Called");
        mSurfaceHolder = null;
    }

    public int getCurrentPosition() {
        if (null != mMediaPlayer) return (int)mMediaPlayer.getCurrentPosition();
        return -1;
    }

    public int getDuration() {
        if (null != mMediaPlayer) return (int)mMediaPlayer.getDuration();
        return -1;
    }

    public boolean isPlaying() {
        if (null != mMediaPlayer) return mMediaPlayer.isPlaying();
        return false;
    }

    public void setUri(String uri) {
        playURI = uri;
        try {
            if (null != mMediaPlayer) {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(playURI);
                if (null != mSurfaceHolder)
                    mMediaPlayer.setDisplay(mSurfaceHolder);
                mMediaPlayer.prepareAsync();
            }
        } catch (IOException e) {
            Log.v(TAG, e.getMessage());
        }
    }

    public void pause() {
        if (null != mMediaPlayer) mMediaPlayer.pause();
        if (null != mMediaListener) mMediaListener.pause();
    }

    public void seekTo(int pos) {
        if (null != mMediaPlayer) mMediaPlayer.seekTo(pos);
        if (null != mMediaListener) mMediaListener.positionChanged(pos);
    }

    public void start() {
        if (null != mMediaPlayer) mMediaPlayer.start();
        if (null != mHandler) mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 200);
        if (null != mMediaListener) mMediaListener.start();
    }

    public void stop() {
        if (null != mMediaPlayer) mMediaPlayer.stop();
        if (null != mMediaListener) mMediaListener.stop();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (MEDIA_PLAYER_PROGRESS_UPDATE != msg.what)
                Log.d(TAG, "msg=" + msg.what);
            switch (msg.what) {
                case MEDIA_PLAYER_PREPARED: {
                    mBufferLayout.setVisibility(View.GONE);
                    break;
                }
                case MEDIA_PLAYER_PROGRESS_UPDATE: {
                    if (null == mMediaPlayer)
                        break;
                    if (!mMediaPlayer.isPlaying())
                        break;

                    int position = (int) mMediaPlayer.getCurrentPosition();
                    int duration = (int) mMediaPlayer.getDuration();
                    if (null != mMediaListener) {
                        mMediaListener.positionChanged(position);
                        mMediaListener.durationChanged(duration);
                    }
                    mTextViewLength.setText(secToTime(duration / 1000));
                    mSeekBarProgress.setMax(duration);
                    mTextViewTime.setText(secToTime(position / 1000));
                    mSeekBarProgress.setProgress(position);
                    mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_PROGRESS_UPDATE, 500);
                    break;
                }
                case MEDIA_PLAYER_VIDEO_SIZE_CHANGED:{
                    if (null != surfaceView) {
                        int videoWidth = msg.arg1, videoHeight = msg.arg2;
                        WindowManager mWindowManager  = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        DisplayMetrics metrics = new DisplayMetrics();
                        mWindowManager.getDefaultDisplay().getMetrics(metrics);
                        int disWidth = metrics.widthPixels;
                        int disHeight = metrics.heightPixels;

                        Log.d(TAG, "Video output W * H = " + videoWidth + " * " + videoHeight + " Screen W * H = " + disWidth + " * " + disHeight);

                        int top = 0, left = 0;
                        int maxW = disHeight * videoWidth / videoHeight;
                        int maxH = disHeight;
                        if (maxW > disWidth) {
                            maxH = disWidth * videoHeight / videoWidth;
                            maxW = disWidth;
                            top = (disHeight - maxH) / 2;
                        } else
                            left = (disWidth - maxW) / 2;

                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(maxW, maxH);
                        lp.leftMargin = left;
                        lp.topMargin = top;
                        surfaceView.setLayoutParams(lp);
                    }
                    break;
                }
                case MEDIA_PLAYER_VOLUME_CHANGED: {
                    mSeekBarSound.setProgress(mAudioManager
                            .getStreamVolume(AudioManager.STREAM_MUSIC));
                    break;
                }
                case MEDIA_PLAYER_HIDDEN_CONTROL: {
                    mLayoutTop.setVisibility(View.GONE);
                    mLayoutBottom.setVisibility(View.GONE);
                    break;
                }
                default:
                    break;
            }
        }
    };

    private PlayBroadcastReceiver playReceiveBroadcast = new PlayBroadcastReceiver();

    public void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Action.DMR);
        intentFilter.addAction(Action.VIDEO_PLAY);
        registerReceiver(playReceiveBroadcast, intentFilter);
    }

    public void unregisterBroadcast() {
        unregisterReceiver(this.playReceiveBroadcast);
    }

    class PlayBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String str1 = intent.getStringExtra("helpAction");
            if (str1.equals(Action.PLAY)) {
                start();
                updatePausePlay();
            } else if (str1.equals(Action.PAUSE)) {
                pause();
                updatePausePlay();
            } else if (str1.equals(Action.SEEK)) {
                boolean isPaused = false;

                if (null != mMediaPlayer) {
                    if (!mMediaPlayer.isPlaying())
                        isPaused = true;
                    int position = intent.getIntExtra("position", 0);
                    mMediaPlayer.seekTo(position);
                }
                if (isPaused) {
                    pause();
                } else {
                    start();
                }
            } else if (str1.equals(Action.SET_VOLUME)) {
                int volume = (int) (intent.getDoubleExtra("volume", 0) * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) ;
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_VOLUME_CHANGED, 100);
            } else if (str1.equals(Action.STOP)) {
                stop();
                exit();
            }
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

        }
    };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            if (null != mMediaListener) {
                mMediaListener.start();
            }
            mHandler.sendEmptyMessage(MEDIA_PLAYER_PREPARED);
            mHandler.sendEmptyMessage(MEDIA_PLAYER_PROGRESS_UPDATE);
            mHandler.sendEmptyMessageDelayed(MEDIA_PLAYER_HIDDEN_CONTROL, 10000);
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            Log.v(TAG, "onCompletion Called");
            if (null != mMediaListener) {
                mMediaListener.endOfMedia();
            }
            exit();
        }
    };

    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
            switch (arg1) {
                case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.d(TAG, "MEDIA_INFO_BUFFERING_START: isPlaying : " + mp.isPlaying());
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                    break;
                case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                    Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                    break;
                case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                    break;
                case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                    break;
                case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                    Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                    break;
                case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                    Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                    break;
            }
            return true;
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);

            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            int mCurrentBufferPercentage = percent;
            Log.d(TAG, "Current buffer Percentage : " + mCurrentBufferPercentage);
        }
    };

    private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Log.v(TAG, "onSeekComplete Called");
//            if (null != mMediaListener) {
//                mMediaListener.endOfMedia();
//            }
        }
    };

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
    public static String secToTime(long paramLong) {
        int time = new Long(paramLong).intValue();
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":"
                        + unitFormat(second);
            }
        }
        return timeStr;
    }
}