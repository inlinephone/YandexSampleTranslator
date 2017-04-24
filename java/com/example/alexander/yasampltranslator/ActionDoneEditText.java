package com.example.alexander.yasampltranslator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/*
*   В отличие от обычного EditText данный класс позволяет устанавливать состояние
*   IME_ACTION_DONE и для многострочных полей ввода. (По умолчанию для многострочных полей
*   принудительно устанавливается флаг IME_FLAG_NO_ENTER_ACTION.
* */

public class ActionDoneEditText extends EditText
{
    public ActionDoneEditText(Context context)
    {
        super(context);
    }

    public ActionDoneEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ActionDoneEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        InputConnection conn = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        return conn;
    }
}
