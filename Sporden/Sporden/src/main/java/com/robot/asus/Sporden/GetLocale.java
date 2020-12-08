package com.robot.asus.Sporden;

import java.util.Locale;

public class GetLocale {
    public static String getLocale() {
        String language = Locale.getDefault().getLanguage();
        return language;
    }
}
