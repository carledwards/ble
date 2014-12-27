package com.bengalbot.lightcast.drawer;


import com.bengalbot.lightcast.R;

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
