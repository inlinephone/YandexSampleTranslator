package com.example.alexander.yasampltranslator;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Данный класс отвечает за перевод текста и его запись в базу данных.
 * Также вывод в TextView осуществляет этот класс, чтобы основной поток не
 * дожидался перевода.
 */

public class ExperimentalTranslate extends AsyncTask<String, Void, String>
{
    private TextView outView;
    private TranslateHistoryDB translateHistoryDB;

    public void setOutView(TextView outView)
    {
        this.outView = outView;
    }
    public void setDB(TranslateHistoryDB db)
    {
        translateHistoryDB = db;
    }

    @Override
    protected String doInBackground(String... params)
    {
        String text               = params[0];
        String translateDirection = params[1];
        String translatedText;

        try
        {
            translatedText = executePost("https://translate.yandex.net/api/v1.5/tr/translate?",
                                         "key=trnsl.1.1.20170418T112657Z.5b7637194e827beb.c9ccb6aac27977ffe84345436632f2ef2aba0034&text=" +
                                         URLEncoder.encode(text, "utf-8") + "&lang="+ translateDirection +"&format=plain");

            Pattern pattern = Pattern.compile("<text>"); // Ищем открывающий тег <text>, т.к. после него будет переведённый текст
            Matcher matcher = pattern.matcher(translatedText);

            if(!matcher.find())
                translatedText = "";
            else
            {
                int pos = matcher.end();
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = pos; translatedText.charAt(i) != '<'; ++i) // Извлекаем все символы от открывающего тега <text>, до первыой угловой
                    stringBuilder.append(translatedText.charAt(i));     // скобки закрывающего тега </text>

                translatedText = stringBuilder.toString();
            }
        }

        catch (UnsupportedEncodingException exception)
        {
            exception.printStackTrace();
            translatedText = "";
        }

        if(!translatedText.isEmpty())
            writeTranslationToDB(text, translatedText, translateDirection);

        return translatedText;
    }

    private void writeTranslationToDB(String text, String translatedText, String translateDirection)
    {
        SQLiteDatabase sqLiteDatabase = translateHistoryDB.getWritableDatabase();
        ContentValues contentValues   = new ContentValues();

        // Если в переведенный текст пользователь вставил разделители (переход на новую строку)
        // то они будут заменены на пробелы

        while(true)
        {
            int currSeparatorIndex = text.indexOf(System.getProperty("line.separator"));

            if(currSeparatorIndex == -1)
                break;
            else
                text = text.replace(System.getProperty("line.separator"), " ");
        }

        contentValues.put(TranslateHistoryDB.KEY_ENTERED_TEXT, text);
        contentValues.put(TranslateHistoryDB.KEY_TRANSLATED_TEXT, translatedText);
        contentValues.put(TranslateHistoryDB.KEY_TRANSLATE_DIRECTION, translateDirection);
        contentValues.put(TranslateHistoryDB.KEY_FAVOURITE_TEXT, "nfav");

        sqLiteDatabase.insert(TranslateHistoryDB.TABLE_HISTORY, null, contentValues);
        sqLiteDatabase.close();
    }

    @Override
    protected void onPostExecute(String translatedText)
    {
        super.onPostExecute(translatedText);

        outView.setText(translatedText);
    }

    private static String executePost(String targetURL, String urlParameters)
    {
        HttpURLConnection connection = null;

        try
        {
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            if(connection.getResponseCode() == 200)
            {
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }

                rd.close();
                return response.toString();
            }

            else
                return String.format("Возникла ошибка при обращении к серверу: %d", connection.getResponseCode());
        }

        catch (Exception e)
        {
            e.printStackTrace();
            return e.getMessage();
        }

        finally
        {
            if (connection != null)
                connection.disconnect();
        }
    }
}
