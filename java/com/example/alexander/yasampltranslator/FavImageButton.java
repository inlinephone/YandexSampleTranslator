package com.example.alexander.yasampltranslator;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Данный класс ничем не отличается от обычной кнопки за исключением того, что он хранит
 * в себе id переведенного текста, с которым связан и выбран ли связанный с кнопкой перевод
 * в избранное.
 */

public final class FavImageButton extends ImageButton
{
    private boolean isFavourite;
    private String id;

    public FavImageButton(Context context)
    {
        super(context);
    }

    public FavImageButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FavImageButton(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public FavImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getIdK() // getId уже существует -> добавлено K
    {
        return id;
    }

    public boolean isFavourite()
    {
        return isFavourite;
    }

    public void setFavourite(boolean favourite)
    {
        isFavourite = favourite;
    }
}
