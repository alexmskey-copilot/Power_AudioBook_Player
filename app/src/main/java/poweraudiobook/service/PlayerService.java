package com.audiobook.pbp_service.service;

import static android.graphics.Bitmap.createScaledBitmap;
import static android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE;
import static com.audiobook.pbp_service.MainActivity.I_READ;
import static com.audiobook.pbp_service.MainActivity.LESSON_MAX;
import static com.audiobook.pbp_service.MainActivity.TRACK_REPEAT;
import static com.audiobook.pbp_service.MainActivity.back_in_time;
import static com.audiobook.pbp_service.MainActivity.its_tablet;
import static com.audiobook.pbp_service.MainActivity.lesson;
import static com.audiobook.pbp_service.MainActivity.lesson_point;
import static com.audiobook.pbp_service.MainActivity.mediaController;
import static com.audiobook.pbp_service.MainActivity.mediaPlayer;
import static com.audiobook.pbp_service.MainActivity.newMediaPlayer;
import static com.audiobook.pbp_service.MainActivity.next_track_from_competition;
import static com.audiobook.pbp_service.MainActivity.playing;
import static com.audiobook.pbp_service.MainActivity.selAlbum;
import static com.audiobook.pbp_service.MainActivity.showLessonNum;
import static com.audiobook.pbp_service.MainActivity.time_speaking_pause_cfg;
import static com.audiobook.pbp_service.MainActivity.trackChange;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.media.utils.MediaConstants;

import com.audiobook.pbp_service.Bookmarks;
import com.audiobook.pbp_service.MainActivity;
import com.audiobook.pbp_service.PABAppWidget;
import com.audiobook.pbp_service.PABAppWidget1x1;
import com.audiobook.pbp_service.PABAppWidgetFast;
import com.audiobook.pbp_service.PABAppWidgetFull;
import com.audiobook.pbp_service.R;
import com.audiobook.pbp_service.SoundScreen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final public class PlayerService extends MediaBrowserServiceCompat {
    private static final String CUSTOM_ACTION_FAST_FORWARD_AUTO = "FAST_FORWARD_AUTO";
    private static final String CUSTOM_ACTION_REWIND_AUTO = "REWIND_AUTO";

    private final int NOTIFICATION_ID = 260;
    private int curPos = 0;
    public final static String NOTIFICATION_DEFAULT_CHANNEL_ID = "powerAudioBook_channel";
    public int inFocusPaused = 0;
    public String playerServiceName = "PowerAudioBookPlayerService";
    public String mediaRootId = "powerMediaRootId";

    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    private PlaybackStateCompat.Builder stateBuilder = null;

    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean audioFocusRequested = false;
    private Context appContext = null;
    public Bitmap thumbnail = null;
    public static Bitmap thumbnail_st = null;
    private static int onLC_counter = 0;
    public static boolean in_fast_moving= false;

    private Result<List<MediaBrowserCompat.MediaItem>> locRes = null;
    private boolean after_pause= false;     // Признак того что только что был выполнен pause
    // (защита от повторного срабатывания от startPlayProgressUpdater()
    public static MainActivity.Action_Element aElement;
    private static boolean itsHandsFree= false;
    public static boolean need_refresh_seekBar_part_for_BM_goto = false;
    private int currentBM= 0;

    @Override

    public void onCreate() {
        super.onCreate();
        stateBuilder = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_FAST_FORWARD
                        | PlaybackStateCompat.ACTION_REWIND
        );
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFICATION_DEFAULT_CHANNEL_ID,
                            getString(R.string.notification_channel_name),
                            NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.enableVibration(false);

            notificationManager.createNotificationChannel(notificationChannel);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setAudioAttributes(audioAttributes)
                    .build();
        }

        AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

        mediaSession = new MediaSessionCompat(this, playerServiceName);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);

        setSessionToken(mediaSession.getSessionToken());

        appContext = getApplicationContext();

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, appContext, MediaButtonReceiver.class);
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(appContext, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE));

        mediaSession.setActive(true); // Сразу после получения фокуса

        if (PABAppWidget.WidgetIds!= null  &&  PABAppWidget.WidgetIds.length> 0) {
            for (int i= 0; i < PABAppWidget.WidgetIds.length; i++) {
                PABAppWidget.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidget.WidgetIds[i]);
            }
        }
        if (PABAppWidget1x1.WidgetIds1x1!= null  &&  PABAppWidget1x1.WidgetIds1x1.length> 0) {
            for (int i= 0; i < PABAppWidget1x1.WidgetIds1x1.length; i++) {
                PABAppWidget1x1.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidget1x1.WidgetIds1x1[i]);
            }
        }
        if (PABAppWidgetFast.WidgetIdsFast!= null  &&  PABAppWidgetFast.WidgetIdsFast.length> 0) {
            for (int i= 0; i < PABAppWidgetFast.WidgetIdsFast.length; i++) {
                PABAppWidgetFast.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFast.WidgetIdsFast[i]);
            }
        }
        if (PABAppWidgetFull.WidgetIdsFull!= null  &&  PABAppWidgetFull.WidgetIdsFull.length> 0) {
            for (int i= 0; i < PABAppWidgetFull.WidgetIdsFull.length; i++) {
                PABAppWidgetFull.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFull.WidgetIdsFull[i]);
            }
        }

    }

    // Обработчик потери и получения аудиофокуса
    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN: // Фокус вернулся — можно возобновлять, если были на паузе из-за фокуса
                    if (inFocusPaused == 1
                        &&  MainActivity.mediaPlayer!= null
                        &&  !MainActivity.mediaPlayer.isPlaying()) {
                        mediaSessionCallback.onPlay();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (!MainActivity.ignore_first_time_ducking) {
                        if (!MainActivity.duck_ignore) {
                            if (MainActivity.mediaPlayer!= null
                                    &&  MainActivity.mediaPlayer.isPlaying()) {
                                inFocusPaused = 1;
                                mediaSessionCallback.onPause();
                            }
                        }
                    } else {
                        MainActivity.ignore_first_time_ducking = false;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (MainActivity.mediaPlayer!= null
                            &&  MainActivity.mediaPlayer.isPlaying()) {
                        inFocusPaused = 1;
                        mediaSessionCallback.onPause();
                    }
                    audioFocusRequested = false; // фокус временно потерян
                    break;
                case AudioManager.AUDIOFOCUS_LOSS: // Полная потеря — останавливаемся навсегда
                    if (MainActivity.other_players_not_ignore) {
                        if (MainActivity.mediaPlayer!= null
                                &&  MainActivity.mediaPlayer.isPlaying()) {
                            mediaSessionCallback.onPause();
                        }
                        audioFocusRequested = false;
                        // Освобождаем фокус
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            audioManager.abandonAudioFocusRequest(audioFocusRequest);
                        } else {
                            audioManager.abandonAudioFocus(audioFocusChangeListener);
                        }
                    }
                    break;
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        if (MainActivity.mediaPlayer != null) {
            MainActivity.mediaPlayer.reset();
            MainActivity.mediaPlayer.release();
            MainActivity.mediaPlayerCounter--;
        }
        if (mediaController != null)
            mediaController.getTransportControls().stop();

        if (audioFocusRequested) {
            audioFocusRequested = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                audioManager.abandonAudioFocus(audioFocusChangeListener);
            }
        }
    }

    public MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        int currentState = PlaybackStateCompat.STATE_STOPPED;

        @Override
        public void onPlay() {
            if (MainActivity.not_touch_mode== MainActivity.ALL_TOUCH_DISABLED
                &&  inFocusPaused!= 1
                &&  !itsHandsFree)
                return;
            if (itsHandsFree)
                itsHandsFree= false;

            if (MainActivity.time_speaking_cfg
                    && MainActivity.time_speaking_play_cfg
                    && inFocusPaused == 0) {
                Date currentDate = new Date();
                DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String timeText = timeFormat.format(currentDate);
                MainActivity.speak(timeText);
                if (mediaController != null) {
                    try {
                        Thread.sleep(2600); //Приостанавливает поток на 2.6 секунды
                    } catch (Exception e) {
                    }
                }
            }
            if (MainActivity.sleep_timer_repeat && MainActivity.sleep_timer_ended) {
                MainActivity.sleep_timer_ended = false;
                if (MainActivity.sleep_timer_time_value_mem > 0) {
                    MainActivity.sleep_timer_time_value = MainActivity.sleep_timer_time_value_mem;
                    MainActivity.local_sleep = 0;
                    MainActivity.sleep_timer_time = true;
                }
                if (MainActivity.sleep_timer_parts_value_mem > 0) {
                    MainActivity.sleep_timer_parts_value = MainActivity.sleep_timer_parts_value_mem - 1;
                    MainActivity.local_sleep = 0;
                    MainActivity.sleep_timer_parts = true;
                }
            }
            change_book_reading_status();
            testAndCreateNewMediaPlayer();
            if (!audioFocusRequested  ||  (audioFocusRequested  &&  inFocusPaused == 0)) {
                int audioFocusResult;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioFocusResult = audioManager.requestAudioFocus(audioFocusRequest);
                }
                else {
                    if (!its_tablet) {
                        audioFocusResult = audioManager.requestAudioFocus(
                                audioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN);
                    } else {
                        audioFocusResult = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
                    }
                }
                if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Log.w("PAB", "Audio focus denied");
                    return;
                }
                audioFocusRequested = true;
            }

// отрисовка встроенной картинки
            try {
                showEmbeddedImage();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                updateMetadataFromMeTrack();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (MainActivity.lesson_point[lesson] != MainActivity.seekBar_part.getProgress())
                MainActivity.seekBar_part.setProgress(MainActivity.lesson_point[lesson]);
            playing = 1;
            loudness_setup();

            // correction play position to minus "back_in_time" secs
            curPos = MainActivity.lesson_point[lesson];
// запись информации для UNDO
            save_history_points();

            if (!MainActivity.simply_play) {
                if (curPos > SoundScreen.musicList.get(lesson - 1).duration)
                    curPos = SoundScreen.musicList.get(lesson - 1).duration;
                if (curPos < 0) curPos = 0;
                if (MainActivity.back_in_time != 0) {
                    curPos -= MainActivity.back_in_time * 1000;
                }
            }
            else {
                MainActivity.simply_play= false;
            }
            MainActivity.mediaPlayer.seekTo(curPos);

            try {
                MainActivity.mediaPlayer.start();
            }
            catch (Exception e) {
                Log.d("PlayerService","onPlay: IllegalStateException");
            }

            setSpeedPS();

            MainActivity.buttonPlayPause.setIconResource(R.drawable.ic_media_pause);
            MainActivity.startPlayProgressUpdater();

            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, curPos, 1).build());
            currentState = PlaybackStateCompat.STATE_PLAYING;

            if (PABAppWidget.WidgetIds!= null  &&  PABAppWidget.WidgetIds.length> 0) {
                for (int i= 0; i < PABAppWidget.WidgetIds.length; i++) {
                    PABAppWidget.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidget.WidgetIds[i]);
                }
            }
            if (PABAppWidget1x1.WidgetIds1x1!= null  &&  PABAppWidget1x1.WidgetIds1x1.length> 0) {
                for (int i= 0; i < PABAppWidget1x1.WidgetIds1x1.length; i++) {
                    PABAppWidget1x1.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidget1x1.WidgetIds1x1[i]);
                }
            }
            if (PABAppWidgetFast.WidgetIdsFast!= null  &&  PABAppWidgetFast.WidgetIdsFast.length> 0) {
                for (int i= 0; i < PABAppWidgetFast.WidgetIdsFast.length; i++) {
                    PABAppWidgetFast.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFast.WidgetIdsFast[i]);
                }
            }
            if (PABAppWidgetFull.WidgetIdsFull!= null  &&  PABAppWidgetFull.WidgetIdsFull.length> 0) {
                for (int i= 0; i < PABAppWidgetFull.WidgetIdsFull.length; i++) {
                    PABAppWidgetFull.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFull.WidgetIdsFull[i]);
                }
            }

            refreshNotificationAndForegroundStatus(currentState);
            after_pause= false;
            if (inFocusPaused== 1)
                inFocusPaused= 0;
        }

        public  void setSpeedPS () {
            if (mediaPlayer!= null) {
                try {
                    MainActivity.playbackParams = mediaPlayer.getPlaybackParams();
                }
                catch (Exception e) {
                    Log.e("Exception", "Mediaplayer get playback params failed: " + e.toString());
                }
                MainActivity.playbackParams.allowDefaults();
                MainActivity.playbackParams.setSpeed((MainActivity.speed_play + 1) / 5);
                if (MainActivity.playbackParams.getSpeed() > 3)
                    MainActivity.playbackParams.setSpeed(3);
                if (mediaPlayer.isPlaying()) {
                    try {
                        mediaPlayer.setPlaybackParams(MainActivity.playbackParams);
                    } catch (Exception e) {
                        Log.e("Exception", "Mediaplayer change speed failed: " + e.toString());
                    }
                }
            }
        }

        public void showEmbeddedImage() throws IOException {

            ImageView iv_background = (ImageView) MainActivity.backImage;

            if (!getEmbeddedImage(selAlbum)) {
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(appContext, SoundScreen.musicList.get(lesson - 1).uri);
                    byte[] data = mmr.getEmbeddedPicture();
                    if (data != null) {
                        thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (thumbnail== null) {
                            iv_background.setImageResource(R.drawable.headphones);
                            SoundScreen.MusicElement ME2 = SoundScreen.musicList.get(lesson - 1);
                            ME2.image_exist = false;
                            SoundScreen.musicList.set(lesson - 1, ME2);
                        }
                        iv_background.setImageBitmap(thumbnail);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    iv_background.setImageResource(R.drawable.headphones);
                    SoundScreen.MusicElement ME2 = SoundScreen.musicList.get(lesson - 1);
                    ME2.image_exist = false;
                    SoundScreen.musicList.set(lesson - 1, ME2);
                } catch (RuntimeException re) {
                    re.printStackTrace();
                    iv_background.setImageResource(R.drawable.headphones);
                    SoundScreen.MusicElement ME2 = SoundScreen.musicList.get(lesson - 1);
                    ME2.image_exist = false;
                    SoundScreen.musicList.set(lesson - 1, ME2);
                }
                mmr.release();
            } else {
                iv_background.setImageBitmap(thumbnail);
            }
            if (thumbnail!= null) {
                int bh = thumbnail.getHeight();
                int bw = thumbnail.getWidth();
                if (bh > 201 || bw > 201) {
                    bh = bh / 2;
                    bw = bw / 2;
                }
                thumbnail_st = createScaledBitmap(thumbnail, bw, bh, false);
            }
        }

        @Override
        public void onPause() {
            if (MainActivity.not_touch_mode== MainActivity.ALL_TOUCH_DISABLED
                    && inFocusPaused!= 1
                    &&  !itsHandsFree)
                return;
            if (!after_pause) {
                if (MainActivity.time_speaking_cfg
                        && time_speaking_pause_cfg
                        && inFocusPaused == 0 && !MainActivity.one_time_no_time_speak) {
                    Date currentDate = new Date();
                    DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String timeText = timeFormat.format(currentDate);
                    MainActivity.speak(timeText);
                }
                if (MainActivity.one_time_no_time_speak) {
                    MainActivity.one_time_no_time_speak = false;
                }

                if (mediaPlayer != null) {
                    MainActivity.mediaPlayer.pause();
                    try {
                        MainActivity.lesson_point[lesson] = mediaPlayer.getCurrentPosition();    // save the current position in file
                    } catch (IllegalStateException e) {
                        MainActivity.lesson_point[lesson] = MainActivity.seekBar_part.getProgress();    // save the current position in file
                    }
                }
                else {
                    if (MainActivity.seekBar_part != null) {
                        MainActivity.lesson_point[lesson] = MainActivity.seekBar_part.getProgress();             // save the current position in file
                    }
                }

                writeToFile();
                MainActivity.update_start = false;
                MainActivity.buttonPlayPause.setIconResource(R.drawable.ic_media_play);

                playing = 0;

                currentState = PlaybackStateCompat.STATE_PAUSED;
                try {
                    updateMetadataFromMeTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, MainActivity.lesson_point[lesson], 1).build());
                if (PABAppWidget.WidgetIds != null && PABAppWidget.WidgetIds.length > 0) {
                    for (int i = 0; i < PABAppWidget.WidgetIds.length; i++) {
                        PABAppWidget.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidget.WidgetIds[i]);
                    }
                }
                if (PABAppWidget1x1.WidgetIds1x1 != null && PABAppWidget1x1.WidgetIds1x1.length > 0) {
                    for (int i = 0; i < PABAppWidget1x1.WidgetIds1x1.length; i++) {
                        PABAppWidget1x1.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidget1x1.WidgetIds1x1[i]);
                    }
                }
                if (PABAppWidgetFast.WidgetIdsFast != null && PABAppWidgetFast.WidgetIdsFast.length > 0) {
                    for (int i = 0; i < PABAppWidgetFast.WidgetIdsFast.length; i++) {
                        PABAppWidgetFast.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFast.WidgetIdsFast[i]);
                    }
                }
                if (PABAppWidgetFull.WidgetIdsFull != null && PABAppWidgetFull.WidgetIdsFull.length > 0) {
                    for (int i = 0; i < PABAppWidgetFull.WidgetIdsFull.length; i++) {
                        PABAppWidgetFull.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFull.WidgetIdsFull[i]);
                    }
                }

                refreshNotificationAndForegroundStatus(currentState);
                after_pause = true;
            }
        }

        @Override
        public void onStop() {
            if (MainActivity.mediaPlayer != null) {
                MainActivity.mediaPlayer.release();
                MainActivity.mediaPlayer = null;
                MainActivity.mediaPlayerCounter--;
            }

            if (audioFocusRequested) {
                audioFocusRequested = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                } else {
                    audioManager.abandonAudioFocus(audioFocusChangeListener);
                }
            }

            mediaSession.setActive(false);

            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, MainActivity.lesson_point[lesson], 1).build());
            currentState = PlaybackStateCompat.STATE_STOPPED;

            refreshNotificationAndForegroundStatus(currentState);

            stopSelf();
        }

        // correction play position to plus XX secs
        @Override
        public void onFastForward() {
            if ((MainActivity.not_touch_mode== MainActivity.ALL_TOUCH_DISABLED
                || MainActivity.not_touch_mode== MainActivity.PLAY_PAUSE_ONLY)
                &&  !itsHandsFree)
                return;
            if (MainActivity.swap_fastMoving_goto_cfg  &&  !MainActivity.std_keys) {
                if (!MainActivity.swap_processed) {
                    MainActivity.swap_processed = true;
                    onSkipToNext();
                    return;
                }
            }
            if (MainActivity.swap_processed)
                MainActivity.swap_processed= false;
            if (MainActivity.std_keys)
                MainActivity.std_keys= false;
            in_fast_moving= true;
            change_book_reading_status();
            testAndCreateNewMediaPlayer();
            if (mediaPlayer != null) {
                    curPos = mediaPlayer.getCurrentPosition();
                } else {
                    curPos = MainActivity.seekBar_part.getProgress();
                }
                if (curPos< 0)
                    curPos= 0;
            save_history_points();
            curPos += jump_counter();
                if (curPos > SoundScreen.musicList.get(lesson - 1).duration) {
                    curPos = SoundScreen.musicList.get(lesson - 1).duration;
                    curPos -= back_in_time * 1000;
                }
            MainActivity.seekBar_part.setProgress(curPos);
            mediaPlayer.seekTo(curPos);
            MainActivity.lesson_point[lesson] = curPos;

            showLessonNum();
            writeToFile();      // MainActivity.lesson_point[lesson] заполнен

            try {
                updateMetadataFromMeTrack();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mediaPlayer.isPlaying()) {
                mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, MainActivity.lesson_point[lesson], 1).build());
                currentState = PlaybackStateCompat.STATE_PLAYING;
            } else {
                mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, MainActivity.lesson_point[lesson], 1).build());
                currentState = PlaybackStateCompat.STATE_PAUSED;
            }

            if (PABAppWidgetFast.WidgetIdsFast!= null  &&  PABAppWidgetFast.WidgetIdsFast.length> 0) {
                for (int i= 0; i < PABAppWidgetFast.WidgetIdsFast.length; i++) {
                    PABAppWidgetFast.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFast.WidgetIdsFast[i]);
                }
            }
            if (PABAppWidgetFull.WidgetIdsFull!= null  &&  PABAppWidgetFull.WidgetIdsFull.length> 0) {
                for (int i= 0; i < PABAppWidgetFull.WidgetIdsFull.length; i++) {
                    PABAppWidgetFull.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFull.WidgetIdsFull[i]);
                }
            }

            refreshNotificationAndForegroundStatus(currentState);
        }

        public int jump_counter () {

            int displacement= MainActivity.jump_weight* 1000;
            displacement= (int) (displacement * ((MainActivity.speed_play+ 1)/ 5));
            return displacement;

        }

        @Override
        public void onSkipToNext() {
            if ((MainActivity.not_touch_mode== MainActivity.ALL_TOUCH_DISABLED
                || MainActivity.not_touch_mode== MainActivity.PLAY_PAUSE_ONLY)
                &&  !MainActivity.next_track_from_competition
                &&  !itsHandsFree)
                return;
            if (next_track_from_competition)            // переход из обработчика конца файла
                next_track_from_competition= false;
            if (MainActivity.swap_fastMoving_goto_cfg  &&  !MainActivity.std_keys) {
                if (!MainActivity.swap_processed) {
                    MainActivity.swap_processed = true;
                    if (MainActivity.swap_fast_moving_cfg)
                        onRewind();
                    else
                        onFastForward();
                    return;
                }
            }
            if (MainActivity.swap_processed)
                MainActivity.swap_processed= false;
            if (MainActivity.std_keys)          // признак нажатия клавиши перемещения на экране
                MainActivity.std_keys= false;

            change_book_reading_status();
            currentBM= MainActivity.getCurrentBookmarkNum();
            need_refresh_seekBar_part_for_BM_goto= false;
            if (MainActivity.goto_bookmarks_in_fucking_apple_style_cfg) {
                // узнать есть ли после текущей позиции в треке, закладки относящиеся к текущему треку
                // если у текущего трека закладок больше нет, то обновляем seek_part и идем дальше по книге
                // иначе идем к следующей закладке текущего трека
                if (!isNextBookmarkInTrackExist()) {
                    need_refresh_seekBar_part_for_BM_goto = true;
                }
            }
            if (lesson < SoundScreen.musicList.size()
                ||  (lesson == SoundScreen.musicList.size() && MainActivity.repeat_state == TRACK_REPEAT)
                ||  (lesson == 1  &&  MainActivity.one_file_and_BMs)
                ||  (lesson == SoundScreen.musicList.size()
                     &&  MainActivity.goto_bookmarks_in_fucking_apple_style_cfg
                     &&  currentBM<= Bookmarks.bookMarkList.size())) {
                MainActivity.showCounter = 0;
                if (mediaPlayer != null) {
                    MainActivity.lesson_point[lesson] = MainActivity.mediaPlayer.getCurrentPosition();
                } else {
                    MainActivity.lesson_point[lesson] = MainActivity.seekBar_part.getProgress();
                }

                if (MainActivity.mediaPlayer != null && MainActivity.mediaPlayer.isPlaying()) {
                    playing = 1;
                    currentState = PlaybackStateCompat.STATE_PLAYING;
                }

                if (MainActivity.repeat_state == TRACK_REPEAT) {
                    MainActivity.lesson_point[lesson] = 0;
                } else {
                    save_history_points ();
                    if (!MainActivity.one_file_and_BMs
                            &&  !MainActivity.goto_bookmarks_in_fucking_apple_style_cfg)
                        lesson++;
                }
                if (need_refresh_seekBar_part_for_BM_goto)
                    lesson++;
                if (!MainActivity.one_file_and_BMs
                        || need_refresh_seekBar_part_for_BM_goto)                     // признак, того что книга состит из одного файла и имеет закладки
                    Bookmarks.need_refresh_BMs= true;                   // нужно перерисовать закладки

                if (lesson > 1
                      &&  (!MainActivity.one_file_and_BMs
                      ||  (MainActivity.goto_bookmarks_in_fucking_apple_style_cfg
                          &&  need_refresh_seekBar_part_for_BM_goto))) {
                    MainActivity.buttonPrev.setEnabled(true);
                    MainActivity.buttonPrev.setBackgroundColor(MainActivity.button_color);
                }
                if (!MainActivity.one_file_and_BMs
                        ||  (MainActivity.goto_bookmarks_in_fucking_apple_style_cfg
                            &&  need_refresh_seekBar_part_for_BM_goto)) {
                    MainActivity.seekBar_part.setMax(SoundScreen.musicList.get(lesson - 1).duration);
                    try {
                        showEmbeddedImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    updateMetadataFromMeTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (MainActivity.lesson_point[lesson] < 0)
                    MainActivity.lesson_point[lesson] = 0;
                if (!MainActivity.one_file_and_BMs
                        ||  (MainActivity.goto_bookmarks_in_fucking_apple_style_cfg
                            &&  need_refresh_seekBar_part_for_BM_goto)) {
                    if (MainActivity.lesson_point[lesson] >= SoundScreen.musicList.get(lesson - 1).duration)
                        MainActivity.lesson_point[lesson] =
                                SoundScreen.musicList.get(lesson - 1).duration - back_in_time * 1000;
                }
                else {
                    if (Bookmarks.bookMarkList.size() > 0) {
                        if (currentBM == 0) {
                            if (Bookmarks.bookMarkList.size() == 1)
                                lesson_point[lesson] = Bookmarks.bookMarkList.get(0).offset;
                            else
                                lesson_point[lesson] = Bookmarks.bookMarkList.get(1).offset;
                        }
                        else {
                            if (currentBM< Bookmarks.bookMarkList.size())
                                lesson_point[lesson] = Bookmarks.bookMarkList.get(currentBM).offset;
                            else
                                lesson_point[lesson] = Bookmarks.bookMarkList.get(currentBM- 1).offset;
                        }
                    }
                }
                MainActivity.seekBar_part.setProgress(MainActivity.lesson_point[lesson]);
                MainActivity.mediaPlayer.seekTo(MainActivity.lesson_point[lesson]);

                if (MainActivity.one_file_and_BMs  &&  !need_refresh_seekBar_part_for_BM_goto)
                    MainActivity.hand_changed= 1;
                else
                    MainActivity.hand_changed= 0;
                try {
                    trackChange();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                loudness_setup();

                showLessonNum();
                if (playing == 1) {
                    MainActivity.mediaPlayer.start();
                    setSpeedPS();
                    MainActivity.buttonPlayPause.setIconResource(R.drawable.ic_media_pause);
                }

                writeToFile();      // MainActivity.lesson_point[lesson] заполнен
                if (mediaPlayer.isPlaying()) {
                    mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, MainActivity.lesson_point[lesson], 1).build());
                    currentState = PlaybackStateCompat.STATE_PLAYING;
                } else {
                    mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, MainActivity.lesson_point[lesson], 1).build());
                    currentState = PlaybackStateCompat.STATE_PAUSED;
                }
// Обновление виджета
                if (PABAppWidget.WidgetIds!= null  &&  PABAppWidget.WidgetIds.length> 0) {
                    for (int i= 0; i < PABAppWidget.WidgetIds.length; i++) {
                        PABAppWidget.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidget.WidgetIds[i]);
                    }
                }
                if (PABAppWidgetFull.WidgetIdsFull!= null  &&  PABAppWidgetFull.WidgetIdsFull.length> 0) {
                    for (int i= 0; i < PABAppWidgetFull.WidgetIdsFull.length; i++) {
                        PABAppWidgetFull.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFull.WidgetIdsFull[i]);
                    }
                }

                refreshNotificationAndForegroundStatus(currentState);
            }
        }

        // запись истории перемещений
        public void save_history_points () {
            aElement= new MainActivity.Action_Element(MainActivity.Action_Element.ACTION_SN, lesson, lesson_point[lesson]);
            MainActivity.actionsList.add(aElement);
        }

        // установка дополнительной громкости
        public void loudness_setup () {
            if (MainActivity.loudness != null) {
                MainActivity.loudness.release();
            }
            int sessionId = mediaPlayer.getAudioSessionId();
            if (sessionId != 0) {
                try {
                    MainActivity.loudness = new LoudnessEnhancer(sessionId);
                    MainActivity.loudness.setEnabled(true);
                    if (MainActivity.curTG != 0) {
                        MainActivity.loudness.setTargetGain((int) MainActivity.curTG);
                    }
                } catch (Exception e) {
                    Log.e("Loudness", "Не удалось инициализировать", e);
                }
            }
        }

        // узнать, если ли после текущей позиции в треке закладки относящиеся к этому треку
        public boolean isNextBookmarkInTrackExist() {
            int BMNum= MainActivity.getCurrentBookmarkNum()- 1;
            if (BMNum>= Bookmarks.bookMarkList.size()- 1)
                return false;
            if ((Bookmarks.bookMarkList.get(BMNum+ 1).lesson)== MainActivity.lesson)
                return true;
            else
                return false;
        }
        // узнать, если ли до текущей позиции в треке закладки относящиеся к этому треку
        public boolean isPrevBookmarkInTrackExist() {
            int BMNum= MainActivity.getCurrentBookmarkNum()- 1;
            if (BMNum== 0)
                return false;
            if ((Bookmarks.bookMarkList.get(BMNum- 1).lesson)== MainActivity.lesson)
                return true;
            else
                return false;
        }
        //  correction play position to minus XX secs
        @Override
        public void onRewind() {
            if ((MainActivity.not_touch_mode== MainActivity.ALL_TOUCH_DISABLED
                || MainActivity.not_touch_mode== MainActivity.PLAY_PAUSE_ONLY)
                &&  !itsHandsFree)
                return;
            if (MainActivity.swap_fastMoving_goto_cfg  &&  !MainActivity.std_keys) {
                if (!MainActivity.swap_processed) {
                    MainActivity.swap_processed = true;
                    onSkipToPrevious();
                    return;
                }
            }
            if (MainActivity.swap_processed)
                MainActivity.swap_processed= false;
            if (MainActivity.std_keys)
                MainActivity.std_keys= false;

            in_fast_moving = true;

                change_book_reading_status();
                testAndCreateNewMediaPlayer();
                curPos = MainActivity.mediaPlayer.getCurrentPosition();
                save_history_points();
            curPos -= jump_counter();
                if (curPos < 0) curPos = 0;

                MainActivity.mediaPlayer.seekTo(curPos);
                MainActivity.seekBar_part.setProgress(curPos);
                MainActivity.lesson_point[lesson] = curPos;
                showLessonNum();
                writeToFile();      // MainActivity.lesson_point[lesson] заполнен

                try {
                    updateMetadataFromMeTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mediaPlayer.isPlaying()) {
                    mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, MainActivity.lesson_point[lesson], 1).build());
                    currentState = PlaybackStateCompat.STATE_PLAYING;
                } else {
                    mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, MainActivity.lesson_point[lesson], 1).build());
                    currentState = PlaybackStateCompat.STATE_PAUSED;
                }

            if (PABAppWidgetFast.WidgetIdsFast!= null  &&  PABAppWidgetFast.WidgetIdsFast.length> 0) {
                for (int i= 0; i < PABAppWidgetFast.WidgetIdsFast.length; i++) {
                    PABAppWidgetFast.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFast.WidgetIdsFast[i]);
                }
            }
            if (PABAppWidgetFull.WidgetIdsFull!= null  &&  PABAppWidgetFull.WidgetIdsFull.length> 0) {
                for (int i= 0; i < PABAppWidgetFull.WidgetIdsFull.length; i++) {
                    PABAppWidgetFull.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFull.WidgetIdsFull[i]);
                }
            }

            refreshNotificationAndForegroundStatus(currentState);
        }

        @Override
        public void onSkipToPrevious() {
            if ((MainActivity.not_touch_mode== MainActivity.ALL_TOUCH_DISABLED
                || MainActivity.not_touch_mode== MainActivity.PLAY_PAUSE_ONLY)
                &&  !itsHandsFree)
                return;
            if (MainActivity.swap_fastMoving_goto_cfg  &&  !MainActivity.std_keys) {
                if (!MainActivity.swap_processed) {
                    MainActivity.swap_processed = true;
                    if (MainActivity.swap_fast_moving_cfg)
                        onFastForward();
                    else
                        onRewind();
                    return;
                }
            }
            if (MainActivity.swap_processed)
                MainActivity.swap_processed= false;
            if (MainActivity.std_keys)
                MainActivity.std_keys= false;

            currentBM= MainActivity.getCurrentBookmarkNum();
            need_refresh_seekBar_part_for_BM_goto= false;
            if (MainActivity.goto_bookmarks_in_fucking_apple_style_cfg) {
                // узнать есть ли перед текущей позицей в треке, закладки относящиеся к текущему треку
                // если у текущего трека закладок больше нет, то обновляем seek_part и идем на предыдущий трек
                // иначе идем к предыдущей закладке текущего трека
                if (!isPrevBookmarkInTrackExist()) {
                    need_refresh_seekBar_part_for_BM_goto = true;
                }
            }
            Bookmarks.need_refresh_BMs= true;  // Нужно перерисовать закладки
            change_book_reading_status();
            if (lesson > 1
                    ||  (lesson == 1  &&  MainActivity.one_file_and_BMs)
                    ||  (lesson == 1
                        &&  need_refresh_seekBar_part_for_BM_goto
                        &&  !Bookmarks.bookMarkList.isEmpty()
                        &&  currentBM>= 0)) {

                testAndCreateNewMediaPlayer();
                MainActivity.lesson_point[lesson] = MainActivity.mediaPlayer.getCurrentPosition();
                if (MainActivity.mediaPlayer != null && MainActivity.mediaPlayer.isPlaying())
                    playing = 1;
                if (lesson <= SoundScreen.musicList.size() && lesson > 0) {
                    save_history_points();
                    if (!MainActivity.one_file_and_BMs) {
                        for (int i = lesson; i <= SoundScreen.musicList.size(); i++)
                            MainActivity.lesson_point[i] = 0;
                        lesson--;
                        if (lesson_point[lesson]>= SoundScreen.musicList.get(lesson- 1).duration - 1000) {
                            lesson_point[lesson]= 0;
                        }
                    }
                    else {
                        if (need_refresh_seekBar_part_for_BM_goto)
                            lesson--;
                        if (currentBM == 0) {
                            if (lesson_point[lesson] > Bookmarks.bookMarkList.get(0).offset)
                                lesson_point[lesson] = Bookmarks.bookMarkList.get(0).offset;
                        }
                        if (!MainActivity.goto_bookmarks_in_fucking_apple_style_cfg
                                && currentBM > 0) {
                            if (lesson_point[lesson] > Bookmarks.bookMarkList.get(currentBM - 1).offset)
                                lesson_point[lesson] = Bookmarks.bookMarkList.get(currentBM - 1).offset;
                            else if (currentBM > 1) {
                                lesson_point[lesson] = Bookmarks.bookMarkList.get(currentBM - 2).offset;
                            }
                        }
                        if (MainActivity.goto_bookmarks_in_fucking_apple_style_cfg
                            &&  currentBM> 1) {
                            int BMind= currentBM- 2;
                            if (currentBM>= Bookmarks.bookMarkList.size())
                                BMind= currentBM- 1;
                            if (need_refresh_seekBar_part_for_BM_goto) {
                                // если трек на который переходим равен треку в предыдущей закладке (-1),
                                // то берем смещение из этой закладки
                                // если не равен, то используем смещение сохраненное в lesson_point
                                if (lesson == Bookmarks.bookMarkList.get(BMind).lesson)
                                    lesson_point[lesson] = Bookmarks.bookMarkList.get(BMind).offset;
                            }
                            if (!need_refresh_seekBar_part_for_BM_goto) {
                                lesson_point[lesson] = Bookmarks.bookMarkList.get(currentBM - 2).offset;
                            }
                        }
                    }
                    if (!need_refresh_seekBar_part_for_BM_goto
                            &&  MainActivity.one_file_and_BMs)
                        MainActivity.hand_changed= 1;
                    else
                        MainActivity.hand_changed= 0;
                    try {
                        trackChange();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    loudness_setup();

                    if (lesson== 0)
                        lesson= 1;
                    MainActivity.seekBar_part.setMax(SoundScreen.musicList.get(lesson - 1).duration);
                    curPos = MainActivity.lesson_point[lesson];
                    MainActivity.seekBar_part.setProgress(curPos);
                    writeToFile();      // MainActivity.lesson_point[lesson] заполнен

                    if (!MainActivity.one_file_and_BMs) {
                        curPos -= MainActivity.back_in_time * 1000;
                    }
                    if (curPos < 0) curPos = 0;
                    MainActivity.mediaPlayer.seekTo(curPos);
                    MainActivity.seekBar_part.setProgress(curPos);
                    MainActivity.lesson_point[lesson] = curPos;

                    if (playing == 1) {
                        MainActivity.mediaPlayer.start();
                        setSpeedPS();
                        MainActivity.buttonPlayPause.setIconResource(R.drawable.ic_media_pause);
                    }

                    try {
                        showEmbeddedImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (MainActivity.lesson_point[lesson] < 0)
                        MainActivity.lesson_point[lesson] = 0;
                }
                try {
                    updateMetadataFromMeTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mediaPlayer.isPlaying()) {
                    mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, MainActivity.lesson_point[lesson], 1).build());
                    currentState = PlaybackStateCompat.STATE_PLAYING;
                } else {
                    mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, MainActivity.lesson_point[lesson], 1).build());
                    currentState = PlaybackStateCompat.STATE_PAUSED;
                }

                showLessonNum();
                MainActivity.bookmarks_draw();
                // Обновление виджета
                if (PABAppWidget.WidgetIds!= null  &&  PABAppWidget.WidgetIds.length> 0) {
                    for (int i= 0; i < PABAppWidget.WidgetIds.length; i++) {
                        PABAppWidget.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidget.WidgetIds[i]);
                    }
                }
                if (PABAppWidgetFull.WidgetIdsFull!= null  &&  PABAppWidgetFull.WidgetIdsFull.length> 0) {
                    for (int i= 0; i < PABAppWidgetFull.WidgetIdsFull.length; i++) {
                        PABAppWidgetFull.updateAppWidget(appContext, AppWidgetManager.getInstance(appContext), PABAppWidgetFull.WidgetIdsFull[i]);
                    }
                }

                refreshNotificationAndForegroundStatus(currentState);
            }
        }

        // Установка статуса чтения книги в I_READ
        public void  change_book_reading_status () {
            if (MainActivity.reading_status != MainActivity.READ)
                MainActivity.reading_status = I_READ;
        }

        public void updateMetadataFromMeTrack() throws IOException {

            if (SoundScreen.musicList.size() > 0) {
                if (!getEmbeddedImage(selAlbum)) {
                    android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    try {
                        mmr.setDataSource(appContext, SoundScreen.musicList.get(lesson - 1).uri);
                        byte[] data = mmr.getEmbeddedPicture();
                        if (data != null) {
                            thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);
                            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, thumbnail);
                        } else {
                            thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.headphones);
                            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, thumbnail);
                            SoundScreen.MusicElement ME2 = SoundScreen.musicList.get(lesson - 1);
                            ME2.image_exist = false;
                            SoundScreen.musicList.set(lesson - 1, ME2);
                        }
                    } catch (IllegalArgumentException e) {
                        thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.headphones);
                        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, thumbnail);
                        SoundScreen.MusicElement ME2 = SoundScreen.musicList.get(lesson - 1);
                        ME2.image_exist = false;
                        SoundScreen.musicList.set(lesson - 1, ME2);
                    } catch (RuntimeException re) {
                        re.printStackTrace();
                        thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.headphones);
                        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, thumbnail);
                        SoundScreen.MusicElement ME2 = SoundScreen.musicList.get(lesson - 1);
                        ME2.image_exist = false;
                        SoundScreen.musicList.set(lesson - 1, ME2);
                    }
                    mmr.release();
                } else {
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, thumbnail);
                }
            }


            if (SoundScreen.musicList.size() > 0) {
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, SoundScreen.musicList.get(lesson - 1).name);

                if (SoundScreen.musicList.get(lesson - 1).artist != null &&
                        !SoundScreen.musicList.get((lesson - 1)).artist.equals("<unknown>"))
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, SoundScreen.musicList.get(lesson - 1).artist);
                else
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, SoundScreen.musicList.get(lesson - 1).album);

                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, SoundScreen.musicList.get(lesson - 1).album);
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, SoundScreen.musicList.get(lesson - 1).album);
                metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, SoundScreen.musicList.get(lesson - 1).duration);
            }
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, lesson);
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(lesson));
            mediaSession.setMetadata(metadataBuilder.build());

            Bundle playbackStateExtras = new Bundle();
            playbackStateExtras.putString(
                    MediaConstants.PLAYBACK_STATE_EXTRAS_KEY_MEDIA_ID, String.valueOf(lesson));
            mediaSession.setPlaybackState(stateBuilder
                    .setExtras(playbackStateExtras)
                    .build());

        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {  // Play для выбранного трека
// статус прочтения книги
            change_book_reading_status();
            testAndCreateNewMediaPlayer();
            if (MainActivity.mediaPlayer.isPlaying()) {
                playing = 1;
                currentState = PlaybackStateCompat.STATE_PLAYING;
            }
            if (MainActivity.loudness != null)
                MainActivity.loudness.release();
            if (Integer.parseInt(mediaId) <= SoundScreen.musicList.size()) {
                lesson = Integer.parseInt(mediaId);
                save_history_points();
                try {
                    trackChange();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                MainActivity.seekBar_part.setMax(SoundScreen.musicList.get(lesson - 1).duration);
                if (!MainActivity.always_begin_part
                    ||  (MainActivity.always_begin_part
                         &&  MainActivity.pos_rqst_from_BMs_controller)) {
                    curPos = MainActivity.lesson_point[lesson];
                    if (!MainActivity.pos_rqst_from_BMs_controller) {
                        curPos -= MainActivity.back_in_time * 1000;
                    }
                    else {
                        MainActivity.pos_rqst_from_BMs_controller= false;
                    }
                    if (curPos < 0) curPos = 0;
                } else {
                    curPos = 0;
                }
                MainActivity.mediaPlayer.seekTo(curPos);
                MainActivity.seekBar_part.setProgress(curPos);
                MainActivity.lesson_point[lesson] = curPos;
                if (playing == 1) {
                    MainActivity.mediaPlayer.start();
                    MainActivity.buttonPlayPause.setIconResource(R.drawable.ic_media_pause);
                }
                showLessonNum();
                try {
                    showEmbeddedImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (MainActivity.lesson_point[lesson] < 0)
                    MainActivity.lesson_point[lesson] = 0;
                loudness_setup();
                try {
                    updateMetadataFromMeTrack();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                playing = 0;
                currentState = PlaybackStateCompat.STATE_PAUSED;
            }
            writeToFile();      // MainActivity.lesson_point[lesson] заполнен
            if (mediaPlayer.isPlaying()) {
                setSpeedPS();
                mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, MainActivity.lesson_point[lesson], 1).build());
                currentState = PlaybackStateCompat.STATE_PLAYING;
            } else {
                mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, MainActivity.lesson_point[lesson], 1).build());
                currentState = PlaybackStateCompat.STATE_PAUSED;
            }
            refreshNotificationAndForegroundStatus(currentState);
        }

        public void testAndCreateNewMediaPlayer () {
            if (MainActivity.mediaPlayer == null) {
                newMediaPlayer(SoundScreen.musicList.get(lesson - 1).uri);
            }
        }
        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {  // перемотка для Android Auto
            if (CUSTOM_ACTION_FAST_FORWARD_AUTO.equals(action)) {
                mediaController.getTransportControls().fastForward();
            }
            if (CUSTOM_ACTION_REWIND_AUTO.equals(action)) {
                mediaController.getTransportControls().rewind();
            }
        }

        @Override
        public void onPrepare() {
            notifyChildrenChanged(mediaRootId);
            playing = 0;
            try {
                updateMetadataFromMeTrack();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, MainActivity.lesson_point[lesson], 1).build());
            currentState = PlaybackStateCompat.STATE_PAUSED;
            if (SoundScreen.musicList.size() > 0)
                refreshNotificationAndForegroundStatus(currentState);

        }

    };

    @Override
    public void notifyChildrenChanged(@NonNull String parentId) {
        super.notifyChildrenChanged(parentId);
    }

    ;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return super.onBind(intent);
        }
        return new PlayerServiceBinder();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // Здесь мы возвращаем rootId - в нашем случае "Root".
        // Значение RootId непринципиально, оно будет просто передано
        // в onLoadChildren как parentId.
        // Идея здесь в том, что мы можем проверить clientPackageName и
        // в зависимости от того, что это за приложение, вернуть ему
        // разные плейлисты.
        // Если с неким приложением мы не хотим работать вообще,
        // можно написать return null;

        final Bundle extras = new Bundle();
        extras.putInt(
                MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
        );
        extras.putInt(
                MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE,
                MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM
        );

        return new BrowserRoot(mediaRootId, extras);
    }

    @Override
    public void onLoadChildren(final String parentId,
                               final Result<List<MediaBrowserCompat.MediaItem>> result) {
        // Возвращаем плейлист. Элементы могут быть FLAG_PLAYABLE
        // или FLAG_BROWSABLE.
        // Элемент FLAG_PLAYABLE нас могут попросить проиграть,
        // а FLAG_BROWSABLE отобразится как папка и, если пользователь
        // в нее попробует войти, то вызовется onLoadChildren с parentId
        // данного browsable-элемента.
        // То есть мы можем построить виртуальную древовидную структуру,
        // а не просто список треков.

        MainActivity.AndroidAutoInit = true;
        String TAG = "AndoidAutoInit";
        locRes = result;
        boolean image_exist = true;
        ArrayList<MediaBrowserCompat.MediaItem> data =
                new ArrayList<>(1);

        Bundle mediaItemExtras = new Bundle();
        int finalTime = 0, timeDuration = 0;
        double sounded = 0;
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        MediaDescriptionCompat.Builder description = new MediaDescriptionCompat.Builder();
        try {
            if (getEmbeddedImage(selAlbum)) {
            }
        } catch (FileNotFoundException e) {
            image_exist = false;
        }

        for (int i = 1; i <= SoundScreen.musicList.size() && i <= LESSON_MAX; i++) {

            finalTime = MainActivity.lesson_point[i];
            timeDuration = SoundScreen.musicList.get(i - 1).duration;
            sounded = (double) finalTime / (double) timeDuration;
            if (finalTime >= timeDuration)
                mediaItemExtras.putInt(
                        MediaConstants.DESCRIPTION_EXTRAS_KEY_COMPLETION_STATUS,
                        MediaConstants.DESCRIPTION_EXTRAS_VALUE_COMPLETION_STATUS_FULLY_PLAYED);
            if (finalTime < timeDuration && finalTime != 0) {
                mediaItemExtras.putInt(
                        MediaConstants.DESCRIPTION_EXTRAS_KEY_COMPLETION_STATUS,
                        MediaConstants.DESCRIPTION_EXTRAS_VALUE_COMPLETION_STATUS_PARTIALLY_PLAYED);
            }
            if (finalTime == 0)
                mediaItemExtras.putInt(
                        MediaConstants.DESCRIPTION_EXTRAS_KEY_COMPLETION_STATUS,
                        MediaConstants.DESCRIPTION_EXTRAS_VALUE_COMPLETION_STATUS_NOT_PLAYED);
            mediaItemExtras.putDouble(
                    MediaConstants.DESCRIPTION_EXTRAS_KEY_COMPLETION_PERCENTAGE, sounded);
            mediaItemExtras.putString(
                    MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_GROUP_TITLE,
                    SoundScreen.musicList.get(i - 1).album);

            if (!image_exist) {
                try {
                    mmr.setDataSource(appContext, SoundScreen.musicList.get(lesson - 1).uri);
                    byte[] bmData = mmr.getEmbeddedPicture();
                    if (bmData != null) {
                        thumbnail = BitmapFactory.decodeByteArray(bmData, 0, bmData.length);
                    } else {
                        thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.headphones);

                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.headphones);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.headphones);
                }
                try {
                    mmr.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            description.setDescription(SoundScreen.musicList.get(i - 1).album);
            description.setTitle(SoundScreen.musicList.get(i - 1).name);
            if (SoundScreen.musicList.get(i - 1).artist != null &&
                    !SoundScreen.musicList.get((i - 1)).artist.equals("<unknown>"))
                description.setSubtitle(SoundScreen.musicList.get(i - 1).artist);
            else
                description.setSubtitle(SoundScreen.musicList.get(i - 1).album);
            description.setMediaId(Integer.toString(i));
            description.setExtras(mediaItemExtras);
            description.setIconBitmap(thumbnail);
            mediaItemExtras.clear();

            data.add(new MediaBrowserCompat.MediaItem(description.build(), FLAG_PLAYABLE));
        }

        if (onLC_counter == 0) {
            onLC_counter = 1;
            Bundle customActionExtras = new Bundle();

            stateBuilder.addCustomAction(
                    new PlaybackStateCompat.CustomAction.Builder(
                            CUSTOM_ACTION_REWIND_AUTO,
                            getResources().getString(R.string.rewind),
                            R.drawable.ic_media_rew)
                            .setExtras(customActionExtras)
                            .build());

            stateBuilder.addCustomAction(
                    new PlaybackStateCompat.CustomAction.Builder(
                            CUSTOM_ACTION_FAST_FORWARD_AUTO,
                            getResources().getString(R.string.FastForward),
                            R.drawable.ic_media_ff)
                            .setExtras(customActionExtras)
                            .build());
        }

        MainActivity.AndroidAutoInit = false;
        result.sendResult(data);

    }

    public class PlayerServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
    }

    private void refreshNotificationAndForegroundStatus(int playbackState) {
        switch (playbackState) {
            case PlaybackStateCompat.STATE_PLAYING: {
                startForeground(NOTIFICATION_ID, getNotification(playbackState));
                break;
            }
            case PlaybackStateCompat.STATE_PAUSED: {
                Notification ntf = getNotification(playbackState);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                NotificationManagerCompat.from(PlayerService.this).notify(NOTIFICATION_ID, ntf);
                if (Build.VERSION.SDK_INT < 31) {
                    stopForeground(false);
                }
                break;
            }
            default: {
                stopForeground(true);
                break;
            }
        }
    }

    private Notification getNotification(int playbackState) {
            MediaControllerCompat controller = mediaSession.getController();
            MediaMetadataCompat mediaMetadata = controller.getMetadata();
            MediaDescriptionCompat description = mediaMetadata.getDescription();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, PlayerService.NOTIFICATION_DEFAULT_CHANNEL_ID);
            builder
                    .setSmallIcon(R.drawable.ic_small_icon)
                    .setContentTitle(description.getTitle())
                    .setContentText(description.getSubtitle())
                    .setSubText(description.getDescription())
                    .setLargeIcon(description.getIconBitmap())
                    .setSilent(true)
                    .setContentIntent(controller.getSessionActivity())
                    .setDeleteIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            if (lesson > 1)
                builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous,
                    getString(R.string.previous),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));

        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause,
                    getString(R.string.pause), MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        else
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play,
                    getString(R.string.play), MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE)));

        if (lesson < SoundScreen.musicList.size())
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next,
                    getString(R.string.next), MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));

        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_rew,
                getString(R.string.rewind), MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                PlaybackStateCompat.ACTION_REWIND)));

        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_ff,
                getString(R.string.FastForward), MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                PlaybackStateCompat.ACTION_FAST_FORWARD)));

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0,1,2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP))
                .setMediaSession(mediaSession.getSessionToken())); // setMediaSession требуется для Android Wear
        builder.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark)); // The whole background (in MediaStyle), not just icon background
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setOnlyAlertOnce(true);
        builder.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);
        builder.setAllowSystemGeneratedContextualActions (false);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setProgress (SoundScreen.musicList.get(lesson -1).duration, MainActivity.lesson_point[lesson], true);

        return builder.build();
    }
    public void writeToFile() {

        MainActivity.oneFileWrite (MainActivity.WORK_FILE_NAME);  // work file writing
        if (!selAlbum.isEmpty())
            MainActivity.oneFileWrite (selAlbum);    // for local library file writing

    }

    private boolean getEmbeddedImage(String curAlbum) throws FileNotFoundException {

        FileInputStream in = null;
        File directory = getFilesDir();

        String fileName = curAlbum + ".png";
        fileName = fileName.replaceAll("/", " ");
        File miFile = new File(directory, fileName);
        if (miFile.exists() == false)
            return false;
        in = new FileInputStream(miFile);
        try {
            thumbnail = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (thumbnail != null)
            return true;
        return false;

    }

}
