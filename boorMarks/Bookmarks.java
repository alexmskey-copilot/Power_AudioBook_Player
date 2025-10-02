package com.audiobook.pbp_service;


import static android.graphics.Color.GRAY;
import static android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
import static com.audiobook.pbp_service.MainActivity.lesson;
import static com.audiobook.pbp_service.MainActivity.lesson_point;
import static com.audiobook.pbp_service.MainActivity.mediaController;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.audiobook.pbp_service.service.PlayerService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Bookmarks extends AppCompatActivity {
    ListView list_bms;
    TextView textBookMarks;
    Context _context;
    private boolean after_del = false;
    public  static boolean need_refresh_BMs= false;

    Button buttonDel, buttonAdd;
    ImageButton buttonPlay;
    boolean in_name_edit= false;
    int     pos_name_edit= 0;

    static ArrayList<String> ArrayBMs = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    static ArrayList<Button> ArrayBtns = new ArrayList<Button>();
    static ArrayList<EditText> ArrayEdtxt = new ArrayList<EditText>();

    public static final int BOOKMARKS_FILE_VERSION= 3;      // Версия формата файла
                                                            // 1 - двоичный файл
                                                            // 2 - файл переведен в формате CSV
                                                            // 3 - добавлен признак ручной правки имени
    public static final int BOOKMARK_TYPE_CUSTOM= 0;        // закладка пользователя
    public static final int BOOKMARK_TYPE_CONTENT= 1;       // оглавление (с дорожки глав M4A/M4B)
    public static int begin_repeate= -1;                    // Начало повторяемого фрагмента
    public static int end_repeate= -1;                      // Конец повторяемого фрагмента
    public static boolean bookmark_repeate_mode= false;     // Режим повтора от закладки до закладки


    public static class bookMarkElement_old implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String partName;
        public final int lesson;
        public final int offset;

        public bookMarkElement_old(String partName, int lesson, int offset) {
            this.partName = partName;
            this.lesson = lesson;
            this.offset = offset;
        }
    }

    public static class bookMarkElement {
        public String partName;       // имя части
        public int lesson;            // часть книги
        public int offset;            // смещение от нпачала части
        public int type_bm;           // тип закладки
                                            //      0 клиентская
                                            //      1 оглавление из файла M4A/M4B
        public boolean self_changed;// признак ручной правки названия закладки
                                            // false - название сгенериовано автоматически
                                            // true  - названиек введено вручную.
                                            //         Автогенерация отключена

        public bookMarkElement(String partName, int lesson, int offset, int type_bm, boolean self_changed) {
            this.partName= partName;
            this.lesson= lesson;
            this.offset= offset;
            this.type_bm= type_bm;
            this.self_changed= self_changed;
        }
    }

    bookMarkElement bm = new bookMarkElement("", 0, 0, 0, false);
    public static List<bookMarkElement> bookMarkList = new ArrayList<>();

    @SuppressLint("ResourceType")
    protected void onCreate(Bundle savedInstanceState) {

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        _context = this;
        super.onCreate(savedInstanceState);
        if (MainActivity.keep_portrait)
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_bookmarks);
        setupWindowInsets();

        testBrightness();
        bookMarkList.clear();
        ArrayBMs.clear();
        bookmark_repeate_mode= false;

        list_bms = (ListView) findViewById(R.id.list_bms);
        textBookMarks = (TextView) findViewById(R.id.textBookMarks);
        buttonDel = (Button) findViewById(R.id.buttonSave);
        buttonDel.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));

        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonAdd.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));

        buttonPlay = (ImageButton) findViewById(R.id.buttonRepeatBookMarks);
        buttonPlay.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        buttonPlay.setBackgroundColor(MainActivity.button_color);
        if ((begin_repeate== -1  &&  end_repeate== -1)
            ||  ArrayBMs.size()< 2) {
            buttonPlay.setEnabled(false);
            buttonPlay.setBackgroundColor(GRAY);
        }
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    write_book_marks ();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                MainActivity.repeat_state= MainActivity.BOOKMARK_REPEAT;
                bookmark_repeate_mode= true;
                MainActivity.pos_rqst_from_BMs_controller= true;
                MainActivity.lesson_point[bookMarkList.get ((int) begin_repeate).lesson]= bookMarkList.get ((int) begin_repeate).offset;
                mediaController.getTransportControls().playFromMediaId(String.valueOf(bookMarkList.get ((int) begin_repeate).lesson), null);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        if (MainActivity.button_color!= 0) {
            buttonAdd.setBackgroundColor(MainActivity.button_color);
            if (buttonDel.isEnabled())
                buttonDel.setBackgroundColor(MainActivity.button_color);
        }

        if (MainActivity.selAlbum.isEmpty()) {
            Toast.makeText(this, this.getResources().getString(R.string.book_not_sel), Toast.LENGTH_LONG).show();
            bookmark_repeate_mode= false;
            begin_repeate= end_repeate= -1;
            finish();
        }

        try {
            get_bm_file();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (ArrayBMs.isEmpty()) {
            buttonDel.setEnabled(false);
            buttonDel.setBackgroundColor(GRAY);
        }
        adapter = new ArrayAdapter<String>(this, R.layout.item_layout, ArrayBMs)
        {
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {

                LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.item_layout, parent, false);

                EditText book_mark_text = (EditText) view.findViewById(R.id.text1);

                if (begin_repeate != -1  &&  end_repeate!= -1) {
                    if (begin_repeate <= position && end_repeate >= position) {
                        book_mark_text.setBackgroundColor(Color.LTGRAY);
                    }
                }
                if (position< bookMarkList.size()) {
                    book_mark_text.setText(ArrayBMs.get(position));
                    if (in_name_edit  &&  pos_name_edit== position) {
                        book_mark_text.setInputType(InputType.TYPE_CLASS_TEXT);
                        book_mark_text.setFocusable(true);
                        book_mark_text.setFocusableInTouchMode(true);
                        book_mark_text.setCursorVisible(true);
                        book_mark_text.requestFocus();
                        book_mark_text.setTextColor(Color.RED);
                    }
                    else {
                        if (bookMarkList.get(position).type_bm == 0) {      // Закладка пользовательская
                            book_mark_text.setTextColor(Color.GREEN);
                        }
                        if (bookMarkList.get(position).type_bm == 1) {      // закладка APPLE
                            book_mark_text.setTextColor(Color.BLUE);
                        }
                        book_mark_text.setInputType(InputType.TYPE_NULL); // полностью отключает ввод
                        book_mark_text.setFocusable(false);
                        book_mark_text.setFocusableInTouchMode(false);
                        book_mark_text.setCursorVisible(false);
                        book_mark_text.setClickable(true);
                    }

                    book_mark_text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (in_name_edit)
                                return;
                            if (after_del) {
                                after_del= false;
                                return;
                            }
                            try {
                                gotobookmark(position);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                        book_mark_text.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                String bm_text = s.toString();
                            }
                        });

                    Button button_edit = (Button) view.findViewById(R.id.edit_button);
                    if (in_name_edit) {
                        if (position== pos_name_edit)
                            button_edit.setBackgroundResource(R.drawable.ic_stylus_red);
                        else
                            button_edit.setBackgroundResource(R.drawable.ic_stylus);
                    }
                    else {
                        button_edit.setBackgroundResource(R.drawable.ic_stylus);
                    }
                    button_edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (in_name_edit) {
                                if (position!= pos_name_edit) {
                                    book_mark_text.setInputType(InputType.TYPE_NULL); // полностью отключает ввод
                                    book_mark_text.setFocusable(false);
                                    book_mark_text.setFocusableInTouchMode(false);
                                    book_mark_text.setCursorVisible(false);
                                    book_mark_text.setClickable(true);
                                    return;
                                }
                                in_name_edit= false;
                                int lesson_loc = bookMarkList.get(position).lesson;
                                int offset_loc = bookMarkList.get(position).offset;
                                int type_bm_loc = bookMarkList.get(position).type_bm;
                                String  bm_text= book_mark_text.getText().toString();
                                bm = new bookMarkElement(bm_text, lesson_loc, offset_loc, type_bm_loc, true);
                                bookMarkList.set(position, bm);
                                ArrayBMs.set(position, bm_text);
                                if (bookMarkList.get(position).type_bm == 0) {      // Закладка пользовательская
                                    book_mark_text.setTextColor(Color.GREEN);
                                }
                                if (bookMarkList.get(position).type_bm == 1) {      // закладка APPLE
                                    book_mark_text.setTextColor(Color.BLUE);
                                }
                                button_edit.setBackgroundResource(R.drawable.ic_stylus);
                                return;
                            }

                            in_name_edit= true;
                            pos_name_edit= position;
                            book_mark_text.setInputType(InputType.TYPE_CLASS_TEXT);
                            book_mark_text.setFocusable(true);
                            book_mark_text.setFocusableInTouchMode(true);
                            book_mark_text.setCursorVisible(true);
                            book_mark_text.requestFocus();
                            book_mark_text.setTextColor(Color.RED);
                            button_edit.setBackgroundResource(R.drawable.ic_stylus_red);
                        }
                    });

                    Button button_repeat = (Button) view.findViewById(R.id.repeate_button);
                    button_repeat.setBackgroundResource(R.drawable.ic_repeat_off);
                    button_repeat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (begin_repeate!= -1  &&  end_repeate!= -1) {
                                end_repeate= begin_repeate= -1;
                                adapter.notifyDataSetChanged();
                                buttonPlay.setEnabled(false);
                                buttonPlay.setBackgroundColor(GRAY);
                                return;
                            }
                            book_mark_text.setBackgroundColor(Color.LTGRAY);
                            if (begin_repeate== -1) {
                                begin_repeate = position;
                            }
                            else {
                                if (position< begin_repeate) {
                                    end_repeate= begin_repeate;
                                    begin_repeate= position;
                                }
                                else {
                                    end_repeate = position;
                                }
                                buttonPlay.setEnabled(true);
                                buttonPlay.setBackgroundColor(MainActivity.button_color);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });


                }

                // Generate ListView Item using TextView
                return view;
            }

        };
        list_bms.setAdapter(adapter);
            list_bms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (after_del) {
                        after_del= false;
                        return;
                    }
                    try {
                        gotobookmark(id);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            list_bms.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    bookMarkList.remove((int) id);
                    ArrayBMs.remove((int) id);
                    adapter.notifyDataSetChanged();
                    after_del= true;
                    if (ArrayBMs.size()< 2) {
                        buttonPlay.setEnabled(false);
                        buttonPlay.setBackgroundColor(GRAY);
                    }
                    return false;
                }
            });
    }

    public static boolean get_bm_file() throws IOException {
        FileInputStream in = null;
        File directory = MainActivity.directory_cfg;
        int cur_bms_version= 0;
        int lesson, offset, type_bm;
        boolean self_changed;
        bookMarkElement bm = new bookMarkElement("", 0, 0, 0, false);

        String fileName= MainActivity.selAlbum + ".bms";
        String fileName_csv= MainActivity.selAlbum + ".bmx";

        fileName= fileName.replaceAll("/", " ");
        fileName= fileName.replaceAll("'", " ");
        fileName_csv= fileName_csv.replaceAll("/", " ");
        fileName_csv= fileName_csv.replaceAll("'", " ");

        File miFile = new File(directory, fileName);
        File miFile_csv = new File(directory, fileName_csv);

        if (miFile_csv.exists()) {
            in = new FileInputStream(miFile_csv);
            InputStreamReader inputStreamReader_csv = new InputStreamReader(in);

            try (BufferedReader reader = new BufferedReader(inputStreamReader_csv)) {
                MainActivity.textBuf = reader.readLine();  // get current file format version
                if (MainActivity.textBuf != null)
                    cur_bms_version = Integer.parseInt(MainActivity.textBuf);
                else
                    cur_bms_version = 0;
                if (cur_bms_version == 2) {
                    bookMarkList.clear();
                    while (true) {
                        MainActivity.textBuf = reader.readLine();  // get lesson, offset, type
                        if (MainActivity.textBuf== null)
                            break;
                        String textPars[] = MainActivity.textBuf.split(",");
                        if (textPars[0] != null) {  // номер трека
                            lesson= Integer.parseInt(textPars[0]);
                        } else {
                            break;
                        }
                        if (textPars[1] != null) {  // позиция в файле
                            offset= Integer.parseInt(textPars[1]);
                        } else {
                            break;
                        }
                        if (textPars[2] != null) {  // тип закладки
                            type_bm= Integer.parseInt(textPars[2]);
                        } else {
                            break;
                        }
                        MainActivity.textBuf = reader.readLine();  // get name
                        if (MainActivity.textBuf== null)
                            break;
                        bm= new bookMarkElement(MainActivity.textBuf, lesson, offset, type_bm, false);
                        bookMarkList.add(bm);
                        ArrayBMs.add(make_bm_view(bm.lesson, bm.partName, bm.offset, bm.self_changed));
                    }
                    in.close();
                    inputStreamReader_csv.close();
                }
                if (cur_bms_version == 3) {
                    bookMarkList.clear();
                    while (true) {
                        MainActivity.textBuf = reader.readLine();  // get lesson, offset, type
                        if (MainActivity.textBuf== null)
                            break;
                        String textPars[] = MainActivity.textBuf.split(",");
                        if (textPars[0] != null) {  // номер трека
                            lesson= Integer.parseInt(textPars[0]);
                        } else {
                            break;
                        }
                        if (textPars[1] != null) {  // позиция в файле
                            offset= Integer.parseInt(textPars[1]);
                        } else {
                            break;
                        }
                        if (textPars[2] != null) {  // тип закладки
                            type_bm= Integer.parseInt(textPars[2]);
                        } else {
                            break;
                        }
                        if (textPars[3] != null) {  // имя введено вручную
                            self_changed= Boolean.parseBoolean(textPars[3]);
                        } else {
                            break;
                        }
                        MainActivity.textBuf = reader.readLine();  // get name
                        if (MainActivity.textBuf== null)
                            break;
                        bm= new bookMarkElement(MainActivity.textBuf, lesson, offset, type_bm, self_changed);
                        bookMarkList.add(bm);
                        ArrayBMs.add(make_bm_view(bm.lesson, bm.partName, bm.offset, bm.self_changed));
                    }
                    in.close();
                    inputStreamReader_csv.close();
                }

            }
            return true;
        }

        if (miFile.exists()) {
            try {
                in = new FileInputStream(miFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            ObjectInputStream objReader = null;
            try {
                objReader = new ObjectInputStream(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            bookMarkList.clear();
            bookMarkElement_old bme = null;
            while (true) {
                try {
                    bme = (bookMarkElement_old) objReader.readObject();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    break;
                }
                bm= new bookMarkElement(bme.partName, bme.lesson, bme.offset, BOOKMARK_TYPE_CUSTOM, false);
                bookMarkList.add(bm);
                ArrayBMs.add(make_bm_view(bme.lesson, bme.partName, bme.offset, false));
            }
            try {
                in.close();
                objReader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return bookMarkList.size() > 0;
        }

        return false;
    }

    public static String make_bm_view(int lesson, String partName, int offset, boolean self_changed) {

        if (self_changed)
            return partName;
        if (TimeUnit.MILLISECONDS.toMinutes((long) offset)< 60) {
            String textBuf = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) offset),
                    TimeUnit.MILLISECONDS.toSeconds((long) offset) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    offset)));
            if (MainActivity.lesson== lesson  &&  !MainActivity.fucking_apple)
                return ("* " + lesson + " " + partName + " " + textBuf);
            return (lesson + " " + partName + " " + textBuf);
        }
        else {
            String textBuf= String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours((long) offset),
                    TimeUnit.MILLISECONDS.toMinutes((long) offset)-
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) offset)),
                    TimeUnit.MILLISECONDS.toSeconds((long) offset) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    offset)));
            if (MainActivity.lesson== lesson  &&  !MainActivity.fucking_apple)
                return ("* " + lesson + " " + partName + " " + textBuf);
            return (lesson + " " + partName + " " + textBuf);
        }

    }

    public void onClickSave(View view) throws IOException {

        buttonDel.setEnabled(false);
        write_book_marks ();
        bookmark_repeate_mode= false;
        begin_repeate= end_repeate= -1;
        finish();
    }

    public static void write_book_marks() throws IOException {
        File directory = MainActivity.directory_cfg;
        bookMarkElement bm = new bookMarkElement("", 0, 0, 0, false);

        String fileName= MainActivity.selAlbum + ".bms";
        String fileName_csv= MainActivity.selAlbum + ".bmx";    // закладки в формате CSV

        fileName= fileName.replaceAll("/", " ");
        fileName= fileName.replaceAll("'", " ");
        fileName_csv= fileName_csv.replaceAll("/", " ");
        fileName_csv= fileName_csv.replaceAll("'", " ");

        File miFile = new File(directory, fileName);
        File miFile_csv = new File(directory, fileName_csv);

        if (miFile.exists())
            miFile.delete();
        if (!miFile_csv.exists())
            miFile_csv.createNewFile();

        FileOutputStream fis_csv = new FileOutputStream(miFile_csv);
        OutputStreamWriter outputStreamWriter_csv = new OutputStreamWriter(fis_csv);

        // сортировка
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bookMarkList.sort (new Comparator<bookMarkElement>() {
                @Override
                public int compare(bookMarkElement o1, bookMarkElement o2) {
                    int i= 0;
                    i= o1.lesson - o2.lesson;
                    if (i!= 0)
                        return i;
                    return (int) (o1.offset - o2.offset);
                }
            });
        }

        // дедубликация
        for (int i = 0; i< bookMarkList.size(); i++) {
            if (!dedoubleMB ())
                break;
            else
                i= 0;
        }

        MainActivity.textBuf = String.format("%d\n", BOOKMARKS_FILE_VERSION);  // current file format version
        outputStreamWriter_csv.write(MainActivity.textBuf);
        for (int i = 0; i< bookMarkList.size(); i++) {
            if (bookMarkList.get(i).partName!= null)
                bm.partName= bookMarkList.get(i).partName;
            else
                bm.partName= "";
            bm.lesson= bookMarkList.get(i).lesson;
            bm.offset= bookMarkList.get(i).offset;
            bm.type_bm= bookMarkList.get(i).type_bm;
            bm.self_changed= bookMarkList.get(i).self_changed;
            MainActivity.textBuf = String.format("%d,%d,%d,%s\n", bm.lesson, bm.offset, bm.type_bm, bm.self_changed);  // current file format version
            outputStreamWriter_csv.write(MainActivity.textBuf);
            MainActivity.textBuf = String.format("%s\n", bm.partName);  // current file format version
            outputStreamWriter_csv.write(MainActivity.textBuf);
        }

        outputStreamWriter_csv.flush();
        outputStreamWriter_csv.close();
        need_refresh_BMs= true;
    }

    public static boolean dedoubleMB() {
        for (int i = 0; i< bookMarkList.size(); i++) {
            if (i< bookMarkList.size()- 1) {
                if (bookMarkList.get(i).lesson == bookMarkList.get(i+ 1).lesson) {
                    if (bookMarkList.get(i).offset == bookMarkList.get(i+ 1).offset) {
                        bookMarkList.remove(i); return true;
                    }
                }
            }
        }
        return false;
    }

    public void onClickAdd(View view) {
        buttonDel.setEnabled(true);
        buttonDel.setBackgroundColor(MainActivity.button_color);
        String newBM = null;
        newBM=  make_bm_view(lesson,SoundScreen.musicList.get(lesson - 1).name, MainActivity.mediaPlayer.getCurrentPosition(), false);
        ArrayBMs.add(newBM);
        int position;
        if (MainActivity.mediaPlayer!= null) {
            position = MainActivity.mediaPlayer.getCurrentPosition();
        }
        else {
            position = MainActivity.seekBar_part.getProgress();
        }
        bookMarkList.add(new bookMarkElement(SoundScreen.musicList.get(lesson - 1).name, lesson, position, BOOKMARK_TYPE_CUSTOM, false));
        adapter.notifyDataSetChanged();
    }

    private void gotobookmark(long idBM) throws IOException {
        write_book_marks ();
        MainActivity.pos_rqst_from_BMs_controller= true;
        MainActivity.lesson_point[bookMarkList.get ((int) idBM).lesson]= bookMarkList.get ((int) idBM).offset;
        PlayerService.aElement= new MainActivity.Action_Element(MainActivity.Action_Element.ACTION_SN, lesson, lesson_point[lesson]);
        MainActivity.actionsList.add(PlayerService.aElement);
        mediaController.getTransportControls().playFromMediaId(String.valueOf(bookMarkList.get ((int) idBM).lesson), null);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
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

    @Override
    protected void onDestroy () {

        super.onDestroy();
        if (!bookmark_repeate_mode)
            begin_repeate= end_repeate= -1;

    }


}
