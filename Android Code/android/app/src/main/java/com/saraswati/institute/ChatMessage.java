package com.saraswati.institute;

/** One message in the Live Query chat. */
public class ChatMessage {

    public static final int TYPE_USER   = 0;
    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_TYPING = 2;   // animated "AI is typing…" indicator

    public final int    type;
    public final String text;
    public final String time;   // empty for TYPE_TYPING

    public ChatMessage(int type, String text, String time) {
        this.type = type;
        this.text = text;
        this.time = time;
    }
}
