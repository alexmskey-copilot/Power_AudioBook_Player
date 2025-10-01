package com.audiobook.pbp_service;


import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;
import static android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_TIME;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.audiobook.pbp_service.service.PlayerService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;


//@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    public static final String  // MAIN_MEDIA_PLAYER = "com.audiobook.powerbookplayer.extra.MAIN_MEDIA_PLAYER",
            WORK_FILE_NAME = "workData",            // for current book file name
            READING_LIST_FILE_NAME = "readingList.txt", // for reading history file name
            FAVORITE_LIST_FILE_NAME = "favoriteList.txt";    // Favorites list
    public static ImageButton favoritButton,
                            touchButton,
                            addBookmarkButton,
                            buttonRepeat,
                            partSelectButton,
                            buttonEqualizer,
                            buttonShare,
                            buttonUndo,
                            menuButton;
    public static final int CONFIG_FILE_VERSION = 1;
    public static /*Button*/ MaterialButton buttonPlayPause;
    public static /*Button*/ MaterialButton buttonPrev, buttonNext;
    public static ImageView backImage;
    private static /*Button*/ MaterialButton buttonPrev10, buttonNext10,
                 buttonSleepTimer;
    public static int  sleepTimerPressCounter= 0; // счетчик нажатий на кнопку включения таймера сна
    public  Button speed_down_button, speed_up_button;
    public static MediaPlayer mediaPlayer;
    public static SeekBar seekBar_part, addVolumeControl, show_progress, speed_control;
    private static TextView track_name, totalView, passedView,
            leftView, curTrack,
            maxTrack,
            sounded_hours,
            hours_total_text,
            future_hours,
            totalDur, //
            track_out_of,
            speed_view_text,
            speed_view;             // current speed of play
    private TextView headerText;
    private static TextView sleep_timer_show, sleep_parts_show, sleep_timer_textView;
    private static TextView ad_volume;
    int night_mode= 0;
    public double totalDuration = 0;
    public static int back_in_time = 4,     // Встроенный откат
                      back_in_time_cust= 0; // Настраиваемый откат
    public static boolean need_back_in_time_cust= false;
    public static int fast_moving_cust= 12;  // значение пользовательской перемотки
    public static boolean need_fast_moving_cust= false;
    public static int lesson = 0, // номер текущего файла в книге
                        pre_lesson= -1, // копия lesson для блокировки лишних отображений главы
                        lesson_point[]; // положение в текущем файле[lesson]
    public static final int LESSON_MAX = 2626, ALBUM_MAX = 260;
    private static AudioManager audioManager;
    public static LoudnessEnhancer loudness;
    public static float curTG = 0;
    public static String textBuf;
    private static final int
            PERMISSION_REQUEST_STORAGE_READING = 1,
            PERMISSION_REQUEST_NOTIFICATIONS = 2;
    public static final int PERMISSION_STORAGE = 101;
    private int     inDestroying = 0;
    private static int music_quan= 0;
    private static int loc_size;
    private static int duration;
    private static int offset;
    public static String    selAlbum = "",
                            textBuf2,
                            currentAlbum,
                            startFolder = "",
                            current_directory= "";

    private static final Handler handler = new Handler();
    public static final int ALBUM_REQUEST_CODE = 100,
            LEFT_HAND_REQUEST_CODE = 200,
            HELP_BOOK_SELECT_REQUEST_CODE = 300,
            SELECT_FOLDER_CODE = 400,
            SLEEP_TIMER_CODE = 500,
            EQUALIZER_CODE = 600,
            SELECT_ROOT_FOLDER_CODE = 800,
            PERMISSIONS_REQUEST_CODE = 900,
            BOOKMARKS_CODE= 1000,
            NEW_CONFIG_LOAD= 1100,
            LOAD_COVERS_CODE= 1200,
            LOAD_ANY_AUDIO_FILE= 1400,
            FOLDERS_VISIBILITY_CODE= 1500,
            SELECT_PARTS_CODE= 1600;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public static int playing= 0, no_change_album= 0;

    private static PlayerService.PlayerServiceBinder playerServiceBinder;
    public static MediaControllerCompat mediaController;
    private static MediaControllerCompat.Callback callback;
    private ServiceConnection serviceConnection;
    private Intent intent = null;
    public static Context appContext;
    private Bitmap thumbnail = null;
    public static boolean simetric_interface = false, new_simetric_interface = false, need_simetric_scin_change = false; // UI с симметричными кнопками
    public static boolean left_hand_interface = false, new_left_hand_interface = false, need_hand_change = false; // UI для левой руки
    public static boolean one_finger_right_interface = false, new_one_finger_right_interface = false, need_one_finger_right = false; // UI для одного пальца правой руки
    public static boolean one_finger_left_interface = false, new_one_finger_left_interface = false, need_one_finger_left = false; // UI для одного пальца левой руки
    public static boolean right_hand_interface = false, new_right_hand_interface = false, need_right_hand_interface = false; // UI default - правая рука
    public static boolean keep_screen_anytime = false, need_screen_keeping_change = false; // Не блокировать главный экран
    public static boolean duck_ignore = false, need_duck_ignore_change = false; // включен режим игнорирования звука СМС и навигатора
    public static boolean other_players_not_ignore = false, need_other_players_not_ignore = false; // режим не игнорирования включения других плееров
    public static boolean bad_wire_correct = false, need_bad_wire_change = false; // включен режим компенсации дребезга клавиатуры
    public static int hand_changed= 0;  // поменяли руку
    private static boolean headset_unplug = false;
    private static Date currentDate = new Date(), preCurrentDate = new Date();
    private long timeDiff = 0;
    public static boolean themesAsSystem = false, themesAuto = false, themesLight= false, themesDark = true,
            themes_change = false;  // UI для управленимя темами
    public static boolean full_brightness = false, need_full_brightness_change = false; // управление режимом полной яркости экрана
    public static boolean keep_portrait = false, need_keep_portrait = false;            // управление режимом "всегда сохранять портретную ориентацию""
    public static boolean show_artist= false, need_show_artist= false;                  // управление режимом вывода тега Artist
    public static boolean show_composer= false, need_show_composer= false;              // управление режимом вывода тега Composer
    public static boolean show_system_folders = false, need_show_system_folders= false; // управление режимом поиска в системных папках (Android и т.д.)
    public static boolean showOneAlbumInFolder = false, need_showOneAlbumInFolder= false; // Показывать название альбома даже если он один в папке
    public static boolean boldFontForLongNames = false, need_boldFontForLongNames= false; // Выводить название книги большим шрифтом для длинных имен
    public static boolean directoryAlbum = false,
            sleep_timer_time= false, sleep_timer_parts= false, sleep_timer_repeat= false,
            sleep_timer_ended= false, need_sleep_timer= false,
            wait_end_part= false;     // ожидание окончания главы после сработки таймера сна по времени
    public static float sourceBrightness= 0;
    public static int LAST_READING_SIZE = 3; // кол-во книг в списке последних выбранных
    public static boolean use_fileNames= false;
    public static boolean new_sleep_timer_mode_cfg= false;  // новый формат задания таймера сна. Нужен один раз при переходе от старого формата.
    public static int showCounter= 0,                       // отладочный счетчик глубины вызовов
                                        sleep_timer_time_value= 0, sleep_timer_time_value_mem= 0, local_sleep= 0,
                                        sleep_timer_parts_value= 0, sleep_timer_parts_value_mem= 0;
    public static float speed_play = 4;
    public static float MAX_PLAY_SPEED = 14;  // 9 = speed 2x
    public static PlaybackParams playbackParams = new PlaybackParams();
    public static boolean ignore_first_time_ducking= false;
    public static RadioButton radio_0sec, radio_2sec, radio_4sec, radio_6sec;
//    public static RadioGroup back_246;
    public static int jump_weight = 15; // Величина перемотки в секундах
//    private TextView fast_moving;
    public static RadioButton radio_jump15, radio_jump60;
    public static boolean favorite_list_using = false;
    public static int buttonsTransparency = 5;  // Значение по умолчанию 5 из 6
    public static float seekBarTransparency= 0.69F;  // прозрачность полос управления
    public static boolean need_transparency_change = false;
    public static int button_color= 0;
    public static boolean play_after_customizing= false;
    public static int buttons_size = 0;  // размер кнопок (по умолчанию 0 - самый большой)
    public static boolean need_buttons_size_change = false;
    private static int started_icon_size = 0;
    public static int NO_REPEAT= 0,             // Повтор выключен
                      TRACK_REPEAT= 1,          // повтор дорожки
                      BOOK_REPEAT= 2,           // повтор книги
                      BOOKMARK_REPEAT= 3,       // повтор от закладки до закладки
                      repeat_state= NO_REPEAT;
    public static int DO_NOT_READ = 0, I_READ = 1, READ = 2, reading_status = DO_NOT_READ;
    public static boolean update_start = false;
    public static int show_all_books = 0, show_no_read_book= 1, show_now_read_books= 2, show_read_books= 3,
            show_book_list= show_all_books;
    public static Equalizer mEqualizer = null;
    public static boolean equalizer_set= false; //  Признак активности эквалайзера
    public static Equalizer.Settings eqs= null;
    public static boolean AndroidAutoInit = false;
    public static boolean non_stop_after_change_orientation= true,
            need_non_stop= false,
            after_start= true,
            simply_play= false, // не делать откат при включении Play
            zero_back_in_time_after_change_orient_cfg = false,   // Нулевой откат после смены ориентации
            zero_back_in_time_after_change_orient_need= false;
    public static boolean show_cover= false, need_show_cover= false;
    public static boolean speed_step_005 = false, need_speed_step_005 = false;
    public static boolean use_root_folder = false, need_use_root_folder= false;
    public static String root_folder_path = null;
    public static boolean its_tablet= false;
    public static boolean always_begin_part = false, need_always_begin_part= false;
    public static boolean time_speaking_cfg = false,            // проговаривать время при нажатии Плэй
                          time_speaking_play_pause_cfg= false,  //  при любом нажатии
                          time_speaking_play_cfg= false,        //  при включении воспроизведения
                          time_speaking_pause_cfg = false,        //  при постановке на паузу
                          ttsEnabled= false,        // Time To Speach доступен
                          need_time_speaking= false,
                          one_time_no_time_speak= false;
    public static TextToSpeech TTS;
    private int permissionReadExternalStorage,
            permissionWriteExternalStorage;
    public static boolean   nosave_speed_for_newbooks= false,
                            need_nosave_speed_for_newbooks= false,
                            speed_use_only_in_cur_book = false;
    public static boolean   is_LightTheme_Manual_Set = false;   // ручная установка светлой темы
                                                                // нужно для принудительного перевода
                                                                // старых версий со светлой темы на темную
    public static boolean   always_show_favorites_cfg= false,       // показывать книгу из избранного даже если
                            need_always_show_favorites_cfg= false;  // она не видна при текущих настройках поиска книг
    public static boolean   exit_only_in_menu_cfg = false,              // Выход только через пункт меню
                            need_exit_only_in_menu_cfg = false;
    public static boolean   backPressed_switch_background_cfg = false,  // Нажатие кнопки Назад переключает в фоновый режим
                            need_backPressed_switch_background_cfg = false;
    public static boolean   swap_fastMoving_goto_cfg = false,     // обмен кнопок быстрой перемотки и перехода по главам
                            need_swap_fastMoving_goto = false,    // для гарнитуры и экрана блокировки
                            swap_processed= false,
                            std_keys= false,                      // признак нажатия клавиши перемещения на экране
                            next_track_from_competition= false,   // переход на следующий трек из обработчика конца файла
                            goto_bookmarks_in_fucking_apple_style_cfg= false, // переход между закладками клавишами перехода между треками
                            need_goto_bookmarks_in_fucking_apple_style_cfg= false;


    public static int       mediaPlayerCounter  = 0;            // счетчик активных сессий mediaPlayer
    public  int screenSize = 0;                                 // класс размера экрана
    // SCREENLAYOUT_SIZE_SMALL, SCREENLAYOUT_SIZE_NORMAL, SCREENLAYOUT_SIZE_LARGE, SCREENLAYOUT_SIZE_XLARGE
    public static int   ALL_TOUCH_ENABLED= 0,                   // Разрешены все кнопки
                        ALL_TOUCH_DISABLED= 1,                  // Запрещены все кнопки
                        PLAY_PAUSE_ONLY= 2,                     // Разрешена только кнопка PLAY/PAUSE
                        not_touch_mode= ALL_TOUCH_ENABLED;      // Режим блокировки нажатия кнопок
    public static int   transparency_in_dont_touch_mode= 0;     // хранилище прозрачности кнопок в режиме блокировки нажатий
    public static boolean   pos_rqst_from_BMs_controller= false;// Запрос позиционирования пришел от контроллера закладок
    private LinearLayout total_line;
    private static int surf_width= 0, surf_height= 0;
    private static Canvas   canvas= null,
                            pre_canvas= null;
    private static SurfaceView surface;
    public static int  loc_off_diff= 0;
    public static final String config_file_name = "config.app",
                               config_file_extention = ".PAB_config",
                               statistic_file_extention= ".PAB_stat",
                               bookmarks_file_extention= ".PAB_bookmrx",
                               png_file_extension= ".png",
                               jpg_file_extension= ".jpg"
//    ,
//                               bmp_file_extension= ".bmp"
                                       ;
    public static int  workFileType= 0;
    public static int TYPE_WF_STAT= 100;
    public static int TYPE_WF_CONFIG= 200;
    public static int TYPE_WF_BOOKMARKS= 300;
    public static File directory_cfg = null;
    private static boolean no_file_writing= false;  // признак того, что делать
                                                    // сохранение текущих файлов не надо
    public static boolean swap_fast_moving_cfg = false,     // клавиши перемотки меняются кодами
                          need_swap_fast_moving= false;     // акутально для однокнопочных BT-гарнитур.
    // по дефолту 2 клика - перемотка назад
    //            3 клика - перемотка вперед
    // в этом режиме все меняется- 2 клика - назад, 3 клика - вперед
    public static boolean fucking_apple= false;     // признак файл в формате Apple (M4A, M4B)
    public static boolean one_file_and_BMs= false;  // признак, того что книга состит из одного файла и имеет закладки
    public static boolean what_100= false;          // признак destroy
    public static int format_work_file_version= 0;  // Версия формата текущего рабочего файла
                                                    // 0 - первая версия, с которой все началось))
                                                    // 1 - добавлено значение тега Composer. Записывается через разделитель после значения тега Artist
    public static String end_of_tag = "<pab_eot>";  // конец тега для строк рабочего файла содержащих больше одного тега
    public static boolean show_full_book_name= false, // выводить полное имя книги, при выборе (стандартно выводится 126 байт)
                          need_show_full_book_name= false;

    private static boolean onReCreate= false;       // запущен recreate();
    public static boolean UI_spoiler_open= false;   // Состояние спойлера отвечающего за UI
    public static boolean screen_spoiler_open= false; // Состояние спойлера отвечающего за Экран
    public static boolean sound_spoiler_open= false; // Состояние спойлера отвечающего за звук
    public static boolean search_spoiler_open= false; // Состояние спойлера отвечающего за выбор книг
    public static boolean speed_spoiler_open= false; // Состояние спойлера отвечающего за скорость
    public static boolean time_spoiler_open= false; // Состояние спойлера отвечающего за время
    public static boolean interface_spoiler_open= false; // Состояние спойлера отвечающего за интерфейс
    public static boolean themes_spoiler_open= false; // Состояние спойлера отвечающего за темы
    public static boolean buttonsCustomizing_spoiler_open= false; // Состояние спойлера отвечающего за настройку кнопок
    public static boolean rootFolder_spoiler_open= false; // Состояние спойлера отвечающего за папку для аудиокниг
    public static boolean fast_rewind_spoiler_open= false; // Состояние спойлера отвечающего за "красные" значения перемотки и отката
    public static boolean folderVisibility_spoiler_open= false; // Состояние спойлера отвечающего за видимость папок

    public static class Action_Element {            // навигационные действия пользователя
        public int action_type;                     // тип действия
        public int track,                           // номер файла в книге
                   offset;                          // смещение от начала файла перед началом действия


        public static int ACTION_PLAY = 1;
        public static int ACTION_FF = 2;
        public static int ACTION_REW = 3;
        public static int ACTION_SN = 4;
        public static int ACTION_SP = 5;
        public static int ACTION_DAT = 6;
        // Типы действий
        // 01 - play
        // 02 - fast forward
        // 03 - rewind
        // 04 - skip to next track
        // 05 - skip to previous track
        // 06 - directly access to track

        public Action_Element(int action_type, int track, int offset) {
            this.action_type= action_type;
            this.offset= offset;
            this.track= track;
        }
    }
    public static List<Action_Element> actionsList = new ArrayList<Action_Element>();
    public static boolean loudness_init= true;          // Признак успешной инициализации доп громкости

    @SuppressLint("ClickableViewAccessibility")
    public void initViews() throws IOException {

        testBrightness();
        appContext= MainActivity.this;

        // запрос разрешения на чтение медиа-файлов
        if (!checkAndRequestPermissions()) {
            intent = new Intent(this, PermissionScreen.class);
            this.startActivityForResult(intent, PERMISSIONS_REQUEST_CODE);
        }

        its_tablet= checkIsTablet();

        total_line= findViewById(R.id.total_line);
        buttonPlayPause = (MaterialButton) findViewById(com.audiobook.pbp_service.R.id.PlayPauseButton);
        buttonPlayPause.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        buttonPrev = (MaterialButton) findViewById (com.audiobook.pbp_service.R.id.skipToPreviousButton);
        buttonPrev.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        buttonNext = (MaterialButton) findViewById (com.audiobook.pbp_service.R.id.skipToNextButton);
        buttonNext.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        buttonPrev10 = (MaterialButton) findViewById (com.audiobook.pbp_service.R.id.back10);
        buttonPrev10.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        buttonNext10 = (MaterialButton) findViewById (com.audiobook.pbp_service.R.id.fwd10);

        buttonNext10.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        speed_down_button = (Button) findViewById (R.id.speedDownButton);
        speed_down_button.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        speed_up_button = (Button) findViewById (R.id.speed_up_button);
        speed_up_button.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        buttonRepeat= (ImageButton) findViewById(R.id.repeat_button);
        buttonRepeat.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        favoritButton= (ImageButton) findViewById(R.id.favoriteButton);
        favoritButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        touchButton= (ImageButton) findViewById(R.id.touch_button);
        touchButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        addBookmarkButton= (ImageButton) findViewById(R.id.bookmarkAddButton);
        addBookmarkButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        partSelectButton= (ImageButton) findViewById(R.id.parts_select_button);
        partSelectButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        menuButton= (ImageButton) findViewById(R.id.menu_button);
        menuButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));

        if (repeat_state == NO_REPEAT) {
            if (button_color== BLACK)
                buttonRepeat.setImageResource(R.drawable.ic_repeat_off_white);
            else
                buttonRepeat.setImageResource(R.drawable.ic_repeat_off);
        }
        if (repeat_state == TRACK_REPEAT) {
            buttonRepeat.setImageResource(R.drawable.ic_repeat_track_on);
        }
        if (repeat_state == BOOK_REPEAT) {
            buttonRepeat.setImageResource(R.drawable.ic_repeat_book_on);
        }
        if (repeat_state == BOOKMARK_REPEAT) {
            if (Bookmarks.bookmark_repeate_mode)
                buttonRepeat.setImageResource(R.drawable.ic_repeat_bookmarks);
            if (!Bookmarks.bookmark_repeate_mode)
                repeat_state= NO_REPEAT;
        }
        buttonSleepTimer= (MaterialButton) findViewById(R.id.sleep_timer_button);
        buttonSleepTimer.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        if (sleep_timer_parts  ||  sleep_timer_time) {
            buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_24);
            if (sleep_timer_repeat) {
                buttonSleepTimer.setText("R");
            }
            else
                buttonSleepTimer.setText("");
        }
        else {
            buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_off_24);
            if (sleep_timer_repeat) {
                buttonSleepTimer.setText("R");
            }
            else
                buttonSleepTimer.setText("");
        }
        buttonEqualizer= (ImageButton) findViewById(R.id.equalizer_button);
        buttonEqualizer.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        buttonShare= (ImageButton) findViewById(R.id.share_button);
        buttonShare.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        buttonUndo= (ImageButton) findViewById(R.id.undo_moving);
        buttonUndo.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        if (!equalizer_set) {
            if (button_color== BLACK)
                buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active_white);
            else
                buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active);
        }
        else {
            buttonEqualizer.setImageResource(R.drawable.ic_equalizer_button);
        }

        int night_mode= AppCompatDelegate.getDefaultNightMode();
        if (button_color== 0) {
            if (night_mode == 2)
                button_color = getResources().getColor(R.color.white, null);
            else
                button_color = getResources().getColor(R.color.purple_500, null);
        }

        if (night_mode== 2  &&  button_color== getResources().getColor(R.color.purple_500, null))
            button_color = getResources().getColor(R.color.purple_200, null);
        if (night_mode!= 2  &&  button_color== getResources().getColor(R.color.purple_200, null))
            button_color = getResources().getColor(R.color.purple_500, null);

        ColorStateList colorSt= null;
        int default_col= 0;
        colorSt = buttonSleepTimer.getTextColors();
        default_col= colorSt.getDefaultColor();
        if (night_mode== 2  ||  (default_col <= 1644167176  &&  default_col> 1627389952)) {
            if (button_color== getResources().getColor(R.color.black, null)) {
                button_color = getResources().getColor(R.color.white, null);
            }
        }
        else {
            if (button_color== getResources().getColor(R.color.white, null)) {
                button_color = getResources().getColor(R.color.black, null);
            }
        }

        if (button_color!= 0) {
            buttonPlayPause.setBackgroundColor(button_color);
            buttonPrev.setBackgroundColor(button_color);
            buttonNext.setBackgroundColor(button_color);
            buttonPrev10.setBackgroundColor(button_color);
            buttonNext10.setBackgroundColor(button_color);
            speed_down_button.setBackgroundColor(button_color);
            speed_up_button.setBackgroundColor(button_color);
            buttonRepeat.setBackgroundColor(button_color);
            buttonSleepTimer.setBackgroundColor(button_color);
            buttonEqualizer.setBackgroundColor(button_color);
            buttonShare.setBackgroundColor(button_color);
            buttonUndo.setBackgroundColor(button_color);
            favoritButton.setBackgroundColor(button_color);
            touchButton.setBackgroundColor(button_color);
            addBookmarkButton.setBackgroundColor(button_color);
            partSelectButton.setBackgroundColor(button_color);
            menuButton.setBackgroundColor(button_color);
        }
        if (button_color== BLACK) {
            partSelectButton.setImageResource(R.drawable.ic_parts_direct_select_white);
            menuButton.setImageResource(R.drawable.ic_menu_white);
            favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_white);
            buttonRepeat.setImageResource(R.drawable.ic_repeat_off_white);
            addBookmarkButton.setImageResource(R.drawable.ic_bookmark_add_white);
            buttonShare.setImageResource(R.drawable.ic_share_24_white);
            buttonUndo.setImageResource(R.drawable.ic_undo_moving_white);
        }

        screenSize= getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        Configuration config = getResources().getConfiguration();

        if (!(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
            if (config.screenHeightDp <= 580) {
                if (buttons_size == 0) {
                    buttons_size = 106;
                }
            }
        }
        if (!(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
            if ((!one_finger_right_interface && !one_finger_left_interface)
                    || config.screenHeightDp <= 569) {
//                Toast.makeText(MainActivity.this, String.format("config.screenHeightDp= %d", config.screenHeightDp), Toast.LENGTH_LONG).show();
                if (started_icon_size == 0) {
                    started_icon_size = buttonPlayPause.getIconSize();
                }
                double iCond = started_icon_size;
                if (buttons_size > 0) {
                    iCond /= ((double) (buttons_size + 100) / 100);
                } else {
                    iCond = started_icon_size;
                }
                int iSize = (int) iCond;
//                Toast.makeText(MainActivity.this, String.format("iCond= %f", iCond), Toast.LENGTH_LONG).show();
                buttonPlayPause.setIconSize(iSize);
                buttonPrev.setIconSize(iSize);
                buttonNext.setIconSize(iSize);
                buttonPrev10.setIconSize(iSize);
                buttonNext10.setIconSize(iSize);
            }
        }

        headerText = (TextView) findViewById(com.audiobook.pbp_service.R.id.headerText);
        sounded_hours = (TextView) findViewById(R.id.sounded_hours);
        hours_total_text= (TextView) findViewById(R.id.hours_total_text);
        future_hours= (TextView) findViewById(R.id.future_hours);
        totalDur= (TextView) findViewById(R.id.totalDuration);
        seekBar_part = (SeekBar) findViewById(com.audiobook.pbp_service.R.id.seekBar_part_place);
        // Расстановка вертикальных полосок на месте закладок
        surface = (SurfaceView) findViewById(R.id.surface);
        backImage= (ImageView) findViewById(com.audiobook.pbp_service.R.id.imageView);
        // доп. громкость
        addVolumeControl = (SeekBar) findViewById(R.id.addVolume);
        ad_volume= (TextView) findViewById(R.id.ad_volume);
        speed_view= (TextView) findViewById(R.id.speed_view);
        speed_view_text= (TextView) findViewById(R.id.speed_view_text);
        track_out_of= (TextView) findViewById(R.id.track_out_of);
        passedView = (TextView) findViewById(R.id.passedView);
        curTrack = (TextView) findViewById(com.audiobook.pbp_service.R.id.curTrack);
        maxTrack = (TextView) findViewById(com.audiobook.pbp_service.R.id.maxTrack);
        totalView = (TextView) findViewById(R.id.totalView);
        sleep_timer_textView = (TextView) findViewById(R.id.sleep_timer_textView);
        sleep_timer_show = (TextView) findViewById(R.id.sleep_timer_show);
        sleep_parts_show = (TextView) findViewById(R.id.sleep_parts_show);
        leftView = (TextView) findViewById(R.id.leftView);

        if (night_mode== 2  ||  (default_col <= 1644167176  &&  default_col> 1627389952)) {
            ad_volume.setTextColor(Color.WHITE);
            speed_view.setTextColor(Color.WHITE);
            speed_view_text.setTextColor(Color.WHITE);
            track_out_of.setTextColor(Color.WHITE);
            passedView.setTextColor(Color.WHITE);
            curTrack.setTextColor(Color.WHITE);
            maxTrack.setTextColor(Color.WHITE);
            totalView.setTextColor(Color.WHITE);
            sleep_timer_textView.setTextColor(Color.WHITE);
            sleep_timer_show.setTextColor(Color.WHITE);
            leftView.setTextColor(Color.WHITE);
            sounded_hours.setTextColor(Color.WHITE);
            hours_total_text.setTextColor(Color.WHITE);
            totalDur.setTextColor(Color.WHITE);
            future_hours.setTextColor(Color.WHITE);
        }
        else {
            ad_volume.setTextColor(Color.BLACK);
            speed_view.setTextColor(Color.BLACK);
            speed_view_text.setTextColor(Color.BLACK);
            track_out_of.setTextColor(Color.BLACK);
            passedView.setTextColor(Color.BLACK);
            curTrack.setTextColor(Color.BLACK);
            maxTrack.setTextColor(Color.BLACK);
            totalView.setTextColor(Color.BLACK);
            sleep_timer_textView.setTextColor(Color.BLACK);
            sleep_timer_show.setTextColor(Color.BLACK);
            sleep_parts_show.setTextColor(Color.BLACK);
            leftView.setTextColor(Color.BLACK);
            sounded_hours.setTextColor(Color.BLACK);
            hours_total_text.setTextColor(Color.BLACK);
            totalDur.setTextColor(Color.BLACK);
            future_hours.setTextColor(Color.BLACK);

        }

        show_progress =  (SeekBar) findViewById(R.id.reading_progress);
        speed_control =  (SeekBar) findViewById(R.id.speed_control);
        addVolumeControl.setMax(6226);
        radio_0sec= (RadioButton) findViewById(R.id.radioButton_0sec);
        radio_2sec= (RadioButton) findViewById(R.id.radioButton_2sec);
        radio_4sec= (RadioButton) findViewById(R.id.radioButton_4sec);
        radio_6sec= (RadioButton) findViewById(R.id.radioButton_6sec);
        radio_6sec.setText("" + (back_in_time_cust+ 6));
        radio_jump15= (RadioButton) findViewById(R.id.radioButton_jump15);
        radio_jump60= (RadioButton) findViewById(R.id.radioButton_jump60);
        radio_jump60.setText(""+ (fast_moving_cust* 5));
        ColorStateList colorStateListRadio = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, //disabled
                        new int[]{android.R.attr.state_checked} //enabled
                },
                new int[] {
                        GRAY, //disabled
                        button_color //enabled
                }
        );
        ColorStateList colorStateListRadio_custom = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, //disabled
                        new int[]{android.R.attr.state_checked} //enabled
                },
                new int[] {
                        GRAY & RED, //disabled
                        RED //enabled
                }
        );

        radio_0sec.setButtonTintList(colorStateListRadio);
        radio_2sec.setButtonTintList(colorStateListRadio);
        radio_4sec.setButtonTintList(colorStateListRadio);
        radio_4sec.invalidate();
        radio_6sec.setButtonTintList(colorStateListRadio_custom);
        radio_jump15.setButtonTintList(colorStateListRadio);
        radio_jump60.setButtonTintList(colorStateListRadio_custom);
        radio_0sec.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        radio_2sec.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        radio_4sec.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        radio_6sec.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        radio_jump15.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        radio_jump60.setAlpha((float) ((float) buttonsTransparency/ 6.0));

        if (buttonsTransparency>= 3) {
            if (button_color== WHITE)
                seekBarTransparency = (float) ((buttonsTransparency- 2) / 6.0);
            else
                seekBarTransparency = (float) ((buttonsTransparency) / 6.0);
            if (seekBarTransparency < 0)
                seekBarTransparency = 0;
        }
        else {
            seekBarTransparency= (float) (buttonsTransparency/ 6.0);
        }

        if (button_color!= 0) {
            ColorStateList tintMe;
            tintMe= ColorStateList.valueOf(button_color);
            seekBar_part.setThumbTintList(tintMe);
            seekBar_part.setAlpha((float) (seekBarTransparency));
            addVolumeControl.setThumbTintList(tintMe);
            addVolumeControl.setAlpha((float) (seekBarTransparency));
            speed_control.setThumbTintList(tintMe);
            speed_control.setMax((int)MAX_PLAY_SPEED);
            show_progress.setThumbTintList(tintMe);
            show_progress.setAlpha((float) (seekBarTransparency));
            speed_control.setAlpha((float) (seekBarTransparency));
        }

// проговаривать текущее время при нажатии плэй / стоп
        if (time_speaking_cfg) {
            TTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override public void onInit(int initStatus) {
                    if (initStatus == TextToSpeech.SUCCESS) {
                        if (TTS.isLanguageAvailable(new Locale(Locale.getDefault().getLanguage()))
                                == TextToSpeech.LANG_AVAILABLE) {
                            TTS.setLanguage(new Locale(Locale.getDefault().getLanguage()));
                        } else {
                            TTS.setLanguage(Locale.US);
                        }
                        TTS.setPitch(1.3f);
                        ttsEnabled = true;
                    } else if (initStatus == TextToSpeech.ERROR) {
//                        Toast.makeText(PagerActivity.this, R.string.tts_error, Toast.LENGTH_LONG).show();
                        ttsEnabled = false;
                    }
                }
            });
        }

        addVolumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (loudness != null)
                        loudness.setTargetGain((int) progress);
                    curTG = progress;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

//----------------------------------------------------------
        if (callback == null) {
            callback = new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (state == null)
                        return;
                    boolean playing = state.getState() == PlaybackStateCompat.STATE_PLAYING;
                }
            };
        }

        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    playerServiceBinder = (com.audiobook.pbp_service.service.PlayerService.PlayerServiceBinder) service;
                    mediaController = new MediaControllerCompat(MainActivity.this, playerServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(callback);
                    callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                    if (play_after_customizing  ||  non_stop_after_change_orientation) {
                        if (zero_back_in_time_after_change_orient_cfg)
                            simply_play= true;
                        if (play_after_customizing) {
                            play_after_customizing = false;
                            mediaController.getTransportControls().play();
                        }
                        else {
                            if (playing== 1) {
                                mediaController.getTransportControls().play();
                            }
                        }
                    }
                    else {
                        mediaController.getTransportControls().prepare();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    playerServiceBinder = null;
                    if (mediaController != null) {
                        mediaController.unregisterCallback(callback);
                        mediaController = null;
                    }
                }

            };
        }
        bindService(new Intent(this, com.audiobook.pbp_service.service.PlayerService.class), serviceConnection, BIND_AUTO_CREATE);

//-----------------------------------------

        if (speed_step_005) {
            speed_view.setText(String.format("%1.2f", (speed_play + 1) / 5));
        } else {
            speed_view.setText(String.format("%1.1f", (speed_play + 1) / 5));
        }
        if (selAlbum != null  &&  selAlbum.length() > 0) {  // заход после выбора новой книги
            existInFavorShow(selAlbum, current_directory);
            headerText.setText(selAlbum);                   // Book name show  + "\nВсего трэков: " + SoundScreen.musicList.size()
            buttonPlayPause.setEnabled(true);
            buttonPlayPause.setBackgroundColor(button_color);
            if (hand_changed == 0) {
                lesson = 0;                                      // init for new book
                try {
                    oneFileRead (selAlbum);
                } catch (FileNotFoundException e) {
                    System.out.println("Book file not found");
                }
            }
            seekBar_part.setProgress(lesson_point[lesson]);
        }
        else {
            if (no_change_album == 0)  {// новый запуск
                lesson_point = new int[LESSON_MAX+ 1];
                buttonPlayPause.setEnabled(false);
                buttonNext10.setEnabled(false);
                buttonPrev10.setEnabled(false);
                buttonPlayPause.setBackgroundColor(GRAY);
                buttonNext10.setBackgroundColor(GRAY);
                buttonPrev10.setBackgroundColor(GRAY);
                try {
                    if (oneFileRead (WORK_FILE_NAME)> 0) {
                        buttonPlayPause.setEnabled(true);
                        buttonNext10.setEnabled(true);
                        buttonPrev10.setEnabled(true);
                        buttonPlayPause.setBackgroundColor(button_color);
                        if (button_color!= 0) {
                            buttonPlayPause.setBackgroundColor(button_color);
                            buttonNext10.setBackgroundColor(button_color);
                            buttonPrev10.setBackgroundColor(button_color);
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Book file not found");
                }
                if (!selAlbum.isEmpty() &&  selAlbum!= null) {
                        headerText.setText(selAlbum); // + "\nВсего трэков: " + SoundScreen.musicList.size()
                    existInFavorShow(selAlbum, current_directory);
                }
                else {
                        headerText.setText(R.string.book_not_sel);
                }
            }
        }
        if (SoundScreen.musicList.size()> 1) {
            show_progress.setMax(SoundScreen.musicList.size() - 1);
        }
        else {
            show_progress.setMax(1);
        }

        if (lesson> 0)
            show_progress.setProgress(lesson- 1);
        else
            show_progress.setProgress(0);
        radio_6sec.setText("" + (back_in_time_cust+ 6));
        if (hand_changed > 0  &&  mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying())
                    buttonPlayPause.setIconResource(R.drawable.ic_media_pause);
                else
                    buttonPlayPause.setIconResource(R.drawable.ic_media_play);
            }
            catch (IllegalStateException e) {
                Log.e("Exception", "SAN:Mediaplayer get status failed: " + e.toString());
            }
        }

        if (lesson == 0) {
            for (int i= 0; i< LESSON_MAX; i++)
                lesson_point[i] = 0;
        }
        if (hand_changed == 0)
            lesson_point[0]= lesson;  // количество частей

        // управление аудиофокусом
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        track_name = (TextView) findViewById(R.id.part_name_view);


        if (lesson == 0) {
            lesson = 1;
        }

        if (back_in_time == 0) {
            radio_0sec.setChecked(true);
        }
        if (back_in_time == 2) {
            radio_2sec.setChecked(true);
        }
        if (back_in_time == 4) {
            radio_4sec.setChecked(true);
        }
        if (back_in_time >= 6) {
            radio_6sec.setChecked(true);
            radio_6sec.setText("" + (back_in_time_cust+ 6));
        }

        if (jump_weight  == 15) {
            radio_jump15.setChecked(true);
            radio_jump60.setChecked(false);
        }
        if (jump_weight  != 15) {
            radio_jump60.setChecked(true);
            radio_jump60.setText(""+ fast_moving_cust* 5);
            radio_jump15.setChecked(false);
        }
        show_totalDuration();
        trackChange();

        if (hand_changed != 0)
            hand_changed = 0;

        ErrorReporter.PhoneModel = android.os.Build.MODEL;
        ErrorReporter.AndroidVersion = android.os.Build.VERSION.RELEASE;
        ErrorReporter.Brand  = android.os.Build.BRAND;

        if (com.audiobook.pbp_service.SoundScreen.musicList.size()> 0) {
            if (mediaPlayer != null)
                seekBar_part.setMax(mediaPlayer.getDuration());

// навигация между треками
            show_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        PlayerService.aElement= new MainActivity.Action_Element(MainActivity.Action_Element.ACTION_PLAY, lesson, lesson_point[lesson]);
                        MainActivity.actionsList.add(PlayerService.aElement);
                        if (SoundScreen.musicList.size()> 1) {
                            if (mediaPlayer != null)
                                lesson_point[lesson] = mediaPlayer.getCurrentPosition();    // save the current position in file
                            else
                                lesson_point[lesson] = seekBar.getProgress();             // save the current position in file
                            lesson = progress + 1;
                            show_sounded_hours();  // прошло времени для всей книги
                            show_future_hours();   // осталось времени для всей книги
                            show_totalTime_track(); // длительность текущего трэка
                            show_current_track_number(); // вывод номера текущего трэка
                            show_current_track_name(); // вывод имени текущего трэка или текущей закладки
                        }
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (SoundScreen.musicList.size()> 1) {
                        int lesson2 = seekBar.getProgress() + 1;
                        Bookmarks.need_refresh_BMs= true;
                        mediaController.getTransportControls().playFromMediaId(String.valueOf(lesson2), null);
                    }
                }
            });

// навигация по треку
            seekBar_part.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        lesson_point[lesson] = seekBar.getProgress();
                        PlayerService.aElement= new MainActivity.Action_Element(MainActivity.Action_Element.ACTION_PLAY, lesson, lesson_point[lesson]);
                        MainActivity.actionsList.add(PlayerService.aElement);
                        if (mediaPlayer != null)
                            mediaPlayer.seekTo(lesson_point[lesson]);
                        show_passed_left_view();
                        show_future_hours();
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
//                    PlayerService.aElement= new MainActivity.Action_Element(MainActivity.Action_Element.ACTION_PLAY, lesson, lesson_point[lesson]);
//                    MainActivity.actionsList.add(PlayerService.aElement);
                    lesson_point[lesson]= seekBar.getProgress();
                    if (mediaPlayer!= null)
                        mediaPlayer.seekTo(lesson_point[lesson]);
                    show_passed_left_view ();
                    show_future_hours ();
                    if (one_file_and_BMs) {
                        oneFileAndBMs_KeysDraw();
                        show_current_track_name();
                    }
                    writeToFile();
                }
            });

            if (mediaPlayer != null  &&  lesson_point[lesson] > 0) {
                mediaPlayer.seekTo(lesson_point[lesson]);  // HERE
                seekBar_part.setProgress(lesson_point[lesson]);
            }
            showLessonNum();

            try {
                showEmbeddedImage();
            } catch (IOException e) {
                Log.e("Exception", "Show embedded Image  failed: " + e.toString());
            }
        }
        else {
            showLessonNum();
            showEmbeddedImage();
        }

        touch_mode_draw();

        surface.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                surface.setZOrderOnTop(true);
                surface.getHolder().setFormat(PixelFormat.TRANSPARENT);
                // Do some drawing when surface is ready
                canvas = holder.lockCanvas();
                if (canvas== null)
                    return;
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (SoundScreen.musicList.size()== 0)
                    return;
                try {
                    if (!Bookmarks.get_bm_file()) {
                        Bookmarks.bookMarkList.clear();
                        Bookmarks.bookMarkList.add(new Bookmarks.bookMarkElement(SoundScreen.musicList.get(0).name, 1, 0, Bookmarks.BOOKMARK_TYPE_CUSTOM, false));
                        try {
                            write_book_marks();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                surf_width= width;
                surf_height= height;
                canvas = holder.lockCanvas();

                if (canvas== null)
                    return;
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                if (Bookmarks.bookMarkList == null)
                    return;
                if (Bookmarks.bookMarkList.size()== 0)
                    return;

                Paint miPaint = new Paint();
                // толщина линии = 6
                miPaint.setStrokeWidth(6);
                int loc_dur= loc_dur = SoundScreen.musicList.get(lesson- 1).duration;
                int loc_diff= loc_dur / surf_width;
                for (int i = 0; i< Bookmarks.bookMarkList.size()
                        &&  lesson >= Bookmarks.bookMarkList.get(i).lesson; i++) {
                    if (lesson > Bookmarks.bookMarkList.get(i).lesson)
                        continue;
                    int loc_off= Bookmarks.bookMarkList.get(i).offset / loc_diff;
                    if (Bookmarks.bookMarkList.get(i).type_bm== Bookmarks.BOOKMARK_TYPE_CUSTOM)
                        miPaint.setColor(Color.GREEN);
                    if (Bookmarks.bookMarkList.get(i).type_bm== Bookmarks.BOOKMARK_TYPE_CONTENT)
                        miPaint.setColor(Color.BLUE);
                    loc_off+= loc_off_diff;
                    if (loc_off> surf_width)
                        loc_off= surf_width - loc_off_diff;
                    canvas.drawLine(loc_off, surf_height, loc_off, 0, miPaint);
                }

                holder.unlockCanvasAndPost(canvas);
                one_file_and_BMs= one_file_and_BMs_test();
                oneFileAndBMs_KeysDraw();
            }
        });

        radio_0sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                back_in_time= 0;
            }
        });

        radio_2sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                back_in_time= 2;
            }
        });

        radio_4sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                back_in_time= 4;
            }
        });

        radio_6sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                back_in_time= back_in_time_cust+ 6;
            }
        });

        radio_jump15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                jump_weight= 15;
            }
        });

        radio_jump60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                jump_weight= fast_moving_cust* 5;
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                if (repeat_state == TRACK_REPEAT) {
                    repeat_state = NO_REPEAT;
                    if (button_color== BLACK)
                        buttonRepeat.setImageResource(R.drawable.ic_repeat_off_white);
                    else
                        buttonRepeat.setImageResource(R.drawable.ic_repeat_off);
                }
                std_keys = true;
                if (mediaController != null)
                    mediaController.getTransportControls().skipToNext();
            }
        });

        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                if (repeat_state== TRACK_REPEAT) {
                    repeat_state = NO_REPEAT;
//                    buttonRepeat.setText(R.string.repeat_no_button_text);
                    if (button_color== BLACK)
                        buttonRepeat.setImageResource(R.drawable.ic_repeat_off_white);
                    else
                        buttonRepeat.setImageResource(R.drawable.ic_repeat_off);
                }
                std_keys= true;
                mediaController.getTransportControls().skipToPrevious();
            }
        });

// correction play position to minus 10 secs
        buttonPrev10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                std_keys= true;
                if (mediaController!= null)
                    mediaController.getTransportControls().rewind();
            }
        });

// correction play position to plus 10 secs
        buttonNext10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                std_keys= true;
                if (mediaController!= null)
                    mediaController.getTransportControls().fastForward();
            }
        });

        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                miPlayAndPause();
            }
        });

        speed_control.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                speed_play = (float) speed_control.getProgress();
                if (mediaPlayer!= null  &&  !SoundScreen.musicList.isEmpty()) {
                    playbackParams = mediaPlayer.getPlaybackParams();
                    playbackParams.allowDefaults();
                }
                playbackParams.setSpeed((speed_play + 1) / 5);
                if (speed_step_005) {
                    speed_view.setText(String.format("%1.2f", (speed_play + 1) / 5));
                } else {
                    speed_view.setText(String.format("%1.1f", (speed_play + 1) / 5));
                }
                if (SoundScreen.musicList.size()> 0) {
                    if (playing == 1) {
                        playbackParams= mediaPlayer.getPlaybackParams();
                        playbackParams.allowDefaults();
                        playbackParams.setSpeed((speed_play + 1) / 5);
                        try {
                            mediaPlayer.setPlaybackParams(playbackParams);
                        } catch (Exception e) {
                            Log.e("Exception", "Mediaplayer change speed failed: " + e.toString());
                        }
                    }
                    show_totalDuration();
                    show_totalTime_track(); // длительность текущего трэка
                    show_sounded_hours();
                    show_passed_left_view();
                    show_future_hours();
                }
                writeConfig();
                return;

            }
        });

        speed_down_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                if (speed_play > 0) {
                    if (speed_step_005) {
                        speed_play -= 0.25;
                    }
                    if (!speed_step_005){
                        speed_play -= 0.5;
                    }
                }
                if (speed_play< 0) speed_play = 0;
                speed_control.setProgress((int) speed_play);
                if (mediaPlayer!= null  &&  !SoundScreen.musicList.isEmpty()) {
                    playbackParams = mediaPlayer.getPlaybackParams();
                    playbackParams.allowDefaults();
                }
                playbackParams.setSpeed((speed_play + 1) / 5);
                if (SoundScreen.musicList.size()> 0) {
                    if (mediaPlayer.isPlaying()) {
                        try {
                            mediaPlayer.setPlaybackParams(playbackParams);
                        } catch (Exception e) {
                            Log.e("Exception", "Mediaplayer change speed failed: " + e.toString());
                        }
                    }
                    show_totalDuration();
                    show_totalTime_track(); // длительность текущего трэка
                    show_sounded_hours();
                    show_passed_left_view();
                    show_future_hours();
                }
                if (speed_step_005) {
                    speed_view.setText(String.format("%1.2f", (speed_play + 1) / 5));
                } else {
                    speed_view.setText(String.format("%1.1f", (speed_play + 1) / 5));
                }

                writeConfig();
            }
        });

        speed_up_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                if (speed_play< MAX_PLAY_SPEED) {
                    if (speed_step_005) {
                        speed_play += 0.25;
                    }
                    if (!speed_step_005) {
                        speed_play += 0.5;
                    }
                }
                if (speed_play> MAX_PLAY_SPEED) speed_play = MAX_PLAY_SPEED;
                speed_control.setProgress((int) speed_play);
                if (mediaPlayer!= null  &&  !SoundScreen.musicList.isEmpty()) {
                    playbackParams = mediaPlayer.getPlaybackParams();
                    playbackParams.allowDefaults();
                }
                playbackParams.setSpeed((speed_play+ 1)/ 5);
                if (playbackParams.getSpeed()> 3)
                    playbackParams.setSpeed(3);
                if (SoundScreen.musicList.size()> 0) {
                    if (mediaPlayer.isPlaying()) {
                        try {
                            mediaPlayer.setPlaybackParams(playbackParams);
                        } catch (Exception e) {
                            Log.e("Exception", "Mediaplayer change speed failed: " + e.toString());
                        }
                    }
                    show_totalDuration();
                    show_totalTime_track(); // длительность текущего трэка
                    show_sounded_hours();
                    show_passed_left_view();
                    show_future_hours();
                }
                if (speed_step_005) {
                    speed_view.setText(String.format("%1.2f", (speed_play + 1) / 5));
                } else {
                    speed_view.setText(String.format("%1.1f", (speed_play + 1) / 5));
                }
                writeConfig();

/* смена локали
                String languageToLoad  = "ru"; //  language
                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.setLocale(locale);
                getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
                recreate();
 */
            }
        });

        buttonRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                repeat_state++;
                if (repeat_state> BOOK_REPEAT  &&  !Bookmarks.bookmark_repeate_mode) {
                    repeat_state = NO_REPEAT;
                }
                if (repeat_state> BOOKMARK_REPEAT  &&  Bookmarks.bookmark_repeate_mode) {
                    repeat_state = NO_REPEAT;
                }
                if (repeat_state == NO_REPEAT) {
                    if (button_color== BLACK)
                        buttonRepeat.setImageResource(R.drawable.ic_repeat_off_white);
                    else
                        buttonRepeat.setImageResource(R.drawable.ic_repeat_off);
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.repeat_no_button_text), Toast.LENGTH_LONG).show();
                }
                if (repeat_state == TRACK_REPEAT) {
                    buttonRepeat.setImageResource(R.drawable.ic_repeat_track_on);
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.repeat_track_button_text), Toast.LENGTH_LONG).show();

                }
                if (repeat_state == BOOK_REPEAT) {
                    buttonRepeat.setImageResource(R.drawable.ic_repeat_book_on);
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.repeat_book_button_text), Toast.LENGTH_LONG).show();
                }
                if (repeat_state == BOOKMARK_REPEAT) {
                    buttonRepeat.setImageResource(R.drawable.ic_repeat_bookmarks);
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.repeat_bookmark_button_text), Toast.LENGTH_LONG).show();
                }

            }
        });

        buttonSleepTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                if (sleepTimerPressCounter == 0) {
                    if (!sleep_timer_time && !sleep_timer_parts) {
                        if (sleep_timer_time_value_mem > 0) {
                            sleep_timer_time_value = sleep_timer_time_value_mem;
                            local_sleep = 0;
                            sleep_timer_time = true;
                        }
                        if (sleep_timer_parts_value_mem > 0) {
                            sleep_timer_parts_value = sleep_timer_parts_value_mem - 1;
                            local_sleep = 0;
                            sleep_timer_parts = true;
                        }
                    }
                }
                if (sleepTimerPressCounter == 2) {
                    if (sleep_timer_time || sleep_timer_parts)  {
                        if (sleep_timer_time_value_mem > 0) {
                            sleep_timer_time_value = 0;
                            local_sleep = 0;
                            sleep_timer_time = false;
                        }
                        if (sleep_timer_parts_value_mem > 0) {
                            sleep_timer_parts_value = 0;
                            local_sleep = 0;
                            sleep_timer_parts = false;
                        }
                    }
                }
                if (sleepTimerPressCounter == 1) {
                    sleep_timer_repeat = true;
                }
                if (sleep_timer_time || sleep_timer_parts) {
                    buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_24);
                    if (sleep_timer_repeat) {
                        buttonSleepTimer.setText("R");
                    }
                    else
                        buttonSleepTimer.setText("");
                }
                if (!sleep_timer_time && !sleep_timer_parts) {
                    buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_off_24);
                    sleep_timer_repeat = false;
                    buttonSleepTimer.setText("");
                }
                if (sleep_timer_parts_value_mem == 0 && sleep_timer_time_value_mem == 0) {
                    Toast.makeText(MainActivity.this, R.string.sleep_timer_not_actived, Toast.LENGTH_LONG).show();
                }
                else {
                    showLessonNum();
                }
                if (sleepTimerPressCounter== 0) {
                    sleepTimerPressCounter= 1;
                    return;
                }
                if (sleepTimerPressCounter== 1) {
                    sleepTimerPressCounter= 2;
                    return;
                }
                if (sleepTimerPressCounter== 2) {
                    sleepTimerPressCounter= 0;
                    return;
                }
            }
        });

        buttonEqualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                writeToFile();
                if (mediaPlayer== null)
                    newMediaPlayer(SoundScreen.musicList.get(lesson- 1).uri);
                intent = new Intent(appContext, com.audiobook.pbp_service.SimpleEqualizer.class);
                startActivityForResult(intent, EQUALIZER_CODE);
            }
        });


        buttonUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionsList.size()> 0) {
                    lesson= actionsList.get(actionsList.size()- 1).track;
                    lesson_point[lesson]= actionsList.get(actionsList.size()- 1).offset;
                    actionsList.remove(actionsList.size()- 1);
                    mediaPlayer.seekTo(lesson_point[lesson]);
                    seekBar_part.setProgress(lesson_point[lesson]);
                    one_time_no_time_speak=true;
                    mediaController.getTransportControls().pause();
                    writeToFile();
                    if (pre_lesson != lesson) {
                        show_progress.setProgress(lesson- 1);
                        pre_lesson= lesson;
                    }
                    show_sounded_hours();  // прошло времени для всей книги
                    show_future_hours();   // осталось времени для всей книги
                    show_totalTime_track(); // длительность текущего трэка
                    show_current_track_number(); // вывод номера текущего трэка
                    show_current_track_name(); // вывод имени текущего трэка или текущей закладки
                    show_passed_left_view ();  // прошло времени для текущего трека
                    if (one_file_and_BMs) {
                        oneFileAndBMs_KeysDraw();
                    }
                }
                return;
            }
        });

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                if (selAlbum != null  &&  selAlbum.length() > 0) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, SoundScreen.musicList.get(lesson - 1).uri);
                    sendIntent.setType("audio/*");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.menu_choose_text)));
                }
                else {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.no_book_for_sharing), Toast.LENGTH_LONG).show();
                }
            }
        });

        favoritButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    if (existInFavorShow(selAlbum, current_directory)) {
                        delOneFavoriteShow(selAlbum, current_directory);
                    } else {
                        addToFavoritesShow(selAlbum, current_directory);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        addBookmarkButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                    ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return false;
                }
                intent = new Intent(appContext, com.audiobook.pbp_service.Bookmarks.class);
                startActivityForResult(intent, BOOKMARKS_CODE);
                return false;
            }
        });

        addBookmarkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (not_touch_mode== ALL_TOUCH_DISABLED
                            ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                            Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (SoundScreen.musicList.size()== 0)
                            return;
                        addBookmarkButton.setEnabled(false);
                        Bookmarks.ArrayBMs.clear();
                        try {
                            Bookmarks.get_bm_file();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        int position;
                        if (mediaPlayer != null) {
                            position = mediaPlayer.getCurrentPosition();
                        } else {
                            position = seekBar_part.getProgress();
                        }
                        Bookmarks.bookMarkList.add(new Bookmarks.bookMarkElement(SoundScreen.musicList.get(lesson - 1).name, lesson, position, Bookmarks.BOOKMARK_TYPE_CUSTOM, false));
                        try {
                            write_book_marks();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        addBookmarkButton.setEnabled(true);
                        Toast.makeText(appContext, appContext.getResources().getString(R.string.bookmark_created_text), Toast.LENGTH_LONG).show();
                        bookmarks_draw();
                    }
                });

        partSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                    ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                if (SoundScreen.musicList.size()== 0)
                    return;
                partSelectButton.setEnabled(false);
                intent = new Intent(appContext, com.audiobook.pbp_service.SelectParts.class);
                startActivityForResult(intent, SELECT_PARTS_CODE);
                partSelectButton.setEnabled(true);
                return;
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                    ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return;
                }
                showMenu(view);
            }
        });

        touchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (not_touch_mode== ALL_TOUCH_ENABLED) {
                    not_touch_mode= ALL_TOUCH_DISABLED;
//                    buttonsTransparency= transparency_in_dont_touch_mode;
                    touch_mode_draw();
                    return;
                }
                if (not_touch_mode== ALL_TOUCH_DISABLED) {
                    not_touch_mode= PLAY_PAUSE_ONLY;
                    touch_mode_draw();
                    return;
                }
                if (not_touch_mode== PLAY_PAUSE_ONLY) {
                    not_touch_mode= ALL_TOUCH_ENABLED;
                    touch_mode_draw();
                    return;
                }
            }
        });

    }

// отрисовка закладок
    public static void bookmarks_draw() {

        if (SoundScreen.musicList.size()== 0)
            return;
        if (surface== null)
            return;
        one_file_and_BMs= one_file_and_BMs_test();
        oneFileAndBMs_KeysDraw();

        surface.setZOrderOnTop(true);
        canvas = surface.getHolder().lockCanvas();

        if (canvas== null) {
            canvas = surface.getHolder().lockCanvas();
            if (canvas== null )
                return;

        }
        else {
            pre_canvas = canvas;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // очистка холста
        Paint miPaint = new Paint();
        // толщина линии = 6
        miPaint.setStrokeWidth(6);

        int loc_dur= 0;
        int loc_diff= loc_dur / surf_width;

        loc_dur = SoundScreen.musicList.get(lesson - 1).duration;
        loc_diff = loc_dur / surf_width;
        for (int i = 0; i < Bookmarks.bookMarkList.size()
                && lesson >= Bookmarks.bookMarkList.get(i).lesson; i++) {
            if (lesson > Bookmarks.bookMarkList.get(i).lesson)
                continue;
            if (Bookmarks.bookMarkList.get(i).type_bm == Bookmarks.BOOKMARK_TYPE_CUSTOM)
                miPaint.setColor(Color.GREEN);
            if (Bookmarks.bookMarkList.get(i).type_bm == Bookmarks.BOOKMARK_TYPE_CONTENT)
                miPaint.setColor(Color.BLUE);
            int loc_off = Bookmarks.bookMarkList.get(i).offset / loc_diff;
            loc_off += loc_off_diff;
            if (loc_off > surf_width)
                loc_off = surf_width - loc_off_diff;
            canvas.drawLine(loc_off, surf_height, loc_off, 0, miPaint);
        }
        if (surface.getHolder().getSurface().isValid())
            surface.getHolder().unlockCanvasAndPost(canvas);

    }

    private boolean checkIsTablet() {
        boolean isTablet= false;
        Display display = ((Activity)   this).getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        float widthInches = metrics.widthPixels / metrics.xdpi;
        float heightInches = metrics.heightPixels / metrics.ydpi;
        double diagonalInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
        if (diagonalInches >= 7.0) {
            isTablet = true;
        }

        return isTablet;
    }

    // вывод общей продолжительности
    public void show_totalDuration () {

        totalDuration = 0;
        for (int i= 0; i< SoundScreen.musicList.size(); i++) {
            totalDuration+= SoundScreen.musicList.get(i).duration;
        }
        totalDuration = totalDuration / ((speed_play+ 1)/ 5);
        String textBuf= String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours((long) totalDuration),
                TimeUnit.MILLISECONDS.toMinutes((long) totalDuration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long)
                                totalDuration)));
        totalDur.setText(textBuf);

    }

    // вывод встроенного изображения
    public void showEmbeddedImage () throws IOException {

        ImageView iv_background = (ImageView) MainActivity.backImage;
        if (!getEmbeddedImage(selAlbum)) {
            if (SoundScreen.musicList.size() > 0) {
//        if (SoundScreen.musicList.get(lesson - 1).image_exist) {
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(appContext, SoundScreen.musicList.get(lesson - 1).uri);
                    byte[] data = mmr.getEmbeddedPicture();
                    if (data != null) {
                        thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);
                        iv_background.setImageBitmap(thumbnail);
                    } else {
                        iv_background.setImageResource(R.drawable.headphones);
                        SoundScreen.MusicElement ME2 = SoundScreen.musicList.get(lesson - 1);
                        ME2.image_exist = false;
                        SoundScreen.musicList.set(lesson - 1, ME2);
                    }
                } catch (RuntimeException e) {
                    iv_background.setImageResource(R.drawable.headphones);
                    SoundScreen.MusicElement ME2 = SoundScreen.musicList.get(lesson - 1);
                    ME2.image_exist = false;
                    SoundScreen.musicList.set(lesson - 1, ME2);
                }
                mmr.release();
            }
            else {
                iv_background.setImageResource(R.drawable.headphones);
            }
        }
        else {
            iv_background.setImageBitmap(thumbnail);
        }

    }

    // дополнительная громкость
    public static void miLoudness() {

        if (curTG >= 0) {
            addVolumeControl.setProgress((int) curTG);
        }

    }

// Чтение одного файла
    public static int oneFileRead(String nameFile) throws FileNotFoundException {
// работа с файлами
// чтение файла с рабочими данными
        String name, album, artist, composer;
        Uri uri;

        File directory = directory_cfg;
        nameFile= nameFile.replaceAll("/", " ");
        File miFile = new File(directory, nameFile);
        if (miFile.exists()) {  // мы эту книгу уже читали
            FileInputStream fis = new FileInputStream(miFile);
            if (fis != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    textBuf = reader.readLine();  // прослушиваемый файл (трек)
                    if (textBuf!= null) {
                        if (textBuf.indexOf(',') == -1) {
                            try {
                                lesson = Integer.parseInt(textBuf);
                            } catch (NumberFormatException e) {
                                lesson = 0;
                            }
                            if (lesson > LESSON_MAX) lesson = 0;
                        } else {
                            String textPars[] = textBuf.split(",");
                            if (textPars[0] != null) {  // номер текущего трэка
                                lesson = Integer.parseInt(textPars[0]);
                            } else {
                                lesson = 0;
                            }
                            if (lesson > LESSON_MAX) lesson = 0;
                            if (textPars[1] != null) {  // статус прочтения трека: не начат, начат, закончен
                                reading_status = Integer.parseInt(textPars[1]);
                            } else {
                                reading_status = DO_NOT_READ;
                            }
                            if (textPars.length > 2 && textPars[2] != null) {      // скорость воспроизведения
                                try {
                                    speed_play = Float.parseFloat(textPars[2]);
                                } catch (NumberFormatException e) {
                                    speed_play = 4;
                                }
                                if (speed_play > MAX_PLAY_SPEED)
                                    speed_play = 4;
                                if (speed_play < 0)
                                    speed_play = 4;
                                speed_control.setProgress((int) (speed_play));
                                playbackParams.setSpeed((speed_play+ 1)/ 5);
                            }
                            else {
                                if (nosave_speed_for_newbooks)
                                    speed_play = 4;
                            }
                            if (textPars.length > 3) {      // признак изменения скорости с шагом 0.05
                                if (textPars[3] != null) {
                                    speed_step_005 = Boolean.parseBoolean(textPars[3]);
                                }
                            }
                            if (textPars.length > 4) {      // дополнительная громкость
                                if (textPars[4] != null) {
                                    try {
                                        curTG = Float.parseFloat(textPars[4]);
                                    } catch (NumberFormatException e) {
                                        curTG = 0;
                                    }
                                } else {
                                    curTG = 0;
                                }
                                if (loudness != null) {
//                                    loudness.setTargetGain((int) curTG);
                                    addVolumeControl.setProgress((int) curTG);
                                }
                            }
                            if (textPars.length > 5) {      // параметры эквалайзера
                                if (textPars[5] != null  &&  textPars[5].length()> 0) {
                                    eqs = new Equalizer.Settings(textPars[5]);
                                }
                                else {
                                    eqs= null;
                                }
                            }
                            else {
                                if (nosave_speed_for_newbooks) {
//                                    eqs = null;
                                    equalizer_set= false;
                                    if (button_color== BLACK)
                                        buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active_white);
                                    else
                                        buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active);
                                }
                            }
                            if (textPars.length > 6) {      // признак активности эквалайзера
                                if (textPars[6] != null) {
                                    equalizer_set= Boolean.parseBoolean(textPars[6]);
                                    if (equalizer_set) {
                                        buttonEqualizer.setImageResource(R.drawable.ic_equalizer_button);
                                    }
                                    else {
                                        if (button_color== BLACK)
                                            buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active_white);
                                        else
                                            buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active);
                                    }
                                }
                                else {
                                    equalizer_set= false;
                                    if (button_color== BLACK)
                                        buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active_white);
                                    else
                                        buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active);
                                }
                            }

                            if (textPars.length > 7) {      // версия формата
                                if (textPars[7] != null) {
                                    format_work_file_version= Integer.parseInt(textPars[7]);
                                    if (format_work_file_version== 0)
                                        format_work_file_version= 1;
                                }
                            }
                            if (textPars.length > 8) {      // признак файла в формате Aplle (M4B, M4A)
                                if (textPars[8] != null) {
                                    fucking_apple = Boolean.parseBoolean(textPars[8]);
                                }
                            }

                        }
                    }
                    else
                        lesson = 0;
                    textBuf = reader.readLine();
                    if (textBuf != null) {
                        try {
                            back_in_time = Integer.parseInt(textBuf);
                        } catch (NumberFormatException e) {
                            back_in_time= 4;
                        }
                    }
                    if (back_in_time < 0  ||  back_in_time > 60)
                        back_in_time= 4;
                    if (back_in_time>= 6)
                        back_in_time_cust= back_in_time- 6;
                    else
                        back_in_time_cust= 0;

                    textBuf = reader.readLine();
                    if (textBuf != null)
                        music_quan = Integer.parseInt(textBuf);
                    if (music_quan > LESSON_MAX)
                        music_quan = LESSON_MAX;
                    if (music_quan == 0) {
                        fis.close();
                        return 0;
                    }
                    if (// music_quan != SoundScreen.musicList.size()  ||
                            music_quan == SoundScreen.musicList.size()  ||
                                    nameFile.equals(WORK_FILE_NAME)) {
                        if (nameFile.equals(WORK_FILE_NAME))
                            SoundScreen.musicList.clear();
                        for (int i = 0; i < music_quan  &&  i< LESSON_MAX; i++) {
                            name = reader.readLine();
                            album = reader.readLine();
                            textBuf= reader.readLine();
                            if (textBuf != null) {
                                if (com.audiobook.pbp_service.SoundScreen.musicList.size()> i)
                                    uri= com.audiobook.pbp_service.SoundScreen.musicList.get(i).uri;
                                else
                                    uri = Uri.parse(textBuf);
                            }
                            else
                                continue;
                            textBuf = reader.readLine();
                            if (textBuf != null) {
                                try {
                                    loc_size = Integer.parseInt(textBuf);
                                }
                                catch (NumberFormatException e) {
                                    if (com.audiobook.pbp_service.SoundScreen.musicList.size()> i)
                                        loc_size = com.audiobook.pbp_service.SoundScreen.musicList.get(i).size;
                                    else loc_size= 100;
                                }
                            }
                            artist = reader.readLine();
                            composer= "";
                            if (format_work_file_version== 1) {
                                if (artist.contains(end_of_tag)) {
                                    composer= artist.substring(artist.indexOf(end_of_tag) + end_of_tag.length());
                                    artist= artist.substring(0, artist.indexOf(end_of_tag));
                                    if ((composer== null  ||  composer.isEmpty())
                                        &&  SoundScreen.musicList.size()> i
                                        &&  (SoundScreen.musicList.get(i).composer!= null
                                            &&  !SoundScreen.musicList.get(i).composer.isEmpty()))
                                        composer= SoundScreen.musicList.get(i).composer;
                                }
                            }
                            textBuf = reader.readLine();
                            if (textBuf != null) {
                                try {
                                    duration = Integer.parseInt(textBuf);
                                }
                                catch (NumberFormatException e) {
                                    if (com.audiobook.pbp_service.SoundScreen.musicList.size()> i)
                                        loc_size = SoundScreen.musicList.get(i).duration;
                                    else duration= 100;
                                }
                            }
                            textBuf = reader.readLine();
                            if (textBuf != null) {
                                try {
                                    offset = Integer.parseInt(textBuf);
                                }
                                catch (NumberFormatException e) {
                                    if (com.audiobook.pbp_service.SoundScreen.musicList.size()> i) {
                                        offset = SoundScreen.musicList.get(i).offset;
                                    }
                                    else offset= 0;
                                }
                            }
                            lesson_point[i+ 1]= offset;
                            if (com.audiobook.pbp_service.SoundScreen.musicList.size()<= i) {
                                com.audiobook.pbp_service.SoundScreen.musicList.add(new com.audiobook.pbp_service.SoundScreen.MusicElement(uri, name, album,
                                        artist, composer, duration, loc_size, offset, true));
                            }
                        }
                    }
                    else {
                        // книгу читали, но кол-во частей не совпадает раньше и сейчас. Книгу берем из musicList..
                        music_quan= SoundScreen.musicList.size();
                        for (int i = 0; i < music_quan; i++) {
                            name= com.audiobook.pbp_service.SoundScreen.musicList.get(i).name;
                            album= com.audiobook.pbp_service.SoundScreen.musicList.get(i).album;
                            uri= com.audiobook.pbp_service.SoundScreen.musicList.get(i).uri;
                            loc_size = com.audiobook.pbp_service.SoundScreen.musicList.get(i).size;
                            artist = com.audiobook.pbp_service.SoundScreen.musicList.get(i).artist;
                            composer = com.audiobook.pbp_service.SoundScreen.musicList.get(i).composer;
                            duration = com.audiobook.pbp_service.SoundScreen.musicList.get(i).duration;
                            offset = 0;
                            if (i< MainActivity.LESSON_MAX- 1) {
                                lesson_point[i + 1] = offset;
                            }
                        }
                    }
                    if (SoundScreen.musicList.size() > 0) {
                        if (fucking_apple) {
                            selAlbum = SoundScreen.musicList.get(0).name;
                        }
                        else {
                            selAlbum = SoundScreen.musicList.get(0).album;
                        }
                    }
                    fis.close();
                } catch (IOException e) {
                }
            }
            return 1;
        }
        else {
            if (nosave_speed_for_newbooks)
                speed_play = 4;
            return 0;
        }
    }

// Проверка наличия разрешений
    public static boolean hasPermissions(Context context) {
        return true;
    }

    public void requestPermissions_local(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", activity.getPackageName())));
                activity.startActivityForResult(intent, requestCode);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, requestCode);
            }
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    requestCode);
        }
    }

    public static MediaPlayer newMediaPlayer (Uri miUri) {

        if (mediaPlayer != null
                &&  SoundScreen.musicList.size()> 0
                &&  !mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            miLoudness ();
            if (sleep_timer_repeat  &&  sleep_timer_ended  &&  !wait_end_part) {
                sleep_timer_ended= false;
                if (sleep_timer_time_value_mem> 0) {
                    sleep_timer_time_value = sleep_timer_time_value_mem;
                    local_sleep= 0;
                    sleep_timer_time= true;
                }
                if (sleep_timer_parts_value_mem> 0) {
                    sleep_timer_parts_value = sleep_timer_parts_value_mem- 1;
                    local_sleep= 0;
                    sleep_timer_parts= true;
                }
                buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_24);
                if (sleep_timer_repeat) {
                    buttonSleepTimer.setText("R");
                }
                else
                    buttonSleepTimer.setText("");
            }
        }

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            miLoudness ();
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            miLoudness ();
        }

        playbackParams.setSpeed((speed_play+ 1)/ 5);
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(appContext, miUri);
            mediaPlayer.prepare();
        } catch (IOException | IllegalArgumentException e) {
            Log.e("Exception", "Mediaplayer prepare failed: " + e.toString());
            return null;
        }

// подключение эквалайзера
        if (equalizer_set) {
            if (mEqualizer!= null) {
                mEqualizer.release();
                mEqualizer = null;
            }
            int sessId= mediaPlayer.getAudioSessionId();
            try {
                mEqualizer = new Equalizer(0, sessId);
            } catch (RuntimeException e) {
                mEqualizer= null;
            }
            if (mEqualizer!= null) {
                mEqualizer.setEnabled(true);
                buttonEqualizer.setImageResource(R.drawable.ic_equalizer_button);
            }
        }
        else {
            if (button_color== BLACK)
                buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active_white);
            else
                buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active);
        }

        if (eqs!= null  &&  mEqualizer!= null) {
                mEqualizer.setProperties(eqs);
            }

// обработчик ошибок
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("MainActivity","WHAT: "+ what + " EXTRA: "+extra);
                what_100= what == 100;
                return false;
            }   // what 100 при destroy
        });
// обработка конца файла
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (what_100)
                    return;
                int cp = mp.getCurrentPosition();
                lesson_point[lesson]= cp;
                if (sleep_timer_parts) {
                    if (sleep_timer_parts_value> 0) {
                        sleep_timer_parts_value--;
                    }
                    else {
                        playing = 0;
                        sleep_timer_parts = false;
                        if (sleep_timer_time) {
                            sleep_timer_time = false;
                            local_sleep = 0;
                        }
                        sleep_timer_ended= true;
                        buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_off_24);
                        if (sleep_timer_repeat) {
                            buttonSleepTimer.setText("R");
                        }
                        else
                            buttonSleepTimer.setText("");
                    }
                }
                if (sleep_timer_ended  &&  wait_end_part) {
                    wait_end_part= false;
                    one_time_no_time_speak = true;
                    mediaController.getTransportControls().pause();
                    buttonPlayPause.setIconResource(R.drawable.ic_media_play);
                    buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_off_24);
                    if (sleep_timer_repeat) {
                        buttonSleepTimer.setText("R");
                    } else
                        buttonSleepTimer.setText("");
                }

                if (playing == 1) {
                    if (lesson < SoundScreen.musicList.size()) {
                        if (reading_status == DO_NOT_READ) {
                            reading_status= I_READ;
                        }
                        if (mediaController!= null) {
                            lesson_point[lesson+ 1] = 0;
                            std_keys=true;
                            next_track_from_competition= true;
                            mediaController.getTransportControls().skipToNext();
                        }
                        else {
                            mediaController = new MediaControllerCompat(appContext, playerServiceBinder.getMediaSessionToken());
                            mediaController.registerCallback(callback);
                            callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                        }
                    }
                    else {
                        if (reading_status != READ) {
                            reading_status= READ;
                        }
                        if (repeat_state == TRACK_REPEAT) {
                            next_track_from_competition= true;
                            mediaController.getTransportControls().skipToNext();
                        }
                        if (repeat_state == BOOK_REPEAT) {
                            lesson= 1;
                            lesson_point[lesson]= 0;
                            buttonPrev.setEnabled(false);
                            buttonPrev.setBackgroundColor(GRAY);
                            buttonNext.setEnabled(true);
                            if (button_color!= 0)
                                buttonNext.setBackgroundColor(button_color);
                            mediaController.getTransportControls().play();
                        }
                        if (repeat_state == NO_REPEAT) {
                            mediaController.getTransportControls().pause();
                        }
                    }
                }
            }
        });
        return mediaPlayer;
    }

    public static void trackChange () throws IOException {

        if (SoundScreen.musicList.size()== 0) {  // первый заход, книга не выбрана
            buttonPlayPause.setEnabled(false);
            buttonPrev.setEnabled(false);
            buttonNext.setEnabled(false);
            buttonNext10.setEnabled(false);
            buttonPrev10.setEnabled(false);
            buttonPlayPause.setBackgroundColor(GRAY);
            buttonPrev.setBackgroundColor(GRAY);
            buttonNext.setBackgroundColor(GRAY);
            buttonNext10.setBackgroundColor(GRAY);
            buttonPrev10.setBackgroundColor(GRAY);

            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            miLoudness();
        }
        else {
            buttonPlayPause.setEnabled(true);
            buttonNext10.setEnabled(true);
            buttonPrev10.setEnabled(true);
            buttonPlayPause.setBackgroundColor(button_color);
            if (button_color!= 0) {
                buttonPlayPause.setBackgroundColor(button_color);
                buttonNext10.setBackgroundColor(button_color);
                buttonPrev10.setBackgroundColor(button_color);
            }

            if (lesson> SoundScreen.musicList.size()) {
                lesson = SoundScreen.musicList.size();
            }
            Bookmarks.get_bm_file();
            one_file_and_BMs= one_file_and_BMs_test();
            if (lesson > 0  &&  lesson <= SoundScreen.musicList.size()) {
                if (!SoundScreen.musicList.isEmpty()
                    &&  (hand_changed == 0  ||
                        (hand_changed  != 0  &&  mediaPlayer == null))) {
                    newMediaPlayer(SoundScreen.musicList.get(lesson - 1).uri);

                }
                if (!one_file_and_BMs) {
                    if (lesson == 1) {
                        buttonPrev.setEnabled(false);
                        buttonPrev.setBackgroundColor(GRAY);
                    } else {
                        buttonPrev.setEnabled(true);
                        if (button_color != 0)
                            buttonPrev.setBackgroundColor(button_color);
                    }
                    if (lesson == SoundScreen.musicList.size()) {
                        buttonNext.setEnabled(false);
                        buttonNext.setBackgroundColor(GRAY);
                    } else {
                        buttonNext.setEnabled(true);
                        if (button_color != 0)
                            buttonNext.setBackgroundColor(button_color);
                    }
                }
                oneFileAndBMs_KeysDraw();
            }
        }
    }
//
// Проверка что книга состоит из одного файла и имеет закладки
//
    public static boolean one_file_and_BMs_test () {

        if (SoundScreen.musicList== null)
            return false;

        if (SoundScreen.musicList.size()> 1  &&  !goto_bookmarks_in_fucking_apple_style_cfg)
            return false;

        if (Bookmarks.bookMarkList== null)
            return false;
        if (Bookmarks.bookMarkList.size()== 0)
            return false;

        return true;
    }
//
// Проверка режима чтения файла Apple и отрисовка кнопок навигации по трекам
// в зависимости от наличия закладок и положения в книге
//
    public  static void oneFileAndBMs_KeysDraw() {
        int currentBM= 0;

        if (one_file_and_BMs) {
            currentBM= getCurrentBookmarkNum();
            if (currentBM == 0) {
                buttonPrev.setEnabled(false);
                buttonPrev.setBackgroundColor(GRAY);
            }
            if (currentBM> 0
                ||  (Bookmarks.bookMarkList.size()== 1
                     &&  lesson_point[lesson]> Bookmarks.bookMarkList.get(0).offset)) {
                buttonPrev.setEnabled(true);
                if (button_color != 0)
                    buttonPrev.setBackgroundColor(button_color);
            }

            if (Bookmarks.bookMarkList.size()< 1
                ||  (currentBM >= Bookmarks.bookMarkList.size()
                    &&  lesson>= SoundScreen.musicList.size())
                ||  (Bookmarks.bookMarkList.size()== 1
                    &&  lesson_point[lesson]>=  Bookmarks.bookMarkList.get(0).offset
                    /*&&  playing== 0*/ )) {
                buttonNext.setEnabled(false);
                buttonNext.setBackgroundColor(GRAY);
            } else {
                buttonNext.setEnabled(true);
                if (button_color != 0)
                    buttonNext.setBackgroundColor(button_color);
            }
//            show_current_track_name();
        }
    }

//
// дать номер текущей закладки
// текущей считается закладка ближайшая и большая чем актуальный lesson_point
//
    public static int getCurrentBookmarkNum () {
        if (Bookmarks.bookMarkList.size()== 0
            ||  Bookmarks.bookMarkList.size()== 1)
            return 0;
        for (int i= 0; i< Bookmarks.bookMarkList.size(); i++) {
            if (goto_bookmarks_in_fucking_apple_style_cfg
                    &&  Bookmarks.bookMarkList.get(i).lesson< lesson)
                continue;
            if (Bookmarks.bookMarkList.get(i).lesson> lesson)
                return i;
            if (Bookmarks.bookMarkList.get(i).offset < lesson_point[lesson]) {
                continue;
            }
            if (lesson_point[lesson] >= Bookmarks.bookMarkList.get(i).offset) {
                if (i== 0)
                    return 0;
                else
                    return i+ 1;
            }
            return i;
        }
        return Bookmarks.bookMarkList.size();
    }

// длительность текущего трека
    public static void show_totalTime_track () {
//        float timeDuration = SoundScreen.musicList.get(lesson- 1).duration;
        float timeDuration= 0;
        if (SoundScreen.musicList.size()> 0  &&  lesson- 1 <= SoundScreen.musicList.size()) {
            if (SoundScreen.musicList.get(lesson - 1).duration > 0) {
                timeDuration = SoundScreen.musicList.get(lesson - 1).duration;
            }
            else {
                if (mediaPlayer != null) {
                    timeDuration = mediaPlayer.getDuration();
                }
            }
        }
        timeDuration= timeDuration / ((speed_play+ 1)/ 5);
        if (TimeUnit.MILLISECONDS.toMinutes((long) timeDuration)< 60) {
            String textBuf = String.format("%02d:%02d",
//        String textBuf= String.format("%02d:%02d:%02d",
//                TimeUnit.MILLISECONDS.toHours((long) timeDuration),
                    TimeUnit.MILLISECONDS.toMinutes((long) timeDuration),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeDuration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    timeDuration)));
            totalView.setText(String.format("%s", textBuf));
        }
        else {
            String textBuf= String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours((long) timeDuration),
                TimeUnit.MILLISECONDS.toMinutes((long) timeDuration)-
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) timeDuration)),
                TimeUnit.MILLISECONDS.toSeconds((long) timeDuration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    timeDuration)));
            totalView.setText(String.format("%s", textBuf));

        }
    }
    public static void showLessonNum () {
        show_totalTime_track (); // длительность текущего трека
        if (sleep_timer_time  ||  sleep_timer_parts) {
            sleep_timer_textView.setText(R.string.sleep_timer_show);
            if (sleep_timer_time) {
                if (sleep_timer_time_value< 60) {
                    sleep_timer_show.setText(String.format("%02d:%02d",
                            (local_sleep + (sleep_timer_time_value)* 60) / 60,
                            (local_sleep + (sleep_timer_time_value)* 60) % 60));
//                          (local_sleep + ((sleep_timer_time_value+ 1)* 5*60))/ 60,
//                          (local_sleep + ((sleep_timer_time_value+ 1)* 5*60))% 60));
                }
                else {
                    sleep_timer_show.setText(String.format("%02d:%02d:%02d",
                            sleep_timer_time_value / 60,
                            sleep_timer_time_value - sleep_timer_time_value / 60 * 60,
                            (local_sleep + (sleep_timer_time_value)* 60) % 60));
                }
            }
            else
                sleep_timer_show.setText("");
            if (sleep_timer_time  &&  !sleep_timer_parts)
                sleep_parts_show.setText("");
            if (sleep_timer_time  &&  sleep_timer_parts)
                sleep_parts_show.setText(String.format(",%d tr.", sleep_timer_parts_value));
            if (!sleep_timer_time  &&  sleep_timer_parts)
                sleep_parts_show.setText(String.format("%d tr.", sleep_timer_parts_value));

        }
        if (!sleep_timer_time  &&  !sleep_timer_parts) {
            sleep_timer_show.setText("");
            sleep_parts_show.setText("");
            sleep_timer_textView.setText("");
        }

        show_current_track_name ();       // вывод имени текущего трека или текущей закладки
        if (pre_lesson != lesson) {
            show_progress.setProgress(lesson- 1);
            pre_lesson= lesson;
        }
        showProgress();
        speed_control.setProgress((int) (speed_play));
        playbackParams.setSpeed((speed_play+ 1)/ 5);
//        speed_view.setText(String.format ("%1.1f", (speed_play+ 1)/ 5));
        if (speed_step_005) {
            speed_view.setText(String.format("%1.2f", (speed_play + 1) / 5));
        }
        else {
            speed_view.setText(String.format("%1.1f", (speed_play + 1) / 5));
        }
        if (Bookmarks.need_refresh_BMs) {
            bookmarks_draw();  // отрисовка закладок
            Bookmarks.need_refresh_BMs= false;
        }

        if (repeat_state== BOOKMARK_REPEAT) {
            int loc_lesson= Bookmarks.bookMarkList.get (Bookmarks.end_repeate).lesson;
            if (loc_lesson<= lesson) {
                int loc_offset= Bookmarks.bookMarkList.get (Bookmarks.end_repeate).offset;
                int loc_position= mediaPlayer.getCurrentPosition();
                if (loc_offset<= loc_position) {
                    pos_rqst_from_BMs_controller= true;
                    Bookmarks.bookmark_repeate_mode= true;
                    Bookmarks.need_refresh_BMs= true;
                    lesson_point[Bookmarks.bookMarkList.get ((int) Bookmarks.begin_repeate).lesson]= Bookmarks.bookMarkList.get ((int) Bookmarks.begin_repeate).offset;
                    mediaController.getTransportControls().playFromMediaId(String.valueOf(Bookmarks.bookMarkList.get ((int) Bookmarks.begin_repeate).lesson), null);
                }
            }
        }

    }

// вывод имени текущего трэка
    public static void show_current_track_name () {
        String artist, composer, art_comp= "";

        if (!one_file_and_BMs) {
            if (SoundScreen.musicList.size() > 0  &&  lesson- 1 <= SoundScreen.musicList.size()) {
                artist= SoundScreen.musicList.get(lesson - 1).artist;
                if (artist== null)
                    artist= "";
                if (artist.equalsIgnoreCase("<unknown>"))
                    artist = "";
                composer= SoundScreen.musicList.get(lesson - 1).composer;
                if (composer== null)
                    composer= "";
                if (composer.equalsIgnoreCase("<unknown>"))
                    composer = "";
                if ((show_artist  &&  !artist.isEmpty())
                    ||  (show_composer  &&  !composer.isEmpty())) {
                    art_comp = "\n";
                    if (show_artist)
                        art_comp+= artist;
                    if (show_composer  &&  !composer.isEmpty())
                        art_comp+= " \u266c" + composer;
                }

                if (!show_artist  &&  !show_composer)
                    track_name.setText(String.format("%s", SoundScreen.musicList.get(lesson - 1).name));
                if (show_artist  ||  show_composer)
                    track_name.setText(String.format("%s", SoundScreen.musicList.get(lesson - 1).name + art_comp));
            }
            else
                track_name.setText("");
            return;
        }

        oneFileAndBMs_KeysDraw();

        if (SoundScreen.musicList.size()> 0) {
            artist = SoundScreen.musicList.get(0).artist;
            if (artist == null)
                artist = "";
            if (artist.equalsIgnoreCase("<unknown>"))
                artist = "";
        }
        else {
            artist= "";
        }
        if (SoundScreen.musicList.size()> 0) {
            composer = SoundScreen.musicList.get(0).composer;
            if (composer == null)
                composer = "";
            if (composer.equalsIgnoreCase("<unknown>"))
                composer = "";
        }
        else {
            composer= "";
        }

        if ((show_artist  &&  !artist.isEmpty())
                ||  (show_composer  &&  !composer.isEmpty())) {
            art_comp = "\n";
            if (show_artist)
                art_comp+= artist;
            if (show_composer  &&  !composer.isEmpty())
                art_comp+= " \u266c" + composer;
        }

        if (goto_bookmarks_in_fucking_apple_style_cfg
                &&  SoundScreen.musicList.size()> 1
                &&  Bookmarks.bookMarkList.size()> 0) {
            int currentBM;
            currentBM= getCurrentBookmarkNum();
            if (currentBM== 0)
                currentBM++;
            if (Bookmarks.bookMarkList.get(currentBM- 1).lesson!= lesson) {
                if (!show_artist  &&  !show_composer)
                    track_name.setText(String.format("%s", SoundScreen.musicList.get(lesson - 1).name));
                if (show_artist  ||  show_composer)
                    track_name.setText(String.format("%s", SoundScreen.musicList.get(lesson - 1).name + art_comp));
            }
            else {
                if (!show_artist && !show_composer)
                    track_name.setText(String.format("%s", Bookmarks.bookMarkList.get(currentBM- 1).partName));
                if (show_artist || show_composer)
                    track_name.setText(String.format("%s", Bookmarks.bookMarkList.get(currentBM- 1).partName + art_comp));
            }
            return;
        }

        if (lesson_point[1]== 0) {
            if (!show_artist  &&  !show_composer)
                track_name.setText(String.format("%s", Bookmarks.bookMarkList.get(0).partName));
            if (show_artist  ||  show_composer)
                track_name.setText(String.format("%s", Bookmarks.bookMarkList.get(0).partName + art_comp));
            return;
        }
        if (lesson_point[1]>= Bookmarks.bookMarkList.get(Bookmarks.bookMarkList.size()- 1).offset) {
            if (!show_artist  &&  !show_composer)
                track_name.setText(String.format("%s", Bookmarks.bookMarkList.get(Bookmarks.bookMarkList.size()- 1).partName));
            if (show_artist  ||  show_composer)
                track_name.setText(String.format("%s", Bookmarks.bookMarkList.get(Bookmarks.bookMarkList.size()- 1).partName + art_comp));
            return;
        }
        for (int i= 1; i<= Bookmarks.bookMarkList.size()  &&  Bookmarks.bookMarkList.size()> 1; i++) {
            if (Bookmarks.bookMarkList.get(i).offset> lesson_point[1]) {
                if (!show_artist  &&  !show_composer)
                    track_name.setText(String.format("%s", Bookmarks.bookMarkList.get(i- 1).partName));
                if (show_artist  ||  show_composer)
                    track_name.setText(String.format("%s", Bookmarks.bookMarkList.get(i- 1).partName + art_comp));
                break;
            }
        }
    }

    // прошло времени для всей книги
    public static void show_sounded_hours () {
        double totalDuration;
        totalDuration = 0;
        if (lesson- 1<= SoundScreen.musicList.size()) {
            for (int i = 0; i < lesson - 1; i++) {
                totalDuration += SoundScreen.musicList.get(i).duration;
            }
        }
        totalDuration+= lesson_point[lesson];
        totalDuration = totalDuration / ((speed_play+ 1)/ 5);
        textBuf= String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours((long) totalDuration),
                TimeUnit.MILLISECONDS.toMinutes((long) totalDuration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long)
                                totalDuration)));
        sounded_hours.setText(String.format ("%s", textBuf));
    }

// Timings for current track
    public static void show_passed_left_view () {

        float finalTime= 0;
        String textBuf;

        finalTime= lesson_point[lesson];

        if (SoundScreen.musicList.size()== 0)
            finalTime= 0;

        float timeDuration = 0;
        if (SoundScreen.musicList.size()> 0  &&  lesson- 1 <= SoundScreen.musicList.size())
            timeDuration= SoundScreen.musicList.get(lesson- 1).duration - finalTime;
        else
            timeDuration= 0;

        finalTime = finalTime / ((speed_play+ 1)/ 5);
        timeDuration = timeDuration / ((speed_play+ 1)/ 5);

        if (timeDuration< 0)
            timeDuration= 0;
        if (TimeUnit.MILLISECONDS.toMinutes((long) timeDuration)< 60) {
            textBuf = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) timeDuration),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeDuration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    timeDuration)));
        }
        else {
            textBuf = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours((long) timeDuration),
                    TimeUnit.MILLISECONDS.toMinutes((long) timeDuration) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) timeDuration)),
                    TimeUnit.MILLISECONDS.toSeconds((long) timeDuration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    timeDuration)));
        }
        if (TimeUnit.MILLISECONDS.toMinutes((long) finalTime)< 60) {
            textBuf2 = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    finalTime)));
        }
        else {
            textBuf2 = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours((long) finalTime),
                    TimeUnit.MILLISECONDS.toMinutes((long) finalTime) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) finalTime)),
                    TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    finalTime)));
        }
        passedView.setText(String.format ("%s", textBuf2));
        leftView.setText(String.format ("-%s", textBuf));
        if (actionsList.size()== 0) {
            buttonUndo.setEnabled(false);
            buttonUndo.setBackgroundColor(GRAY);
        }
        else {
            buttonUndo.setEnabled(true);
            buttonUndo.setBackgroundColor(button_color);
        }
    }
// осталось времени для всей книги
    public static void show_future_hours () {
        double totalDuration;

        totalDuration = 0;
        for (int i= lesson; i< SoundScreen.musicList.size(); i++) {
            totalDuration+= SoundScreen.musicList.get(i).duration;
        }
        if (SoundScreen.musicList.size()> 0  &&  lesson- 1 <= SoundScreen.musicList.size()) {
            totalDuration += SoundScreen.musicList.get(lesson - 1).duration - lesson_point[lesson];
            totalDuration = totalDuration / ((speed_play + 1) / 5);
        }

        textBuf= String.format("-%02d:%02d",
                TimeUnit.MILLISECONDS.toHours((long) totalDuration),
                TimeUnit.MILLISECONDS.toMinutes((long) totalDuration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long)
                                totalDuration)));
        future_hours.setText(textBuf);

    }

    public static void showProgress () {

        show_passed_left_view ();       // timings for current track
        show_sounded_hours ();  // прошло времени для всей книги

        show_current_track_number (); // вывод номера текущего трэка
        show_max_track_number();      // вывод максимального номера трека
        show_future_hours ();   // осталось времени для всей книги
    }

// вывод номера текущего трека
    public static void show_current_track_number () {
        if (SoundScreen.musicList.size()== 0)
            textBuf= " 0";
        else
            textBuf= String.format(" %s", lesson);
        curTrack.setText(String.format ("%s", textBuf));
    }

// вывод номера максимального трэка
    public static void show_max_track_number () {
        textBuf= String.format("%s ", SoundScreen.musicList.size());
        maxTrack.setText(String.format ("%s", textBuf));
    }

    public static void startPlayProgressUpdater() {

        Runnable notification = new Runnable() {
            public void run() {
                if (mediaPlayer != null  /*&&  !update_start*/) // обработка паралельного onDestroy
                {
                    startPlayProgressUpdater();
                }
            }
        };
            seekBar_part.setProgress(mediaPlayer.getCurrentPosition());
            lesson_point[lesson]= mediaPlayer.getCurrentPosition();
                if (playing == 1) {
                    if (sleep_timer_time) {
                        if (local_sleep <= 0) {
                            local_sleep = 60;
                            sleep_timer_time_value--;
                        }
                        local_sleep--;
                        if (sleep_timer_time_value < 0) {
                            local_sleep = 0;
                            sleep_timer_time = false;
                            if (sleep_timer_parts)
                                sleep_timer_parts = false;
                            sleep_timer_ended = true;
                            if (!wait_end_part) {
                                playing = 0;
                                one_time_no_time_speak = true;
                                mediaController.getTransportControls().pause();
                                buttonPlayPause.setIconResource(R.drawable.ic_media_play);
                                buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_off_24);
                                if (sleep_timer_repeat) {
                                    buttonSleepTimer.setText("R");
                                } else
                                    buttonSleepTimer.setText("");
                            }
                        }
                    }
                    handler.postDelayed(notification, 1000);
                }
                else {
                    if (sleep_timer_time) {
                        if (mediaController != null) {
                            mediaController.getTransportControls().pause();
                            buttonPlayPause.setIconResource(R.drawable.ic_media_play);
                            handler.removeCallbacks(notification);
                        }
                    }
                }
        showLessonNum();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ErrorReporter reporter = ErrorReporter.getInstance();
        reporter.init(this);
        reporter.CheckErrorAndSendMail (this);
        directory_cfg= getFilesDir();

        try {
            readConfig();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", "Readconfig failed: " + e.toString());
        }
        if (keep_portrait)
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (themesAsSystem)
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
        if (themesAuto)
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_TIME);
        if (themesLight) {
            if (is_LightTheme_Manual_Set) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
            }
            else {
                themesLight= false;
                themesDark= true;
                writeConfig();
            }
        }
        if (themesDark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!right_hand_interface  &&
                !left_hand_interface  &&
                !simetric_interface  &&
                !one_finger_right_interface  &&
                !one_finger_left_interface) {
                    right_hand_interface = true;
            }
//            surf_width= 0;
            if (right_hand_interface) {
                setContentView(R.layout.activity_main);
            }
            if (left_hand_interface) {
                setContentView(R.layout.activity_main_lh);
            }
            if (simetric_interface) {
                setContentView(R.layout.activity_main_simetric);
            }
            if (one_finger_right_interface) {
                setContentView(R.layout.activity_main_one_finger_right);
            }
            if (one_finger_left_interface) {
                setContentView(R.layout.activity_main_one_finger_left);
            }
        }

        setupWindowInsets();

        if (keep_screen_anytime) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        try {
            initViews();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setupWindowInsets() {
        View rootView = findViewById(R.id.main_layout); // ваш корневой контейнер (например, ConstraintLayout, LinearLayout и т.п.)

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Применяем отступы: сверху — статус-бар, снизу — навигационная панель
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            // Возвращаем EMPTY, чтобы дочерние View не получали insets повторно
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.main_menu);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

// Обработчик создания меню
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        return true;
//    }

            // Обработчик нажатия кнопки в меню
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (not_touch_mode== ALL_TOUCH_DISABLED
                        ||  not_touch_mode== PLAY_PAUSE_ONLY) {
                    Toast.makeText(appContext, appContext.getResources().getString(R.string.not_touch_mode_mess), Toast.LENGTH_LONG).show();
                    return true;
                }
                File directory;
                File chooseDir;
                String nameFile = selAlbum, nameFile_out = null;
                String title;
                Uri fileUri = null;
                File miFile_in;
                File miFile_out;

                int id = item.getItemId();
//                TextView headerView = findViewById(R.id.selectedMenuItem);
                switch (id) {
                    case R.id.book_select:
                        if (use_root_folder) {
                            startFolder = root_folder_path;
                        } else {
                            startFolder = "";
                        }
                        checkAndRequestPermissions();
                        if (checkAndRequestPermissions()) {
                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                if (selAlbum.length() > 0)
                                    headerText.setText(selAlbum);
                                else {
                                    headerText.setText(R.string.book_not_sel);
                                    if (button_color == BLACK)
                                        favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_white);
                                    else
                                        favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground);
                                }
                            }
                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                if (selAlbum.length() > 0)
                                    headerText.setText(selAlbum);
                                else {
                                    headerText.setText(R.string.book_not_sel);
                                    if (button_color == BLACK)
                                        favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_white);
                                    else
                                        favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground);
                                }
                            }
                            if (playing == 1) {
                                if (time_speaking_cfg) {
                                    one_time_no_time_speak = true;
                                }
                                miPlayAndPause();
                            } else
                                writeToFile();
                            if (SoundScreen.musicList.size() > 0)
                                currentAlbum = SoundScreen.musicList.get(0).album;
                            intent = new Intent(appContext, SoundScreen.class);
                            startActivityForResult(intent, ALBUM_REQUEST_CODE);
                        } else {
                            headerText.setText(R.string.no_reading_granted);
                        }
                        return true;
                    case R.id.book_select_folder:
                        if (playing == 1) {
                            if (time_speaking_cfg) {
                                one_time_no_time_speak = true;
                            }
                            miPlayAndPause();
                        } else {
                            writeToFile();
                        }
                        checkAndRequestPermissions();
                        if (checkAndRequestPermissions()) {
                            Intent intent = new Intent(appContext, com.audiobook.pbp_service.SoundScreenFolder.class);
                            startActivityForResult(intent, SELECT_FOLDER_CODE);
                        } else {
                            headerText.setText(R.string.no_reading_granted);
                        }
                        return true;
                        case R.id.select_any_audio_file:
                            if (playing == 1) {
                                if (time_speaking_cfg) {
                                    one_time_no_time_speak = true;
                                }
                                miPlayAndPause();
                            } else {
                                writeToFile();
                            }
                            checkAndRequestPermissions();
                            if (checkAndRequestPermissions()) {
                                Intent intent = new Intent(appContext, com.audiobook.pbp_service.openAnyAudioFile.class);
                                startActivityForResult(intent, LOAD_ANY_AUDIO_FILE);
                            } else {
                                headerText.setText(R.string.no_reading_granted);
                            }
                            return true;
                            case R.id.bookmark:
                                intent = new Intent(appContext, com.audiobook.pbp_service.Bookmarks.class);
                                startActivityForResult(intent, BOOKMARKS_CODE);
                                return true;
                            case R.id.load_cover:
                                intent = new Intent(appContext, com.audiobook.pbp_service.LoadCovers.class);
                                startActivityForResult(intent, LOAD_COVERS_CODE);
                                return true;
                            case R.id.sleep_timer:
                                writeToFile();
                                intent = new Intent(appContext, com.audiobook.pbp_service.SleepTimer.class);
                                startActivityForResult(intent, SLEEP_TIMER_CODE);
                                return true;
                    case R.id.help_view:
                        writeToFile();
                        intent = new Intent(appContext, com.audiobook.pbp_service.HelpScreen.class);
                        startActivity(intent);
                        return true;
                    case R.id.tuning_settings:
                        writeToFile();
                        intent = new Intent(appContext, com.audiobook.pbp_service.CustomizingScreen.class);
                        startActivityForResult(intent, LEFT_HAND_REQUEST_CODE);
                        return true;
                    case R.id.choose_stat:
                        writeToFile();
                        directory = getFilesDir();
                        chooseDir = new File(appContext.getFilesDir(), "/choos_dir/");
                        if (!chooseDir.exists()) {
                            boolean result;
                            result = chooseDir.mkdirs();
                            if (!result)
                                Toast.makeText(appContext, appContext.getResources().getString(R.string.do_not_unload_file), Toast.LENGTH_LONG).show();
                        }
                        nameFile = nameFile.replaceAll("/", " ");
                        nameFile = nameFile.replaceAll("'", " ");
                        nameFile_out = nameFile + statistic_file_extention;
                        miFile_in = new File(directory, nameFile);
                        miFile_out = new File(chooseDir, nameFile_out);
                        try {
                            copyFile(miFile_in, miFile_out);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            fileUri = FileProvider.getUriForFile(
                                    MainActivity.this,
                                    "com.audiobook.powerbookplayer.fileprovider",
                                    miFile_out);
                        } catch (IllegalArgumentException e) {
                            Log.e("File Selector",
                                    "The selected file can't be shared: " + miFile_out.toString());
                        }

                        intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri); // Uri.fromFile(file)
                        title = appContext.getResources().getString(R.string.stat_file_title) + " " + selAlbum;
                        intent.putExtra(Intent.EXTRA_TITLE, title);
                        intent.putExtra(Intent.EXTRA_SUBJECT, title);
                        intent.setType("file/*");
                        startActivity(Intent.createChooser(intent, getString(R.string.menu_choose_stat)));
                        return true;
                    case R.id.choose_config:
                        writeConfig();
                        directory = getFilesDir();
                        chooseDir = new File(appContext.getFilesDir(), "/choos_dir/");
                        if (!chooseDir.exists()) {
                            boolean result;
                            result = chooseDir.mkdirs();
                            if (!result)
                                Toast.makeText(appContext, appContext.getResources().getString(R.string.do_not_unload_file), Toast.LENGTH_LONG).show();
                        }
                        nameFile = config_file_name;
                        nameFile_out = config_file_name.replaceAll("\\.", "_") + config_file_extention;
                        miFile_in = new File(directory, nameFile);
                        miFile_out = new File(chooseDir, nameFile_out);
                        try {
                            copyFile(miFile_in, miFile_out);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            fileUri = FileProvider.getUriForFile(
                                    MainActivity.this,
                                    "com.audiobook.powerbookplayer.fileprovider",
                                    miFile_out);
                        } catch (IllegalArgumentException e) {
                            Log.e("File Selector",
                                    "The selected file can't be shared: " + miFile_out.toString());
                        }

                        intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri); // Uri.fromFile(file)
                        title = appContext.getResources().getString(R.string.config_file_title);
                        intent.putExtra(Intent.EXTRA_TITLE, title);
                        intent.putExtra(Intent.EXTRA_SUBJECT, title);
                        intent.setType("file/*");
                        startActivity(Intent.createChooser(intent, getString(R.string.menu_choose_config)));
                        return true;
                    case R.id.choose_bookmarks:
                        writeToFile();
                        try {
                            write_book_marks();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        directory = getFilesDir();
                        chooseDir = new File(appContext.getFilesDir(), "/choos_dir/");
                        if (!chooseDir.exists()) {
                            boolean result;
                            result = chooseDir.mkdirs();
                            if (!result)
                                Toast.makeText(appContext, appContext.getResources().getString(R.string.do_not_unload_file), Toast.LENGTH_LONG).show();
                        }
                        nameFile = nameFile.replaceAll("'", " ");
                        nameFile = nameFile.replaceAll("/", " ") + ".bmx";
                        nameFile_out = nameFile.replaceAll("\\.", "_") + bookmarks_file_extention;
                        miFile_in = new File(directory, nameFile);
                        if (!miFile_in.exists()) {
                            Toast.makeText(appContext, appContext.getResources().getString(R.string.bookmarks_not_found), Toast.LENGTH_LONG).show();
                            return true;
                        }
                        miFile_out = new File(chooseDir, nameFile_out);
                        try {
                            copyFile(miFile_in, miFile_out);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            fileUri = FileProvider.getUriForFile(MainActivity.this, "com.audiobook.powerbookplayer.fileprovider", miFile_out);
                        } catch (IllegalArgumentException e) {
                            Log.e("File Selector", "The selected file can't be shared: " + miFile_out.toString());
                        }

                        intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(Intent.EXTRA_STREAM, fileUri); // Uri.fromFile(file)
                        title = appContext.getResources().getString(R.string.bookmarks_file_title);
                        intent.putExtra(Intent.EXTRA_TITLE, title);
                        intent.putExtra(Intent.EXTRA_SUBJECT, title);
                        intent.setType("file/*");
                        startActivity(Intent.createChooser(intent, getString(R.string.menu_choose_bookmarks)));
                        return true;
                    case R.id.load_stat:
                        workFileType = TYPE_WF_STAT;
                        intent = new Intent(appContext, com.audiobook.pbp_service.loadWorkFile.class);
                        startActivityForResult(intent, NEW_CONFIG_LOAD);
                        return true;
                    case R.id.load_config:
                        workFileType = TYPE_WF_CONFIG;
                        intent = new Intent(appContext, com.audiobook.pbp_service.loadWorkFile.class);
                        startActivityForResult(intent, NEW_CONFIG_LOAD);
                        return true;

                    case R.id.load_bookmarks:
                        workFileType = TYPE_WF_BOOKMARKS;
                        intent = new Intent(appContext, com.audiobook.pbp_service.loadWorkFile.class);
                        startActivityForResult(intent, NEW_CONFIG_LOAD);
                        return true;
                    case R.id.about_settings:
                        writeToFile();
                        intent = new Intent(appContext, com.audiobook.pbp_service.AboutScreen.class);
                        startActivity(intent);
                        return true;
                    case R.id.what_news:
                        writeToFile();
                        intent = new Intent(appContext, com.audiobook.pbp_service.WhatNews.class);
                        startActivity(intent);
                        return true;
                    case R.id.tech_support:
                        writeToFile();
                        intent = new Intent(appContext, com.audiobook.pbp_service.LinkTechSupport.class);
                        startActivity(intent);
                        return true;
                    case R.id.telegram_channel:
                        writeToFile();
                        intent = new Intent(appContext, com.audiobook.pbp_service.TelegramChan.class);
                        startActivity(intent);
                        return true;
                    case R.id.app_exit:
                        finish();
                        return true;
                }
                return true;
            }
        });
        popup.show();
    }

// копирование служебного файла без расширения в файл с расширением.
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    // обработчик нажатия кнопок на проводной гарнитуре
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {   // KEYCODE_MEDIA_PLAY   KEYCODE_HEADSETHOOK
            miPlayAndPause ();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void seekChange(View v) {
        lesson_point[lesson]= seekBar_part.getProgress();
        if (mediaPlayer!= null)
            mediaPlayer.seekTo(lesson_point[lesson]);
        show_passed_left_view ();
        show_future_hours ();
    }

//    @Override
    public static void speak(String text) {
        if (!ttsEnabled) return;
        ttsGreater21(text);
    }

    private static void ttsGreater21(String text) {
        String utteranceId = appContext.hashCode() + "";
        TTS.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
    }

    public void miPlayAndPause () {

        if (mediaPlayer != null) {
            if (!goto_bookmarks_in_fucking_apple_style_cfg) {
                try {
                    lesson_point[lesson] = mediaPlayer.getCurrentPosition();
                } catch (IllegalStateException e) {
                    lesson_point[lesson] = seekBar_part.getProgress();
                }
            }
            if (goto_bookmarks_in_fucking_apple_style_cfg) {
                mediaPlayer.seekTo(lesson_point[lesson]);
            }
        }
        else {
            lesson_point[lesson] = seekBar_part.getProgress();
        }
        if (playing == 1) {
            // освободить аудио фокус
            if (becomingNoisyReceiver.isOrderedBroadcast())
                unregisterReceiver(becomingNoisyReceiver); // отключение обработчика выдергивания наушников
            if (mediaController != null)
                mediaController.getTransportControls().pause();
        }
        if (playing == 0) {
            try{
                // проверка наличия аудиофокуса
/*                int audioFocusResult = audioManager.requestAudioFocus(
                        audioFocusChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                );
                if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                    return;
*/
                // бродкаст для анализа выдергивания наушников
                IntentFilter myPhoneIntent = new IntentFilter();
                myPhoneIntent.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
                registerReceiver(
                        becomingNoisyReceiver,
                        myPhoneIntent);
            }catch (IllegalStateException e) {
                e.printStackTrace();
                Log.e("Exception", "registerinf receiver failed: " + e.toString());

                // освободить аудиофокус
                unregisterReceiver(becomingNoisyReceiver); // отключение обработчика выдергивания наушников
            }
            if (sleep_timer_repeat  &&  sleep_timer_ended  &&  !wait_end_part) {
                writeToFile();
                buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_24);
                if (sleep_timer_repeat) {
                    buttonSleepTimer.setText("R");
                }
                else
                    buttonSleepTimer.setText("");
            }
            if (mediaController != null) {
                mediaController.getTransportControls().play();
            }
        }
    }

    // обработчик выдергивания наушников
    final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (bad_wire_correct) {
                } else {
                    if (playing == 1)
                        miPlayAndPause();
                }
            }
        }
    };

// for Android 12 and higher
    @Override
    public void onBackPressed() {
        if (!exit_only_in_menu_cfg  &&  !backPressed_switch_background_cfg) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (playing == 1)
                    miPlayAndPause();
                writeConfig();
            }
            super.onBackPressed();
        }
        if (backPressed_switch_background_cfg) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            home.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(home);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (no_file_writing)
            no_file_writing= false;
        else
            writeToFile();
        writeConfig();
        if (mediaPlayer != null) {
            if (time_speaking_cfg) {
                one_time_no_time_speak= true;
            }
            seekBar_part.destroyDrawingCache();
            show_progress.destroyDrawingCache();
            try {
                mediaPlayer.pause();
            } catch (Exception e) {
//                e.printStackTrace();
                Log.e("Exception", "Mediaplayer pause failed: " + e.toString());
            }
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                Log.e("Exception", "Mediaplayer stop failed: " + e.toString());
            }
            if (mediaController != null)
                mediaController.getTransportControls().stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            inDestroying = 1;
            playerServiceBinder = null;
            if (mediaController != null) {
                mediaController.unregisterCallback(callback);
                mediaController = null;
            }
            unbindService(serviceConnection);
        }
    }

    public void writeToFile() {

        if (playing== 1) {
            if (SoundScreen.musicList.size() > 0 && mediaPlayer != null) {
                //{Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted}
                try {
                    lesson_point[lesson] = mediaPlayer.getCurrentPosition();    // save the current position in file
                } catch (IllegalStateException e) {
                    lesson_point[lesson] = seekBar_part.getProgress();    // save the current position in file
                }
            } else
                if (seekBar_part != null) {
                    lesson_point[lesson] = seekBar_part.getProgress();             // save the current position in file
                }
        }

        oneFileWrite (WORK_FILE_NAME);  // work file writing
        if (!selAlbum.isEmpty()) {
            oneFileWrite(selAlbum);    // for local library file writing
        }
    }

    // write one status-file
    public static void oneFileWrite(String fileName) {
        try {
//            File directory = getFilesDir();
            File directory = directory_cfg;
            fileName= fileName.replaceAll("/", " ");
            File miFile = new File(directory, fileName);
            if (miFile.exists() == false)
                miFile.createNewFile();
            FileOutputStream fis = new FileOutputStream(miFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fis);
            textBuf = String.format("%d,%d,", lesson, reading_status);  // прослушиваемый сейчас трек и признак прочтения
            textBuf+= Float.toString(speed_play);                       // Скорость воспроизведения
            textBuf+= textBuf = String.format(",%s,", speed_step_005);  // признак изменения скорости с шагом 0.05
            textBuf+= Float.toString(curTG);                            // дополнительная громкость
            if (eqs!= null)                                             // параметры эквалайзера
                textBuf+= String.format(",%s,", eqs.toString());
            else
                textBuf+= ",,";
            textBuf+= Boolean.toString(equalizer_set);                  // признак активности эквалайзера
            textBuf+= "," + Integer.toString(format_work_file_version); // версия формата файла
            textBuf+= String.format(",%s", fucking_apple);              // признак файла в формате Apple (M4A, M4B)
            outputStreamWriter.write(textBuf);
            outputStreamWriter.write("\n");
            textBuf = String.format("%d\n", back_in_time);              // откат при паузе
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%d\n", SoundScreen.musicList.size());  // число частей книги
            outputStreamWriter.write(textBuf);
            for (int i= 0; i < SoundScreen.musicList.size(); i++) {
                textBuf = String.format("%s\n", SoundScreen.musicList.get(i).name);
                outputStreamWriter.write(textBuf);
                textBuf = String.format("%s\n", SoundScreen.musicList.get(i).album);
                outputStreamWriter.write(textBuf);
                textBuf = String.format("%s\n", SoundScreen.musicList.get(i).uri);
                outputStreamWriter.write(textBuf);
                textBuf = String.format("%s\n", SoundScreen.musicList.get(i).size);
                outputStreamWriter.write(textBuf);
                if (format_work_file_version== 0)
                    textBuf = String.format("%s\n", SoundScreen.musicList.get(i).artist);
                else
                    textBuf = String.format("%s%s%s\n", SoundScreen.musicList.get(i).artist, end_of_tag, SoundScreen.musicList.get(i).composer);
                outputStreamWriter.write(textBuf);
                textBuf = String.format("%d\n", SoundScreen.musicList.get(i).duration);
                outputStreamWriter.write(textBuf);
                if (i< MainActivity.LESSON_MAX- 1) {
                    textBuf = String.format("%d\n", lesson_point[i + 1]);
                }
                outputStreamWriter.write(textBuf);
            }
            outputStreamWriter.flush();
            fis.flush();
            outputStreamWriter.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static void readConfig() throws IOException {
        int cur_config_version;

        File miFile = new File(directory_cfg, config_file_name);
        if (!miFile.exists()) {  // конфигурационного файла нет, действуют умолчания
            return;
        }
            FileInputStream fis = new FileInputStream(miFile);
            if (fis != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                StringBuilder stringBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    textBuf = reader.readLine();  // get current file format version
                    if (textBuf != null)
                        cur_config_version = Integer.parseInt(textBuf);
                    else
                        cur_config_version = 0;
                    if (cur_config_version == 1) { // file format version is 1
                        textBuf = reader.readLine();  // left_handed_interface
                        if (textBuf != null)
                            left_hand_interface = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // keep_screen_active_anytime
                        if (textBuf != null)
                            keep_screen_anytime = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // duck_ignore
                        if (textBuf != null)
                            duck_ignore = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // bad_wire_correct
                        if (textBuf != null)
                            bad_wire_correct = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // themesAsSystem
                        if (textBuf != null)
                            themesAsSystem = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // themesAuto
                        if (textBuf != null)
                            themesAuto = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // themesLight
                        if (textBuf != null)
                            themesLight = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // themesDark
                        if (textBuf != null)
                            themesDark = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // full_brightness
                        if (textBuf != null)
                            full_brightness = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // directoryAlbum - иерархия вывода при выборе книги
                        if (textBuf != null)
                            directoryAlbum = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // show_System_Folders - искать аудио файлы в системных папках
                        if (textBuf != null)
                            show_system_folders = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // showOneAlbumInFolder - показывать альбом в папке даже если в ней всего один альбом
                        if (textBuf != null)
                            showOneAlbumInFolder = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // speed_play - скорость воспроизведения
                        if (textBuf != null) {
                            try {
                                speed_play = Float.parseFloat(textBuf);
                            } catch (NumberFormatException e) {
                                speed_play = 4;
                            }
                            if (speed_play> MAX_PLAY_SPEED)
                                speed_play= 4;
                            if (speed_play< 0)
                                speed_play= 4;
                        }
                        textBuf = reader.readLine();  // jump_weight - размер перемотки 15 / 60 секунд
                        if (textBuf != null) {
                            try {
                                jump_weight = Integer.parseInt(textBuf);
                            } catch (NumberFormatException e) {
                                jump_weight = 15;
                            }
                        }
                        textBuf = reader.readLine();  // buttons transparency - прозрачность кнопок
                        if (textBuf != null) {
                            try {
                                buttonsTransparency = Integer.parseInt(textBuf);
                            } catch (NumberFormatException e) {
                                buttonsTransparency = 5;
                            }
                            if (buttonsTransparency > 6)
                                buttonsTransparency = 5;
                            if (buttonsTransparency < 0)
                                buttonsTransparency = 5;
                        }
                        textBuf = reader.readLine();  // симметричные кнопки
                        if (textBuf != null)
                            simetric_interface = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // цвет кнопок
                        if (textBuf != null) {
                            try {
                                button_color = Integer.parseInt(textBuf);
                            } catch (NumberFormatException e) {
                                button_color = 0;
                            }
                        }
                        textBuf = reader.readLine();  // размер кнопок
                        if (textBuf != null) {
                            try {
                                buttons_size = Integer.parseInt(textBuf);
                            } catch (NumberFormatException e) {
                                buttons_size = 0;
                            }
                        }
                        textBuf = reader.readLine();  // режим повтора
                        if (textBuf != null) {
                            try {
                                repeat_state = Integer.parseInt(textBuf);
                            } catch (NumberFormatException e) {
                                repeat_state = NO_REPEAT;
                            }
                        }
                        textBuf = reader.readLine();  // boldFontForLongNames - выводить название книги крупным шрифтом для длинных имен
                        if (textBuf != null)
                            boldFontForLongNames = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // equalizer_set - признак активности эквалайзера
                        if (textBuf != null)
                            equalizer_set = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // eqs - параметры эквалайзера
                        if (textBuf != null  &&  !textBuf.equals(""))
                            eqs = new Equalizer.Settings(textBuf);
                        textBuf = reader.readLine();  // non_stop_after_change_orientation - не останавливать воспроизведение при сменее ориентации
                        if (textBuf != null)
                            non_stop_after_change_orientation = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // show_cover - показывать обложки
                        if (textBuf != null)
                            show_cover = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // speed_step_005 - шаг изменения скорости кнопками 0.05
                        if (textBuf != null)
                            speed_step_005 = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();    // использовать папку для хранения аудиокниг
                        if (textBuf != null)
                            use_root_folder = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();    // папка для хранения аудиокниг
                        if (textBuf != null)
                            root_folder_path = textBuf;
                        textBuf = reader.readLine();  // always_begin_part - при прямом переходе между главами всегда. начинать главу с начала
                        if (textBuf != null)
                            always_begin_part = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // time_speaking - проговаривать текущее время при нажатии кнопки Плэй
                        if (textBuf != null)
                            time_speaking_cfg = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // right_hand_interface - стандартный интерфейс
                        if (textBuf != null)
                            right_hand_interface = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // one_finger_right_interface - интерфейс для одного пальца правая рука
                        if (textBuf != null)
                            one_finger_right_interface = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // one_finger_left_interface - интерфейс для одного пальца левая рука
                        if (textBuf != null)
                            one_finger_left_interface = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // nosave_speed_for_newbooks - не сохранять настройки скорости, громкости и эквалайзера для новых книг
                        if (textBuf != null)
                            nosave_speed_for_newbooks = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // sleep_timer_parts_value_mem - таймер сна, части
                        if (textBuf != null)
                            sleep_timer_parts_value_mem = Integer.parseInt(textBuf);
                        textBuf = reader.readLine();  // sleep_timer_time_value_mem - таймер сна, время
                        if (textBuf != null)
                            sleep_timer_time_value_mem = Integer.parseInt(textBuf);
                        textBuf = reader.readLine();  // is_LightTheme_Manual_Set ручная установка светлой темы
                        //   нужно для принудительного перевода старых версий со светлой темы на темную
                        if (textBuf != null)
                            is_LightTheme_Manual_Set = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // always_show_favorites_cfg - показывать избранное при любых настройках отбора книг
                        if (textBuf != null)
                            always_show_favorites_cfg = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // other_players_not_ignore - режим не игнорирования других плееров
                        if (textBuf != null)
                            other_players_not_ignore = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // exit_only_in_menu_cfg - режим выхода только через меню
                        if (textBuf != null)
                            exit_only_in_menu_cfg = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // backPressed_switch_background_cfg - нажатие кнопки назад переключает в фоновый режим
                        if (textBuf != null)
                            backPressed_switch_background_cfg = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // new_sleep_timer_mode_cfg - новый формат задания таймера сна
                        if (textBuf != null)
                            new_sleep_timer_mode_cfg = Boolean.parseBoolean(textBuf);
                        if (!new_sleep_timer_mode_cfg) {
                            sleep_timer_time_value_mem*= 5;
                            new_sleep_timer_mode_cfg= true;
                        }
                        textBuf = reader.readLine();  // swap_fastMoving_goto_cfg -
                                                      // обмен клавиш быстрой перемотки и перехода по главам
                                                      // на гарнитуре и на экране блокировки
                        if (textBuf != null)
                            swap_fastMoving_goto_cfg = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // swap_fast_moving_cfg -
                        // обмен клавиш быстрой перемотки на BT-гарнитуре
                        if (textBuf != null)
                            swap_fast_moving_cfg = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // проговаривать время при начале воспроизведения
                        if (textBuf != null)
                            time_speaking_play_cfg = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();  // проговаривать время при остановке воспроизведения
                        if (textBuf != null)
                            time_speaking_pause_cfg = Boolean.parseBoolean(textBuf);
                        if (!time_speaking_play_cfg  &&  !time_speaking_pause_cfg) {
                            time_speaking_play_cfg= time_speaking_pause_cfg= true;
                        }
                        textBuf = reader.readLine();  // пользовательский шаг перемотки
                        if (textBuf != null)
                            fast_moving_cust = Integer.parseInt(textBuf);
                        if (fast_moving_cust== 0)
                            fast_moving_cust= 12;
                        if (jump_weight!= 15)
                            jump_weight= fast_moving_cust* 5;
                        textBuf = reader.readLine();  // back_in_time == 0 при смене ориентации
                        if (textBuf != null)
                            zero_back_in_time_after_change_orient_cfg = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();    // show_artist
                        if (textBuf != null)            // выводить артиста
                            show_artist = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();    // show_composer
                        if (textBuf != null)            // выводить композитора
                            show_composer = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();    // keep_portrait
                        if (textBuf != null)            // всегда сохранять портретную ориентацию
                            keep_portrait = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();    // show_full_book_name
                        if (textBuf != null)            // Всегда выводить полное имя книги
                            show_full_book_name = Boolean.parseBoolean(textBuf);
                        textBuf = reader.readLine();    // goto_bookmarks_in_fucking_apple_style_cfg
                        if (textBuf != null)            // переход между закладками кнопками перехода по трекам
                            goto_bookmarks_in_fucking_apple_style_cfg = Boolean.parseBoolean(textBuf);

                    }
                    fis.close();
                }
            }
    }

    private boolean writeConfig () {
// format versions:
// 1 - contain only flag for lef_handed_interface
//
        try {
            File directory = getFilesDir();
            File miFile = new File(directory, config_file_name);
            if (!miFile.exists())
                miFile.createNewFile();
            FileOutputStream fis = new FileOutputStream(miFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fis);
            textBuf = String.format("%d\n", CONFIG_FILE_VERSION);  // current file format version
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", left_hand_interface);   // Интерфейс для левшей
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", keep_screen_anytime);   // Никогда не выключать экран
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", duck_ignore);           // Игнорировать СМС и навигатор
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", bad_wire_correct);      // корректировать плохой провод
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", themesAsSystem);        // Тема приложения определяется ситемой
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", themesAuto);            // Тема приложения определяется веременм суток
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", themesLight);           // Тема приложения - светлая
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", themesDark);            // Тема приложения - темная
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", full_brightness);       // Режим максимальной яркости экрана
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", directoryAlbum);       // Иерархия вывода при выборе книги
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", show_system_folders);  // Искать аудиофайлы в системных папках
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", showOneAlbumInFolder); // Показывать альбом даже если он один в папке
            outputStreamWriter.write(textBuf);
            textBuf = Float.toString(speed_play);                  // Скорость воспроизведения
            outputStreamWriter.write(textBuf);
            outputStreamWriter.write("\n");
            textBuf = String.format ("%d\n", jump_weight);               // величина перемотки 15 / 60
            outputStreamWriter.write(textBuf);
            textBuf = String.format ("%d\n", buttonsTransparency);       // Прозрачность кнопок
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", simetric_interface);        // Симметричный интерфейс
            outputStreamWriter.write(textBuf);
            textBuf = String.format ("%d\n", button_color);             // Цвет кнопок
            outputStreamWriter.write(textBuf);
            textBuf = String.format ("%d\n", buttons_size);             // Размер кнопок
            outputStreamWriter.write(textBuf);
            textBuf = String.format ("%d\n", repeat_state);             // Режим повтора
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", boldFontForLongNames);      // выводить название книги крупным шрифтом для длинных имен
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", equalizer_set);             // признак активности эквалайзера
            outputStreamWriter.write(textBuf);
            if (eqs== null)                                             // параметры эквалайзера
                textBuf = String.format("\n");
            else
                textBuf = String.format("%s\n", eqs.toString());
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", non_stop_after_change_orientation);    // не остнавливать воспроизведение при смене ориентации
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", show_cover);    // показывать обложки
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", speed_step_005);    // шаг изменения скорости кнопками 0.05
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", use_root_folder);    // использовать папку для хранения аудилкниг
            outputStreamWriter.write(textBuf);
            if (root_folder_path == null) {    // папка для хранения аудиокниг
                textBuf = "\n";
            }
            else {
                textBuf = String.format("%s\n", root_folder_path);
            }
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", always_begin_part);    // припрямом переходе между главами, всегда начинать главу с начала
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", time_speaking_cfg);    // проговаривать время при нажатии кнопки Плэй
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", right_hand_interface);    // стандартный интерфейс
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", one_finger_right_interface);    // один палец правая рука
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", one_finger_left_interface);    // один палец левая рука
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", nosave_speed_for_newbooks);    // не сохранять настройки скорости, громкости и эквалайзера для новых книг
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%d\n", sleep_timer_parts_value_mem);   // значение таймера сна части
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%d\n", sleep_timer_time_value_mem);   // значение таймера сна время
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", is_LightTheme_Manual_Set);   // значение таймера сна время
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", always_show_favorites_cfg);   // показывать избранное при любых настройках
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", other_players_not_ignore);   // не игнорировать другие плееры
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", exit_only_in_menu_cfg);   // выход только через пункт меню
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", backPressed_switch_background_cfg);   // Нажатие кнопки назад переводит в фоновый режим
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", new_sleep_timer_mode_cfg);   // новый формат задания таймера сна
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", swap_fastMoving_goto_cfg);   // swap_fastMoving_goto_cfg -
                                                                         // обмен клавиш быстрой перемотки и перехода по главам
                                                                         // на гарнитуре и на экране блокировки
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", swap_fast_moving_cfg);       // swap_fast_moving_cfg -
                                                                         // обмен клавиш быстрой перемотки на BT-гарнитере
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", time_speaking_play_cfg);     // проговаривать время при запуске воспроизведения
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", time_speaking_pause_cfg);     // проговаривать время при остановке воспроизведения
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%d\n", fast_moving_cust);            // пользвательский шаг перемотки
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", zero_back_in_time_after_change_orient_cfg); // нулевой back_in_time при смене ориентации
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", show_artist);       // Режим вывода значения тега Artist
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", show_composer);       // Режим вывода значения тега Composer
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", keep_portrait);       // Режим сохранения портретной ориентации
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", show_full_book_name); // Режим не обрезать длинные имена книг
            outputStreamWriter.write(textBuf);
            textBuf = String.format("%s\n", goto_bookmarks_in_fucking_apple_style_cfg); // Режим перехода между закладками по кнопкам перехода между треками
            outputStreamWriter.write(textBuf);

            outputStreamWriter.flush();
            fis.flush();
            outputStreamWriter.close();
            fis.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("Exception", "Config file write failed: " + e.toString());
        } catch (IOException e) {
            Log.e("Exception", "Config file write failed: " + e.toString());
            e.printStackTrace();
        }
        return false;

    }

    // обработчик выбора левой / правой руки
    // обработчик отключения ьлокировки экрана приложения
    // обработчик отключения пропуска звука для DUCK (СМС и навигатор)
    // обработчик выбора альбома (взятие книги с полки) все папки
    // обработчик выбора альбома из конкретной папки
    // обработчик выбора темы приложения
    // обработчик выбора режима полной яркости экрана
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        one_file_and_BMs= one_file_and_BMs_test();
        if (requestCode == LEFT_HAND_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (need_hand_change                               ||
                    need_screen_keeping_change                     ||
                    need_duck_ignore_change                        ||
                    need_other_players_not_ignore                  ||
                    need_bad_wire_change                           ||
                    need_full_brightness_change                    ||
                    need_show_artist                               ||
                    need_show_composer                             ||
                    need_show_system_folders                       ||
                    need_showOneAlbumInFolder                      ||
                    need_boldFontForLongNames                      ||
                    need_transparency_change                       ||
                    need_buttons_size_change                       ||
                    need_non_stop                                  ||
                    need_show_cover                                ||
                    need_speed_step_005                            ||
                    need_use_root_folder                           ||
                    need_always_begin_part                         ||
                    need_time_speaking                             ||
                    need_nosave_speed_for_newbooks                 ||
                    need_always_show_favorites_cfg                 ||
                    need_exit_only_in_menu_cfg                     ||
                    need_backPressed_switch_background_cfg         ||
                    need_swap_fastMoving_goto                      ||
                    need_swap_fast_moving                          ||
                    need_back_in_time_cust                         ||
                    need_show_full_book_name                       ||
                    need_goto_bookmarks_in_fucking_apple_style_cfg) {
                    writeToFile();
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        if (need_hand_change) {
                            callback = null;
                            serviceConnection = null;
                            need_hand_change= false;
                            writeConfig();
                            recreate();
                        }
                    }
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        if (left_hand_interface != new_left_hand_interface) {
                            left_hand_interface = new_left_hand_interface;
                            callback = null;
                            serviceConnection = null;
                            setContentView(R.layout.activity_main);
                        }
                    }
                    writeConfig();
                    if (need_screen_keeping_change) {
                        if (keep_screen_anytime) {
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
                        } else {
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                        need_screen_keeping_change = false;
                    }
                    if (need_back_in_time_cust) {
                        need_back_in_time_cust= false;
                        radio_6sec.setText("" + (back_in_time_cust+ 6));
                        back_in_time= back_in_time_cust+ 6;
                    }
                    if (need_duck_ignore_change) {
                        need_duck_ignore_change = false;
                    }
                    if (need_other_players_not_ignore) {
                        need_other_players_not_ignore = false;
                    }
                    if (need_bad_wire_change) {
                        need_bad_wire_change = false;
                    }
                    if (need_full_brightness_change)
                        need_full_brightness_change= false;
                    if (need_show_artist)
                        need_show_artist= false;
                    if (need_show_composer)
                        need_show_composer= false;
                    if (need_show_system_folders)
                        need_show_system_folders= false;
                    if (need_showOneAlbumInFolder)
                        need_showOneAlbumInFolder= false;
                    if (need_boldFontForLongNames)
                        need_boldFontForLongNames= false;
                    if (need_transparency_change)
                        need_transparency_change= false;
                    if (need_buttons_size_change)
                        need_buttons_size_change= false;
                    if (need_non_stop)
                        need_non_stop= false;
                    if (need_show_cover)
                        need_show_cover= false;
                    if (need_speed_step_005) {
                        need_speed_step_005 = false;
                        if (!speed_step_005) {
                            speed_play= Math.round (speed_play* 10)/ 10;    // округление, чтобы отбросить 0.5 у скорости
                        }
                    }
                    if (need_use_root_folder)
                        need_use_root_folder= false;
                    if (need_always_begin_part)
                        need_always_begin_part= false;
                    if (need_time_speaking)
                        need_time_speaking= false;
                    if (need_nosave_speed_for_newbooks)
                        need_nosave_speed_for_newbooks= false;
                    if (need_always_show_favorites_cfg)
                        need_always_show_favorites_cfg= false;
                    if (need_exit_only_in_menu_cfg)
                        need_exit_only_in_menu_cfg= false;
                    if (need_backPressed_switch_background_cfg)
                        need_backPressed_switch_background_cfg= false;
                    if (need_swap_fastMoving_goto)
                        need_swap_fastMoving_goto= false;
                    if (need_swap_fast_moving)
                        need_swap_fast_moving= false;
                    if (need_show_full_book_name)
                        need_show_full_book_name= false;
                    if (need_goto_bookmarks_in_fucking_apple_style_cfg)
                        need_goto_bookmarks_in_fucking_apple_style_cfg= false;
                    try {
                        initViews();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (need_fast_moving_cust) {
                    need_fast_moving_cust= false;
                    radio_jump60.setText("" + (fast_moving_cust* 5));
                    jump_weight= fast_moving_cust* 5;
                    writeConfig();
                }

                if (themes_change) {
                    if (themesAsSystem)
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                    if (themesAuto)
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_TIME);
                    if (themesLight)
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                    if (themesDark)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    writeToFile();
                    writeConfig();
                    themes_change= false;
                    onReCreate= true;
                    recreate();
                }

                if (need_keep_portrait) {
                    need_keep_portrait= false;
                    if (keep_portrait)
                        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    if (!keep_portrait)
                        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    writeToFile();
                    writeConfig();
                    onReCreate= true;
                    recreate();
                }

            }
        }

        if (requestCode == NEW_CONFIG_LOAD) {
            if (resultCode == Activity.RESULT_OK) {
                if (selAlbum.length()> 0) {
                    try {
                        oneFileRead(selAlbum);
                        show_progress.setProgress(lesson - 1);
                        seekBar_part.setProgress(lesson_point[lesson]);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    onReCreate= true;
                    recreate();
                }
           }
        }

        if (requestCode == LOAD_COVERS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                onReCreate= true;
                recreate();
            }
        }

        if (requestCode == ALBUM_REQUEST_CODE
           ||  requestCode == LOAD_ANY_AUDIO_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                selAlbum = data.getStringExtra(SoundScreen.ALBUM_NAME);
                no_change_album = 0;
                reading_status= DO_NOT_READ;
                actionsList.clear();
                Bookmarks.bookmark_repeate_mode= false;
                Bookmarks.begin_repeate= Bookmarks.end_repeate= -1;
                repeat_state= NO_REPEAT;
                if (playing == 1) {
                    playing = 0;
                    if (becomingNoisyReceiver.isOrderedBroadcast())
                        unregisterReceiver(becomingNoisyReceiver); // отключение обработчика выдергивания наушников
                }
                else {
                    no_change_album = 1;
                }
                if (fucking_apple) {
                    if (requestCode == LOAD_ANY_AUDIO_FILE) {
                        selAlbum= SoundScreen.musicList.get(0).name;
                    }
                    try {
                        m4a_processing(SoundScreen.album_uri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (requestCode == ALBUM_REQUEST_CODE
                &&  resultCode == 0) {
                selAlbum= "";
                no_change_album = 0;
            }
            if (mediaController != null)
                mediaController.getTransportControls().prepare();
            try {
                initViews();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (requestCode == SELECT_FOLDER_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                startFolder = data.getStringExtra(SoundScreenFolder.SELECTED_FOLDER_NAME);
                if (startFolder.length()> 0) {
                    if (startFolder.endsWith("/"))
                        startFolder= startFolder.substring(0, startFolder.length()- 1);
                }
                if (selAlbum.length() > 0)
                    headerText.setText(selAlbum);
                else
                    headerText.setText(R.string.book_not_sel);
                try {
                    existInFavorShow(selAlbum, current_directory);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (playing == 1)
                    miPlayAndPause();
                else
                    writeToFile();
                if (SoundScreen.musicList.size() > 0)
                    currentAlbum = SoundScreen.musicList.get(0).album;
                intent = new Intent(this, SoundScreen.class);
                startActivityForResult(intent, ALBUM_REQUEST_CODE);
            }
        }


        if (requestCode == PERMISSION_STORAGE  ||  requestCode == PERMISSIONS_REQUEST_CODE) {
                if (!checkAndRequestPermissions()) {
                    Snackbar.make(findViewById(R.id.main_layout), R.string.photo_media_access_required, Snackbar.LENGTH_SHORT).show();
                }
        }

        if (requestCode == EQUALIZER_CODE) {
            if (equalizer_set) {
                buttonEqualizer.setImageResource(R.drawable.ic_equalizer_button);
            }
            else {
                if (button_color== BLACK)
                    buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active_white);
                else
                    buttonEqualizer.setImageResource(R.drawable.ic_equ_not_active);
            }
        }

        if (requestCode == SLEEP_TIMER_CODE) {
            if (need_sleep_timer) {
                need_sleep_timer= false;
                writeConfig();
                if (sleep_timer_time || sleep_timer_parts) {
                    buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_24);
                    if (sleep_timer_repeat) {
                        buttonSleepTimer.setText("R");
                    }
                    else
                        buttonSleepTimer.setText("");
                }
                else {
                    buttonSleepTimer.setIconResource(R.drawable.ic_bedtime_off_24);
                    if (sleep_timer_repeat) {
                        buttonSleepTimer.setText("R");
                    }
                    else
                        buttonSleepTimer.setText("");
                }
            }
        }
        if (requestCode == BOOKMARKS_CODE  &&  resultCode== Activity.RESULT_OK) {
            writeConfig();
            bookmarks_draw();
            if (repeat_state== BOOKMARK_REPEAT)
                buttonRepeat.setImageResource(R.drawable.ic_repeat_bookmarks);
        }

    }

    private void m4a_processing (Uri afn) throws IOException {
        int i, numTracks;

        MediaExtractor mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(appContext, afn, null);

// select the first audio track in the file and return it's format
        MediaFormat mediaFormat = null;
        numTracks = mediaExtractor.getTrackCount();
        for (i = 0; i < numTracks; i++) {
            mediaFormat = mediaExtractor.getTrackFormat(i);
            if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("text/")) {
                mediaExtractor.selectTrack(i);
                break;
            }
        }

        int minBuffSize = 1024 * 260;
        ByteBuffer inputBuffer = ByteBuffer.allocate(minBuffSize);

        String sampleName= "";
        ArrayList<Long> Chapters = new ArrayList<>(26);
        ArrayList<String> ChaptNames = new ArrayList<>(1);
        for (i= 0; mediaExtractor.readSampleData(inputBuffer, 0) >= 0; i++) {
            long trackIndex = mediaExtractor.getSampleTime()/ 1000;
            if (trackIndex > Integer.MAX_VALUE)
                trackIndex= Integer.MAX_VALUE;
            sampleName= StandardCharsets.UTF_8.decode(inputBuffer).toString();
            if (sampleName.length()> 1  &&  sampleName.contains("\f")) {
                sampleName = sampleName.substring(2, sampleName.indexOf("\f", 2));
            }
            Chapters.add(i, trackIndex);
            ChaptNames.add(i, sampleName);
            mediaExtractor.advance();
        }
        inputBuffer.clear();

        Bookmarks.bookMarkList.clear();
        Bookmarks.get_bm_file();
        for (i= 0; i< Chapters.size(); i++) {
            if (ChaptNames.get(i).isEmpty())
                sampleName= getString(R.string.part_text) + " " + (i+ 1) + ", ";
            else
                sampleName= ChaptNames.get(i);
            Bookmarks.bookMarkList.add(
                    new Bookmarks.bookMarkElement(sampleName,
                            1, Math.toIntExact(Chapters.get(i)), Bookmarks.BOOKMARK_TYPE_CONTENT, false));
        }
        try {
            write_book_marks();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return;
    }

    public void testBrightness() {
        if (full_brightness) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = BRIGHTNESS_OVERRIDE_FULL;
            getWindow().setAttributes(layoutParams);
        }
        if (!full_brightness) {
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                layoutParams.screenBrightness = -1;
                getWindow().setAttributes(layoutParams);
        }
    }

    private boolean getEmbeddedImage (String curAlbum) throws FileNotFoundException {

        FileInputStream in = null;
        File directory = getFilesDir();
        String fileName= curAlbum + ".png";
        fileName= fileName.replaceAll("/", " ");
        File miFile = new File(directory, fileName);
        if (miFile.exists() == false)
            return false;
        in = new FileInputStream(miFile);
        try {
            thumbnail= BitmapFactory.decodeStream(in);
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
        if (thumbnail!= null)
            return true;
        return false;

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.put(Manifest.permission.READ_MEDIA_IMAGES, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.READ_MEDIA_AUDIO, PackageManager.PERMISSION_GRANTED);
//                    perms.put(Manifest.permission.READ_MEDIA_VIDEO, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.POST_NOTIFICATIONS, PackageManager.PERMISSION_GRANTED);
                }
                else  {
                    perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                    perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                }
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (perms.get(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
//                            && perms.get(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(this, appContext.getResources().getString(R.string.storage_permission_available), Toast.LENGTH_LONG).show();
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)
                                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)
//                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_VIDEO)
                                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
                            ) {
                                showDialogOK(this.getResources().getString(R.string.requred_permissions_text),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        checkAndRequestPermissions();
                                                        break;
                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                        Toast.makeText(MainActivity.appContext, appContext.getResources().getString(R.string.requred_permissions_text), Toast.LENGTH_LONG).show();
                                                        break;
                                                }
                                            }
                                        });
                            }
                            else {
                                permissionSettingScreen();
                            }
                        }
                    }else {
                        if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                                && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Toast.makeText(this, appContext.getResources().getString(R.string.storage_permission_available), Toast.LENGTH_LONG).show();
                            //else any one or both the permissions are not granted
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            ) {
                                showDialogOK(this.getResources().getString(R.string.requred_permissions_text),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        checkAndRequestPermissions();
                                                        break;
                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                        // proceed with logic by disabling the related features or quit the app.
                                                        Toast.makeText(MainActivity.appContext, appContext.getResources().getString(R.string.requred_permissions_text), Toast.LENGTH_LONG).show();
                                                        // permissionSettingScreen ( );
                                                        //  finish();
                                                        break;
                                                }
                                            }
                                        });
                            }
                            else {
                                permissionSettingScreen();
                            }
                        }
                    }
                }
            }
        }
    }

    private void permissionSettingScreen() {
        Toast.makeText(this, appContext.getResources().getString(R.string.enable_all_perms), Toast.LENGTH_LONG)
                .show();

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
        // finishAffinity();
        finish();

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    public boolean checkAndRequestPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (Environment.isExternalStorageManager())
                return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionReadExternalStorage = ContextCompat.checkSelfPermission(MainActivity.appContext,
                    android.Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissionReadExternalStorage = ContextCompat.checkSelfPermission(MainActivity.appContext,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionWriteExternalStorage = ContextCompat.checkSelfPermission(MainActivity.appContext,
                    android.Manifest.permission.READ_MEDIA_AUDIO);
        } else {
            permissionWriteExternalStorage = ContextCompat.checkSelfPermission(MainActivity.appContext,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                listPermissionsNeeded.add(android.Manifest.permission.READ_MEDIA_AUDIO);
            else
                listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                listPermissionsNeeded.add(android.Manifest.permission.READ_MEDIA_IMAGES);
            else
                listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int notificationPermission = ContextCompat.checkSelfPermission(MainActivity.appContext,
                    android.Manifest.permission.POST_NOTIFICATIONS);
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[0]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    public boolean existInFavorShow (String album, String dirWay) throws IOException {
        File directory = getFilesDir();
        String localAlb = null, localDir = null;

        if (button_color== BLACK)
            favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_white);
        else
            favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground);

        File miFile = new File(directory, MainActivity.FAVORITE_LIST_FILE_NAME);
        if (album.isEmpty())
            return false;
        if (miFile.exists()) {
            FileInputStream fis = new FileInputStream(miFile);
            if (fis != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    while ((MainActivity.textBuf = reader.readLine()) != null) {
                        if (MainActivity.textBuf.indexOf("#") >= 0) {
                            localAlb = MainActivity.textBuf.substring(0, MainActivity.textBuf.indexOf("#"));
                            localDir = MainActivity.textBuf.substring(MainActivity.textBuf.indexOf("#") + 1);
                        }
                        else {
                            localAlb= textBuf;
                            localDir= "";
                        }
                        if (dirWay.isEmpty()  &&  localAlb.equals(album)) {
                            fis.close();
                            favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
                            return true;
                        }
                        if (localDir.isEmpty()  &&  localAlb.equals(album)) {
                            fis.close();
                            favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
                            return true;
                        }
                        if (localAlb.equals(album)  &&  localDir.equals(dirWay)) {
                            fis.close();
                            favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
                            return true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fis.close();
                return false;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public void addToFavoritesShow (String album, String dirWay) throws IOException {

        File directory = getFilesDir();

        File miFile = new File(directory, MainActivity.FAVORITE_LIST_FILE_NAME);

        if (miFile.exists()) {
            FileInputStream fis = new FileInputStream(miFile);
            if (fis != null) {
                fis.close();
                FileOutputStream fos = new FileOutputStream(miFile, true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
                MainActivity.textBuf = String.format("%s#%s\n", album, dirWay);
                outputStreamWriter.write(MainActivity.textBuf);
                outputStreamWriter.close();
                fis.close();
                favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
                return;
            }
            else {
                fis.close();
                miFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(miFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
                MainActivity.textBuf = String.format("%s#%s\n", album, dirWay);
                outputStreamWriter.write(MainActivity.textBuf);
                outputStreamWriter.close();
                favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
                return;
            }
        }
        else {
            miFile.createNewFile();
            FileOutputStream fis = new FileOutputStream(miFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fis);
            MainActivity.textBuf = String.format("%s#%s\n", album, dirWay);
            outputStreamWriter.write(MainActivity.textBuf);
            outputStreamWriter.close();
            favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
            return;
        }
    }

    public void delOneFavoriteShow (String album, String dirWay) throws IOException {

        String albums[], dirs[];
        String localAlb, localDir= "";
        localAlb = album;
        albums= new String[SoundScreen.MAX_FAVORITE_SIZE];
        dirs= new String[SoundScreen.MAX_FAVORITE_SIZE];
        File directory = getFilesDir();
        File miFile = new File(directory, MainActivity.FAVORITE_LIST_FILE_NAME);
        if (!miFile.exists()) {
            return;
        }
        FileInputStream fis = new FileInputStream(miFile);
        if (fis != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                for (int i = 0; i < SoundScreen.MAX_FAVORITE_SIZE; i++) {
                    MainActivity.textBuf = reader.readLine();  // get album name and dir name
                    if (MainActivity.textBuf== null)
                        break;
                    localAlb= MainActivity.textBuf.substring(0, MainActivity.textBuf.indexOf("#"));
                    localDir= MainActivity.textBuf.substring(MainActivity.textBuf.indexOf("#")+ 1);
                    albums[i]= localAlb;
                    dirs[i]= localDir;
                    if (localAlb.equals(album)  &&  localDir.equals(dirWay)) {
                        albums[i]= dirs[i]= null;
                        i--;
                        continue;
                    }
                    if (dirWay.isEmpty()  &&  localAlb.equals(album)) {
                        albums[i]= dirs[i]= null;
                        i--;
                        continue;
                    }
                }
            }
            fis.close();
            delOneFile(MainActivity.FAVORITE_LIST_FILE_NAME);
        }

        miFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(miFile);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
        for (int i= 0; i< SoundScreen.MAX_FAVORITE_SIZE; i++) {
            if ((albums[i] == null || albums[i].isEmpty())  && (dirs[i] == null || dirs[i].isEmpty()))
                break;
            MainActivity.textBuf = String.format("%s#%s\n", albums[i], dirs[i]);
            outputStreamWriter.write(MainActivity.textBuf);
        }
        outputStreamWriter.close();
        if (button_color== BLACK)
            favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground_white);
        else
            favoritButton.setImageResource(R.drawable.ic_favorite_icon_foreground);

    }

    public void delOneFile (String fileName) {
        File file=new File(fileName);
        file.delete();
    }

    public static void  show_MPC(int counter_cnange) {

        mediaPlayerCounter+= counter_cnange;
        ad_volume.setText("mediaPlayerCounter " + mediaPlayerCounter);
    }

    public void touch_mode_draw () {

        if (not_touch_mode== ALL_TOUCH_DISABLED) {
            touchButton.setImageResource(R.drawable.ic_touch_app);
            seekBar_part.setEnabled(false);
            addVolumeControl.setEnabled(false);
            show_progress.setEnabled(false);
            radio_0sec.setEnabled(false);
            radio_2sec.setEnabled(false);
            radio_4sec.setEnabled(false);
            radio_6sec.setEnabled(false);
            radio_jump15.setEnabled(false);
            radio_jump60.setEnabled(false);
//            transparency_in_dont_touch_mode= buttonsTransparency;
            buttonPlayPause.setAlpha((float) 0.1);
            buttonPrev.setAlpha((float) 0.1);
            buttonNext.setAlpha((float) 0.1);
            buttonPrev10.setAlpha((float) 0.1);
            buttonNext10.setAlpha((float) 0.1);
            speed_down_button.setAlpha((float) 0.1);
            speed_up_button.setAlpha((float) 0.1);
            buttonRepeat.setAlpha((float) 0.1);
            buttonSleepTimer.setAlpha((float) 0.1);
            buttonEqualizer.setAlpha((float) 0.1);
            buttonShare.setAlpha((float) 0.1);
            buttonUndo.setAlpha((float) 0.1);
            favoritButton.setAlpha((float) 0.1);
            addBookmarkButton.setAlpha((float) 0.1);
            partSelectButton.setAlpha((float) 0.1);
            menuButton.setAlpha((float) 0.1);
            radio_0sec.setAlpha((float) 0.1);
            radio_2sec.setAlpha((float) 0.1);
            radio_4sec.setAlpha((float) 0.1);
            radio_6sec.setAlpha((float) 0.1);
            radio_jump15.setAlpha((float) 0.1);
            radio_jump60.setAlpha((float) 0.1);
            touchButton.setAlpha((float) ((float) 1.0));
        }
        if (not_touch_mode== PLAY_PAUSE_ONLY) {
            buttonPlayPause.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        }
        if (not_touch_mode== ALL_TOUCH_ENABLED) {
            if (button_color== BLACK)
                touchButton.setImageResource(R.drawable.ic_do_not_touch_white);
            else
                touchButton.setImageResource(R.drawable.ic_do_not_touch);
            seekBar_part.setEnabled(true);
            addVolumeControl.setEnabled(true);
            show_progress.setEnabled(true);
            radio_0sec.setEnabled(true);
            radio_2sec.setEnabled(true);
            radio_4sec.setEnabled(true);
            radio_6sec.setEnabled(true);
            radio_jump15.setEnabled(true);
            radio_jump60.setEnabled(true);
//            buttonsTransparency= transparency_in_dont_touch_mode;
            buttonPlayPause.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            buttonPrev.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            buttonNext.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            buttonPrev10.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            buttonNext10.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            speed_down_button.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            speed_up_button.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            buttonRepeat.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            buttonSleepTimer.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            buttonEqualizer.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            buttonShare.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            buttonUndo.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            favoritButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            addBookmarkButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            partSelectButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            menuButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            radio_0sec.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            radio_2sec.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            radio_4sec.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            radio_6sec.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            radio_jump15.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            radio_jump60.setAlpha((float) ((float) buttonsTransparency/ 6.0));
            touchButton.setAlpha((float) ((float) buttonsTransparency/ 6.0));
        }

    }

    static void write_book_marks() throws IOException {

        Bookmarks.write_book_marks();
        Bookmarks.ArrayBMs.clear();

    }

}
