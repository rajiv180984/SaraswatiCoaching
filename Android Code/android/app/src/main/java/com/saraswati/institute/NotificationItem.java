package com.saraswati.institute;

public class NotificationItem {

    public static final int TYPE_ANNOUNCEMENT = 0;
    public static final int TYPE_TEST         = 1;
    public static final int TYPE_FEE          = 2;
    public static final int TYPE_SCHEDULE     = 3;
    public static final int TYPE_RESULT       = 4;

    public final int    type;
    public final String title;
    public final String description;
    public final String time;
    public boolean      isRead;

    public NotificationItem(int type, String title, String description, String time, boolean isRead) {
        this.type        = type;
        this.title       = title;
        this.description = description;
        this.time        = time;
        this.isRead      = isRead;
    }
}
