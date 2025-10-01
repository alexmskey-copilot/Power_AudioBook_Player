package com.audiobook.pbp_service;


import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;
import static android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
import static android.widget.LinearLayout.VERTICAL;
import static com.audiobook.pbp_service.MainActivity.DO_NOT_READ;
import static com.audiobook.pbp_service.MainActivity.LAST_READING_SIZE;
import static com.audiobook.pbp_service.MainActivity.READ;
import static com.audiobook.pbp_service.MainActivity.currentAlbum;
import static com.audiobook.pbp_service.MainActivity.directoryAlbum;
import static com.audiobook.pbp_service.MainActivity.its_tablet;
import static com.audiobook.pbp_service.MainActivity.reading_status;
import static com.audiobook.pbp_service.MainActivity.selAlbum;
import static com.audiobook.pbp_service.MainActivity.show_cover;
import static com.audiobook.pbp_service.MainActivity.startFolder;
import static com.audiobook.pbp_service.R.color.i_read_color;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundScreen extends AppCompatActivity {

    private TextView show_screen;
    private LinearLayout linearLayout;
    MediaPlayer mediaPlayer = new MediaPlayer();
    public static final String ALBUM_NAME = "com.audiobook.powerbookplayer.extra.ALBUM";
    public static final String DIR_WAY = "com.audiobook.powerbookplayer.extra.DIRWAY";
    private int PERMISSION_REQUEST_STORAGE_WRITE = 2;
    public static Uri allsongsuri;
    private String clearedSelAlbum = "", selection = "";
    private String[] lastReadingAlbums, lastReadingDirs;
    private int k, j, asli;
    public boolean show_view_del_buttons = true;
    public ImageButton bquestion;
    public static String st0 = "/storage/emulated/0";
    public String st1 = "Android/media";
    public String st2 = "/storage/emulated/0/Download";
    public boolean delete_succes =false; // успешность операции удаления
    public static int MAX_FAVORITE_SIZE = 226; // Максимальный размер избранного
    private Uri songUri= null;
    private MediaMetadataRetriever mmr= new MediaMetadataRetriever();
    private byte[] data;
    private Bitmap thumbnail= null;
    private Drawable drwFromBMP= null;
    private boolean fileExist= false;
    public static String album_file_name;         // имя файла для книг в формате m4a/m4b
    public static Uri album_uri;
    private static Canvas canvas= null;
    private static int surf_width= 0, surf_height= 0;

    public static String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.COMPOSER,
//            MediaStore.Audio.Media.AUTHOR,
//                MediaStore.Audio.Media.IS_AUDIOBOOK,
            MediaStore.Audio.Media.IS_MUSIC
//            ,MediaStore.Audio.Media.TRACK
//            ,MediaStore.Audio.Media.CD_TRACK_NUMBER
    };

    public static String[] old_projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.COMPOSER,
//            MediaStore.Audio.Media.AUTHOR,
//                MediaStore.Audio.Media.IS_AUDIOBOOK,
            MediaStore.Audio.Media.IS_MUSIC
    };

    public static String[] del_projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
    };

    public static String[] projection_album = {
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.ALBUM_ID,
//            MediaStore.Audio.Media.IS_AUDIOBOOK,
            MediaStore.Audio.Media.IS_MUSIC
    };
    public static String[] old_projection_album = {
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
//            MediaStore.Audio.Media.IS_AUDIOBOOK,
            MediaStore.Audio.Media.IS_MUSIC
    };



    public static class MusicElement {
        public final Uri uri;
        public final String name;
        public String album;
        public final String artist;
        public final String composer;
        public final int duration, size, offset;
        public boolean image_exist;

        public MusicElement(Uri uri, String name, String album, String artist, String composer, int duration, int size, int offset, boolean image_exist) {
            this.uri = uri;
            this.name = name;
            this.album = album;
            this.artist = artist;
            this.composer = composer;
            this.duration = duration;
            this.size = size;
            this.offset = offset;
            this.image_exist= image_exist;
        }
    }
    public static List<MusicElement> musicList = new ArrayList<MusicElement>();
    public static List<MusicElement> musicDelList = new ArrayList<MusicElement>();

    public class AlbumElement {
        private final long album_id;
        private final Uri uri;
        private final String album;
        private String dir;
        private int reading_state;

        public AlbumElement(long album_id, Uri uri, String album, String dir) {
            this.album_id       = album_id;
            this.uri            = uri;
            this.album          = album;
            this.dir            = dir;
            this.reading_state  = DO_NOT_READ;
        }
        public AlbumElement(AlbumElement localAE) {
            this.album_id     = localAE.album_id;
            this.uri          = localAE.uri;
            this.album        = localAE.album;
            this.dir          = localAE.dir;
            this.reading_state= localAE.reading_state;
        }
    }
    public  List<AlbumElement> AlbumList = new ArrayList<AlbumElement>();
    private List<AlbumElement> sortAlbumList = new ArrayList<AlbumElement>();
    private static int  one_alb_some_dir= 0, one_alb_some_dir_favorite= 0,
                        one_dir_some_alb= 0, one_dir_some_alb_favorite= 0;
    public String sortOrder;
    public int alb_count = 0;
    public Button  but_aldir, but_diral, but_question,
                    all_books_button, not_reading_button, in_read_button, reading_button;
    public ImageButton  search_button,
                        favorite_filter_button;
    EditText search_string;
    String  search_text= "";
    private LinearLayout lh;
    private LinearLayout lv;
    private SurfaceView sv;
    private int RL_size= 0;
    private boolean line_in_favorite= false;
    public static boolean in_scaning= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        super.onCreate(savedInstanceState);
        if (MainActivity.keep_portrait)
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        testBrightness();
        setContentView(R.layout.activity_sound_screen);
        setupWindowInsets();

//        File file = new File(Environment. getExternalStorageDirectory().getPath() + "/Music");
//        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
//        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" +
//                Environment.getExternalStorageDirectory())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            while (in_scaning) {
            }
//            in_scaning = true;

            MediaScannerConnection.scanFile(MainActivity.appContext,
                    new String[]{Environment.getExternalStorageDirectory().getPath() + "/Music",
                            Environment.getExternalStorageDirectory().getPath() + "/Download",
                            Environment.getExternalStorageDirectory().getPath() + "/Movies",
                            Environment.getExternalStorageDirectory().getPath() + "/Audiobooks"},
//                new String[] {"audio/*"},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            in_scaning = false;
                        }
                    });
        }
        else {
            File sfile = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/.nomedia");
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(sfile));
            sendBroadcast(intent);
            sfile = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/.nomedia");
            intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(sfile));
            sendBroadcast(intent);
            sfile = new File(Environment.getExternalStorageDirectory().getPath() + "/Audiobooks/.nomedia");
            intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(sfile));
            sendBroadcast(intent);
            sfile = new File(Environment.getExternalStorageDirectory().getPath() + "/Movies/.nomedia");
            intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(sfile));
            sendBroadcast(intent);
        }

        try {
            getAllSongsFromSDCARD();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void getAllSongsFromSDCARD() throws IOException {

        int relPath = 0;
        String[] projects;
        int night_mode= AppCompatDelegate.getDefaultNightMode();
        lh = new LinearLayout (getApplicationContext());
        lv = new LinearLayout (getApplicationContext());
        lv.setOrientation(VERTICAL);

        but_aldir = (Button) findViewById (R.id.but_aldir);
        but_aldir.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        but_aldir.setEnabled(directoryAlbum);
        but_diral = (Button) findViewById (R.id.bit_diral);
        but_diral.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        but_diral.setEnabled(!directoryAlbum);
        Switch show_cover_switch = (Switch) findViewById(R.id.show_cover_switch);
        but_question = (Button) findViewById (R.id.but_question);
        but_question.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        all_books_button = (Button) findViewById (R.id.all_books_button);
        all_books_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        not_reading_button = (Button) findViewById (R.id.not_reading_button);
//        not_reading_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        in_read_button = (Button) findViewById (R.id.in_read_button);
//        in_read_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        reading_button = (Button) findViewById (R.id.reading_button);
        search_button = (ImageButton) findViewById (R.id.buttonSearch);
        search_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        favorite_filter_button = (ImageButton) findViewById (R.id.buttonFavorite);
        favorite_filter_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        if (MainActivity.favorite_list_using)
            favorite_filter_button.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
        else
            favorite_filter_button.setImageResource(R.drawable.ic_favorite_icon_foreground);
//        favorite_filter_button.setPadding(6, 14, 6, 14);
        search_string = (EditText) findViewById(R.id.searchString);
        if (search_text.length()> 0) {
            search_string.setText(search_text);
        }
//        reading_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        not_reading_button.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
        in_read_button.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
        reading_button.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));

        if (MainActivity.button_color!= 0) {
            but_aldir.setBackgroundColor(MainActivity.button_color);
            but_diral.setBackgroundColor(MainActivity.button_color);
            but_question.setBackgroundColor(MainActivity.button_color);
            all_books_button.setBackgroundColor(MainActivity.button_color);
            search_button.setBackgroundColor(MainActivity.button_color);
            favorite_filter_button.setBackgroundColor(MainActivity.button_color);
            if (directoryAlbum) {
                but_aldir.setBackgroundColor(MainActivity.button_color);
                but_diral.setBackgroundColor(GRAY);
            } else {
                but_aldir.setBackgroundColor(GRAY);
                but_diral.setBackgroundColor(MainActivity.button_color);
            }
        }
        if (MainActivity.show_book_list == MainActivity.show_all_books) {
            all_books_button.setEnabled(false);
            all_books_button.setBackgroundColor(GRAY);
            all_books_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        }
        if (MainActivity.show_book_list == MainActivity.show_no_read_book) {
            not_reading_button.setEnabled(false);
            not_reading_button.setBackgroundColor(GRAY);
            not_reading_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        }
        if (MainActivity.show_book_list == MainActivity.show_now_read_books) {
            in_read_button.setEnabled(false);
            in_read_button.setBackgroundColor(GRAY);
            in_read_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        }
        if (MainActivity.show_book_list == MainActivity.show_read_books) {
            reading_button.setEnabled(false);
            reading_button.setBackgroundColor(GRAY);
            reading_button.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        }
//        if (MainActivity.show_cover)
            show_cover_switch.setChecked(show_cover);
//        else
//           show_cover_switch.setChecked(false);

//        getAllVideo ();


        search_string.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                search_text = search_string.getText().toString();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                search_text = s.toString().toLowerCase();
//                setContentView(R.layout.activity_sound_screen);
//                AlbumList.clear();
                try {
//                    getAllSongsFromSDCARD();
                    show_book_list_to_lh();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        search_string.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    v = getCurrentFocus();
                    if (v instanceof EditText) {
                        v.clearFocus();
                        InputMethodManager imm = (InputMethodManager) MainActivity.appContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });

        but_aldir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directoryAlbum= !directoryAlbum;
//                setContentView(R.layout.activity_sound_screen);
                AlbumList.clear();
//                but_aldir.setBackgroundColor(GRAY);
//                but_diral.setBackgroundColor(MainActivity.button_color);
                try {
                    getAllSongsFromSDCARD();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        but_diral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directoryAlbum= !directoryAlbum;
//                setContentView(R.layout.activity_sound_screen);
                AlbumList.clear();
                try {
                    getAllSongsFromSDCARD();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        but_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                bquestion.setImageResource(R.drawable.sign_question_negative_foreground);
//                String selAlbum = album;
                Intent intent = new Intent(v.getContext(), HelpBookSelect.class);
                startActivityForResult(intent, MainActivity.HELP_BOOK_SELECT_REQUEST_CODE);
//                    startActivity(intent);
            }
        });

        all_books_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.show_book_list= MainActivity.show_all_books;
//                setContentView(R.layout.activity_sound_screen);
                AlbumList.clear();
                try {
                    getAllSongsFromSDCARD();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        not_reading_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.show_book_list= MainActivity.show_no_read_book;
//                setContentView(R.layout.activity_sound_screen);
                AlbumList.clear();
                try {
                    getAllSongsFromSDCARD();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        in_read_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.show_book_list= MainActivity.show_now_read_books;
//                setContentView(R.layout.activity_sound_screen);
                AlbumList.clear();
                try {
                    getAllSongsFromSDCARD();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        reading_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.show_book_list= MainActivity.show_read_books;
//                setContentView(R.layout.activity_sound_screen);
                AlbumList.clear();
                try {
                    getAllSongsFromSDCARD();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View v = getCurrentFocus();
                if (v instanceof EditText) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) MainActivity.appContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

            }
        });

        favorite_filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.favorite_list_using= !MainActivity.favorite_list_using;
                if (MainActivity.favorite_list_using)
                    favorite_filter_button.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
                else
                    favorite_filter_button.setImageResource(R.drawable.ic_favorite_icon_foreground);
                View v = getCurrentFocus();
                if (v instanceof EditText) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) MainActivity.appContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                try {
                    show_book_list_to_lh();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        show_cover_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                show_cover = isChecked;
                MainActivity.need_show_cover= true;
                if (isChecked)
                    Toast.makeText(getApplicationContext(), R.string.wait_cover_text, Toast.LENGTH_LONG).show();
//                setContentView(R.layout.activity_sound_screen);
                AlbumList.clear();
                try {
                    getAllSongsFromSDCARD();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        delete_succes= false;
//        linearLayout = (LinearLayout) findViewById(R.id.button_layer);
//        linearLayout.removeAllViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 OR " + MediaStore.Audio.Media.IS_AUDIOBOOK + " != 0";
//            selection = "";
            allsongsuri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            projects = SoundScreen.projection;
            if (!directoryAlbum)
                sortOrder = MediaStore.Audio.Media.ALBUM + " ASC, " + MediaStore.Audio.Media.RELATIVE_PATH + " ASC ";
            if (directoryAlbum)
                sortOrder = MediaStore.Audio.Media.RELATIVE_PATH + " ASC, " + MediaStore.Audio.Media.ALBUM + " ASC ";
        } else {
            selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            projects = SoundScreen.old_projection;
            if (!directoryAlbum)
                sortOrder = MediaStore.Audio.Media.ALBUM + " ASC, " + MediaStore.Audio.Media.DATA + " ASC";
            if (directoryAlbum)
                sortOrder = MediaStore.Audio.Media.DATA + " ASC, " + MediaStore.Audio.Media.ALBUM + " ASC";
        }

//        String sortOrder = MediaStore.Audio.Media.ALBUM + " ASC ";// + MediaStore.Audio.Media.DISPLAY_NAME + " ASC";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            while (in_scaning) { }
        Cursor cursor = getApplicationContext().getContentResolver().query(allsongsuri,
                projects, selection, null, sortOrder);
//        if (cursor.moveToFirst()) {
          if (cursor.getCount()> 0) {
//            projection_album, null, null, sortOrder)) {
//            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
//            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int nameAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int idAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int nameArtist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
//            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
//            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                  relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH);
              } else {
                  relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
              }
//            int external_uri = cursor.getColumnIndexOrThrow(String.valueOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI));
            while (cursor.moveToNext()  &&  AlbumList.size() < MainActivity.ALBUM_MAX) {
//                long id = cursor.getLong(idColumn);
//                String name = cursor.getString(nameColumn);
                String artist = cursor.getString(nameArtist);
                String relP = cursor.getString(relPath);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    relP= relP.substring(0, relP.lastIndexOf("/"));
                }
                String album = cursor.getString(nameAlbum);
                if (startFolder.isEmpty()) {
                    if (relP!= null  &&  relP.length() > 0) {
                        if (!MainActivity.show_system_folders  &&  startFolder.length()== 0) {
                            if (relP.startsWith(st1))
                                continue;
                            if (relP.startsWith(st2))
                                continue;
                        }
                    }
                }
                if (!startFolder.isEmpty()) {
                    if (relP!= null  &&  relP.length() > 0) {
                        if (!relP.startsWith("/storage/emulated/0")  &&
                            startFolder.startsWith("/storage/emulated/0/"))
                            startFolder= startFolder.substring("/storage/emulated/0/".length());
                        Pattern p= Pattern.compile("/storage/....-..../.*"), pr= Pattern.compile("/storage/.*");
                        Matcher m= p.matcher(startFolder), mr= pr.matcher(relP);
                        if (m.matches()  &&  !mr.matches()) {
                            startFolder= startFolder.substring("/storage/....-..../".length());
                        }
//                        if (relP.startsWith("Sound"))
//                            continue;
                        if (!relP.startsWith(startFolder)  &&  !MainActivity.always_show_favorites_cfg) {
                            continue;
                        }
                        if (!relP.startsWith(startFolder)  &&  MainActivity.always_show_favorites_cfg) {
                            if (!existInFavor(album, ""))
                                continue;
                        }
                    }
                }

//                album = cursor.getString(titleColumn);
//                if (album.equalsIgnoreCase("whatsapp audio")) continue;
//                int duration = cursor.getInt(durationColumn);
//                int size = cursor.getInt(sizeColumn);
//                Uri external_uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                album= album + "(" + relP + ")";
//                musicList.add(new MusicElement(contentUri, name, album, artist, duration, size));
//                musicList.add(new MusicElement(contentUri, album));
                long album_id = cursor.getLong(idAlbum);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, album_id);
                if (relP== null)
                    relP= "";
                if (album== null  ||  album.isEmpty())
                    album= "No Name Album";
                AlbumElement AE= new AlbumElement (album_id, contentUri, album, relP);
                boolean Album_found = false;

                if (!directoryAlbum) { // альбом - папки
                    for (int i = 0; i < AlbumList.size(); i++) {
                        AlbumElement AE2 = AlbumList.get(i);
                        if (AE.album.equalsIgnoreCase(AE2.album)) {
                            if (AE.dir.equalsIgnoreCase(AE2.dir)) {
                                Album_found = true;
                                break;
                            }
                        }
                    }
                    if (!Album_found) {
                        AE.reading_state= getSigneRead(AE.album);
                        if (MainActivity.show_book_list== MainActivity.show_all_books  ||
                            (MainActivity.show_book_list== MainActivity.show_no_read_book  &&  AE.reading_state== DO_NOT_READ) ||
                            (MainActivity.show_book_list== MainActivity.show_now_read_books  &&  AE.reading_state== MainActivity.I_READ) ||
                            (MainActivity.show_book_list== MainActivity.show_read_books  &&  AE.reading_state== MainActivity.READ)) {
                            AlbumList.add(AE);
                        }
                    }
                }
                if (directoryAlbum) { // папка - альбомы
                    for (int i = 0; i < AlbumList.size(); i++) {
                        AlbumElement AE2 = AlbumList.get(i);
                        if (AE.dir.equalsIgnoreCase(AE2.dir)) {
                            if (AE.album.equalsIgnoreCase(AE2.album)) {
                                Album_found = true;
                                break;
                            }
                        }
                    }
                    if (!Album_found) {
                        AE.reading_state= getSigneRead(AE.album);
                        if (MainActivity.show_book_list== MainActivity.show_all_books  ||
                                (MainActivity.show_book_list== MainActivity.show_no_read_book  &&  AE.reading_state== DO_NOT_READ) ||
                                (MainActivity.show_book_list== MainActivity.show_now_read_books  &&  AE.reading_state== MainActivity.I_READ) ||
                                (MainActivity.show_book_list== MainActivity.show_read_books  &&  AE.reading_state== MainActivity.READ)) {
                            AlbumList.add(AE);
                        }                    }

                }

            }
            cursor.close();
            alb_count = 0;
            if (!directoryAlbum) {
                String alb_cur = "";
                for (int i = 0; i < AlbumList.size(); i++) {
                    if (alb_count == 0) {
                        alb_cur = AlbumList.get(i).album;
                        alb_count++;
                        continue;
                    }
                    if (alb_cur.equals(AlbumList.get(i).album)) {
                        alb_count++;
                        continue;
                    }
                    if (alb_count == 1) {
                        alb_cur = AlbumList.get(i).album;
                        AlbumElement AE2 = AlbumList.get(i - 1);
                        AE2.dir = "";
                        AlbumList.set(i - 1, AE2);
                        continue;
                    }
                    alb_cur = AlbumList.get(i).album;
                    alb_count = 1;
                }
            }
            if (alb_count== 1) {
                int i = AlbumList.size();
                AlbumElement AE2 = AlbumList.get(i- 1);
                AE2.dir = "";
                AlbumList.set(i - 1, AE2);
            }
        }
        one_alb_some_dir= 0;
        one_alb_some_dir_favorite= 0;
        one_dir_some_alb= 0;
        one_dir_some_alb_favorite= 0;
        linearLayout = (LinearLayout) findViewById(R.id.button_layer);
        linearLayout.removeAllViews();
        if (AlbumList.size() > 0) {
            RL_size= 0;
            try {
                RL_size= albumListSorting_ReadingBooksFirst ();
            } catch (IOException e) {
                e.printStackTrace();
            }
// control buttons drawing
// Album - Directories
// Directory - albums


//            lv.setOrientation(VERTICAL);

/*
            Button baldir = new Button(getApplicationContext());
            createChangeViewButton(baldir, R.drawable.folder_in_album_foreground, "Альбом - Папки", directoryAlbum);
            lh.addView(baldir);

            Button bdiral = new Button(getApplicationContext());
            createChangeViewButton(bdiral, R.drawable.album_in_folder_foreground, "Папка - Альбомы", !directoryAlbum);
            lh.addView(bdiral);

            bquestion = new ImageButton (getApplicationContext());
            bquestion.setImageResource(R.drawable.sign_question_foreground);
            bquestion.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            0
                    )
            );
            bquestion.setMaxHeight(28);
            bquestion.setMaxWidth(28);
            bquestion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bquestion.setImageResource(R.drawable.sign_question_negative_foreground);
//                String selAlbum = album;
                    Intent intent = new Intent(v.getContext(), com.audiobook.pbp_service.HelpBookSelect.class);
                    startActivityForResult(intent, MainActivity.HELP_BOOK_SELECT_REQUEST_CODE);
//                    startActivity(intent);
                }
            });


            lh.addView(bquestion);

            linearLayout.addView(lh);

 */

/*
            if (directoryAlbum) {
                lh = new LinearLayout (getApplicationContext());
                Switch showOneAlbumInFolders = new Switch(getApplicationContext());
                showOneAlbumInFolders.setText("Показывать название альбома в папке, даже если в ней всего один альбом");
                lh.addView(showOneAlbumInFolders);
                linearLayout.addView(lh);
            }
 */

            show_book_list_to_lh ();
//            show_screen. setText(to_show);

        }
//        else {
//            show_screen.setText(String.format("NOTHING IN THIS PHONE"));
//        }
        if (AlbumList.size()== 0) {
            LinearLayout lh = new LinearLayout (getApplicationContext());
            TextView textNoBook= new TextView(getApplicationContext());
            textNoBook.setText(R.string.no_audio_files);
            if (night_mode == 2)
                textNoBook.setTextColor(Color.WHITE);
            else
                textNoBook.setTextColor(BLACK);
            lh.addView(textNoBook);
            linearLayout.addView(lh);
        }
    }
    public void show_book_list_to_lh () throws IOException {

        linearLayout.removeAllViews();
        for (int i = 0; i < AlbumList.size(); i++) {
            if (search_text.length()> 0) {
                if (!AlbumList.get(i).album.toLowerCase().contains(search_text)) {
                    continue;
                }
            }
            lh = new LinearLayout(getApplicationContext());
//            sv = new SurfaceView(getApplicationContext());
//            sv = new SurfaceView(getApplicationContext());
//                LinearLayout lh = new LinearLayout (getApplicationContext());
            if (i == RL_size) {
                TextView textDivider = new TextView(getApplicationContext());
                textDivider.setText(" ");
                lh.addView(textDivider);
                linearLayout.addView(lh);
                lh = new LinearLayout(getApplicationContext());
//                sv = new SurfaceView(getApplicationContext());
                lv = new LinearLayout(getApplicationContext());
                lv.setOrientation(VERTICAL);
            }

            if (i >= RL_size) {
                if (!directoryAlbum) {
                    if (one_alb_some_dir == 0 && i < AlbumList.size() - 1) { // One album in some dirs
                        if (AlbumList.get(i).album.equalsIgnoreCase(AlbumList.get(i + 1).album) &&
                                !AlbumList.get(i).dir.isEmpty()) {
                            if (!MainActivity.favorite_list_using ||
                                    (MainActivity.favorite_list_using &&
                                            existInFavor(AlbumList.get(i).album, ""))) {
                                if (existInFavor(AlbumList.get(i).album, "")) {
                                    one_alb_some_dir_favorite++;
                                    line_in_favorite = true;
                                }
                                lh = new LinearLayout(getApplicationContext());
                                lv = new LinearLayout(getApplicationContext());
                                lv.setOrientation(VERTICAL);

                                Button b = new Button(getApplicationContext());
                                    createPlayButtonInBookList(b,
                                            AlbumList.get(i).uri,
                                            960,
                                            AlbumList.get(i).album,
                                            "",
                                            AlbumList.get(i).album,
                                            false,
                                            false,
                                            false,
                                            AlbumList.get(i).reading_state);

                                lh.addView(b);
//                                        int ih= b.getMeasuredHeightAndState();

                                fileExist = albumFileExist(AlbumList.get(i).album);
                                if (show_cover) {
                                    ImageButton bReadStat = new ImageButton(getApplicationContext());
                                    createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, fileExist, AlbumList.get(i).reading_state, lh, i, lv);
                                    lv.addView(bReadStat);
                                }
                                if (!show_cover) {
                                    if (fileExist) {
                                        ImageButton bReadStat = new ImageButton(getApplicationContext());
                                        createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, true, AlbumList.get(i).reading_state, lh, i);
                                        lh.addView(bReadStat);
                                    }
                                }

                                ImageButton bView = new ImageButton(getApplicationContext());
                                createViewButtonInBookList(bView,
                                        AlbumList.get(i).album,
                                        "", /* Поиск по всем папкам,
                                                        исп. если альбом в нескольких папках
                                                        AlbumList.get(i).dir, */
                                        AlbumList.get(i).reading_state);
                                if (!show_cover)
                                    lh.addView(bView);

                                ImageButton bFavor = new ImageButton(getApplicationContext());
                                createFavorButtonInBookList(bFavor, AlbumList.get(i).album, "", AlbumList.get(i).reading_state);
                                if (!show_cover)
                                    lh.addView(bFavor);

                                ImageButton bDel = new ImageButton(getApplicationContext());
                                createDelButtonInBookList(bDel, AlbumList.get(i).album, "", AlbumList.get(i).reading_state);
                                one_alb_some_dir++;
                                if (!show_cover)
                                    lh.addView(bDel);

                                linearLayout.addView(lh);
                                if (show_cover) {
                                    lv.addView(bView);
                                    lv.addView(bFavor);
                                    bDel.setLayoutParams(
                                            new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    10
                                            )
                                    );
//                                            bDel.setForegroundGravity(Gravity.BOTTOM);
                                    lv.addView(bDel);
                                    lh.addView(lv);

                                    if (AlbumList.get(i).reading_state == MainActivity.I_READ) {
                                        lh.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                                    }
                                    if (AlbumList.get(i).reading_state == MainActivity.READ) {
                                        lh.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                                    }
                                    if (AlbumList.get(i).reading_state == DO_NOT_READ) {
                                        lh.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                                    }
                                    if (selAlbum.equalsIgnoreCase(AlbumList.get(i).album)) {
                                        lh.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                                    }
                                }
                                lh = new LinearLayout(getApplicationContext());
                                lv = new LinearLayout(getApplicationContext());
                                lv.setOrientation(VERTICAL);
                            }
                        }
                    }
                }
                if (directoryAlbum) {
                    if (one_dir_some_alb == 0 && i < AlbumList.size() - 1) { // One dir with some albums
                        if (AlbumList.get(i).dir.equalsIgnoreCase(AlbumList.get(i + 1).dir) &&
                                !AlbumList.get(i).album.isEmpty()) {
                            if (!MainActivity.favorite_list_using ||
                                    (MainActivity.favorite_list_using &&
                                            existInFavor("", AlbumList.get(i).dir))) {
                                if (existInFavor("", AlbumList.get(i).dir)) {
                                    one_dir_some_alb_favorite++;
                                    line_in_favorite = true;
                                }
                                lh = new LinearLayout(getApplicationContext());
                                lv = new LinearLayout(getApplicationContext());
                                lv.setOrientation(VERTICAL);
                                Button b = new Button(getApplicationContext());
                                createPlayButtonInBookList(b,
                                        AlbumList.get(i).uri,
                                        960,
                                        AlbumList.get(i).dir,
//                                                AlbumList.get(i).album,
                                        AlbumList.get(i).dir,
//                                                "",
                                        AlbumList.get(i).album,
                                        true,
                                        true,
                                        false,
                                        AlbumList.get(i).reading_state);
                                lh.addView(b);

                                fileExist = albumFileExist(AlbumList.get(i).album);
                                ImageButton bReadStat = new ImageButton(getApplicationContext());
                                if (show_cover) {
//                                    createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, fileExist, AlbumList.get(i).reading_state, lh, i, lv);
                                    createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).dir, fileExist, AlbumList.get(i).reading_state, lh, i, lv);
                                    lv.addView(bReadStat);
                                }
                                if (!show_cover) {
                                    if (fileExist) {
//                                        createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, true, AlbumList.get(i).reading_state, lh, i);
                                        createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).dir, true, AlbumList.get(i).reading_state, lh, i);
                                        lh.addView(bReadStat);
                                    }
                                }

                                ImageButton bView = new ImageButton(getApplicationContext());
                                createViewButtonInBookList(bView, "", AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                                if (!show_cover)
                                    lh.addView(bView);

                                ImageButton bFavor = new ImageButton(getApplicationContext());
                                createFavorButtonInBookList(bFavor, "", AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                                if (!show_cover)
                                    lh.addView(bFavor);

                                ImageButton bDel = new ImageButton(getApplicationContext());
                                createDelButtonInBookList(bDel, "", AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                                if (!show_cover)
                                    lh.addView(bDel);

                                if (show_cover) {
                                    lv.addView(bView);
                                    lv.addView(bFavor);
                                    lv.addView(bDel);
                                    lh.addView(lv);
                                    if (AlbumList.get(i).reading_state == MainActivity.I_READ) {
                                        lh.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                                    }
                                    if (AlbumList.get(i).reading_state == MainActivity.READ) {
                                        lh.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                                    }
                                    if (AlbumList.get(i).reading_state == DO_NOT_READ) {
                                        lh.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                                    }
                                    if (selAlbum.equalsIgnoreCase(AlbumList.get(i).album)) {
                                        lh.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                                    }
                                }
                                one_dir_some_alb++;

                                linearLayout.addView(lh);
                                lh = new LinearLayout(getApplicationContext());
                                lv = new LinearLayout(getApplicationContext());
                                lv.setOrientation(VERTICAL);
                            }
                        }
                    }
                }
            }

            if (!directoryAlbum && one_alb_some_dir > 0) {
                if (!MainActivity.favorite_list_using ||
                        (MainActivity.favorite_list_using &&
                                existInFavor(AlbumList.get(i).album, ""))) {
                    createFolderOrAlbumButton (directoryAlbum,
                            lh,
                            AlbumList.get(i).reading_state,
                            selAlbum.equalsIgnoreCase(AlbumList.get(i).album));
                    if (existInFavor(AlbumList.get(i).album, "")) {
                        line_in_favorite = true;
                    }
                }
            }
            if (directoryAlbum && one_dir_some_alb > 0) {
                if (!MainActivity.favorite_list_using ||
                        one_dir_some_alb_favorite > 0 ||
                        (MainActivity.favorite_list_using &&
                                existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                    createFolderOrAlbumButton (directoryAlbum,
                            lh,
                            AlbumList.get(i).reading_state,
                            selAlbum.equalsIgnoreCase(AlbumList.get(i).album));
                    if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                        line_in_favorite = true;
                    }
                }
            }
            Button b = new Button(getApplicationContext());

            if (!directoryAlbum) {
//                b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play_button_foreground, 0, 0, 0);
                if (one_alb_some_dir == 0) {
                    if (i >= RL_size) {
                        if (!MainActivity.favorite_list_using ||
                                (MainActivity.favorite_list_using &&
                                        existInFavor(AlbumList.get(i).album, ""))) {
                            if (existInFavor(AlbumList.get(i).album, "")) {
                                line_in_favorite = true;
                            }
                            if (selAlbum.equalsIgnoreCase(AlbumList.get(i).album)) {
                                createPlayButtonInBookList(b,
                                        AlbumList.get(i).uri,
                                        906,
                                        AlbumList.get(i).album,
                                        "",
//                                            AlbumList.get(i).dir,
                                        AlbumList.get(i).album,
                                        false,
                                        false,
                                        false,
                                        AlbumList.get(i).reading_state);
                                lh.addView(b);
                            }
                            else {
                                createPlayButtonInBookList(b,
                                        AlbumList.get(i).uri,
                                        906,
                                        AlbumList.get(i).album,
                                        "",
//                                            AlbumList.get(i).dir,
                                        AlbumList.get(i).album,
                                        false,
                                        false,
                                        false,
                                        AlbumList.get(i).reading_state);
                                lh.addView(b);
                            }
                        }
                    }
                    if (i < RL_size) {
                        if (!MainActivity.favorite_list_using ||
                                (MainActivity.favorite_list_using &&
                                        existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                            if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                                line_in_favorite = true;
                            }
//                            show_reading_status ();
                            createPlayButtonInBookList(b,
                                    AlbumList.get(i).uri,
                                    906,
                                    AlbumList.get(i).album,
                                    AlbumList.get(i).dir,
                                    AlbumList.get(i).album,
                                    false,
                                    false,
                                    false,
                                    AlbumList.get(i).reading_state);
                            lh.addView(b);
                        }
                    }
                }
                if (one_alb_some_dir > 0) {
//                    b.setText(AlbumList.get(i).dir);
                    if (!MainActivity.favorite_list_using ||
                            one_alb_some_dir_favorite > 0 ||
                            (MainActivity.favorite_list_using &&
                                    existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                        if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                            line_in_favorite = true;
                        }
                        createPlayButtonInBookList(b,
                                AlbumList.get(i).uri,
                                906,
                                AlbumList.get(i).dir,
                                AlbumList.get(i).dir,
                                AlbumList.get(i).album,
                                true,
                                true,
                                false,
                                AlbumList.get(i).reading_state);
                        lh.addView(b);
                    }
                }


                ImageButton bView = new ImageButton(getApplicationContext());
                ImageButton bFavor = new ImageButton(getApplicationContext());
                ImageButton bReadStat = new ImageButton(getApplicationContext());
                lv = new LinearLayout(getApplicationContext());
                lv.setOrientation(VERTICAL);

//                        final ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
//                        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
//                        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
//                        lv.setLayoutParams(layoutParams);
//                        lv.setBackgroundColor(R.id.in_read_button);

                if (one_alb_some_dir == 0) {
                    if (i >= RL_size) {
                        if (!MainActivity.favorite_list_using ||
                                (MainActivity.favorite_list_using &&
                                        existInFavor(AlbumList.get(i).album, ""))) {
                            if (existInFavor(AlbumList.get(i).album, "")) {
                                line_in_favorite = true;
                            }
                            createViewButtonInBookList(bView, AlbumList.get(i).album, /* "" */ AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                            fileExist = albumFileExist(AlbumList.get(i).album);
                            if (show_cover) {
                                bReadStat = new ImageButton(getApplicationContext());
                                createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, fileExist, AlbumList.get(i).reading_state, lh, i, lv);
                                lv.addView(bReadStat);
                            }
                            if (!show_cover) {
                                if (fileExist) {
                                    createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, true, AlbumList.get(i).reading_state, lh, i);
                                    lh.addView(bReadStat);
                                }
                            }
                        }
                        createFavorButtonInBookList(bFavor, AlbumList.get(i).album, "", AlbumList.get(i).reading_state);
                        if (!show_cover) {
                            lh.addView(bView);
                            lh.addView(bFavor);
                        }
                        if (show_cover) {
                            lv.addView(bView);
                            lv.addView(bFavor);
                            lh.addView(lv);
                            if (AlbumList.get(i).reading_state == MainActivity.I_READ) {
                                lh.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                            }
                            if (AlbumList.get(i).reading_state == MainActivity.READ) {
                                lh.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                            }
                            if (AlbumList.get(i).reading_state == DO_NOT_READ) {
                                lh.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                            }
                            if (selAlbum.equalsIgnoreCase(AlbumList.get(i).album)) {
                                lh.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                            }
                        }
                    }
                    if (i < RL_size) {
                        if (!MainActivity.favorite_list_using ||
                                (MainActivity.favorite_list_using &&
                                        existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                            if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                                line_in_favorite = true;
                            }
                            createViewButtonInBookList(bView,
                                    AlbumList.get(i).album,
                                    AlbumList.get(i).dir,
                                    AlbumList.get(i).reading_state);
                            fileExist = albumFileExist(AlbumList.get(i).album);
                            if (show_cover) {
                                bReadStat = new ImageButton(getApplicationContext());
                                createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, fileExist, AlbumList.get(i).reading_state, lh, i, lv);
                                lv.addView(bReadStat);
                            }
                            if (!show_cover) {
                                if (fileExist) {
                                    createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, true, AlbumList.get(i).reading_state, lh, i);
                                    lh.addView(bReadStat);
                                }
                            }
                            createFavorButtonInBookList(bFavor, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                            if (!show_cover) {
                                lh.addView(bView);
                                lh.addView(bFavor);
                            }
                            if (show_cover) {
                                lv.addView(bView);
                                lv.addView(bFavor);
                                lh.addView(lv);
                                if (AlbumList.get(i).reading_state == MainActivity.I_READ) {
                                    lh.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                                }
                                if (AlbumList.get(i).reading_state == MainActivity.READ) {
                                    lh.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                                }
                                if (AlbumList.get(i).reading_state == DO_NOT_READ) {
                                    lh.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                                }
                                if (selAlbum.equalsIgnoreCase(AlbumList.get(i).album)) {
                                    lh.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                                }
                            }
                        }
                    }
                }
                if (one_alb_some_dir > 0) {
                    if (!MainActivity.favorite_list_using ||
                            one_alb_some_dir_favorite > 0 ||
                            (MainActivity.favorite_list_using &&
                                    existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                        if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                            line_in_favorite = true;
                        }
                        createViewButtonInBookList(bView, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                        if (albumFileExist(AlbumList.get(i).album)) {
                            createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, true, AlbumList.get(i).reading_state, lh, i);
                            lh.addView(bReadStat);
                        }
                        createFavorButtonInBookList(bFavor, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                        lh.addView(bView);
                        lh.addView(bFavor);
                    }
                }

                ImageButton bDel = new ImageButton(getApplicationContext());
                if (one_alb_some_dir == 0) {
                    if (i >= RL_size) {
                        if (!MainActivity.favorite_list_using ||
                                (MainActivity.favorite_list_using &&
                                        existInFavor(AlbumList.get(i).album, ""))) {
                            if (existInFavor(AlbumList.get(i).album, "")) {
                                line_in_favorite = true;
                            }
                            createDelButtonInBookList(bDel, AlbumList.get(i).album, "", AlbumList.get(i).reading_state);
                            if (!show_cover)
                                lh.addView(bDel);
                            if (show_cover) {
                                lv.addView(bDel);
                            }
                        }
                    } else {
                        if (!MainActivity.favorite_list_using ||
                                (MainActivity.favorite_list_using &&
                                        existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                            if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                                line_in_favorite = true;
                            }
                            createDelButtonInBookList(bDel, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                            if (!show_cover)
                                lh.addView(bDel);
                            if (show_cover)
                                lv.addView(bDel);
                        }
                    }
                }
                if (one_alb_some_dir > 0) {
                    if (!MainActivity.favorite_list_using ||
                            one_alb_some_dir_favorite > 0 ||
                            (MainActivity.favorite_list_using &&
                                    existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                        if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                            line_in_favorite = true;
                        }
                        createDelButtonInBookList(bDel, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                        lh.addView(bDel);
                    }
                }
            }

            if (directoryAlbum) {
//                        lv = new LinearLayout (getApplicationContext());
//                        lv.setOrientation(VERTICAL);
                if (one_dir_some_alb == 0) {
                    if (!MainActivity.favorite_list_using ||
                            (MainActivity.favorite_list_using &&
                                    existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                        if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                            line_in_favorite = true;
                        }
                        if (i >= RL_size) {
                            createPlayButtonInBookList(b,
                                    AlbumList.get(i).uri,
                                    906,
                                    AlbumList.get(i).dir + " - \"" + AlbumList.get(i).album + "\"",
                                    AlbumList.get(i).dir,
                                    AlbumList.get(i).album,
                                    true,
                                    true,
                                    false,
                                    AlbumList.get(i).reading_state);
                            lh.addView(b);

                            fileExist = albumFileExist(AlbumList.get(i).album);
                            ImageButton bReadStat = new ImageButton(getApplicationContext());
                            if (show_cover) {
                                lv = new LinearLayout(getApplicationContext());
                                lv.setOrientation(VERTICAL);
                                createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, fileExist, AlbumList.get(i).reading_state, lh, i, lv);
                                lv.addView(bReadStat);
                            }
                            if (!show_cover) {
                                if (fileExist) {
                                    createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, true, AlbumList.get(i).reading_state, lh, i);
                                    lh.addView(bReadStat);
                                }
                            }

                            ImageButton bView = new ImageButton(getApplicationContext());
                            createViewButtonInBookList(bView,
                                    AlbumList.get(i).album,
                                    AlbumList.get(i).dir,
                                    AlbumList.get(i).reading_state);

                            ImageButton bFavor = new ImageButton(getApplicationContext());
                            createFavorButtonInBookList(bFavor, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);

                            ImageButton bDel = new ImageButton(getApplicationContext());
                            createDelButtonInBookList(bDel, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
                            if (!show_cover) {
                                lh.addView((bView));
                                lh.addView((bFavor));
                                lh.addView(bDel);
                            }

                            if (show_cover) {
                                lv.addView((bView));
                                lv.addView((bFavor));
                                lv.addView(bDel);
                                lh.addView(lv);
                                if (AlbumList.get(i).reading_state == MainActivity.I_READ) {
                                    lh.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                                }
                                if (AlbumList.get(i).reading_state == MainActivity.READ) {
                                    lh.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                                }
                                if (AlbumList.get(i).reading_state == DO_NOT_READ) {
                                    lh.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                                }
                                if (selAlbum.equalsIgnoreCase(AlbumList.get(i).album)) {
                                    lh.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                                }
                            }
                            linearLayout.addView(lh);
                            lh = new LinearLayout(getApplicationContext());
                            lv = new LinearLayout(getApplicationContext());
                            lv.setOrientation(VERTICAL);

                            if (MainActivity.showOneAlbumInFolder) {
                                createFolderOrAlbumButton(directoryAlbum,
                                        lh,
                                        AlbumList.get(i).reading_state,
                                        selAlbum.equalsIgnoreCase(AlbumList.get(i).album));
                                b = new Button(getApplicationContext());
                                createPlayButtonInBookList(b,
                                        AlbumList.get(i).uri,
                                        906,
//                                    AlbumList.get(i).dir,
                                        AlbumList.get(i).album,
                                        AlbumList.get(i).dir,
                                        AlbumList.get(i).album,
                                        true,
                                        true,
                                        true,
                                        AlbumList.get(i).reading_state);
                                lh.addView(b);
                            }
                            if (!MainActivity.showOneAlbumInFolder) {
                                show_view_del_buttons = false;
                            }
                        }
                        if (i < RL_size) {
                            if (!MainActivity.favorite_list_using ||
                                    (MainActivity.favorite_list_using &&
                                            existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                                if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                                    line_in_favorite = true;
                                }
                                createPlayButtonInBookList(b,
                                        AlbumList.get(i).uri,
                                        906,
                                        AlbumList.get(i).album,
                                        AlbumList.get(i).dir,
                                        AlbumList.get(i).album,
                                        false,
                                        false,
                                        false,
                                        AlbumList.get(i).reading_state);
                                lh.addView(b);
                            }
                        }
                    }
                }
                if (one_dir_some_alb > 0) {
//                    b.setText(AlbumList.get(i).dir);
                    if (!MainActivity.favorite_list_using ||
                            one_dir_some_alb_favorite > 0 ||
                            (MainActivity.favorite_list_using &&
                                    existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                        if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                            line_in_favorite = true;
                        }
                        createPlayButtonInBookList(b,
                                AlbumList.get(i).uri,
                                906,
                                AlbumList.get(i).album,
                                AlbumList.get(i).dir,
                                AlbumList.get(i).album,
                                true,
                                false,
                                true,
                                AlbumList.get(i).reading_state);
                        lh.addView(b);
                    }
                }

                if (!MainActivity.favorite_list_using ||
                        one_alb_some_dir_favorite > 0 ||
                        one_dir_some_alb_favorite > 0 ||
                        (MainActivity.favorite_list_using &&
                                existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir))) {
                    if (existInFavor(AlbumList.get(i).album, AlbumList.get(i).dir)) {
                        line_in_favorite = true;
                    }
                    if (show_view_del_buttons) {
                        ImageButton bView = new ImageButton(getApplicationContext());
                        ImageButton bFavor = new ImageButton(getApplicationContext());
                        ImageButton bReadStat = new ImageButton(getApplicationContext());
//                                if (one_alb_some_dir == 0) {
//                                if (one_dir_some_alb == 0) {
//                                    if (i >= RL_size) {
                        createViewButtonInBookList(bView,
                                AlbumList.get(i).album,
                                AlbumList.get(i).dir,
                                AlbumList.get(i).reading_state);
                        createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, true, AlbumList.get(i).reading_state, lh, i, lv);
                        createFavorButtonInBookList(bFavor, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
//                                    } else {
//                                        createVieButtonInBookList          (bView, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
//                                        createReadingStatusButtonInBookList(bReadStat, AlbumList.get(i).album, true, AlbumList.get(i).reading_state);
//                                        createFavorButtonInBookList        (bFavor, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
//                                    }
                        //                               }
//                                if (one_alb_some_dir> 0) {
//                                    createVieButtonInBookList              (bView, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
//                                    createReadingStatusButtonInBookList    (bReadStat, AlbumList.get(i).album, true, AlbumList.get(i).reading_state);
//                                    createFavorButtonInBookList            (bFavor, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
//                                }

                        fileExist = albumFileExist(AlbumList.get(i).album);
                        if (show_cover && one_dir_some_alb == 0) {
                            lv = new LinearLayout(getApplicationContext());
                            lv.setOrientation(VERTICAL);
                            lv.addView(bReadStat);
                        }
                        if (!show_cover || one_dir_some_alb > 0) {
                            if (fileExist) {
                                lh.addView(bReadStat);
                            }
                        }
                        if (show_cover && one_dir_some_alb == 0) {
                            lv.addView((bView));
                            lv.addView((bFavor));
//                                    lh.addView(lv);
                            if (AlbumList.get(i).reading_state == MainActivity.I_READ) {
                                lh.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                            }
                            if (AlbumList.get(i).reading_state == MainActivity.READ) {
                                lh.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                            }
                            if (AlbumList.get(i).reading_state == DO_NOT_READ) {
                                lh.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                            }
                            if (selAlbum.equalsIgnoreCase(AlbumList.get(i).album)) {
                                lh.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                            }
                        }
                        if (!show_cover || one_dir_some_alb > 0) {
                            lh.addView(bView);
                            lh.addView(bFavor);
                        }

                        ImageButton bDel = new ImageButton(getApplicationContext());
//                                if (one_alb_some_dir == 0) {
//                                    if (i >= RL_size) {
//                                        createDelButtonInBookList(bDel, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
//                                    } else {
//                                        createDelButtonInBookList(bDel, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
//                                    }
//                                }
//                                if (one_alb_some_dir> 0) {
                        createDelButtonInBookList(bDel, AlbumList.get(i).album, AlbumList.get(i).dir, AlbumList.get(i).reading_state);
//                                }
                        if (show_cover && one_dir_some_alb == 0) {
                            lv.addView(bDel);
                            lh.addView(lv);
                        }
                        if (!show_cover || one_dir_some_alb > 0)
                            lh.addView(bDel);
                    }
                }
                if (!show_view_del_buttons)
                    show_view_del_buttons = true;
            }


            if (!MainActivity.favorite_list_using ||
                    (MainActivity.favorite_list_using && line_in_favorite)) {
                linearLayout.addView(lh);
            }
            line_in_favorite = false;
            if (!directoryAlbum) {
                if (one_alb_some_dir != 0 && i > 0 && i < AlbumList.size() - 1) {
                    if (!AlbumList.get(i).album.equalsIgnoreCase(AlbumList.get(i + 1).album)) {
                        if (AlbumList.get(i).album.equalsIgnoreCase(AlbumList.get(i - 1).album)) {
                            one_alb_some_dir = 0;
                            one_alb_some_dir_favorite = 0;
                        }
                    }
                }
            }
            if (directoryAlbum) {
                if (one_dir_some_alb != 0 && i > 0 && i < AlbumList.size() - 1) {
                    if (!AlbumList.get(i).dir.equalsIgnoreCase(AlbumList.get(i + 1).dir)) {
                        if (AlbumList.get(i).dir.equalsIgnoreCase(AlbumList.get(i - 1).dir)) {
                            one_dir_some_alb = 0;
                            one_dir_some_alb_favorite = 0;
                        }
                    }
                }
            }

        }
    }

    public static void albumFilling(String selAlbum, String dirWay) throws IOException {
        int relPath = 0;
        String[] projects;
        String  cleared_sel_album= null;
        boolean one_any_file= false;

        if ((selAlbum== null  || selAlbum.isEmpty())  &&  !dirWay.isEmpty())
            one_any_file= true;

        cleared_sel_album = selAlbum.replace("\'", "\'\'");
//        List<MusicElement> musicList = (List<MusicElement>) getIntent().getSerializableExtra("AllMusics");
        //       List<AlbumElement> AlbumList = (List<AlbumElement>) getIntent().getSerializableExtra("AllAlbums");
// ---------------------------------------
        Uri allsongsuri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            allsongsuri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

//        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 OR " + MediaStore.Audio.Media.IS_AUDIOBOOK + " != 0";
//        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
        String selection = "";
//        if (!selAlbum.isEmpty()) {
//            selection = " " +
//                    MediaStore.Audio.Media.ALBUM + " = \'" + clearedSelAlbum + "\'";
//        }
        if (selAlbum.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                selection = " " +
                        MediaStore.Audio.Media.RELATIVE_PATH + " = \'" + dirWay + "\'";
            } else {
                selection = " " +
                        MediaStore.Audio.Media.DATA + " = \'" + dirWay + "\'";
            }
            selection = "";
        }
//        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC ";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projects = SoundScreen.projection;
            sortOrder = MediaStore.Audio.Media.RELATIVE_PATH + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
//            sortOrder = MediaStore.Audio.Media.RELATIVE_PATH + " ASC, " + MediaStore.Audio.Media.TITLE + " ASC ";
        } else {
            projects = SoundScreen.old_projection;
            sortOrder = MediaStore.Audio.Media.DATA + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
//            sortOrder = MediaStore.Audio.Media.DATA + " ASC, " + MediaStore.Audio.Media.TITLE + " ASC ";
        }

        if (!one_any_file) {
            MainActivity.use_fileNames = testNamesInSelect(allsongsuri, selection, dirWay, sortOrder);
        }
        try (Cursor cursor = MainActivity.appContext.getContentResolver().query(allsongsuri, projects, selection, null, sortOrder)) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int nameTitle = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int nameAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int idAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int nameArtist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int nameComposer = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER);
//            int nameAuthor = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.AUTHOR);
            if (one_any_file) {
                relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH);
                } else {
                    relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                }
            }
//            int track= cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
//            int cd_track= cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.CD_TRACK_NUMBER);

            SoundScreen.musicList.clear();
            //int external_uri = cursor.getColumnIndexOrThrow(String.valueOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI));
            while (cursor.moveToNext()) {
//                int num_track= cursor.getInt(track);
//                int cd_track_number= cursor.getInt(cd_track);
                long id = cursor.getLong(idColumn);
                String file_name = cursor.getString(nameColumn);
                String name = cursor.getString(nameTitle);
                String artist = cursor.getString(nameArtist);
                String album = cursor.getString(nameAlbum);
                if (!one_any_file &&  (album == null  ||  album.isEmpty()))
                    continue;
                String relP = cursor.getString(relPath);
                if (startFolder.length()== 0
                    &&  MainActivity.use_root_folder
                    &&  !one_any_file
                    &&  relP!= null) {
                    if (!relP.startsWith(st0)) {
                        relP = st0 + "/" + relP;
                    }
                    if (!relP.startsWith(MainActivity.root_folder_path + "/"))
                        continue;
                }

//                if (relP== null)
//                    continue;
//                album = cursor.getString(titleColumn);
                long album_id = cursor.getLong(idAlbum);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);
                Uri external_uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                if (!cleared_sel_album.isEmpty()) {
                    if (!(album.replace("\'", "\'\'")).equalsIgnoreCase(cleared_sel_album))
                        continue;
                }
                album_file_name= file_name;
                album_uri= contentUri;
                String composer = cursor.getString(nameComposer);
                if (composer== null)
                    composer= "";
//                String author= cursor.getString(nameAuthor);
                if (dirWay.isEmpty()) {

                        SoundScreen.musicList.add(new MusicElement(contentUri,
                                (MainActivity.use_fileNames ? file_name + "(" + name + ")" : name),
                                album, artist, composer, duration, size, 0, true));
                }
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (one_any_file) {
                            if (relP.equals(dirWay)) {
                                SoundScreen.musicList.add(new MusicElement(contentUri,
                                        (MainActivity.use_fileNames ? file_name + "(" + name + ")" : name),
                                        album, artist, composer, duration, size, 0, true));
                                break;
                            }
                            else
                                continue;
                        }

                        if (relP.equalsIgnoreCase(dirWay))
                            SoundScreen.musicList.add(new MusicElement(contentUri,
                                    (MainActivity.use_fileNames? file_name + "(" + name + ")": name),
                                    album, artist, composer, duration, size, 0, true));
                    }
                    else {
                        if (relP.startsWith(dirWay)) {
                            SoundScreen.musicList.add(new MusicElement(contentUri,
                                    (MainActivity.use_fileNames ? file_name + "(" + name + ")" : name),
                                    album, artist, composer, duration, size, 0, true));
                            if (one_any_file)
                                break;
                            else
                                continue;
                        }
                    }
                    if (dirWay.equals(relP)) {
                        if (one_any_file) {
                            SoundScreen.musicList.add(new MusicElement(contentUri,
                                    (MainActivity.use_fileNames ? file_name + "(" + name + ")" : name),
                                    album, artist, composer, duration, size, 0, true));
                            break;
                        }
                    }
                }
            }
        }
        openAnyAudioFileIntent.fucking_apple_test ();
    }

    private void delOneAlbum(String delAlbum, String miUri) {

        int loc_relPath = 0;

        if (!MainActivity.hasPermissions(this)) {
//            MainActivity.requestPermissions_local(this, MainActivity.PERMISSION_STORAGE);
            Snackbar.make(findViewById(R.id.main_layout), R.string.delete_permission,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(SoundScreen.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_STORAGE_WRITE);
                }
            }).show();
        }
/*        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(findViewById(R.id.sound_screen), R.string.delete_permission,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(SoundScreen.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_STORAGE_WRITE);
                }
            }).show();
        }

 */

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_GRANTED) {
            if (MainActivity.hasPermissions(this)) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    List<Uri> urisToModify = new ArrayList<Uri>();
                    clearedSelAlbum = delAlbum.replace("\'", "\'\'");
                    if (!delAlbum.isEmpty()) {
                        selection = MediaStore.Audio.Media.ALBUM + "= \'" + clearedSelAlbum + "\'";
                    }
                    if (delAlbum.isEmpty()) {
                        selection = "";
                    }
                    Uri allsongsuri;
                    String[] projects;
                    String sortOrder = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        allsongsuri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    } else {
                        allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        projects = SoundScreen.projection;
                        sortOrder = MediaStore.Audio.Media.RELATIVE_PATH + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
                    } else {
                        projects = SoundScreen.old_projection;
                        sortOrder = MediaStore.Audio.Media.DATA + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
                    }

                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    try (Cursor cursor = getApplicationContext().getContentResolver().query(allsongsuri,
                            projects, selection, null, sortOrder)) {
                        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            loc_relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH);
                        } else {
                            loc_relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                        }

                        while (cursor.moveToNext()) {
                            long id = cursor.getLong(idColumn);
                            Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                            String relP = cursor.getString(loc_relPath);
                            if (MainActivity.use_root_folder) {
                                if (!relP.startsWith(st0)) {
                                    relP = st0 + "/" + relP;
                                }
                                if (!relP.startsWith(MainActivity.root_folder_path + "/"))
                                    continue;
                            }
                            if (miUri.isEmpty()) {
                                urisToModify.add(contentUri);
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    if (relP.equalsIgnoreCase(miUri))
                                        urisToModify.add(contentUri);
                                } else {
                                    if (relP.startsWith(miUri))
                                        urisToModify.add(contentUri);
                                }
                            }
                        }
                    }
                    if (urisToModify == null || urisToModify.isEmpty()) {
                        Toast.makeText(getApplicationContext(),  delAlbum+ " not delete!", Toast.LENGTH_LONG).show();
                    }
                    else {
                        //           ContentResolver resolver = getApplicationContext().getContentResolver();
                        PendingIntent editPendingIntent = MediaStore.createDeleteRequest(resolver, urisToModify);
                        try {
                            startIntentSenderForResult(editPendingIntent.getIntentSender(),
                                    AlbumScreen.DELETE_REQUEST_CODE, null, 0, 0, 0);
                            if (!delAlbum.isEmpty()) {
                                if (delAlbum.equals(MainActivity.selAlbum)) {
                                    SoundScreen.musicList.clear();
// удаление файла обложки
                                    File directory = getFilesDir();
                                    File miFile = new File(directory, delAlbum + ".png");
                                    if (miFile.exists() == true)
                                        miFile.delete();
                                    miFile = new File(directory, MainActivity.WORK_FILE_NAME);
                                    if (miFile.exists()) {
                                        if (!miFile.delete())
                                            Toast.makeText(getApplicationContext(), miFile.toString() + " not delete!", Toast.LENGTH_LONG).show();
                                    }
//                                delOneFile(MainActivity.WORK_FILE_NAME);
                                    MainActivity.selAlbum = "";
                                    SelectParts.ArrayParts.clear();
                                    SelectParts.PartNum.clear();
                                    MainActivity.lesson = 0;
                                }
                            }
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    String warn = "", temp_str = "";
                    temp_str = getResources().getString(R.string.delete_old_prompt);
                    warn = warn.format(temp_str, delAlbum);
                    Snackbar.make(findViewById(R.id.main_layout), warn, // R.string.delete_confirmation,
                            Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                        //                @RequiresApi(api = Build.VERSION_CODES.R)
                        @Override
                        public void onClick(View view) {
                            int loc_relPath2 = 0, numImagesRemoved = 0;
                            clearedSelAlbum = delAlbum.replace("\'", "\'\'");
                            if (!delAlbum.isEmpty()) {
                                selection = MediaStore.Audio.Media.ALBUM + "= \'" + clearedSelAlbum + "\'";
                            }
                            if (delAlbum.isEmpty())
                                selection = "";
                            Uri allsongsuri;
                            String[] projects;
                            String sortOrder = "";
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                allsongsuri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
                            } else {
                                allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                projects = SoundScreen.projection;
                                sortOrder = MediaStore.Audio.Media.RELATIVE_PATH + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
                            } else {
                                projects = SoundScreen.old_projection;
                                sortOrder = MediaStore.Audio.Media.DATA + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
                            }

                            ContentResolver resolver = getApplicationContext().getContentResolver();

                            try (Cursor cursor = getApplicationContext().getContentResolver().query(allsongsuri,
                                    projects, selection, null, sortOrder)) {
                                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    loc_relPath2 = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH);
                                } else {
                                    loc_relPath2 = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                                }

                                while (cursor.moveToNext()) {
                                    long id = cursor.getLong(idColumn);
                                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                                    String relP = cursor.getString(loc_relPath2);
                                    if (MainActivity.use_root_folder) {
                                        if (!relP.startsWith(st0)) {
                                            relP = st0 + "/" + relP;
                                        }
                                        if (!relP.startsWith(MainActivity.root_folder_path + "/"))
                                            continue;
                                    }
                                    if (miUri.isEmpty()) {
                                        numImagesRemoved = resolver.delete(contentUri, "", null);
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            if (relP.equalsIgnoreCase(miUri))
                                                numImagesRemoved = resolver.delete(contentUri, "", null);
                                        } else {
                                            if (relP.startsWith(miUri))
                                                numImagesRemoved = resolver.delete(contentUri, "", null);
                                        }
                                    }
                                }

                                if (!delAlbum.isEmpty()) {
// удаление файла обложки
                                    File directory = getFilesDir();
                                    File miFile = new File(directory, delAlbum + ".png");
                                    if (miFile.exists())
                                        miFile.delete();

                                    if (delAlbum.equals(MainActivity.selAlbum)) {
                                        SoundScreen.musicList.clear();
//                                        delOneFile(MainActivity.WORK_FILE_NAME);
                                        // work file writing
                                        miFile = new File(directory, MainActivity.WORK_FILE_NAME);
                                        if (miFile.exists()) {
                                            if (!miFile.delete())
                                                Toast.makeText(getApplicationContext(), miFile.toString() + " not delete!", Toast.LENGTH_LONG).show();
                                        }
//                                if (!com.audiobook.pbp_service.MainActivity.selAlbum.equals(""))
//                                    delOneFile(com.audiobook.pbp_service.MainActivity.selAlbum);    // for local library file writing
                                        MainActivity.selAlbum = "";
                                        SelectParts.ArrayParts.clear();
                                        SelectParts.PartNum.clear();
                                        MainActivity.lesson= 0;
                                    }
                                }
                            }

//                            setContentView(R.layout.activity_sound_screen);
                            AlbumList.clear();
                            try {
                                getAllSongsFromSDCARD();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).show();
                }
            }

    }

    public void delOneFile (String fileName) {
        File file=new File(fileName);
        if (!file.delete())
            Toast.makeText(getApplicationContext(), fileName + " not delete!", Toast.LENGTH_LONG).show();
    }

    public void saveToReadingList (String selAlbum, String selDir) throws IOException {
        String albums[], dirs[];
        String localAlb, localDir= "";
        localAlb = selAlbum;
        albums= new String[25];
        dirs= new String[25];
        File directory = getFilesDir();
        File miFile = new File(directory, MainActivity.READING_LIST_FILE_NAME);
        if (miFile.exists()) {
            FileInputStream fis = new FileInputStream(miFile);
            if (fis != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    for (int i = 0; i < 25; i++) {
                        MainActivity.textBuf = reader.readLine();  // get album name and dir name
                        if (MainActivity.textBuf.contains("#")) {
                            localAlb= MainActivity.textBuf.substring(0, MainActivity.textBuf.indexOf("#"));
                            localDir= MainActivity.textBuf.substring(MainActivity.textBuf.indexOf("#")+ 1);
                        }
                        else {
                            localAlb= MainActivity.textBuf;
                            localDir= "";
                        }
                        if (localAlb != null  &&  !localAlb.isEmpty())
                            if (!localAlb.equals(selAlbum)) {
                                albums[i] = localAlb;
                                dirs[i]= localDir;
                            }
                            else {
                                i--;
                                continue;
                            }
                        else {
                            albums[i]= null;
                            dirs[i]= null;
                        }
                    }
                }
                fis.close();
//                delOneFile(MainActivity.READING_LIST_FILE_NAME);
            }
        }
        miFile.createNewFile();
        FileOutputStream fis = new FileOutputStream(miFile);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fis);
        MainActivity.textBuf = String.format("%s#%s\n", selAlbum, selDir);
        outputStreamWriter.write(MainActivity.textBuf);
        for (int i= 0; i< 25; i++) {
            if (albums[i] == null)
                MainActivity.textBuf = "\n";
            else
                MainActivity.textBuf = String.format("%s#%s\n", albums[i], dirs[i]);
            outputStreamWriter.write(MainActivity.textBuf);
        }
        outputStreamWriter.close();
    }

    public int albumListSorting_ReadingBooksFirst () throws IOException {

        String localAlb, localDir;
        lastReadingAlbums = new String[3];
        lastReadingDirs = new String[3];
        int read_from_list= 0;

        File directory = getFilesDir();
//        for (int i= 0 ; i< sortAlbumList.size();) {
//            sortAlbumList.remove(i);
//        }
        sortAlbumList.clear();
        File miFile = new File(directory, MainActivity.READING_LIST_FILE_NAME);
        if (miFile.exists()) {
            FileInputStream fis = new FileInputStream(miFile);
            if (fis != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    while ((MainActivity.textBuf = reader.readLine()) != null
                           &&  !MainActivity.textBuf.isEmpty()) {
                        if (MainActivity.textBuf.contains("#")) {
                            localAlb= MainActivity.textBuf.substring(0, MainActivity.textBuf.indexOf("#"));
                            localDir= MainActivity.textBuf.substring(MainActivity.textBuf.indexOf("#")+ 1);
                        }
                        else {
                            localAlb= MainActivity.textBuf;
                            localDir= "";
                        }
                        for (int i= 0; i< AlbumList.size(); i++) {
                            if (localAlb.equals(currentAlbum)) {
                                break;
                            }
                            if (localAlb.equals(AlbumList.get(i).album)) {
                                if (!localDir.isEmpty()) {
                                    if (localDir.equals(AlbumList.get(i).dir)) {
                                        lastReadingAlbums[read_from_list] = localAlb;
                                        lastReadingDirs[read_from_list++] = localDir;
                                        break;
                                    }
                                }
                                if (localDir.isEmpty()) {
                                    lastReadingAlbums[read_from_list] = localAlb;
                                    lastReadingDirs[read_from_list++] = localDir;
                                    break;
                                }
                            }
                        }
                        if (read_from_list>= LAST_READING_SIZE)
                            break;
                    }
                }
                fis.close();
                if (read_from_list> LAST_READING_SIZE)
                    read_from_list= LAST_READING_SIZE;
            }
        }
        boolean found_empty_dir = false;
        for (j= 0, asli= 0; j< LAST_READING_SIZE; j++) {
            for (int i= 0; i< AlbumList.size(); i++) {
                if ((AlbumList.get(i).album).equals(lastReadingAlbums[j])) {
                    if (lastReadingDirs[j].isEmpty()) {
                        for (int kl= i; kl< AlbumList.size(); kl++) {
                            if ((AlbumList.get(kl).album).equals(lastReadingAlbums[j])  &&
                                AlbumList.get(i).dir.isEmpty()) {
                                sortAlbumList.add(asli++, AlbumList.get(kl));
//                                AlbumList.remove(kl);   // убираем дубль в основном списке
                                found_empty_dir= true;
                                break;
                            }
                        }
                        if (found_empty_dir) {
                            found_empty_dir= false;
                            break;
                        }
                        AlbumElement AE2 = new AlbumElement(AlbumList.get(i));
                        AE2.dir = "";
                        sortAlbumList.add(asli++, AE2);
                        break;
                    }
                    if (lastReadingDirs[j]!= null) {
                        if (AlbumList.get(i).dir.equalsIgnoreCase(lastReadingDirs[j])) {
                            sortAlbumList.add(asli++, AlbumList.get(i));
//                            for (k = i; k < AlbumList.size() - 1; k++) {
//                                AlbumList.set(k, AlbumList.get(k + 1));
//                            }
//                            AlbumList.remove(k);  // убираем дубль в основном списке
                        }
                    }
                }
            }
        }
        if (sortAlbumList.size()> 0) {
            k= sortAlbumList.size();
            for (int i= 0; i< AlbumList.size(); i++)
                sortAlbumList.add(i+ k, AlbumList.get(i));
            AlbumList.clear();
            AlbumList.addAll(sortAlbumList);
//            sortAlbumList.clear();
        }
        return read_from_list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AlbumScreen.DELETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
//                setContentView(R.layout.activity_sound_screen);
                AlbumList.clear();
                try {
                    getAllSongsFromSDCARD();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (requestCode == MainActivity.HELP_BOOK_SELECT_REQUEST_CODE) {
//            bquestion.setImageResource(R.drawable.sign_question_foreground);
//            getAllSongsFromSDCARD();
        }

    }

    public void testBrightness() {
        if (MainActivity.full_brightness) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
//            if (layoutParams.screenBrightness< 1)
//                sourceBrightness= layoutParams.screenBrightness;
            layoutParams.screenBrightness = BRIGHTNESS_OVERRIDE_FULL;
            getWindow().setAttributes(layoutParams);
        }
        if (!MainActivity.full_brightness) {
//            if (sourceBrightness> 0) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
//            layoutParams.screenBrightness = sourceBrightness;
            layoutParams.screenBrightness = -1;
            getWindow().setAttributes(layoutParams);
//            }
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

    public void createPlayButtonInBookList (Button b,
                                            Uri wayUri,
                                            int butWidth,
                                            String naming,
                                            String dirWay, String curAlbum,
                                            boolean mini_allow,
                                            boolean truncate_allow,
                                            boolean mini_anyway,
                                            int album_reading_state) {

        Resources res = getApplicationContext().getResources();
//        b.setCompoundDrawablesWithIntrinsicBounds (butIcon, butIcon, butIcon, butIcon);
        b.setMinHeight(26);
//        b.setPadding(6, b.getPaddingTop(), b.getPaddingRight(), b.getPaddingBottom());

        if (mini_anyway) {
            truncate_allow= true;
        }
        if (truncate_allow) {
            if (naming.contains(st0)) {
                if (naming.substring(0, st0.length()).equalsIgnoreCase(st0))
                    naming = naming.substring(st0.length());
            }
        }
        if (naming.length()== 0)
            naming= curAlbum;
//        if (checkIsTablet()) {
        if (show_cover) {
            b.setTextSize(18);
            if (!MainActivity.show_full_book_name)
                b.setMaxLines(6);
//            b.setTextSize(16);
        }
        if (!show_cover) {
            b.setTextSize(17);
            if (!MainActivity.show_full_book_name)
                b.setMaxLines(3);
        }
        b.setEllipsize(TextUtils. TruncateAt.END);
        if (its_tablet) {
            b.setTextSize(26);
        }
        else {
            if (((naming.length() > 26 && mini_allow) || mini_anyway) && !MainActivity.boldFontForLongNames &&
                    (!show_cover || (show_cover && (one_alb_some_dir != 0 || one_dir_some_alb != 0)))) {
                b.setTextSize(12);
            }
        }
        if (naming.length()> 126
                &&  mini_allow
                &&  !show_cover
                &&  !MainActivity.show_full_book_name) {
            b.setText(naming.substring(0, 126));
        }
        else {
            b.setText(naming);
        }
        b.setContentDescription(dirWay);
        b.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        6
                )
        );

        b.setGravity(Gravity.LEFT);
        b.setAllCaps(false);
        //b.setId("album" + i);
        b.setWidth(butWidth);
        if (album_reading_state== MainActivity.I_READ) {
            b.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
        }
        if (album_reading_state== MainActivity.READ) {
            b.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
        }
        if (album_reading_state== DO_NOT_READ) {
            b.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
        }
        if (selAlbum.equalsIgnoreCase(curAlbum)) {
            b.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
        }
        if (show_cover) {
            int corrector_size= 100;
            if (checkIsTablet()) {
                corrector_size= 69;
            }
            try {
                if (one_alb_some_dir != 0 || one_dir_some_alb != 0) {
                    b.setPadding(6, b.getPaddingTop(), 0, b.getPaddingBottom());
                }
                if (one_alb_some_dir == 0 && one_dir_some_alb == 0) {
                    if (naming.equalsIgnoreCase("Music/")
                            || naming.equalsIgnoreCase("Audiobooks/")
                            || naming.equalsIgnoreCase("download/")) {
                        b.setCompoundDrawablePadding(1);
                        b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_file_folder, 0, 0, 0);
                        thumbnail = BitmapFactory.decodeResource(res, R.drawable.ic_file_folder);
                        drwFromBMP = new BitmapDrawable(thumbnail);
                        drwFromBMP.setBounds(6, 0, 389* corrector_size/ 100, 296* corrector_size/ 100);
                        b.setCompoundDrawables(drwFromBMP, null, null, null);
//                        saveEmbeddedImage(curAlbum);
                    } else {
                        if (!getPreparedImageInFolder(curAlbum)) {
                            songUri = albumGetOneUri(curAlbum, dirWay);
                            if (songUri == null) {
                                b.setCompoundDrawablePadding(1);
                                b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.headphones, 0, 0, 0);
                                thumbnail = BitmapFactory.decodeResource(res, R.drawable.headphones);
                                saveEmbeddedImage(curAlbum);
                            }
                            if (songUri != null) {
                                b.setCompoundDrawablePadding(9);
//                        mmr = new MediaMetadataRetriever();
                                mmr.setDataSource(getApplicationContext(), songUri);
                                data = mmr.getEmbeddedPicture();
                                if (data != null) {
                                    thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    saveEmbeddedImage(curAlbum);
                                    drwFromBMP = new BitmapDrawable(thumbnail);
                                    drwFromBMP.setBounds(6, 6, 389* corrector_size/ 100, 389* corrector_size/ 100);
                                    b.setCompoundDrawables(drwFromBMP, null, null, null);
//                        else
//                            drwFromBMP.setBounds(6, 6, 206, 206);
//                        b.setCompoundDrawables(drwFromBMP, null, null, null);
                                }
                                if (data == null) {
//                    Resources res = getApplicationContext().getResources();
//                    drwFromBMP = res.getDrawable(R.drawable.headphones);
//                b.setBackground(d);
//                b.setCompoundDrawables(null, null, d, null);
                                    if (!seekEmbeddedImage(curAlbum)) {
                                        b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.headphones, 0, 0, 0);
                                        thumbnail = BitmapFactory.decodeResource(res, R.drawable.headphones);
                                        saveEmbeddedImage(curAlbum);
                                    } else {
                                        saveEmbeddedImage(curAlbum);
                                        drwFromBMP = new BitmapDrawable(thumbnail);
                                        b.setCompoundDrawablePadding(9);
                                        b.setPadding(0, 0, 0, 0);
                                        drwFromBMP.setBounds(6, 6, 389* corrector_size/ 100, 389* corrector_size/ 100);
                                        b.setCompoundDrawables(drwFromBMP, null, null, null);
                                    }
                                }
                            }
                        }
                        if (getPreparedImageInFolder(curAlbum)) {
                            drwFromBMP = new BitmapDrawable(thumbnail);
                            b.setCompoundDrawablePadding(9);
                            b.setPadding(0, 0, 0, 0);
                            drwFromBMP.setBounds(6, 6, 389* corrector_size/ 100, 389* corrector_size/ 100);
                            b.setCompoundDrawables(drwFromBMP, null, null, null);
                        }
                    }
                }
//                if (one_alb_some_dir != 0 || one_dir_some_alb != 0) {
//                    drwFromBMP.setBounds(6, 6, 206, 206);
//                    b.setCompoundDrawables(drwFromBMP, null, null, null);
//                }
            } catch (RuntimeException e) {
//            Resources res = getApplicationContext().getResources();
//            Drawable d = res.getDrawable(R.drawable.headphones);
//            Drawable pd = res.getDrawable(R.drawable.play_button_foreground);
//            b.setCompoundDrawables(pd, null, d, null);
                if (one_alb_some_dir== 0  &&  one_dir_some_alb== 0) {
                    b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.headphones, 0, 0, 0);
                    thumbnail= BitmapFactory.decodeResource(res, R.drawable.headphones);
                    saveEmbeddedImage(curAlbum);
                }
            } catch (FileNotFoundException e) {
//                e.printStackTrace();
                b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.headphones, 0, 0, 0);
                thumbnail= BitmapFactory.decodeResource(res, R.drawable.headphones);
                saveEmbeddedImage(curAlbum);
            }
//            mmr.release();
        }
        else {
            b.setPadding(0, b.getPaddingTop(), 0, b.getPaddingBottom());
            b.setCompoundDrawablesWithIntrinsicBounds (R.drawable.play_button_foreground, 0, 0, 0);
        }

//        int bViewHeight= b.getHeight();
//        Layout blayout= b.getLayout();
//        final Paint paint = b.getPaint();
//        final int endPos = naming.length();
//        int startPos = 0;
//        int breakCount = 0;
//        float bwidth= (float) (b.getTextSize());
//        float[] measuredWidth = new float[(int) 1];
//
//        while (startPos < endPos) {
//            startPos += paint.breakText(naming.substring(startPos, endPos),
//                    true,  bwidth, /*measuredWidth*/ null);
//            breakCount++;
//            if (breakCount> 3) {
//                b.setText(naming.substring (0, startPos) + "...");
//                break;
//            }
//        }

//        breakCount= paint.breakText(naming.substring(startPos, endPos),
//                true,  bwidth, (float[]) null);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// Click button PLAY on Album list
                //linearLayout.removeView(v);
                //b.setText("PRESSED");
//                String selAlbum = b.getText().toString();
//                String selAlbum = curAlbum;
                try {
                    saveToReadingList (curAlbum, dirWay);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                linearLayout = (LinearLayout) findViewById(R.id.button_layer);
                linearLayout.removeAllViews();
                if (!show_cover) {
                    try {
                        if (!getPreparedImageInFolder(curAlbum)) {
                            seekEmbeddedImage(curAlbum);
                            saveEmbeddedImage(curAlbum);
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    albumFilling (curAlbum, dirWay);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                MainActivity.current_directory= dirWay;
                Intent intent = new Intent();
                if (curAlbum.isEmpty()  && !dirWay.isEmpty()) {
//                    String buff;
//                    buff= dirWay.substring(0, st0.length()).equalsIgnoreCase(st0);
                    if (dirWay.contains(st0)) {
                        intent.putExtra(ALBUM_NAME, dirWay.substring(st0.length()));
                    }
                    else {
                        if (dirWay.contains("\\")) {
                            intent.putExtra(ALBUM_NAME, dirWay.substring(dirWay.lastIndexOf("\\")+ 1));
                        }
                        else {
                            intent.putExtra(ALBUM_NAME, dirWay);
                        }
                    }
                }
                else
                    intent.putExtra(ALBUM_NAME, curAlbum);
//                        startActivity(intent);
                setResult(RESULT_OK, intent);
//                        onBackPressed();
                finish();
            }
        });
    }

    public void createReadingStatusButtonInBookList (ImageButton bView,         // кнопка
                                                     String album,              // название альбома
                                                     boolean albInd,            // признак активности кнопки
                                                     // в режиме обложек кнопка выводится всегда, в режиме без обложек только для начатых книг
                                                     int album_reading_state,   // статус чтения альбома
                                                     LinearLayout lh,           // гориознтальный лайоут списка книг
                                                     int albumList_ind         // индекс в списке альбомов

    ) {
        createReadingStatusButtonInBookList (bView, album, albInd, album_reading_state, lh, albumList_ind, null);
    }


    public void createReadingStatusButtonInBookList (ImageButton bView,         // кнопка
                                                     String album,              // название альбома
                                                     boolean albInd,            // признак активности кнопки
                                                     // в режиме обложек кнопка выводится всегда, в режиме без обложек только для начатых книг
                                                     int album_reading_state,   // статус чтения альбома
                                                     LinearLayout lh,           // гориознтальный лайоут списка книг
                                                     int albumList_ind,         // индекс в списке альбомов
                                                     LinearLayout lv            // Вертикальный лэйаут для режима с обложками
                                                    ) {

        if (show_cover  &&  one_alb_some_dir== 0  &&  one_dir_some_alb== 0)
            bView.setPadding(bView.getPaddingLeft(), 5, bView.getPaddingRight(), 5);
        else
            bView.setPadding(6, bView.getPaddingTop(), 6, bView.getPaddingBottom());
        bView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                )
        );
//        bView.setMaxHeight(28);
        bView.setMaxWidth(28);

        if (album_reading_state== MainActivity.I_READ) {
            bView.setImageResource(R.drawable.reading_status_begin);
            bView.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
        }
        if (album_reading_state== MainActivity.READ) {
            bView.setImageResource(R.drawable.reading_status_end);
            bView.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
        }
        if (album_reading_state== DO_NOT_READ) {
            bView.setImageResource(R.drawable.reading_status_no);
            bView.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
        }
        if (selAlbum.equalsIgnoreCase(album)) {
            bView.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
        }
        bView.setEnabled(albInd);
        bView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                bView.setImageResource(R.drawable.view_button_foreground_negative);
                try {
                    reading_status= changeSigneRead (album);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                AlbumElement loc_AE = AlbumList.get(albumList_ind);
                loc_AE.reading_state= reading_status;
                AlbumList.set(albumList_ind, loc_AE);

                int chldcnt= lh.getChildCount();
                int chlcdnt_lv= 0;
                if (lv!= null) {
                    chlcdnt_lv= lv.getChildCount();
                }
                if (reading_status== MainActivity.I_READ) {
                    bView.setImageResource(R.drawable.reading_status_begin);
                    for (int i= 0; i< chldcnt; i++) {
                        lh.getChildAt(i).setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                    }
                    for (int i= 0; i< chlcdnt_lv; i++) {
                        lv.getChildAt(i).setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                    }
                    if (lv != null)
                        lv.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                    lh.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
                }
                if (reading_status== MainActivity.READ) {
                    bView.setImageResource(R.drawable.reading_status_end);
                    for (int i= 0; i< chldcnt; i++) {
                        lh.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                    }
                    for (int i= 0; i< chlcdnt_lv; i++) {
                        lv.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                    }
                    if (lv != null)
                        lv.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                    lh.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
                }
                if (reading_status== DO_NOT_READ) {
                    bView.setImageResource(R.drawable.reading_status_no);
                    for (int i= 0; i< chldcnt; i++) {
                        lh.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                    }
                    for (int i= 0; i< chlcdnt_lv; i++) {
                        lv.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                    }
                    if (lv != null)
                        lv.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                    lh.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
                }
                if (selAlbum.equalsIgnoreCase(album)) {
//                    bView.setImageResource(R.drawable.reading_status_no);
                    for (int i= 0; i< chldcnt; i++) {
                        lh.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                    }
                    for (int i= 0; i< chlcdnt_lv; i++) {
                        lv.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                    }
                    if (lv != null)
                        lv.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                    lh.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
                }

//                setContentView(R.layout.activity_sound_screen);
//                AlbumList.clear();
/*
                try {
//                    getAllSongsFromSDCARD();
                    show_book_list();
                } catch (IOException e) {
                    e.printStackTrace();
                }

 */


//                bView.setImageResource(R.drawable.reading_status);
//                Intent intent = new Intent(v.getContext(), com.audiobook.pbp_service.AlbumScreen.class);
//                intent.putExtra(ALBUM_NAME, album);
//                intent.putExtra(DIR_WAY, dirWay);
//                startActivity(intent);
            }
        });
    }
// создание кнопки Глаз
    public void createViewButtonInBookList(ImageButton bView,
                                           String album_loc,
                                           String dirWay_loc,
                                           int album_reading_state) {

//        Toast.makeText(getApplicationContext(), "ALBUM=" + album_loc + ", dir=" + dirWay_loc, Toast.LENGTH_LONG).show();
        bView.setImageResource(R.drawable.view_button_foreground);
        if (show_cover  &&  one_alb_some_dir== 0  &&  one_dir_some_alb== 0)
            bView.setPadding(bView.getPaddingLeft(), 5, bView.getPaddingRight(), 4);
        else
            bView.setPadding(6, bView.getPaddingTop(), 6, bView.getPaddingBottom());
        bView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                )
        );
//        bView.setMaxHeight(28);
        bView.setMaxWidth(28);
        if (album_reading_state== MainActivity.I_READ) {
            bView.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
        }
        if (album_reading_state== MainActivity.READ) {
            bView.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
        }
        if (album_reading_state== DO_NOT_READ) {
            bView.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
        }
        if (selAlbum.equalsIgnoreCase(album_loc)) {
            bView.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
        }
        bView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bView.setImageResource(R.drawable.view_button_foreground_negative);
//                String selAlbum = album;
                Intent intent = new Intent(v.getContext(), AlbumScreen.class);
                intent.putExtra(ALBUM_NAME, album_loc);
                intent.putExtra(DIR_WAY, dirWay_loc);
                startActivity(intent);
            }
        });
    }

    public void createFavorButtonInBookList (ImageButton bView, String album, String dirWay, int album_reading_state) throws IOException {

        boolean in_favor= existInFavor (album, dirWay);

        if (in_favor) {
            bView.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
        }
        else {
            bView.setImageResource(R.drawable.ic_favorite_icon_foreground);
        }
        if (show_cover  &&  one_alb_some_dir== 0  &&  one_dir_some_alb== 0)
//            bView.setPadding(bView.getPaddingLeft(), bView.getPaddingTop(), bView.getPaddingRight(), bView.getPaddingBottom());
            bView.setPadding(bView.getPaddingLeft(), 4, bView.getPaddingRight(), 5);
        else
            bView.setPadding(6, bView.getPaddingTop(), 6, bView.getPaddingBottom());
        bView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                )
        );
//        bView.setMaxHeight(28);
        bView.setMaxWidth(28);
        if (album_reading_state== MainActivity.I_READ) {
            bView.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
        }
        if (album_reading_state== MainActivity.READ) {
            bView.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
        }
        if (album_reading_state== DO_NOT_READ) {
            bView.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
        }
        if (selAlbum.equalsIgnoreCase(album)) {
            bView.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
        }
        bView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean in_favor= false;
                try {
                    in_favor = existInFavor (album, dirWay);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (in_favor) {
                    try {
                        delOneFavorite(album, dirWay);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bView.setImageResource(R.drawable.ic_favorite_icon_foreground);
                }
                else {
                    try {
                        addToFavorites (album, dirWay);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bView.setImageResource(R.drawable.ic_favorite_icon_foreground_negative);
                }
            }
        });
    }

    public void createDelButtonInBookList (ImageButton bDel, String album, String uri, int album_reading_state) {

        bDel.setImageResource(R.drawable.trash_button_foreground);
        if (show_cover  &&  one_alb_some_dir== 0  &&  one_dir_some_alb== 0)
//            bDel.setPadding(bDel.getPaddingLeft(), bDel.getPaddingTop(), bDel.getPaddingRight(), bDel.getPaddingBottom());
            bDel.setPadding(bDel.getPaddingLeft(), 5, bDel.getPaddingRight(), 5);
        else
            bDel.setPadding(6, bDel.getPaddingTop(), 0, bDel.getPaddingBottom());
        bDel.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                )
        );
//        bDel.setMaxHeight(28);
        bDel.setMaxWidth(26);
//                if (MainActivity.currentAlbum.equals(AlbumList.get(i).album))
//                    bDel.setEnabled(false);
        if (album_reading_state== MainActivity.I_READ) {
            bDel.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
        }
        if (album_reading_state== MainActivity.READ) {
            bDel.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
        }
        if (album_reading_state== DO_NOT_READ) {
            bDel.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
        }
        if (selAlbum.equalsIgnoreCase(album)) {
            bDel.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
        }
        bDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bDel.setImageResource(R.drawable.trash_button_foreground_negative);
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                }
//                String selAlbum = b.getText().toString();
//                String selAlbum = album;
//                Uri miUri = Uri.parse (b.getContentDescription().toString());
//                Uri miUri = uri;
                delOneAlbum (album, uri);
            }
        });

    }

    public void createFolderOrAlbumButton (boolean directoryAlbum,
                                           LinearLayout lh,
                                           int album_reading_state,  // статусм чтения книги
                                           boolean cur_track) {

        ImageButton b = new ImageButton(getApplicationContext());
        b.setPadding(0, b.getPaddingTop(), 0, b.getPaddingBottom());

        if (!directoryAlbum)
            b.setImageResource (R.drawable.folder_in_album_foreground);
        else
            b.setImageResource (R.drawable.album_in_folder_foreground);

        b.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                )
        );

//        b.setMaxHeight(28);
        b.setMaxWidth(28);
        if (cur_track) {
            b.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
        }
        else {
            if (album_reading_state == MainActivity.I_READ) {
                b.setBackgroundColor(getResources().getColor(i_read_color, getTheme()));
            }
            if (album_reading_state == MainActivity.READ) {
                b.setBackgroundColor(getResources().getColor(R.color.read_color, getTheme()));
            }
            if (album_reading_state == DO_NOT_READ) {
                b.setBackgroundColor(getResources().getColor(R.color.do_not_read_color, getTheme()));
            }
        }
        lh.addView(b);
    }

/*
    public void createChangeViewButton (Button baldir, int butIcon, String naming, boolean enabl) {

        baldir.setCompoundDrawablesWithIntrinsicBounds (0, 0, butIcon, 0);
        baldir.setText(naming);
        baldir.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        6
                )
        );
        baldir.setGravity(Gravity.LEFT);
        baldir.setAllCaps(false);
        baldir.setEnabled(enabl);


        baldir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directoryAlbum= !directoryAlbum;
                setContentView(R.layout.activity_sound_screen);
                AlbumList.clear();
                getAllSongsFromSDCARD();
            }
        });
    }

 */

    public static boolean testNamesInSelect(Uri uris, String selection, String dirWay, String sortOrder) {

        int relPath = 0;
        String preName = "";
        String[] projects = new String[0];

        try (Cursor cursor = MainActivity.appContext.getContentResolver().query(uris,
                projects, selection, null, sortOrder)) {
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH);
            } else {
                relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            }

            if (cursor.getCount() < 2) {
                cursor.close();
                return false;
            }
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameColumn);
                String relP = cursor.getString(relPath);

                if (dirWay != null  &&  relP != null  &&  !dirWay.isEmpty()  &&  !relP.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (!relP.equalsIgnoreCase(dirWay))
                            continue;
                    }
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (!relP.startsWith(dirWay))
                            continue;
                    }
                }

                if (!preName.isEmpty()  &&  !preName.equals(name)) {
                    cursor.close();
                    return false;
                }
                preName = name;
            }
            cursor.close();
            return true;
        }
    }

    public boolean existInFavor (String album, String dirWay) throws IOException {
        File directory = getFilesDir();
        String localAlb = null, localDir = null;

        File miFile = new File(directory, MainActivity.FAVORITE_LIST_FILE_NAME);
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
                        if (localAlb.equals(album)  &&  localDir.equals(dirWay)) {
                            fis.close();
                            return true;
                        }
                        if (localDir.isEmpty()  &&  localAlb.equals(album)) {
                            fis.close();
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

    public void addToFavorites (String album, String dirWay) throws IOException {

        File directory = getFilesDir();
        String localAlb = null, localDir = null;

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
            return;
        }
    }

    public void delOneFavorite(String album, String dirWay) throws IOException {

        String albums[], dirs[];
        String localAlb, localDir= "";
        localAlb = album;
        albums= new String[MAX_FAVORITE_SIZE];
        dirs= new String[MAX_FAVORITE_SIZE];
        File directory = getFilesDir();
        File miFile = new File(directory, MainActivity.FAVORITE_LIST_FILE_NAME);
        if (!miFile.exists()) {
            return;
        }
            FileInputStream fis = new FileInputStream(miFile);
            if (fis != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    for (int i = 0; i < MAX_FAVORITE_SIZE; i++) {
                        MainActivity.textBuf = reader.readLine();  // get album name and dir name
                        if (MainActivity.textBuf== null)
                            break;
                        localAlb= MainActivity.textBuf.substring(0, MainActivity.textBuf.indexOf("#"));
                        localDir= MainActivity.textBuf.substring(MainActivity.textBuf.indexOf("#")+ 1);
                        if (localAlb.equals(album)  &&  localDir.equals(dirWay)) {
                            albums[i]= dirs[i]= null;
                            i--;
                            continue;
                        }
                        else {
                            albums[i]= localAlb;
                            dirs[i]= localDir;
                        }
                    }
                }
                fis.close();
                delOneFile(MainActivity.FAVORITE_LIST_FILE_NAME);
            }

        miFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(miFile);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
        for (int i= 0; i< MAX_FAVORITE_SIZE; i++) {
            if ((albums[i] == null || albums[i].isEmpty())  && (dirs[i] == null || dirs[i].isEmpty()))
                break;
            MainActivity.textBuf = String.format("%s#%s\n", albums[i], dirs[i]);
            outputStreamWriter.write(MainActivity.textBuf);
        }
        outputStreamWriter.close();

    }

    // Поиск признака прочтения книги
    private int getSigneRead (String nameFile) throws FileNotFoundException {
        String name, album, artist;
        int read_answer= DO_NOT_READ;
        Uri uri;

        File directory = getFilesDir();
        nameFile= nameFile.replaceAll("/", " ");
        File miFile = new File(directory, nameFile);
        if (miFile.exists()) {  // мы эту книгу уже читали
            FileInputStream fis = new FileInputStream(miFile);
            if (fis != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                StringBuilder stringBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    MainActivity.textBuf = reader.readLine();  // прослушиваемый файл (трек)
                    if (MainActivity.textBuf != null) {
                        if (MainActivity.textBuf.indexOf(',') == -1) {
                            read_answer = MainActivity.I_READ;
                        } else {
                            String textPars[] = MainActivity.textBuf.split(",");
                            if (textPars[1] != null)
                                read_answer = Integer.parseInt(textPars[1]);
                            else
                                read_answer = MainActivity.I_READ;
                        }
                    }
                    fis.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
            return read_answer;
        }
        return DO_NOT_READ;
    }

    private int changeSigneRead (String nameFile) throws IOException {
        String name, album, artist, nameFileOut;
        String textPars[] = new String[0];
        Uri uri;
        int loc_read_state= DO_NOT_READ;
        int loc_lesson= 0;

        File directory = getFilesDir();
        nameFileOut= nameFile.replaceAll("/", " ") + ".out";
        File miFile_out = new File(directory, nameFileOut);
        if (miFile_out.exists() == false)
            miFile_out.createNewFile();
        FileOutputStream fis_out = new FileOutputStream(miFile_out);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fis_out);

        nameFile= nameFile.replaceAll("/", " ");
        File miFile = new File(directory, nameFile);
        if (miFile.exists()) {  // мы эту книгу уже читали
            FileInputStream fis = new FileInputStream(miFile);
            if (fis != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                StringBuilder stringBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    MainActivity.textBuf = reader.readLine();  // прослушиваемый файл (трек)
                    if (MainActivity.textBuf != null) {
                        if (MainActivity.textBuf.indexOf(',') == -1) {
                            loc_lesson = Integer.parseInt(MainActivity.textBuf);
                        } else {
                            textPars = MainActivity.textBuf.split(",");
                            if (textPars[0] != null)
                                loc_lesson = Integer.parseInt(textPars[0]);
                            else
                                loc_lesson = 0;
                            if (loc_lesson > MainActivity.LESSON_MAX) loc_lesson = 0;
                            if (textPars[1] != null)
                                loc_read_state = Integer.parseInt(textPars[1]);
                            else
                                loc_read_state = DO_NOT_READ;
                        }
                    } else
                        loc_lesson = 0;
                    loc_read_state++;
                    if (loc_read_state > MainActivity.READ)
                        loc_read_state = DO_NOT_READ;
                    for (int i= 0; i< textPars.length; i++) {
                        switch (i) {
                            case 0:
                                MainActivity.textBuf= String.format ("%d", loc_lesson);
                                break;
                            case 1:
                                MainActivity.textBuf+= String.format (",%d", loc_read_state);
                                break;
                            default:
                                MainActivity.textBuf+= ",";
                                MainActivity.textBuf+= textPars[i];
                                break;
                        }
                    }
                    if (MainActivity.textBuf!= null) {
//                    MainActivity.textBuf = String.format("%d,%d\n", loc_lesson, loc_read_state);  // прослушиваемый сейчас трек и признак прочтения
                        outputStreamWriter.write(MainActivity.textBuf);
                        outputStreamWriter.write("\n");
                        while ((MainActivity.textBuf = reader.readLine()) != null) {
                            outputStreamWriter.write(MainActivity.textBuf + "\n");
                        }
                    }
                }
                fis.close();
            }
        }
        outputStreamWriter.close();
//        fis_out.close();
        miFile.delete();
        miFile_out.renameTo(miFile);

        return loc_read_state;
    }

    private boolean albumFileExist(String nameFile) throws FileNotFoundException {

        int   reading_progress= 0;

        File directory = getFilesDir();
        nameFile= nameFile.replaceAll("/", " ");
        File miFile = new File(directory, nameFile);
        if (miFile.exists()) {  // мы эту книгу уже читали
            reading_progress= workFileRead(nameFile);
            return true;
        }
        return false;
    }

// Чтение одного рабочего файла и подсчет процента прочтения книги
    public static int workFileRead(String nameFile) throws FileNotFoundException {

        String  name, album, artist, textBuf;
        int     music_quan= 0, lesson= 0, loc_reading_status= 0;
        Uri uri;

//        File directory = getFilesDir();
        File directory = MainActivity.directory_cfg;
        nameFile= nameFile.replaceAll("/", " ");
        File miFile = new File(directory, nameFile);
        FileInputStream fis = new FileInputStream(miFile);
        if (fis != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                textBuf = reader.readLine();    // прослушиваемый файл (трек)
                if (textBuf == null) {
                    fis.close();
                    return 0;
                }
                if (textBuf.indexOf(',') == -1) {
                    try {
                        lesson = Integer.parseInt(textBuf);
                    } catch (NumberFormatException e) {
                        lesson = 0;
                    }
                    if (lesson > MainActivity.LESSON_MAX) lesson = 0;
                }
                else {
                    String textPars[] = textBuf.split(",");
                    if (textPars[0] != null) {  // номер текущего трека
                        lesson = Integer.parseInt(textPars[0]);
                    } else {
                        lesson = 0;
                    }
                    if (lesson > MainActivity.LESSON_MAX)
                        lesson = 0;
                    if (textPars[1] != null) {  // статус прочтения трэка: не начат, начат, закончен
                        loc_reading_status = Integer.parseInt(textPars[1]);
                    } else {
                        loc_reading_status = DO_NOT_READ;
                    }
                    if (loc_reading_status== READ) {
                        fis.close();
                        return 100;
                    }
                    if (loc_reading_status== DO_NOT_READ) {
                        fis.close();
                        return 0;
                    }
                }
                textBuf = reader.readLine();    // Back in time
                if (textBuf == null) {
                    fis.close();
                    return 0;
                }
                textBuf = reader.readLine();    // число файлов
                if (textBuf == null) {
                    fis.close();
                    return 0;
                }
                music_quan = Integer.parseInt(textBuf);
                if (music_quan > MainActivity.LESSON_MAX)
                    music_quan = MainActivity.LESSON_MAX;
                if (music_quan == 0) {
                    fis.close();
                    return 0;
                }
                fis.close();
                return  (lesson* 100) / music_quan;
            }
            catch (IOException e) {
            }
        }
        return 1;
    }


    public Uri albumGetOneUri (String selAlbum, String dirWay) {
        int relPath = 0;
        String[] projects;

        clearedSelAlbum = selAlbum.replace("\'", "\'\'");
//        List<MusicElement> musicList = (List<MusicElement>) getIntent().getSerializableExtra("AllMusics");
        //       List<AlbumElement> AlbumList = (List<AlbumElement>) getIntent().getSerializableExtra("AllAlbums");
// ---------------------------------------
        Uri allsongsuri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            allsongsuri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

//        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 OR " + MediaStore.Audio.Media.IS_AUDIOBOOK + " != 0";
//        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
        String selection = "";
        if (!selAlbum.isEmpty()) {
            selection = " " +
                    MediaStore.Audio.Media.ALBUM + " = \'" + clearedSelAlbum + "\'";
        }
        if (selAlbum.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                selection = " " +
                        MediaStore.Audio.Media.RELATIVE_PATH + " = \'" + dirWay + "\'";
            } else {
                selection = " " +
                        MediaStore.Audio.Media.DATA + " = \'" + dirWay + "\'";
            }
            selection = "";
        }
//        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC ";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projects = SoundScreen.projection;
            sortOrder = MediaStore.Audio.Media.RELATIVE_PATH + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
//            sortOrder = MediaStore.Audio.Media.RELATIVE_PATH + " ASC, " + MediaStore.Audio.Media.TITLE + " ASC ";
        } else {
            projects = SoundScreen.old_projection;
            sortOrder = MediaStore.Audio.Media.DATA + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
//            sortOrder = MediaStore.Audio.Media.DATA + " ASC, " + MediaStore.Audio.Media.TITLE + " ASC ";
        }

        try (Cursor cursor = getApplicationContext().getContentResolver().query(allsongsuri, projects, selection, null, sortOrder)) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            if (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                cursor.close();
                return contentUri;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void saveEmbeddedImage (String curAlbum) {

        FileOutputStream out = null;
        File directory = getFilesDir();
        String fileName= curAlbum + ".png";
        fileName= fileName.replaceAll("/", " ");
        try {
            File miFile = new File(directory, fileName);
            if (miFile.exists() == false)
                miFile.createNewFile();
            out = new FileOutputStream(miFile);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean getPreparedImageInFolder(String curAlbum) throws FileNotFoundException {

        FileInputStream in = null;
        File directory = getFilesDir();
        String fileName= curAlbum + ".png";
        fileName= fileName.replaceAll("/", " ");
        File miFile = new File(directory, fileName);
        if (miFile.exists() == false)
            return false;
        in = new FileInputStream(miFile);
        try {
//            thumbnail= BitmapFactory.decodeFile(fileName);
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

    private boolean seekEmbeddedImage (String curAlbum) throws FileNotFoundException {

        FileInputStream in = null;
        String dirWay= albumGetDir (curAlbum);
        if (dirWay== null)
            return false;
        int ind = dirWay.lastIndexOf("/");
        dirWay= dirWay.substring(0, ind+ 1);
//        dirWay= shortDirWay;
//        File directory = getFilesDir();
        File miFile;
        String fileName= dirWay + "folder.jpg";
        File sdPath = Environment.getExternalStorageDirectory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            miFile = new File(sdPath, fileName);
        } else {
            miFile = new File(fileName);
        }

        if (miFile.exists() == false  ||  !miFile.canRead()) {
            fileName= dirWay + "cover.jpg";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                miFile = new File(sdPath, fileName);
            } else {
                miFile = new File(fileName);
            }
        }

        if (miFile.exists() == false  ||  !miFile.canRead()) {
            fileName= dirWay + "embeddedcover.jpg";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                miFile = new File(sdPath, fileName);
            } else {
                miFile = new File(fileName);
            }
            if (miFile.exists() == false)
                return false;
        }
//        boolean canr= false;
//        canr= miFile.canRead();
        in = new FileInputStream(miFile);
        try {
//            thumbnail= BitmapFactory.decodeFile(fileName);
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
        if (thumbnail!= null  &&  !thumbnail.toString().equals(""))
            return true;
        return false;

    }

    public String albumGetDir (String curAlbum) {
        int relPath = 0;
        String[] projects;

        if (curAlbum.isEmpty())
            return null;

        clearedSelAlbum = curAlbum.replace("\'", "\'\'");
//        List<MusicElement> musicList = (List<MusicElement>) getIntent().getSerializableExtra("AllMusics");
        //       List<AlbumElement> AlbumList = (List<AlbumElement>) getIntent().getSerializableExtra("AllAlbums");
// ---------------------------------------
        Uri allsongsuri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            allsongsuri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            allsongsuri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

//        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 OR " + MediaStore.Audio.Media.IS_AUDIOBOOK + " != 0";
//        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
        String selection = "";
            selection = " " +
                    MediaStore.Audio.Media.ALBUM + " = \'" + clearedSelAlbum + "\'";
//        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC ";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projects = SoundScreen.projection;
            sortOrder = MediaStore.Audio.Media.RELATIVE_PATH + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
//            sortOrder = MediaStore.Audio.Media.RELATIVE_PATH + " ASC, " + MediaStore.Audio.Media.TITLE + " ASC ";
        } else {
            projects = SoundScreen.old_projection;
            sortOrder = MediaStore.Audio.Media.DATA + " ASC, " + MediaStore.Audio.Media.DISPLAY_NAME + " ASC ";
//            sortOrder = MediaStore.Audio.Media.DATA + " ASC, " + MediaStore.Audio.Media.TITLE + " ASC ";
        }

        try (Cursor cursor = getApplicationContext().getContentResolver().query(allsongsuri, projects, selection, null, sortOrder)) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH);
            }
            else {
                relPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            }
            if (cursor.moveToNext()) {
                String relP = cursor.getString(relPath);
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                cursor.close();
//                String absP= contentUri.getPathSegments()
                return relP;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }



}
