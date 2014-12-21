package com.bengalbot.android.drawer;


import com.bengalbot.android.R;

public enum DrawerListItem {

    CONNECT(R.string.connect),
    CONFIGURE_COLOR(R.string.configure_color),
    CONFIGURE_FLASH_MODE(R.string.configure_flash_mode);

    private int resId;

    DrawerListItem(int resId) {
        this.resId = resId;
    }

    public int getResId() {
        return resId;
    }
}
