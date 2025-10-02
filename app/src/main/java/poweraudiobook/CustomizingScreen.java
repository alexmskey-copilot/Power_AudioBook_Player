package com.audiobook.pbp_service;

import static android.graphics.Color.GRAY;
import static android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomizingScreen  extends AppCompatActivity {

    private ScrollView mainScroll;
    private Button  button_read_end,
            button_set_root_folder,
            button_folder_visibility,
            button_folder_visibility_list;
    private MaterialButton  buttons_color1, buttons_color2, buttons_color3,
                            buttons_color4, buttons_color5, buttons_color6;
    private Switch right_handed;
    private Switch left_handed;
    private Switch simetric_buttons;
    private Switch one_finger_right;
    private Switch one_finger_left;
    private Switch keep_screen;
    private Switch duck_ignoring;
    private Switch other_players_not_ignore_sw;

    private Switch bad_wire;
    private Switch full_brightness;
    private Switch keep_portrait_sw;
    private Switch show_artist;
    private Switch show_composer;
    private Switch showSystemFolders;
    private Switch show_one_album_in_folder;
    private Switch bold_font_for_long_names_sw;
    private Switch show_full_book_name_sw;
    private Switch systemTheme;
    private Switch autoThemeSwith;
    private Switch lightTheme;
    private Switch darkTheme;
    private Switch non_stop_change_orient;
    private Switch zero_back_in_time_change_orient;
    private Switch set_speed_step;
    private Switch set_always_begin;
    private Switch use_audiobook_folder;
    private Switch time_speaking_sw;
    private Switch time_speaking_on_play_sw;
    private Switch time_speaking_on_pause_sw;
    private Switch nosave_speed;
    private Switch pause_skipping_sw;
    private Switch always_show_favorites_sw;
    private Switch exit_only_in_menu_sw;
    private Switch backPressed_switch_to_background_sw;
    private Switch swap_fastMoving_and_goto_sw;
    private Switch swap_fastMoving_sw;
    private Switch goto_bookmarks_in_fucking_apple_style_sw;
    private SeekBar transparency_buttons;
    private TextView transparency_value;
    private TextView audio_book_path;
    private TextView pause_back_value_view;
    private TextView fast_moving_value_view;
    private TextView folder_ibvisible_view;
    private TextView buttonColorSelect;
    private static Context appContext;
    private SeekBar button_size_selector;
    private SeekBar pause_back_value_selector;
    private SeekBar fast_moving_value_selector;
    private FloatingActionButton scrollButton;
    private LinearLayout buttons_color_layout;
    private TextView buttons_size_text;
    private LinearLayout buttonSize_layout;
    private LinearLayout pause_back_layout;
    private LinearLayout fast_moving_value_layout;

    private int night_mode= 0;
    private ArrayList<String> DirHidden = new ArrayList<String>();
    private int BEGIN_SCROLL= 0, // начало скролируемой формы
                END_SCROLL= 1,   // конец скролируемой формы
                scrollPosition= BEGIN_SCROLL;   // текущее положение в скролируемой форме.



    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        super.onCreate(savedInstanceState);
        if (MainActivity.keep_portrait)
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        testBrightness();
        MainActivity.need_hand_change = false;
        MainActivity.need_screen_keeping_change = false;
        setContentView(R.layout.activity_customizing_screen);
        setupWindowInsets();

        mainScroll= (ScrollView) findViewById(R.id.mainScroll);
        right_handed = (Switch) findViewById(R.id.right_handed);
        left_handed = (Switch) findViewById(R.id.left_handed);
        simetric_buttons = (Switch) findViewById(R.id.simetric_buttons);
        one_finger_right = (Switch) findViewById(R.id.one_finger_right);
        one_finger_left = (Switch) findViewById(R.id.one_finger_left);
        keep_screen = (Switch) findViewById(R.id.keep_screen);
        duck_ignoring = (Switch) findViewById(R.id.duck_ignoring);
        other_players_not_ignore_sw = (Switch) findViewById(R.id.other_players_not_ignoring);
        bad_wire = (Switch) findViewById(R.id.bad_wire);
        full_brightness = (Switch) findViewById(R.id.full_brightness);
        keep_portrait_sw = (Switch) findViewById(R.id.keep_portrait_orient);
        show_artist = (Switch) findViewById(R.id.show_artist);
        show_composer = (Switch) findViewById(R.id.show_composer);
        showSystemFolders = (Switch) findViewById(R.id.showSystemFolders);
        show_one_album_in_folder = (Switch) findViewById(R.id.showOneAlbumInFolder);
        bold_font_for_long_names_sw = (Switch) findViewById(R.id.boldFontForLongNames);
        show_full_book_name_sw = (Switch) findViewById(R.id.showFullBookName);
        systemTheme = (Switch) findViewById(R.id.asSystemThemes);
        autoThemeSwith = (Switch) findViewById(R.id.autoThemes);
        lightTheme = (Switch) findViewById(R.id.lightThemes);
        darkTheme = (Switch) findViewById(R.id.darkThemes);
        non_stop_change_orient =  (Switch) findViewById(R.id.nonStopChangeOrient);
        zero_back_in_time_change_orient =  (Switch) findViewById(R.id.zero_back_in_time_after_change);
        set_speed_step =  (Switch) findViewById(R.id.speedStep005);
        set_always_begin =  (Switch) findViewById(R.id.alwaysBeginPart);
        use_audiobook_folder =  (Switch) findViewById(R.id.useAudioBookFolder);
        time_speaking_sw = (Switch) findViewById(R.id.time_speaking);
        time_speaking_on_play_sw = (Switch) findViewById(R.id.time_speaking_play);
        time_speaking_on_pause_sw = (Switch) findViewById(R.id.time_speaking_stop);
        nosave_speed = (Switch) findViewById(R.id.nosave_speed);
        pause_skipping_sw = (Switch) findViewById(R.id.pause_skipping);
        always_show_favorites_sw = (Switch) findViewById(R.id.always_show_favorites);
        exit_only_in_menu_sw = (Switch) findViewById(R.id.exit_only_in_menu);
        backPressed_switch_to_background_sw = (Switch) findViewById(R.id.backPressed_switch_to_background);
        swap_fastMoving_and_goto_sw = (Switch) findViewById(R.id.change_fastMoving_and_goTo_keys);
        swap_fastMoving_sw = (Switch) findViewById(R.id.exchange_fastMoving_keys);
        swap_fastMoving_sw.setEnabled(false);
        goto_bookmarks_in_fucking_apple_style_sw = (Switch) findViewById(R.id.goto_bookmarks_in_fucking_apple_style);
        transparency_buttons = (SeekBar) findViewById(R.id.transaprency_bar);
        transparency_value = (TextView) findViewById(R.id.transparency_value);
        buttonColorSelect= (TextView) findViewById(R.id.buttonColorSelect);
        scrollButton= (FloatingActionButton) findViewById(R.id.scroll_button);
        buttons_color_layout= (LinearLayout) findViewById(R.id.buttons_color_layout);
        buttons_size_text= (TextView) findViewById(R.id.buttons_size);
        buttonSize_layout= (LinearLayout) findViewById(R.id.buttonSize_layout);
        pause_back_layout= (LinearLayout) findViewById(R.id.pause_back_layout);
        fast_moving_value_layout= (LinearLayout) findViewById(R.id.fast_moving_value_layout);


        audio_book_path = (TextView) findViewById(R.id.audioBookFolderPatch);
        if (MainActivity.root_folder_path == null)
            audio_book_path.setText(R.string.root_folder_not_defined_text);
        else
            audio_book_path.setText(MainActivity.root_folder_path);

        button_size_selector= (SeekBar) findViewById(R.id.buttons_size_selector);

        pause_back_value_view = (TextView) findViewById(R.id.pause_back_value);
        pause_back_value_view.setText(" " + (MainActivity.back_in_time_cust+ 6) + " ");
        fast_moving_value_view = (TextView) findViewById(R.id.fast_moving_value);
        fast_moving_value_view.setText(" " + ((MainActivity.fast_moving_cust)* 5) + " ");
        folder_ibvisible_view = (TextView) findViewById(R.id.folder_invisible_list);
        pause_back_value_selector= (SeekBar) findViewById(R.id.pause_back_value_selector);
        pause_back_value_selector.setProgress(MainActivity.back_in_time_cust- 6);
        fast_moving_value_selector= (SeekBar) findViewById(R.id.fast_moving_value_selector);
        fast_moving_value_selector.setProgress(MainActivity.fast_moving_cust- 1);

        night_mode= AppCompatDelegate.getDefaultNightMode();
        button_read_end = (Button) findViewById(R.id.buttonReadEnd);
        button_read_end.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        if (MainActivity.button_color!= 0) {
            button_read_end.setBackgroundColor(MainActivity.button_color);
        }
        button_set_root_folder = (Button) findViewById(R.id.audioBookFolderButton);
        if (MainActivity.button_color!= 0) {
            button_set_root_folder.setBackgroundColor(MainActivity.button_color);
        }
        button_set_root_folder.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));

        button_folder_visibility = (Button) findViewById(R.id.buttonFolderVisibility);
        if (MainActivity.button_color!= 0) {
            button_folder_visibility.setBackgroundColor(MainActivity.button_color);
        }
        button_folder_visibility.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));

        button_folder_visibility_list = (Button) findViewById(R.id.buttonFolderVisibilityList);
        if (MainActivity.button_color!= 0) {
            button_folder_visibility_list.setBackgroundColor(MainActivity.button_color);
        }
        button_folder_visibility_list.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));

        buttons_color1 = (MaterialButton) findViewById(R.id.button_color_select1);
        buttons_color2 = (MaterialButton) findViewById(R.id.button_color_select2);
        buttons_color3 = (MaterialButton) findViewById(R.id.button_color_select3);
        buttons_color4 = (MaterialButton) findViewById(R.id.button_color_select4);
        buttons_color5 = (MaterialButton) findViewById(R.id.button_color_select5);
        buttons_color6 = (MaterialButton) findViewById(R.id.button_color_select6);
        colorButtonsReDraw ();

        appContext = getApplicationContext();

        View UI_spoilerHeader = findViewById(R.id.UI_spoiler);
        TextView UI_arrowIcon = findViewById(R.id.UI_arrowIcon);
        View screen_spoilerHeader = findViewById(R.id.screen_spoiler);
        TextView screen_arrowIcon = findViewById(R.id.screen_arrowIcon);
        View sound_spoilerHeader = findViewById(R.id.sound_spoiler);
        TextView sound_arrowIcon = findViewById(R.id.sound_arrowIcon);
        View search_spoilerHeader = findViewById(R.id.search_spoiler);
        TextView search_arrowIcon = findViewById(R.id.search_arrowIcon);
        View speed_spoilerHeader = findViewById(R.id.speed_spoiler);
        TextView speed_arrowIcon = findViewById(R.id.speed_arrowIcon);
        View time_spoilerHeader = findViewById(R.id.time_spoiler);
        TextView time_arrowIcon = findViewById(R.id.time_arrowIcon);
        View interface_spoilerHeader = findViewById(R.id.interface_spoiler);
        TextView interface_arrowIcon = findViewById(R.id.interface_arrowIcon);
        View themes_spoilerHeader = findViewById(R.id.themes_spoiler);
        TextView themes_arrowIcon = findViewById(R.id.themes_arrowIcon);
        View buttonsCustomizing_spoilerHeader = findViewById(R.id.buttonsCustomizing_spoiler);
        TextView buttonsCustomizing_arrowIcon = findViewById(R.id.buttonsCustomizing_arrowIcon);
        View rootFolder_spoilerHeader = findViewById(R.id.rootFolder_spoiler);
        TextView rootFolder_arrowIcon = findViewById(R.id.rootFolder_arrowIcon);
        View fast_rewind_spoilerHeader = findViewById(R.id.fast_rewind_spoiler);
        TextView fast_rewind_arrowIcon = findViewById(R.id.fast_rewind_arrowIcon);
        View folderVisibility_spoilerHeader = findViewById(R.id.folderVisibility_spoiler);
        TextView folderVisibility_arrowIcon = findViewById(R.id.folderVisibility_arrowIcon);

        if (!MainActivity.UI_spoiler_open) {
            right_handed.setVisibility(View.GONE);
            left_handed.setVisibility(View.GONE);
            simetric_buttons.setVisibility(View.GONE);
            one_finger_left.setVisibility(View.GONE);
            one_finger_right.setVisibility(View.GONE);
            UI_arrowIcon.setText("▶");
        }

        UI_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) UI_spoilerHeader.getParent()
                );

                if (right_handed.getVisibility() == View.GONE) {
                    right_handed.setVisibility(View.VISIBLE);
                    left_handed.setVisibility(View.VISIBLE);
                    simetric_buttons.setVisibility(View.VISIBLE);
                    one_finger_left.setVisibility(View.VISIBLE);
                    one_finger_right.setVisibility(View.VISIBLE);
                    UI_arrowIcon.setText("▼");
                    MainActivity.UI_spoiler_open= true;
                } else {
                    right_handed.setVisibility(View.GONE);
                    left_handed.setVisibility(View.GONE);
                    simetric_buttons.setVisibility(View.GONE);
                    one_finger_left.setVisibility(View.GONE);
                    one_finger_right.setVisibility(View.GONE);
                    UI_arrowIcon.setText("▶");
                    MainActivity.UI_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.screen_spoiler_open) {
            keep_screen.setVisibility(View.GONE);
            full_brightness.setVisibility(View.GONE);
            keep_portrait_sw.setVisibility(View.GONE);
            show_artist.setVisibility(View.GONE);
            show_composer.setVisibility(View.GONE);
            screen_arrowIcon.setText("▶");
        }

        screen_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) screen_spoilerHeader.getParent()
                );

                if (keep_screen.getVisibility() == View.GONE) {
                    keep_screen.setVisibility(View.VISIBLE);
                    full_brightness.setVisibility(View.VISIBLE);
                    keep_portrait_sw.setVisibility(View.VISIBLE);
                    show_artist.setVisibility(View.VISIBLE);
                    show_composer.setVisibility(View.VISIBLE);
                    screen_arrowIcon.setText("▼");
                    MainActivity.screen_spoiler_open= true;
                } else {
                    keep_screen.setVisibility(View.GONE);
                    full_brightness.setVisibility(View.GONE);
                    keep_portrait_sw.setVisibility(View.GONE);
                    show_artist.setVisibility(View.GONE);
                    show_composer.setVisibility(View.GONE);
                    screen_arrowIcon.setText("▶");
                    MainActivity.screen_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.sound_spoiler_open) {
            duck_ignoring.setVisibility(View.GONE);
            other_players_not_ignore_sw.setVisibility(View.GONE);
            bad_wire.setVisibility(View.GONE);
            non_stop_change_orient.setVisibility(View.GONE);
            zero_back_in_time_change_orient.setVisibility(View.GONE);
            sound_arrowIcon.setText("▶");
        }

        sound_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) sound_spoilerHeader.getParent()
                );

                if (duck_ignoring.getVisibility() == View.GONE) {
                    duck_ignoring.setVisibility(View.VISIBLE);
                    other_players_not_ignore_sw.setVisibility(View.VISIBLE);
                    bad_wire.setVisibility(View.VISIBLE);
                    non_stop_change_orient.setVisibility(View.VISIBLE);
                    zero_back_in_time_change_orient.setVisibility(View.VISIBLE);
                    sound_arrowIcon.setText("▼");
                    MainActivity.sound_spoiler_open= true;
                } else {
                    duck_ignoring.setVisibility(View.GONE);
                    other_players_not_ignore_sw.setVisibility(View.GONE);
                    bad_wire.setVisibility(View.GONE);
                    non_stop_change_orient.setVisibility(View.GONE);
                    zero_back_in_time_change_orient.setVisibility(View.GONE);
                    sound_arrowIcon.setText("▶");
                    MainActivity.sound_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.search_spoiler_open) {
            showSystemFolders.setVisibility(View.GONE);
            show_one_album_in_folder.setVisibility(View.GONE);
            bold_font_for_long_names_sw.setVisibility(View.GONE);
            show_full_book_name_sw.setVisibility(View.GONE);
            always_show_favorites_sw.setVisibility(View.GONE);
            search_arrowIcon.setText("▶");
        }

        search_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) search_spoilerHeader.getParent()
                );

                if (showSystemFolders.getVisibility() == View.GONE) {
                    showSystemFolders.setVisibility(View.VISIBLE);
                    show_one_album_in_folder.setVisibility(View.VISIBLE);
                    bold_font_for_long_names_sw.setVisibility(View.VISIBLE);
                    show_full_book_name_sw.setVisibility(View.VISIBLE);
                    always_show_favorites_sw.setVisibility(View.VISIBLE);
                    search_arrowIcon.setText("▼");
                    MainActivity.search_spoiler_open= true;
                } else {
                    showSystemFolders.setVisibility(View.GONE);
                    show_one_album_in_folder.setVisibility(View.GONE);
                    bold_font_for_long_names_sw.setVisibility(View.GONE);
                    show_full_book_name_sw.setVisibility(View.GONE);
                    always_show_favorites_sw.setVisibility(View.GONE);
                    search_arrowIcon.setText("▶");
                    MainActivity.search_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.speed_spoiler_open) {
            set_speed_step.setVisibility(View.GONE);
            nosave_speed.setVisibility(View.GONE);
            pause_skipping_sw.setVisibility(View.GONE);
            speed_arrowIcon.setText("▶");
        }

        speed_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) speed_spoilerHeader.getParent()
                );

                if (set_speed_step.getVisibility() == View.GONE) {
                    set_speed_step.setVisibility(View.VISIBLE);
                    nosave_speed.setVisibility(View.VISIBLE);
                    pause_skipping_sw.setVisibility(View.VISIBLE);
                    speed_arrowIcon.setText("▼");
                    MainActivity.speed_spoiler_open= true;
                } else {
                    set_speed_step.setVisibility(View.GONE);
                    nosave_speed.setVisibility(View.GONE);
                    pause_skipping_sw.setVisibility(View.GONE);
                    speed_arrowIcon.setText("▶");
                    MainActivity.speed_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.time_spoiler_open) {
            time_speaking_sw.setVisibility(View.GONE);
            time_speaking_on_play_sw.setVisibility(View.GONE);
            time_speaking_on_pause_sw.setVisibility(View.GONE);
            time_arrowIcon.setText("▶");
        }

        time_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) time_spoilerHeader.getParent()
                );

                if (time_speaking_sw.getVisibility() == View.GONE) {
                    time_speaking_sw.setVisibility(View.VISIBLE);
                    time_speaking_on_play_sw.setVisibility(View.VISIBLE);
                    time_speaking_on_pause_sw.setVisibility(View.VISIBLE);
                    time_arrowIcon.setText("▼");
                    MainActivity.time_spoiler_open= true;
                } else {
                    time_speaking_sw.setVisibility(View.GONE);
                    time_speaking_on_play_sw.setVisibility(View.GONE);
                    time_speaking_on_pause_sw.setVisibility(View.GONE);
                    time_arrowIcon.setText("▶");
                    MainActivity.time_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.interface_spoiler_open) {
            set_always_begin.setVisibility(View.GONE);
            exit_only_in_menu_sw.setVisibility(View.GONE);
            backPressed_switch_to_background_sw.setVisibility(View.GONE);
            swap_fastMoving_and_goto_sw.setVisibility(View.GONE);
            swap_fastMoving_sw.setVisibility(View.GONE);
            goto_bookmarks_in_fucking_apple_style_sw.setVisibility(View.GONE);
            interface_arrowIcon.setText("▶");
        }

        interface_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) interface_spoilerHeader.getParent()
                );

                if (set_always_begin.getVisibility() == View.GONE) {
                    set_always_begin.setVisibility(View.VISIBLE);
                    exit_only_in_menu_sw.setVisibility(View.VISIBLE);
                    backPressed_switch_to_background_sw.setVisibility(View.VISIBLE);
                    swap_fastMoving_and_goto_sw.setVisibility(View.VISIBLE);
                    swap_fastMoving_sw.setVisibility(View.VISIBLE);
                    goto_bookmarks_in_fucking_apple_style_sw.setVisibility(View.VISIBLE);
                    interface_arrowIcon.setText("▼");
                    MainActivity.interface_spoiler_open= true;
                } else {
                    set_always_begin.setVisibility(View.GONE);
                    exit_only_in_menu_sw.setVisibility(View.GONE);
                    backPressed_switch_to_background_sw.setVisibility(View.GONE);
                    swap_fastMoving_and_goto_sw.setVisibility(View.GONE);
                    swap_fastMoving_sw.setVisibility(View.GONE);
                    goto_bookmarks_in_fucking_apple_style_sw.setVisibility(View.GONE);
                    interface_arrowIcon.setText("▶");
                    MainActivity.interface_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.themes_spoiler_open) {
            systemTheme.setVisibility(View.GONE);
            autoThemeSwith.setVisibility(View.GONE);
            lightTheme.setVisibility(View.GONE);
            darkTheme.setVisibility(View.GONE);
            themes_arrowIcon.setText("▶");
        }

        themes_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) themes_spoilerHeader.getParent()
                );

                if (systemTheme.getVisibility() == View.GONE) {
                    systemTheme.setVisibility(View.VISIBLE);
                    autoThemeSwith.setVisibility(View.VISIBLE);
                    lightTheme.setVisibility(View.VISIBLE);
                    darkTheme.setVisibility(View.VISIBLE);
                    themes_arrowIcon.setText("▼");
                    MainActivity.themes_spoiler_open= true;
                } else {
                    systemTheme.setVisibility(View.GONE);
                    autoThemeSwith.setVisibility(View.GONE);
                    lightTheme.setVisibility(View.GONE);
                    darkTheme.setVisibility(View.GONE);
                    themes_arrowIcon.setText("▶");
                    MainActivity.themes_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.buttonsCustomizing_spoiler_open) {
            transparency_buttons.setVisibility(View.GONE);
            transparency_value.setVisibility(View.GONE);
            buttonColorSelect.setVisibility(View.GONE);
            buttons_color_layout.setVisibility(View.GONE);
            buttons_size_text.setVisibility(View.GONE);
            button_size_selector.setVisibility(View.GONE);
            buttonSize_layout.setVisibility(View.GONE);
            buttonsCustomizing_arrowIcon.setText("▶");
        }

        buttonsCustomizing_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) buttonsCustomizing_spoilerHeader.getParent()
                );

                if (transparency_buttons.getVisibility() == View.GONE) {
                    transparency_buttons.setVisibility(View.VISIBLE);
                    transparency_value.setVisibility(View.VISIBLE);
                    buttonColorSelect.setVisibility(View.VISIBLE);
                    buttons_color_layout.setVisibility(View.VISIBLE);
                    buttons_size_text.setVisibility(View.VISIBLE);
                    button_size_selector.setVisibility(View.VISIBLE);
                    buttonSize_layout.setVisibility(View.VISIBLE);
                    buttonsCustomizing_arrowIcon.setText("▼");
                    MainActivity.buttonsCustomizing_spoiler_open= true;
                } else {
                    transparency_buttons.setVisibility(View.GONE);
                    transparency_value.setVisibility(View.GONE);
                    buttonColorSelect.setVisibility(View.GONE);
                    buttons_color_layout.setVisibility(View.GONE);
                    buttons_size_text.setVisibility(View.GONE);
                    button_size_selector.setVisibility(View.GONE);
                    buttonSize_layout.setVisibility(View.GONE);
                    buttonsCustomizing_arrowIcon.setText("▶");
                    MainActivity.buttonsCustomizing_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.rootFolder_spoiler_open) {
            use_audiobook_folder.setVisibility(View.GONE);
            audio_book_path.setVisibility(View.GONE);
            button_set_root_folder.setVisibility(View.GONE);
            rootFolder_arrowIcon.setText("▶");
        }

        rootFolder_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) rootFolder_spoilerHeader.getParent()
                );

                if (use_audiobook_folder.getVisibility() == View.GONE) {
                    use_audiobook_folder.setVisibility(View.VISIBLE);
                    audio_book_path.setVisibility(View.VISIBLE);
                    button_set_root_folder.setVisibility(View.VISIBLE);
                    rootFolder_arrowIcon.setText("▼");
                    MainActivity.rootFolder_spoiler_open= true;
                } else {
                    use_audiobook_folder.setVisibility(View.GONE);
                    audio_book_path.setVisibility(View.GONE);
                    button_set_root_folder.setVisibility(View.GONE);
                    rootFolder_arrowIcon.setText("▶");
                    MainActivity.rootFolder_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.fast_rewind_spoiler_open) {
            pause_back_layout.setVisibility(View.GONE);
            pause_back_value_selector.setVisibility(View.GONE);
            fast_moving_value_layout.setVisibility(View.GONE);
            fast_moving_value_selector.setVisibility(View.GONE);
            fast_rewind_arrowIcon.setText("▶");
        }

        fast_rewind_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) fast_rewind_spoilerHeader.getParent()
                );

                if (pause_back_layout.getVisibility() == View.GONE) {
                    pause_back_layout.setVisibility(View.VISIBLE);
                    pause_back_value_selector.setVisibility(View.VISIBLE);
                    fast_moving_value_layout.setVisibility(View.VISIBLE);
                    fast_moving_value_selector.setVisibility(View.VISIBLE);
                    fast_rewind_arrowIcon.setText("▼");
                    MainActivity.fast_rewind_spoiler_open= true;
                } else {
                    pause_back_layout.setVisibility(View.GONE);
                    pause_back_value_selector.setVisibility(View.GONE);
                    fast_moving_value_layout.setVisibility(View.GONE);
                    fast_moving_value_selector.setVisibility(View.GONE);
                    fast_rewind_arrowIcon.setText("▶");
                    MainActivity.fast_rewind_spoiler_open= false;
                }
            }
        });

        if (!MainActivity.folderVisibility_spoiler_open) {
            folder_ibvisible_view.setVisibility(View.GONE);
            button_folder_visibility_list.setVisibility(View.GONE);
            button_folder_visibility.setVisibility(View.GONE);
            fast_rewind_arrowIcon.setText("▶");
        }

        folderVisibility_spoilerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ваш код
                TransitionManager.beginDelayedTransition(
                        (ViewGroup) folderVisibility_spoilerHeader.getParent()
                );

                if (folder_ibvisible_view.getVisibility() == View.GONE) {
                    folder_ibvisible_view.setVisibility(View.VISIBLE);
                    button_folder_visibility_list.setVisibility(View.VISIBLE);
                    button_folder_visibility.setVisibility(View.VISIBLE);
                    folderVisibility_arrowIcon.setText("▼");
                    MainActivity.folderVisibility_spoiler_open= true;
                } else {
                    folder_ibvisible_view.setVisibility(View.GONE);
                    button_folder_visibility_list.setVisibility(View.GONE);
                    button_folder_visibility.setVisibility(View.GONE);
                    folderVisibility_arrowIcon.setText("▶");
                    MainActivity.folderVisibility_spoiler_open= false;
                }
            }
        });



        buttons_color1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(appContext, R.string.themes_color_text, Toast.LENGTH_LONG).show();
                if (night_mode== 2)
                    MainActivity.button_color= getResources().getColor(R.color.purple_200, null);
                else
                    MainActivity.button_color= getResources().getColor(R.color.purple_500, null);
                MainActivity.themes_change= true;
                if (MainActivity.playing == 1)
                    MainActivity.play_after_customizing= true;
                colorButtonsReDraw ();
            }
        });

        buttons_color2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (night_mode == 2) {
                    Toast.makeText(appContext, R.string.white_color_text, Toast.LENGTH_LONG).show();
                    MainActivity.button_color= getResources().getColor(R.color.white, null);
                }
                else {
                    Toast.makeText(appContext, R.string.black_color_text, Toast.LENGTH_LONG).show();
                    MainActivity.button_color= getResources().getColor(R.color.black, null);
                }
                MainActivity.themes_change= true;
                if (MainActivity.playing == 1)
                    MainActivity.play_after_customizing= true;
                colorButtonsReDraw ();
            }
        });

        buttons_color3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(appContext, R.string.primaryDark_color_text, Toast.LENGTH_LONG).show();
                MainActivity.button_color= getResources().getColor(R.color.colorPrimaryDark, null);
                MainActivity.themes_change= true;
                if (MainActivity.playing == 1)
                    MainActivity.play_after_customizing= true;
                colorButtonsReDraw ();
            }
        });

        buttons_color4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(appContext, R.string.colorAccent_color_text, Toast.LENGTH_LONG).show();
                MainActivity.button_color= getResources().getColor(R.color.colorAccent, null);
                MainActivity.themes_change= true;
                if (MainActivity.playing == 1)
                    MainActivity.play_after_customizing= true;
                colorButtonsReDraw ();
            }
        });

        buttons_color5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (night_mode == 2) {
                    Toast.makeText(appContext, R.string.purple_color_text, Toast.LENGTH_LONG).show();
                    MainActivity.button_color= getResources().getColor(R.color.purple_501, null);
                }
                else {
                    Toast.makeText(appContext, R.string.teal700_color_text, Toast.LENGTH_LONG).show();
                    MainActivity.button_color= getResources().getColor(R.color.teal_700, null);
                }
                MainActivity.themes_change= true;
                if (MainActivity.playing == 1)
                    MainActivity.play_after_customizing= true;
                colorButtonsReDraw ();
            }
        });

        buttons_color6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(appContext, R.string.teal200_color_text, Toast.LENGTH_LONG).show();
                MainActivity.button_color= getResources().getColor(R.color.teal_200, null);
                MainActivity.themes_change= true;
                if (MainActivity.playing == 1)
                    MainActivity.play_after_customizing= true;
                colorButtonsReDraw ();
            }
        });

        button_set_root_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_set_root_folder.setEnabled(false);
                MainActivity.favorite_list_using= false;
                Intent intent = new Intent(getApplicationContext(), com.audiobook.pbp_service.SoundScreenFolder.class);
                startActivityForResult(intent, MainActivity.SELECT_ROOT_FOLDER_CODE);
            }
        });

        button_folder_visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), com.audiobook.pbp_service.FoldersVisibility.class);
                startActivityForResult(intent, MainActivity.FOLDERS_VISIBILITY_CODE);
            }
        });

        button_folder_visibility_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(appContext, R.string.wait_cover_load_text, Toast.LENGTH_LONG).show();
//                folder_ibvisible_view.setText(R.string.wait_cover_load_text);
                folders_nomedia_seek();
                folder_ibvisible_view.setMinLines(DirHidden.size());
                String for_view = getString(R.string.folder_invisibility_list) + "\n\n";
                for (int i= 0; i< DirHidden.size(); i++) {
                    for_view= for_view + "- " + DirHidden.get(i) + "\n";
                }
                folder_ibvisible_view.setText(for_view);
            }
        });

        if (!MainActivity.right_hand_interface       &&
            !MainActivity.left_hand_interface        &&
            !MainActivity.simetric_interface         &&
            !MainActivity.one_finger_left_interface  &&
            !MainActivity.one_finger_right_interface) {
            MainActivity.one_finger_right_interface = true;
        }
        if (MainActivity.right_hand_interface) {
            right_handed.setChecked(true);
            MainActivity.left_hand_interface= false;
            MainActivity.simetric_interface= false;
            MainActivity.one_finger_left_interface= false;
            MainActivity.one_finger_right_interface= false;
        }
        if (MainActivity.left_hand_interface) {
            left_handed.setChecked(true);
            MainActivity.right_hand_interface= false;
            MainActivity.simetric_interface= false;
            MainActivity.one_finger_left_interface= false;
            MainActivity.one_finger_right_interface= false;
        }
        if (MainActivity.simetric_interface) {
            simetric_buttons.setChecked(true);
            MainActivity.right_hand_interface= false;
            MainActivity.left_hand_interface= false;
            MainActivity.one_finger_left_interface= false;
            MainActivity.one_finger_right_interface= false;
        }
        if (MainActivity.one_finger_left_interface) {
            one_finger_left.setChecked(true);
            MainActivity.right_hand_interface= false;
            MainActivity.left_hand_interface= false;
            MainActivity.simetric_interface= false;
            MainActivity.one_finger_right_interface= false;
        }
        if (MainActivity.one_finger_right_interface) {
            one_finger_right.setChecked(true);
            MainActivity.right_hand_interface= false;
            MainActivity.left_hand_interface= false;
            MainActivity.simetric_interface= false;
            MainActivity.one_finger_left_interface= false;
        }

        keep_screen.setChecked(MainActivity.keep_screen_anytime);
        duck_ignoring.setChecked(MainActivity.duck_ignore);
        other_players_not_ignore_sw.setChecked(MainActivity.other_players_not_ignore);
        bad_wire.setChecked(MainActivity.bad_wire_correct);
        full_brightness.setChecked(MainActivity.full_brightness);
        keep_portrait_sw.setChecked(MainActivity.keep_portrait);
        show_artist.setChecked(MainActivity.show_artist);
        show_composer.setChecked(MainActivity.show_composer);
        showSystemFolders.setChecked(MainActivity.show_system_folders);
        show_one_album_in_folder.setChecked(MainActivity.showOneAlbumInFolder);
        bold_font_for_long_names_sw.setChecked(MainActivity.boldFontForLongNames);
        show_full_book_name_sw.setChecked(MainActivity.show_full_book_name);
        if (!MainActivity.themesAsSystem)
            systemTheme.setChecked(false);
        if (!MainActivity.themesAuto)
            autoThemeSwith.setChecked(false);
        if (!MainActivity.themesLight)
            lightTheme.setChecked(false);
        if (!MainActivity.themesDark)
            darkTheme.setChecked(false);
        if (MainActivity.themesAsSystem)
            systemTheme.setChecked(true);
        if (MainActivity.themesAuto)
            autoThemeSwith.setChecked(true);
        if (MainActivity.themesLight)
            lightTheme.setChecked(true);
        if (MainActivity.themesDark)
            darkTheme.setChecked(true);
        non_stop_change_orient.setChecked(MainActivity.non_stop_after_change_orientation);
        if (!MainActivity.non_stop_after_change_orientation) {
            zero_back_in_time_change_orient.setChecked(false);
            zero_back_in_time_change_orient.setEnabled(false);
            MainActivity.zero_back_in_time_after_change_orient_cfg = false;
        }
        else {
            zero_back_in_time_change_orient.setEnabled(true);
            zero_back_in_time_change_orient.setChecked(MainActivity.zero_back_in_time_after_change_orient_cfg);
        }
        set_speed_step.setChecked(MainActivity.speed_step_005);
        set_always_begin.setChecked(MainActivity.always_begin_part);
        use_audiobook_folder.setChecked(MainActivity.use_root_folder);
        time_speaking_sw.setChecked(MainActivity.time_speaking_cfg);
        if (time_speaking_sw.isChecked()) {
            time_speaking_on_play_sw.setChecked(MainActivity.time_speaking_play_cfg);
            time_speaking_on_pause_sw.setChecked(MainActivity.time_speaking_pause_cfg);
            if (!MainActivity.time_speaking_pause_cfg
            &&  !MainActivity.time_speaking_play_cfg) {
                time_speaking_on_pause_sw.setChecked(true);
                time_speaking_on_play_sw.setChecked(true);
                MainActivity.time_speaking_pause_cfg= true;
                MainActivity.time_speaking_play_cfg= true;
            }
        }
        nosave_speed.setChecked(MainActivity.nosave_speed_for_newbooks);
        always_show_favorites_sw.setChecked(MainActivity.always_show_favorites_cfg);
        exit_only_in_menu_sw.setChecked(MainActivity.exit_only_in_menu_cfg);
        backPressed_switch_to_background_sw.setChecked(MainActivity.backPressed_switch_background_cfg);
        swap_fastMoving_and_goto_sw.setChecked(MainActivity.swap_fastMoving_goto_cfg);
        if (swap_fastMoving_and_goto_sw.isChecked()) {
            swap_fastMoving_sw.setEnabled(true);
            swap_fastMoving_sw.setChecked(MainActivity.swap_fast_moving_cfg);
        }
        goto_bookmarks_in_fucking_apple_style_sw.setChecked(MainActivity.goto_bookmarks_in_fucking_apple_style_cfg);
        audio_book_path.setEnabled(MainActivity.use_root_folder);
        button_set_root_folder.setEnabled(MainActivity.use_root_folder);
        if (!MainActivity.use_root_folder)
            button_set_root_folder.setBackgroundColor(GRAY);
        else
            button_set_root_folder.setBackgroundColor(MainActivity.button_color);
        transparency_buttons.setProgress(MainActivity.buttonsTransparency- 1);
        transparency_value.setText(getString(R.string.transparency_text) + " " + (6 - MainActivity.buttonsTransparency));
        button_size_selector.setProgress(MainActivity.buttons_size);


        right_handed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.need_hand_change = true;
                    MainActivity.right_hand_interface = true;
                    MainActivity.left_hand_interface = false;
                    MainActivity.simetric_interface = false;
                    MainActivity.one_finger_right_interface = false;
                    MainActivity.one_finger_left_interface = false;
                    left_handed.setChecked(false);
                    simetric_buttons.setChecked(false);
                    one_finger_right.setChecked(false);
                    one_finger_left.setChecked(false);
                }
                else {
                    MainActivity.right_hand_interface = false;
                    right_handed.setChecked(false);
                    if (!right_handed.isChecked()
                            && !left_handed.isChecked()
                            && !simetric_buttons.isChecked()
                            && !one_finger_right.isChecked()
                            && !one_finger_left.isChecked()) {
                        MainActivity.need_hand_change = true;
                        MainActivity.right_hand_interface = false;
                        MainActivity.left_hand_interface = false;
                        MainActivity.simetric_interface = false;
                        MainActivity.one_finger_right_interface = true;
                        MainActivity.one_finger_left_interface = false;
                        one_finger_right.setChecked(true);
                    }
                }
            }
        });

        left_handed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.need_hand_change = true;
                    MainActivity.right_hand_interface = false;
                    MainActivity.left_hand_interface = true;
                    MainActivity.simetric_interface = false;
                    MainActivity.one_finger_right_interface = false;
                    MainActivity.one_finger_left_interface = false;
                    right_handed.setChecked(false);
                    simetric_buttons.setChecked(false);
                    one_finger_right.setChecked(false);
                    one_finger_left.setChecked(false);
                }
                else {
                    MainActivity.left_hand_interface = false;
                    left_handed.setChecked(false);
                    if (!right_handed.isChecked()
                            && !left_handed.isChecked()
                            && !simetric_buttons.isChecked()
                            && !one_finger_right.isChecked()
                            && !one_finger_left.isChecked()) {
                        MainActivity.need_hand_change = true;
                        MainActivity.right_hand_interface = false;
                        MainActivity.left_hand_interface = false;
                        MainActivity.simetric_interface = false;
                        MainActivity.one_finger_right_interface = true;
                        MainActivity.one_finger_left_interface = false;
                        one_finger_right.setChecked(true);
                    }
                }
            }
        });

        simetric_buttons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.need_hand_change = true;
                    MainActivity.right_hand_interface = false;
                    MainActivity.left_hand_interface = false;
                    MainActivity.simetric_interface = true;
                    MainActivity.one_finger_right_interface = false;
                    MainActivity.one_finger_left_interface = false;
                    right_handed.setChecked(false);
                    left_handed.setChecked(false);
                    one_finger_right.setChecked(false);
                    one_finger_left.setChecked(false);
                }
                else {
                    MainActivity.simetric_interface = false;
                    simetric_buttons.setChecked(false);
                    if (!right_handed.isChecked()
                            && !left_handed.isChecked()
                            && !simetric_buttons.isChecked()
                            && !one_finger_right.isChecked()
                            && !one_finger_left.isChecked()) {
                        MainActivity.need_hand_change = true;
                        MainActivity.right_hand_interface = false;
                        MainActivity.left_hand_interface = false;
                        MainActivity.simetric_interface = false;
                        MainActivity.one_finger_right_interface = true;
                        MainActivity.one_finger_left_interface = false;
                        one_finger_right.setChecked(true);
                    }
                }
            }
        });

        one_finger_right.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.need_hand_change = true;
                    MainActivity.right_hand_interface = false;
                    MainActivity.left_hand_interface = false;
                    MainActivity.simetric_interface = false;
                    MainActivity.one_finger_right_interface = true;
                    MainActivity.one_finger_left_interface = false;
                    right_handed.setChecked(false);
                    left_handed.setChecked(false);
                    simetric_buttons.setChecked(false);
                    one_finger_left.setChecked(false);
                }
                else {
                    MainActivity.one_finger_right_interface = false;
                    one_finger_right.setChecked(false);
                    if (!right_handed.isChecked()
                            && !left_handed.isChecked()
                            && !simetric_buttons.isChecked()
                            && !one_finger_right.isChecked()
                            && !one_finger_left.isChecked()) {
                        MainActivity.need_hand_change = true;
                        MainActivity.right_hand_interface = false;
                        MainActivity.left_hand_interface = false;
                        MainActivity.simetric_interface = false;
                        MainActivity.one_finger_right_interface = true;
                        MainActivity.one_finger_left_interface = false;
                        one_finger_right.setChecked(true);
                    }
                }
            }
        });

        one_finger_left.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.need_hand_change = true;
                    MainActivity.right_hand_interface = false;
                    MainActivity.left_hand_interface = false;
                    MainActivity.simetric_interface = false;
                    MainActivity.one_finger_right_interface = false;
                    MainActivity.one_finger_left_interface = true;
                    right_handed.setChecked(false);
                    left_handed.setChecked(false);
                    simetric_buttons.setChecked(false);
                    one_finger_right.setChecked(false);
                }
                else {
                    MainActivity.one_finger_left_interface = false;
                    one_finger_left.setChecked(false);
                    if (!right_handed.isChecked()
                            && !left_handed.isChecked()
                            && !simetric_buttons.isChecked()
                            && !one_finger_right.isChecked()
                            && !one_finger_left.isChecked()) {
                        MainActivity.need_hand_change = true;
                        MainActivity.right_hand_interface = false;
                        MainActivity.left_hand_interface = false;
                        MainActivity.simetric_interface = false;
                        MainActivity.one_finger_right_interface = true;
                        MainActivity.one_finger_left_interface = false;
                        one_finger_right.setChecked(true);
                    }
                }
            }
        });

        keep_screen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_screen_keeping_change = true;
                MainActivity.keep_screen_anytime = isChecked;
            }
        });

        duck_ignoring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_duck_ignore_change = true;
                MainActivity.duck_ignore = isChecked;
            }
        });

        other_players_not_ignore_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_other_players_not_ignore = true;
                MainActivity.other_players_not_ignore = isChecked;
            }
        });

        bad_wire.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_bad_wire_change = true;
                MainActivity.bad_wire_correct = isChecked;
            }
        });

        full_brightness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_full_brightness_change = true;
                MainActivity.full_brightness = isChecked;
            }
        });

        keep_portrait_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_keep_portrait = true;
                MainActivity.keep_portrait = isChecked;
            }
        });

        show_artist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_show_artist = true;
                MainActivity.show_artist = isChecked;
            }
        });

        show_composer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_show_composer = true;
                MainActivity.show_composer = isChecked;
            }
        });

        showSystemFolders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_show_system_folders = true;
                MainActivity.show_system_folders = isChecked;
            }
        });

        show_one_album_in_folder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_showOneAlbumInFolder = true;
                MainActivity.showOneAlbumInFolder = isChecked;
            }
        });

        bold_font_for_long_names_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_boldFontForLongNames = true;
                MainActivity.boldFontForLongNames = isChecked;
            }
        });

        show_full_book_name_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_boldFontForLongNames = true;
                MainActivity.show_full_book_name = isChecked;
            }
        });

        non_stop_change_orient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_non_stop= true;
                MainActivity.non_stop_after_change_orientation = isChecked;
                if (!isChecked) {
                    zero_back_in_time_change_orient.setChecked(false);
                    zero_back_in_time_change_orient.setEnabled(false);
                    MainActivity.zero_back_in_time_after_change_orient_cfg = false;
                }
                else {
                    zero_back_in_time_change_orient.setEnabled(true);
                    zero_back_in_time_change_orient.setChecked(MainActivity.zero_back_in_time_after_change_orient_cfg);
                }
            }
        });

        zero_back_in_time_change_orient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.zero_back_in_time_after_change_orient_cfg = isChecked;
            }
        });

        set_speed_step.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_speed_step_005= true;
                MainActivity.speed_step_005 = isChecked;
            }
        });

        set_always_begin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_always_begin_part= true;
                MainActivity.always_begin_part = isChecked;
            }
        });

        use_audiobook_folder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_use_root_folder= true;
                MainActivity.use_root_folder = isChecked;
                audio_book_path.setEnabled(isChecked);
                button_set_root_folder.setEnabled(isChecked);
                if (!isChecked) {
                    button_set_root_folder.setBackgroundColor(GRAY);
                }
                else {
                    button_set_root_folder.setBackgroundColor(MainActivity.button_color);
                    button_set_root_folder.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
                }
            }
        });

        time_speaking_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_time_speaking= true;
                MainActivity.time_speaking_cfg = isChecked;
                if (!isChecked) {
                    MainActivity.time_speaking_play_cfg= time_speaking_on_play_sw.isChecked();
                    MainActivity.time_speaking_pause_cfg= time_speaking_on_pause_sw.isChecked();
                    time_speaking_on_play_sw.setChecked(false);
                    time_speaking_on_play_sw.setEnabled(false);
                    time_speaking_on_pause_sw.setChecked(false);
                    time_speaking_on_pause_sw.setEnabled(false);
                }
                else {
                    time_speaking_on_play_sw.setEnabled(true);
                    time_speaking_on_pause_sw.setEnabled(true);
                    if (!MainActivity.time_speaking_pause_cfg
                     && !MainActivity.time_speaking_play_cfg) {
                        MainActivity.time_speaking_pause_cfg = true;
                        MainActivity.time_speaking_play_cfg = true;
                    }
                    time_speaking_on_play_sw.setChecked(MainActivity.time_speaking_play_cfg);
                    time_speaking_on_pause_sw.setChecked(MainActivity.time_speaking_pause_cfg);
                }
            }
        });

        time_speaking_on_play_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_time_speaking= true;
//                MainActivity.time_speaking_play_cfg = isChecked;
            }
        });

        time_speaking_on_pause_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_time_speaking= true;
            }
        });

        nosave_speed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_nosave_speed_for_newbooks= true;
                MainActivity.nosave_speed_for_newbooks = isChecked;
            }
        });

        always_show_favorites_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_always_show_favorites_cfg= true;
                MainActivity.always_show_favorites_cfg = isChecked;
            }
        });

        exit_only_in_menu_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_exit_only_in_menu_cfg= true;
                MainActivity.exit_only_in_menu_cfg = isChecked;
                if (isChecked) {
                    backPressed_switch_to_background_sw.setChecked(false);
                    MainActivity.backPressed_switch_background_cfg = false;
                }
                else {
                    backPressed_switch_to_background_sw.setEnabled(true);
                }
            }
        });

        backPressed_switch_to_background_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_backPressed_switch_background_cfg= true;
                MainActivity.backPressed_switch_background_cfg = isChecked;
            }
        });

        swap_fastMoving_and_goto_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_swap_fastMoving_goto= true;
                MainActivity.swap_fastMoving_goto_cfg = isChecked;
                if (isChecked) {
                    exit_only_in_menu_sw.setChecked(false);
                    MainActivity.exit_only_in_menu_cfg = false;
                    swap_fastMoving_sw.setEnabled(true);
                    swap_fastMoving_sw.setChecked(MainActivity.swap_fast_moving_cfg);
                }
                else {
                    exit_only_in_menu_sw.setEnabled(true);
                    swap_fastMoving_sw.setEnabled(false);
                    swap_fastMoving_sw.setChecked(false);
                    MainActivity.swap_fast_moving_cfg= false;
                    MainActivity.need_swap_fast_moving= true;
                }
            }
        });

        swap_fastMoving_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_swap_fast_moving= true;
                MainActivity.swap_fast_moving_cfg = isChecked;
            }
        });

        goto_bookmarks_in_fucking_apple_style_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.need_goto_bookmarks_in_fucking_apple_style_cfg= true;
                MainActivity.goto_bookmarks_in_fucking_apple_style_cfg = isChecked;
            }
        });

        systemTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.themes_change = true;
                if (isChecked) {
                    MainActivity.themesAsSystem = true;
                    MainActivity.themesAuto = false;
                    MainActivity.themesLight = false;
                    MainActivity.themesDark = false;
                    systemTheme.setChecked(true);
                    autoThemeSwith.setChecked(false);
                    lightTheme.setChecked(false);
                    darkTheme.setChecked(false);
                } else {
                    MainActivity.themesAsSystem = false;
                    systemTheme.setChecked(false);
                    if (!autoThemeSwith.isChecked()  &&
                        !lightTheme.isChecked()      &&
                        !darkTheme.isChecked()) {

                        MainActivity.themesAsSystem = false;
                        MainActivity.themesAuto = false;
                        MainActivity.themesLight = false;
                        MainActivity.themesDark = true;
                        systemTheme.setChecked(false);
                        autoThemeSwith.setChecked(false);
                        lightTheme.setChecked(false);
                        darkTheme.setChecked(true);
                    }
                }
            }
        });

        autoThemeSwith.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.themes_change = true;
                if (isChecked) {
                    MainActivity.themesAsSystem = false;
                    MainActivity.themesAuto = true;
                    MainActivity.themesLight = false;
                    MainActivity.themesDark = false;
                    systemTheme.setChecked(false);
                    autoThemeSwith.setChecked(true);
                    lightTheme.setChecked(false);
                    darkTheme.setChecked(false);
                } else {
                    MainActivity.themesAuto = false;
                    autoThemeSwith.setChecked(false);
                    if (!systemTheme.isChecked()  &&
                            !lightTheme.isChecked()      &&
                            !darkTheme.isChecked()) {

                        MainActivity.themesAsSystem = false;
                        MainActivity.themesAuto = false;
                        MainActivity.themesLight = false;
                        MainActivity.themesDark = true;
                        systemTheme.setChecked(false);
                        autoThemeSwith.setChecked(false);
                        lightTheme.setChecked(false);
                        darkTheme.setChecked(true);
                    }
                }
            }
        });

        lightTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.themes_change = true;
                if (isChecked) {
                    MainActivity.themesAsSystem = false;
                    MainActivity.themesAuto = false;
                    MainActivity.themesLight = true;
                    MainActivity.themesDark = false;
                    MainActivity.is_LightTheme_Manual_Set= true;
                    systemTheme.setChecked(false);
                    autoThemeSwith.setChecked(false);
                    lightTheme.setChecked(true);
                    darkTheme.setChecked(false);
                } else {
                    MainActivity.themesLight = false;
                    lightTheme.setChecked(false);
                    if (!systemTheme.isChecked()  &&
                            !autoThemeSwith.isChecked()      &&
                            !darkTheme.isChecked()) {

                        MainActivity.themesAsSystem = false;
                        MainActivity.themesAuto = false;
                        MainActivity.themesLight = false;
                        MainActivity.themesDark = true;
                        systemTheme.setChecked(false);
                        autoThemeSwith.setChecked(false);
                        lightTheme.setChecked(false);
                        darkTheme.setChecked(true);
                    }
                }
            }
        });

        darkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.themes_change = true;
                if (isChecked) {
                    MainActivity.themesAsSystem = false;
                    MainActivity.themesAuto = false;
                    MainActivity.themesLight = false;
                    MainActivity.themesDark = true;
                    systemTheme.setChecked(false);
                    autoThemeSwith.setChecked(false);
                    lightTheme.setChecked(false);
                    darkTheme.setChecked(true);
                } else {
                    MainActivity.themesDark = false;
                    darkTheme.setChecked(false);
                    if (!systemTheme.isChecked()  &&
                            !autoThemeSwith.isChecked()      &&
                            !lightTheme.isChecked()) {

                        MainActivity.themesAsSystem = false;
                        MainActivity.themesAuto = false;
                        MainActivity.themesLight = false;
                        MainActivity.themesDark = true;
                        systemTheme.setChecked(false);
                        autoThemeSwith.setChecked(false);
                        lightTheme.setChecked(false);
                        darkTheme.setChecked(true);
                    }
                }
            }
        });

        transparency_buttons.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.buttonsTransparency = progress + 1;
                colorButtonsReDraw();
                transparency_value.setText(getString(R.string.transparency_text) + " " + (6 - MainActivity.buttonsTransparency));
                MainActivity.need_transparency_change = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        button_size_selector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.buttons_size = progress;
                MainActivity.need_buttons_size_change = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        pause_back_value_selector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.back_in_time_cust = progress;
                pause_back_value_view.setText(" " + (MainActivity.back_in_time_cust+ 6) + " ");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MainActivity.need_back_in_time_cust = true;
            }
        });

        fast_moving_value_selector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.fast_moving_cust = progress+ 1;
                fast_moving_value_view.setText(""+ (MainActivity.fast_moving_cust * 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MainActivity.need_fast_moving_cust = true;
            }
        });


    }

    private void colorButtonsReDraw () {
        if (night_mode== 2)
            buttons_color1.setBackgroundColor(getResources().getColor(R.color.purple_200, null));
        else
            buttons_color1.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        if (MainActivity.button_color== getResources().getColor(R.color.purple_200, null)
                ||  MainActivity.button_color== getResources().getColor(R.color.purple_500, null)) {
            buttons_color1.setIconResource(R.drawable.ic_media_pause);
        }
        else {
            buttons_color1.setIconResource(R.drawable.ic_media_play);
        }

        if (night_mode == 2) {
            buttons_color2.setBackgroundColor(getResources().getColor(R.color.white, null));
        }
        else {
            buttons_color2.setBackgroundColor(getResources().getColor(R.color.black, null));
        }
        if (MainActivity.button_color== getResources().getColor(R.color.white, null)
                ||  MainActivity.button_color== getResources().getColor(R.color.black, null)) {
            buttons_color2.setIconResource(R.drawable.ic_media_pause);
        }
        else {
            buttons_color2.setIconResource(R.drawable.ic_media_play);
        }

        buttons_color3.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, null));
        if (MainActivity.button_color== getResources().getColor(R.color.colorPrimaryDark, null)) {
            buttons_color3.setIconResource(R.drawable.ic_media_pause);
        }
        else {
            buttons_color3.setIconResource(R.drawable.ic_media_play);
        }

        buttons_color4.setBackgroundColor(getResources().getColor(R.color.colorAccent, null));
        if (MainActivity.button_color== getResources().getColor(R.color.colorAccent, null)) {
            buttons_color4.setIconResource(R.drawable.ic_media_pause);
        }
        else {
            buttons_color4.setIconResource(R.drawable.ic_media_play);
        }

        if (night_mode == 2) {
            buttons_color5.setBackgroundColor(getResources().getColor(R.color.purple_500, null));
        }
        else {
            buttons_color5.setBackgroundColor(getResources().getColor(R.color.teal_700, null));
        }
        if (MainActivity.button_color== getResources().getColor(R.color.purple_501, null)
                ||  MainActivity.button_color== getResources().getColor(R.color.teal_700, null)) {
            buttons_color5.setIconResource(R.drawable.ic_media_pause);
        }
        else {
            buttons_color5.setIconResource(R.drawable.ic_media_play);
        }

        buttons_color6.setBackgroundColor(getResources().getColor(R.color.teal_200, null));
        if (MainActivity.button_color== getResources().getColor(R.color.teal_200, null)) {
            buttons_color6.setIconResource(R.drawable.ic_media_pause);
        }
        else {
            buttons_color6.setIconResource(R.drawable.ic_media_play);
        }
        buttons_color1.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        buttons_color2.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        buttons_color3.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        buttons_color4.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        buttons_color5.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));
        buttons_color6.setAlpha((float) ((float) MainActivity.buttonsTransparency/ 6.0));

    }

    public void folders_nomedia_seek () {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            List<StorageVolume> stores;
            ArrayList<String> ArrayDir = new ArrayList<String>();
            StorageManager storage = MainActivity.appContext.getSystemService(StorageManager.class);
            File[] anyFiles;
            stores = storage.getStorageVolumes();
            DirHidden.clear();

            for (int i = 0; i < stores.size(); i++) {
                if (stores.get(i).isRemovable()) {
                    ArrayDir.add("/storage/" + stores.get(i).getUuid());
                } else {
                    ArrayDir.add("/storage/emulated/0/");
                }
            }

            String  work_file_name;
            for (int i= 0; i< ArrayDir.size(); i++) {
                anyFiles = new File(ArrayDir.get(i)).listFiles();
                if (anyFiles== null)
                    continue;
                for (File aFile : anyFiles) {
                    work_file_name= aFile.toString();
                    if (work_file_name.contains("/Android/"))
                        continue;

                    file_dir_processing (aFile);
                }
            }
        }

    }

    public void file_dir_processing (File aFile) {

        if (aFile.toString().contains("/Android/"))
            return;
        if (aFile.toString().contains("/WhatsApp/"))
            return;
        if (aFile.toString().contains("/Telegram/"))
            return;
        if (aFile.toString().contains("/MIUI/"))
            return;
        if (aFile.toString().contains("/.cache"))
            return;
        if (aFile.toString().endsWith(".trashBin"))
            return;
        if (aFile.toString().endsWith(".thumbnails"))
            return;
        if (aFile.toString().endsWith(".picker_transcoded"))
            return;
        if (aFile.toString().endsWith(".share_protect"))
            return;
        if (aFile.toString().endsWith("_thumbnail"))
            return;

        if (!aFile.isDirectory()) {
            if (aFile.toString().endsWith(".nomedia")) {
                dirHidden_add(aFile.toString());
                return;
            }
        }
        else {
            File[] listFils = new File(aFile.toString()).listFiles();
            if (listFils== null)
                return;
            for (File oneFile : listFils) {
                if (!oneFile.isDirectory()) {
                    if (oneFile.toString().endsWith(".nomedia")) {
                        dirHidden_add(oneFile.toString());
//                        continue;
                        break;
                    }
                }
                else {
                    file_dir_processing (oneFile);
                }
            }
        }

    }

    private   void dirHidden_add (String fileName) {
        fileName= fileName.substring(0, fileName.length() - "/.nomedia".length ());
        DirHidden.add(fileName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void gotoEndScroll (View view) {
        mainScroll.post(new Runnable() {
            @Override
            public void run() {
                if (scrollPosition== BEGIN_SCROLL) {
                    mainScroll.smoothScrollTo(0, mainScroll.getChildAt(0).getHeight());
                    scrollPosition = END_SCROLL;
                    scrollButton.setImageResource(android.R.drawable.arrow_up_float);
                    return;
                }
                if (scrollPosition== END_SCROLL) {
                    mainScroll.smoothScrollTo(0, 0);
                    scrollPosition = BEGIN_SCROLL;
                    scrollButton.setImageResource(android.R.drawable.arrow_down_float);
                    return;
                }
            }
        });
    }

    public void readEnd (View view) {
        if (!MainActivity.themesAsSystem  &&
                !MainActivity.themesAuto  &&
                !MainActivity.themesLight  &&
                !MainActivity.themesDark) {
            MainActivity.themesDark = true;
            darkTheme.setChecked(true);
        }

        if (time_speaking_sw.isChecked()) {
            MainActivity.time_speaking_play_cfg= time_speaking_on_play_sw.isChecked();
            MainActivity.time_speaking_pause_cfg= time_speaking_on_pause_sw.isChecked();
            if (!MainActivity.time_speaking_play_cfg
              && !MainActivity.time_speaking_pause_cfg) {
                MainActivity.time_speaking_play_cfg= true;
                MainActivity.time_speaking_pause_cfg= true;
                time_speaking_on_play_sw.setChecked(true);
                time_speaking_on_pause_sw.setChecked(true);
            }
        }

        MainActivity.hand_changed++;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainActivity.SELECT_ROOT_FOLDER_CODE) {
            button_set_root_folder.setEnabled(true);
            if (resultCode == Activity.RESULT_OK) {
                MainActivity.root_folder_path = data.getStringExtra(SoundScreenFolder.SELECTED_FOLDER_NAME);
                if (MainActivity.root_folder_path.length()> 0) {
                    if (MainActivity.root_folder_path.endsWith("/"))
                        MainActivity.root_folder_path = MainActivity.root_folder_path.substring(0, MainActivity.root_folder_path.length()- 1);
                    audio_book_path.setText(MainActivity.root_folder_path);
                }
            }
        }
    }

}
