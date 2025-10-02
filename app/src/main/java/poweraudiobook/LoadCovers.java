package com.audiobook.pbp_service;


import static android.app.UiModeManager.MODE_NIGHT_YES;
import static android.graphics.Color.GRAY;
import static android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class LoadCovers extends AppCompatActivity {
    ListView list_dir;
    TextView textPath;
    Context _context;
    int select_id_list = -1;
    private boolean first_time = true;
    private Bitmap thumbnail= null;

    Button buttonBack, buttonGo;
    public static final String SELECTED_FOLDER_NAME = "com.audiobook.powerbookplayer.extra.SELECTED_FOLDER";
    public static String ROOT_FOLDER_NAME = "/storage/emulated/0/";
    public static String ROOT_FOLDER_NAME_SHORT = "/storage/emulated/0";
    public static String ROOT_FOLDER_NAME_SUPER_SHORT = "/storage/emulated";
    String path = ROOT_FOLDER_NAME;

    ArrayList<String> ArrayDir = new ArrayList<String>();
    ArrayList<String> ArrayDisks = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    List<StorageVolume> stores;
    int night_mode;

    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        _context = this;
        if (MainActivity.keep_portrait)
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_work_file);
        setupWindowInsets();

        testBrightness();

        list_dir = (ListView) findViewById(R.id.list_dirs);
        textPath = (TextView) findViewById(R.id.textBookMarks);
        buttonBack = (Button) findViewById(R.id.buttonSave);
        buttonBack.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        night_mode= AppCompatDelegate.getDefaultNightMode();

        if (MainActivity.button_color!= 0) {
            if (buttonBack.isEnabled())
                buttonBack.setBackgroundColor(MainActivity.button_color);
        }
        buttonBack.setEnabled(false);
        buttonBack.setBackgroundColor(GRAY);

        StorageManager storage= _context.getSystemService(StorageManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayDir) {
                @NonNull
                @Override
                public View getView (int position, View convertView, @NonNull ViewGroup parent) {
                    // Get the Item from ListView
                    View view = super.getView(position, convertView, parent);

                    // Initialize a TextView for ListView each Item
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    String buff= (String) tv.getText();
                    if (!buff.contains(MainActivity.png_file_extension)
                        &&  !buff.contains(MainActivity.jpg_file_extension)) {
                        if (night_mode == MODE_NIGHT_YES)
                            tv.setTextColor(Color.YELLOW);
                        else
                            tv.setTextColor(Color.BLUE);
                    }
                    else {
                        if (night_mode == MODE_NIGHT_YES)
                            tv.setTextColor(Color.WHITE);
                        else
                            tv.setTextColor(Color.BLACK);
                    }
                    return view;
                }
            };
            stores = storage.getStorageVolumes();
            if (stores.size() > 1) {
                for (int i = 0; i < stores.size(); i++) {
                    if (stores.get(i).isRemovable()) {
                        ArrayDir.add("/storage/" + stores.get(i).getUuid());
                    } else {
                        ArrayDir.add("/storage/emulated/0/");
                    }
                }
                list_dir.setAdapter(adapter);
                select_id_list= -2;
                list_dir.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (first_time) {
                            select_id_list = (int) -1;
                            ROOT_FOLDER_NAME = ArrayDir.get((int) id);
                            ROOT_FOLDER_NAME_SHORT = ROOT_FOLDER_NAME.substring(0, ROOT_FOLDER_NAME.length()- 1);
                            path = ROOT_FOLDER_NAME;
                            first_time= false;
                        }
                        else {
                            select_id_list = (int) id;
                        }
                        String dirName= ArrayDir.get((int) id);
                        try {
                            update_list_dir(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

            } else {
                ROOT_FOLDER_NAME_SHORT = ROOT_FOLDER_NAME;
                path = ROOT_FOLDER_NAME;

                list_dir.setAdapter(adapter);

                try {
                    update_list_dir(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                list_dir.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        select_id_list = (int) id;
                        try {
                            update_list_dir(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }

    }

    public void onClickBack(View view) throws IOException {
        buttonBack.setEnabled(false);
        path = (new File(path)).getParent();
        if (path != null && path.equalsIgnoreCase(ROOT_FOLDER_NAME_SUPER_SHORT)) {
            path = ROOT_FOLDER_NAME_SHORT;
        }
        update_list_dir(path);
    }

    public void onClickGo(View view) {
        Intent intent = new Intent();
        if (path == null) {
            path = "";
            buttonBack.setEnabled(false);
            buttonBack.setBackgroundColor(GRAY);
        }
        intent.putExtra(SELECTED_FOLDER_NAME, path);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void update_list_dir(String loc_path) throws IOException {
        String work_file_name= null;
        if (select_id_list != -1) {
            if (loc_path.length() > 0) {
                if (!loc_path.endsWith("/")) {
                    loc_path = loc_path + "/" + ArrayDir.get(select_id_list) + "/";
                }
                else
                    loc_path = loc_path + ArrayDir.get(select_id_list) + "/";
            }
            if (ArrayDir.get(select_id_list).contains(MainActivity.png_file_extension)) {
                Intent intent = new Intent();
                load_cover_file (loc_path);
                setResult(Activity.RESULT_OK, intent);
                finish();
                return;
            }
            if (ArrayDir.get(select_id_list).contains(MainActivity.jpg_file_extension)) {
                Intent intent = new Intent();
                load_cover_file (loc_path);
                setResult(Activity.RESULT_OK, intent);
                finish();
                return;
            }
        }

        if (loc_path == null
            ||  loc_path.length()< 1
            ||  loc_path.isEmpty()
            ||  loc_path.equalsIgnoreCase("null")
            ||  loc_path.equalsIgnoreCase(ROOT_FOLDER_NAME)
            ||  loc_path.equalsIgnoreCase(ROOT_FOLDER_NAME_SHORT)
            ||  loc_path.equalsIgnoreCase(ROOT_FOLDER_NAME_SUPER_SHORT)
        ) {
                buttonBack.setEnabled(false);
                buttonBack.setBackgroundColor(GRAY);
        }
        else {
            buttonBack.setEnabled(true);
            buttonBack.setBackgroundColor(MainActivity.button_color);
        }

        select_id_list = -1;
        ArrayDir.clear();

        File[] unorderedFiles = new File(loc_path).listFiles();
        Comparator<File> comparator = (f1, f2) ->{
            if (f1.isDirectory() && f2.isFile())
                return 1;
            else if(f2.isDirectory() && f1.isFile())
                return -1;
            else
              return f1.toString().toLowerCase().compareTo(f2.toString().toLowerCase());

        };
        TreeSet<File> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(Arrays.asList(unorderedFiles));

        int pos = 0;
        File[] files = new File[treeSet.size()];
        for (File next: treeSet) {
            files[pos++] = next;
        }

        if (files != null) {
            for (File aFile : files) {
                if (aFile.isDirectory()) {
                    if (dir_opened(aFile.getPath())) {
                        ArrayDir.add(aFile.getName());
                    }
                }
                else {
                    long lsize= aFile.length();
                    if (lsize> 1006000)
                        continue;
                    work_file_name= aFile.getName();
                    if (work_file_name.contains(MainActivity.png_file_extension)) {
                        ArrayDir.add(work_file_name);
                        continue;
                    }
                    if (work_file_name.contains(MainActivity.jpg_file_extension)) {
                        ArrayDir.add(work_file_name);
                        continue;
                    }
                }
            }
        }

        adapter.notifyDataSetChanged();
        textPath.setText(loc_path);
        path= loc_path;
    }

    private boolean dir_opened(String url) {
        try {
            File[] files = new File(url).listFiles();
            for (@SuppressWarnings("unused") File aFile : files) {
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void load_cover_file (String way) throws IOException {

        if (get_cover(way)) {
            save_cover (MainActivity.selAlbum);
        }
    }

    public boolean get_cover(String way) throws FileNotFoundException {

        FileInputStream in = null;
        File miFile = new File(way);
        if (!miFile.exists())
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

    private void save_cover (String curAlbum) {

        FileOutputStream out = null;
        File directory = getFilesDir();
        String fileName= curAlbum + ".png";
        fileName= fileName.replaceAll("/", " ");
        try {
            File miFile = new File(directory, fileName);
            if (!miFile.exists())
                miFile.createNewFile();
            out = new FileOutputStream(miFile);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 10, out); // bmp is your Bitmap instance
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

    public void testBrightness() {
        if (MainActivity.full_brightness) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = BRIGHTNESS_OVERRIDE_FULL;
            getWindow().setAttributes(layoutParams);
        }
        if (!MainActivity.full_brightness) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = -1;
            getWindow().setAttributes(layoutParams);
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

}
