package com.saraswati.institute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/** Renders user, system, and typing-indicator chat messages. */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override public int getItemViewType(int pos) { return messages.get(pos).type; }
    @Override public int getItemCount() { return messages.size(); }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ChatMessage.TYPE_USER:
                return new UserVH(inf.inflate(R.layout.item_chat_user, parent, false));
            case ChatMessage.TYPE_SYSTEM:
                return new SystemVH(inf.inflate(R.layout.item_chat_system, parent, false));
            default: // TYPE_TYPING
                return new TypingVH(inf.inflate(R.layout.item_chat_typing, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = messages.get(position);
        if (holder instanceof UserVH)   ((UserVH)   holder).bind(m);
        if (holder instanceof SystemVH) ((SystemVH) holder).bind(m);
        // TypingVH has no dynamic content
    }

    // ── View holders ──────────────────────────────────────────────────────────

    static class UserVH extends RecyclerView.ViewHolder {
        final TextView text, time;
        UserVH(View v) { super(v); text = v.findViewById(R.id.msg_text); time = v.findViewById(R.id.msg_time); }
        void bind(ChatMessage m) { text.setText(m.text); time.setText(m.time); }
    }

    static class SystemVH extends RecyclerView.ViewHolder {
        final TextView text, time;
        SystemVH(View v) { super(v); text = v.findViewById(R.id.msg_text); time = v.findViewById(R.id.msg_time); }
        void bind(ChatMessage m) { text.setText(m.text); time.setText(m.time); }
    }

    static class TypingVH extends RecyclerView.ViewHolder {
        TypingVH(View v) { super(v); }
    }
}
