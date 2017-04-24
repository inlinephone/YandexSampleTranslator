package com.example.alexander.yasampltranslator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends AppCompatActivity
{
    private TranslateHistoryDB translateHistoryDB = new TranslateHistoryDB(this);
    private Menu removeHistoryMenu; // Меню с пунктом для удаления истории, нужно чтобы добавлять / удалять этот пункт из меню программно
    private boolean isHistoryTabSelected; // После нажатия на пункт меню "удалить"
    private String textFromHistory = null; // Используется для передачи данных на вкладку перевода с вкладок "история" и "избранное"

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        int prevSelection = R.id.translate_item;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.translate_item:
                    if(prevSelection != R.id.translate_item)
                    {
                        removeHistoryMenu.removeItem(R.id.remove_item);
                        setTranslateTab();
                        prevSelection = R.id.translate_item;
                    }
                    return true;
                case R.id.favourites_item:
                    if(prevSelection != R.id.favourites_item)
                    {
                        setFavouritesTab();
                        prevSelection = R.id.favourites_item;
                    }
                    return true;
                case R.id.about_item:
                    if(prevSelection != R.id.about_item)
                    {
                        removeHistoryMenu.removeItem(R.id.remove_item);
                        setAboutTab();
                        prevSelection = R.id.about_item;
                    }
                    return true;
            }

            return false;
        }
    };


    private void setTranslateTab()
    {
        final EditText inputView                   = new ActionDoneEditText(this);                   // Поле ввода текста
        final LinearLayout contentLayout           = (LinearLayout) findViewById(R.id.content);      // Слой с основным содержимым
        final LinearLayout languageSelectionLayout = new LinearLayout(this);                         // Горизонтальный слой со спиннерами выбора языка и кнопкой обмена
        final Spinner sourceLangSpinner            = new Spinner(this);                              // Спиннер выбора языка с которого будет перевод
        final Spinner destLangSpinner              = new Spinner(this);                              // Спиннер выбора языка на который будет перевод
        final ImageButton swapLangBtn              = new ImageButton(this);                          // Кнопка обмена значений спиннеров
        final TextView outView                     = new TextView(this);                             // Поле вывода переведенного текста

        // Вытаскиваем языки из ресурсов
        final ArrayAdapter<CharSequence> adapter   = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);

        contentLayout.removeAllViews();

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sourceLangSpinner.setAdapter(adapter);
        destLangSpinner.setAdapter(adapter);
        swapLangBtn.setImageResource(R.drawable.swap_icon);

        sourceLangSpinner.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 2));
        destLangSpinner.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 2));

        swapLangBtn.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 3));
        swapLangBtn.setAdjustViewBounds(true);
        swapLangBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        swapLangBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));

        inputView.setGravity(Gravity.TOP | Gravity.LEFT);

        languageSelectionLayout.addView(sourceLangSpinner);
        languageSelectionLayout.addView(swapLangBtn);
        languageSelectionLayout.addView(destLangSpinner);

        int langChooseHeight = (int) getResources().getDimension(R.dimen.languageChooseLayoutHeight);
        languageSelectionLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT,
                                                                              langChooseHeight));
        outView.setTextSize(20);
        outView.setTextIsSelectable(true);
        outView.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        outView.setTextColor(Color.WHITE);
        outView.setMinHeight((int) getResources().getDimension(R.dimen.inputViewHeight));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 0);

        contentLayout.addView(languageSelectionLayout);
        contentLayout.addView(inputView, new ViewGroup.LayoutParams(MATCH_PARENT, (int) getResources().getDimension(R.dimen.inputViewHeight)));
        contentLayout.addView(outView, params);

        inputView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        inputView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE) // По нажатие "Done" на клавиатуре текст будет переводиться
                {
                    ExperimentalTranslate experimentalTranslate = new ExperimentalTranslate();

                    ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(MainActivity.this, // Вытаскиваем из ресурсов языковые обозначения
                            R.array.lang_description, android.R.layout.simple_spinner_item);                 // типа "ru, en, ..."

                    String fromLang = mAdapter.getItem(sourceLangSpinner.getSelectedItemPosition()).toString(); // Поскольку индексы языков в спиннере и
                    String toLang   = mAdapter.getItem(destLangSpinner.getSelectedItemPosition()).toString();   // языковых значений в mAdapter оовпадают
                                                                                                                // то можно легко узнать обозначение языка по
                                                                                                                // его полному названию
                    if(isConnected())
                    {
                        outView.setText("Переводится...");

                        experimentalTranslate.setOutView(outView);
                        experimentalTranslate.setDB(translateHistoryDB);
                        experimentalTranslate.execute(inputView.getText().toString(), fromLang + "-" + toLang);
                    }

                    else
                        outView.setText("Проверьте подключение к интернету");

                    hideKeyboard();

                    return true;
                }

                return false;
            }
        });

        swapLangBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int sourcePos = sourceLangSpinner.getSelectedItemPosition();
                int destPos   = destLangSpinner.getSelectedItemPosition();

                sourceLangSpinner.setSelection(destPos);
                destLangSpinner.setSelection(sourcePos);
            }
        });

        if(textFromHistory != null) // textFromHistory служит для передачи сюда текста, потом он ищется в базе, откуда извлекается направление
        {                           // перевода и переведенный текст и устанавливаются в соответсвующие поля
            inputView.setText(textFromHistory);

            SQLiteDatabase database = translateHistoryDB.getReadableDatabase();

            Cursor cursor = null;

            try
            {
                cursor = database.rawQuery("select * from " + TranslateHistoryDB.TABLE_HISTORY + " where "
                        + TranslateHistoryDB.KEY_ENTERED_TEXT + " like ?", new String[] { textFromHistory });
            }

            catch (Exception exception)
            {
                Toast.makeText(MainActivity.this, "Возникла ошибка при поиске в базе данных", Toast.LENGTH_LONG).show();
            }

            if(cursor == null)
            {
                Toast.makeText(MainActivity.this, "Возникла ошибка при поиске в базе данных", Toast.LENGTH_LONG).show();
                textFromHistory = null;
                return;
            }

            final int translateDirectionIndex = cursor.getColumnIndex(TranslateHistoryDB.KEY_TRANSLATE_DIRECTION);
            final int transltedTextIndex      = cursor.getColumnIndex(TranslateHistoryDB.KEY_TRANSLATED_TEXT);

            boolean check = cursor.moveToFirst();

            final String trDirection = cursor.getString(translateDirectionIndex);

            outView.setText(cursor.getString(transltedTextIndex));

            database.close();

            final String fromLang    = trDirection.substring(0, trDirection.indexOf('-'));
            final String toLang      = trDirection.substring(trDirection.indexOf('-') + 1);

            ArrayAdapter<CharSequence> langDescArray = ArrayAdapter.createFromResource(MainActivity.this,
                    R.array.lang_description, android.R.layout.simple_spinner_item);

            int fromLangIndex = langDescArray.getPosition(fromLang);
            int toLangIndex   = langDescArray.getPosition(toLang);

            sourceLangSpinner.setSelection(fromLangIndex);
            destLangSpinner.setSelection(toLangIndex);

            textFromHistory = null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) // скрывает клавиатуру при нажатии на любые элементы
    {
        if (ev.getAction() ==  MotionEvent.ACTION_DOWN)
            hideKeyboard();
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() // скрыть клавиатуру
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void setFavouritesTab() // Устанавлвает вкладку нижней навигации "избранное"
    {
        final LinearLayout contentLayout  = (LinearLayout) findViewById(R.id.content);               // Слой с основным содержимым
        final TabLayout tabLayout         = new TabLayout(this);                                     // Слой с вкладками "история" и "избранное"
        final TabLayout.Tab historyTab    = tabLayout.newTab();                                      // Вкладка "история"
        final TabLayout.Tab favouritesTab = tabLayout.newTab();                                      // Вкладка "избранное"

        contentLayout.removeAllViews();

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.remove_menu, removeHistoryMenu); // добавляем пункт меню "удалить"

        historyTab.setText("История");
        favouritesTab.setText("Избранное");

        tabLayout.addTab(historyTab);
        tabLayout.addTab(favouritesTab);

        contentLayout.addView(tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                if(tab.getText().equals("История"))
                {
                    setHistoryTab();
                    isHistoryTabSelected = true;
                }

                else
                {
                    setFavTab();
                    isHistoryTabSelected = false;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        isHistoryTabSelected = true;
        setHistoryTab();
    }

    private void setFavTab()
    {
        SQLiteDatabase sqLiteDatabase = translateHistoryDB.getReadableDatabase();
        final Cursor cursor = sqLiteDatabase.query(TranslateHistoryDB.TABLE_HISTORY, null, null, null, null, null, null);
        final LinearLayout contentLayout  = (LinearLayout) findViewById(R.id.content);

        int viewsNum = contentLayout.getChildCount();

        if(viewsNum > 1)
            contentLayout.removeViews(1, viewsNum - 1);

        boolean isFirstItem = true;

        if(cursor.moveToFirst())
        {
            final int textIndex           = cursor.getColumnIndex(TranslateHistoryDB.KEY_ENTERED_TEXT);
            final int translatedTextIndex = cursor.getColumnIndex(TranslateHistoryDB.KEY_TRANSLATED_TEXT);
            final int favSignIndex        = cursor.getColumnIndex(TranslateHistoryDB.KEY_FAVOURITE_TEXT);

            TextView trView;
            LinearLayout.LayoutParams firstElemParams;
            LinearLayout.LayoutParams otherElemParams;
            LinearLayout.LayoutParams brLineParams;
            View separateLine;

            firstElemParams = new LinearLayout.LayoutParams(MATCH_PARENT, (int) getResources().getDimension(R.dimen.historyElemHeight), 1);
            otherElemParams = new LinearLayout.LayoutParams(MATCH_PARENT, (int) getResources().getDimension(R.dimen.historyElemHeight), 1);
            brLineParams    = new LinearLayout.LayoutParams(MATCH_PARENT, (int) getResources().getDimension(R.dimen.brLineHeight));

            firstElemParams.setMargins(5, 20, 5, 0);
            otherElemParams.setMargins(5, 0, 5, 0);
            brLineParams.setMargins(5, 0, 5, 0);

            do
            {
                if(!cursor.getString(favSignIndex).equals("fav"))
                    continue;

                trView = new TextView(this);

                trView.setText(cursor.getString(textIndex) + System.getProperty("line.separator") + cursor.getString(translatedTextIndex));
                trView.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                trView.setTextColor(Color.WHITE);
                trView.setTextSize((int) getResources().getDimension(R.dimen.historyFontSize));
                trView.setMovementMethod(new ScrollingMovementMethod()); // Добавляем вертикальную прокрутку

                trView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        View translateTextTab  = findViewById(R.id.translate_item);

                        String allText              = ((TextView) v).getText().toString();
                        String text                 = allText.substring(0, allText.indexOf(System.getProperty("line.separator")));

                        textFromHistory = text;

                        translateTextTab.performClick();
                    }
                });

                separateLine = new View(this);
                separateLine.setBackgroundColor(Color.WHITE);

                if(isFirstItem != true)
                    trView.setLayoutParams(otherElemParams);

                else
                {
                    trView.setLayoutParams(firstElemParams);
                    isFirstItem = false;
                }

                contentLayout.addView(trView);
                contentLayout.addView(separateLine, brLineParams);

            }while (cursor.moveToNext());
        }

        if(isFirstItem == true)
        {
            TextView tv = new TextView(this);
            ScrollView mainScrollView = (ScrollView) findViewById(R.id.scroll_view);

            tv.setText("Нет избранных переведенных слов");

            tv.setGravity(Gravity.CENTER);
            contentLayout.addView(tv, new ViewGroup.LayoutParams(MATCH_PARENT, mainScrollView.getHeight()));
        }

        sqLiteDatabase.close();
        cursor.close();
    }

    public boolean isConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        removeHistoryMenu = menu;
        return true;
    }

    public void removeDBInfo(MenuItem menuItem)
    {
        translateHistoryDB.removeDB();

        if(isHistoryTabSelected)
            setHistoryTab();
        else
            setFavTab();
    }

    private void setHistoryTab()
    {
        SQLiteDatabase sqLiteDatabase = translateHistoryDB.getReadableDatabase();
        final Cursor cursor = sqLiteDatabase.query(TranslateHistoryDB.TABLE_HISTORY, null, null, null, null, null, null);
        final LinearLayout contentLayout  = (LinearLayout) findViewById(R.id.content);

        int viewsNum = contentLayout.getChildCount();

        if(viewsNum > 1)
            contentLayout.removeViews(1, viewsNum - 1);

        if(cursor.moveToFirst())
        {
            final int textIndex           = cursor.getColumnIndex(TranslateHistoryDB.KEY_ENTERED_TEXT);
            final int translatedTextIndex = cursor.getColumnIndex(TranslateHistoryDB.KEY_TRANSLATED_TEXT);
            final int favSignIndex        = cursor.getColumnIndex(TranslateHistoryDB.KEY_FAVOURITE_TEXT);
            final int idIndex             = cursor.getColumnIndex(TranslateHistoryDB.KEY_ID);

            TextView translationView;
            LinearLayout.LayoutParams firstElemParams;
            LinearLayout.LayoutParams otherElemParams;
            LinearLayout.LayoutParams brLineParams;
            View separateLine;
            LinearLayout translationNoteLO;
            FavImageButton favStar;

            firstElemParams = new LinearLayout.LayoutParams(MATCH_PARENT, (int) getResources().getDimension(R.dimen.historyElemHeight), 1);
            otherElemParams = new LinearLayout.LayoutParams(MATCH_PARENT, (int) getResources().getDimension(R.dimen.historyElemHeight), 1);
            brLineParams    = new LinearLayout.LayoutParams(MATCH_PARENT, (int) getResources().getDimension(R.dimen.brLineHeight));

            firstElemParams.setMargins(5, 20, 5, 0);
            otherElemParams.setMargins(5, 0, 5, 0);
            brLineParams.setMargins(5, 0, 5, 0);

            boolean isFirstItem = true; // Используется для отступа первого элемента от переключателей вкладок "история" и "избранное"

            do {
                translationView = new TextView(this);

                translationView.setText(cursor.getString(textIndex) + System.getProperty("line.separator") + cursor.getString(translatedTextIndex));
                translationView.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                translationView.setTextColor(Color.WHITE);
                translationView.setTextSize((int) getResources().getDimension(R.dimen.historyFontSize));
                translationView.setMovementMethod(new ScrollingMovementMethod()); // Добавляем вертикальную прокрутку

                translationView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        View translateTextTab  = findViewById(R.id.translate_item);

                        String allText  = ((TextView) v).getText().toString();
                        textFromHistory = allText.substring(0, allText.indexOf(System.getProperty("line.separator")));

                        translateTextTab.performClick();
                    }
                });

                translationNoteLO = new LinearLayout(this);

                separateLine = new View(this);
                separateLine.setBackgroundColor(Color.WHITE);

                if(isFirstItem != true)
                    translationNoteLO.setLayoutParams(otherElemParams);

                else
                {
                    translationNoteLO.setLayoutParams(firstElemParams);
                    isFirstItem = false;
                }

                favStar = new FavImageButton(this);

                if(cursor.getString(favSignIndex).equals("fav"))
                {
                    favStar.setImageResource(R.drawable.star_fav_icon);
                    favStar.setFavourite(true);
                }

                else
                {
                    favStar.setImageResource(R.drawable.star_default_icon);
                    favStar.setFavourite(false);
                }

                favStar.setId(cursor.getString(idIndex));

                favStar.setAdjustViewBounds(true);
                favStar.setScaleType(ImageView.ScaleType.FIT_CENTER);
                favStar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                favStar.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        FavImageButton btn = (FavImageButton) v;

                        SQLiteDatabase database = translateHistoryDB.getWritableDatabase();

                        ContentValues cv = new ContentValues();

                        if(btn.isFavourite() == false)
                        {
                            btn.setImageResource(R.drawable.star_fav_icon);
                            btn.setFavourite(true);

                            cv.put(TranslateHistoryDB.KEY_FAVOURITE_TEXT, "fav");
                            database.update(TranslateHistoryDB.TABLE_HISTORY, cv, TranslateHistoryDB.KEY_ID + " = " + btn.getIdK(), null);
                        }

                        else
                        {
                            btn.setImageResource(R.drawable.star_default_icon);
                            btn.setFavourite(false);

                            cv.put(TranslateHistoryDB.KEY_FAVOURITE_TEXT, "nfav");
                            database.update(TranslateHistoryDB.TABLE_HISTORY, cv, TranslateHistoryDB.KEY_ID + " = ?", new String[] { btn.getIdK() });
                        }

                        database.close();
                    }
                });

                translationNoteLO.addView(translationView, otherElemParams);
                translationNoteLO.addView(favStar, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 9));
                translationNoteLO.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));

                contentLayout.addView(translationNoteLO);
                contentLayout.addView(separateLine, brLineParams);

            }while (cursor.moveToNext());
        }

        else
        {
            TextView tv = new TextView(this);
            ScrollView mainScrollView = (ScrollView) findViewById(R.id.scroll_view);

            tv.setText("Нет переведенных слов");

            tv.setGravity(Gravity.CENTER);
            contentLayout.addView(tv, new ViewGroup.LayoutParams(MATCH_PARENT, mainScrollView.getHeight()));
        }

        sqLiteDatabase.close();
        cursor.close();
    }

    private void setAboutTab()
    {
        final LinearLayout contentLayout  = (LinearLayout) findViewById(R.id.content);

        contentLayout.removeAllViews();

        TextView tv = new TextView(this);
        ScrollView mainScrollView = (ScrollView) findViewById(R.id.scroll_view);

        tv.setText("Переведено сервисом «Яндекс.Переводчик»" + System.getProperty("line.separator") + "http://translate.yandex.ru/");

        tv.setGravity(Gravity.CENTER);
        contentLayout.addView(tv, new ViewGroup.LayoutParams(MATCH_PARENT, mainScrollView.getHeight()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        setTranslateTab();
    }
}
