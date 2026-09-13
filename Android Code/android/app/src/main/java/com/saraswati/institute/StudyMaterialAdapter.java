package com.saraswati.institute;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * Two-view-type adapter for the Study Material tab.
 *   TYPE_HEADER → subject section label (reuses item_test_section_header layout)
 *   TYPE_ITEM   → study material card
 */
public class StudyMaterialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMaterialAction {
        void onDownload(StudyMaterial item);
        void onWatch(StudyMaterial item);
    }

    private List<StudyMaterial> items = new ArrayList<>();
    private final OnMaterialAction listener;

    public StudyMaterialAdapter(List<StudyMaterial> items, OnMaterialAction listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void setItems(List<StudyMaterial> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int position) { return items.get(position).type; }
    @Override public int getItemCount() { return items.size(); }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == StudyMaterial.TYPE_HEADER) {
            // Reuse the same section-header layout from the Tests tab
            View v = inf.inflate(R.layout.item_test_section_header, parent, false);
            return new HeaderVH(v);
        }
        View v = inf.inflate(R.layout.item_study_material, parent, false);
        return new ItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StudyMaterial m = items.get(position);
        if (m.type == StudyMaterial.TYPE_HEADER) {
            ((HeaderVH) holder).bind(m);
        } else {
            ((ItemVH) holder).bind(m, listener);
        }
    }

    // ── Section header VH ─────────────────────────────────────────────────────

    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView title;
        HeaderVH(View v) { super(v); title = v.findViewById(R.id.header_title); }
        void bind(StudyMaterial m) { title.setText(m.sectionTitle); }
    }

    // ── Material item VH ──────────────────────────────────────────────────────

    static class ItemVH extends RecyclerView.ViewHolder {
        final TextView      typeBadge, subjectLabel, materialTitle, materialMeta, downloadStatus;
        final View          subjectDot;
        final MaterialButton actionBtn;

        ItemVH(View v) {
            super(v);
            typeBadge      = v.findViewById(R.id.type_badge);
            subjectDot     = v.findViewById(R.id.subject_dot);
            subjectLabel   = v.findViewById(R.id.subject_label);
            materialTitle  = v.findViewById(R.id.material_title);
            materialMeta   = v.findViewById(R.id.material_meta);
            downloadStatus = v.findViewById(R.id.download_status);
            actionBtn      = v.findViewById(R.id.action_btn);
        }

        void bind(StudyMaterial m, OnMaterialAction listener) {
            Context ctx = itemView.getContext();

            // Subject colour dot
            subjectDot.setBackgroundTintList(
                ColorStateList.valueOf(ctx.getColor(m.subjectColor)));
            subjectLabel.setText(m.subject);

            materialTitle.setText(m.title);
            materialMeta.setText(m.chapter + "  ·  " + m.meta);

            // Type badge — text, text colour, background tint
            applyTypeBadge(ctx, m.kind);

            // Action button & status
            if (m.isDownloaded) {
                downloadStatus.setText(ctx.getString(R.string.study_available_offline));
                actionBtn.setText(m.kind == StudyMaterial.KIND_VIDEO
                    ? ctx.getString(R.string.study_watch)
                    : ctx.getString(R.string.study_open));
                actionBtn.setOnClickListener(v -> {
                    if (listener != null) listener.onWatch(m);
                });
            } else {
                downloadStatus.setText("");
                actionBtn.setText(m.kind == StudyMaterial.KIND_VIDEO
                    ? ctx.getString(R.string.study_watch)
                    : ctx.getString(R.string.study_download));
                actionBtn.setOnClickListener(v -> {
                    if (listener != null) listener.onDownload(m);
                });
            }
        }

        private void applyTypeBadge(Context ctx, int kind) {
            int textColor, bgColor;
            String label;
            switch (kind) {
                case StudyMaterial.KIND_VIDEO:
                    label     = ctx.getString(R.string.study_type_video);
                    textColor = ctx.getColor(R.color.saffron_dark);
                    bgColor   = ctx.getColor(R.color.saffron_light);
                    break;
                case StudyMaterial.KIND_QPAPER:
                    label     = ctx.getString(R.string.study_type_qpaper);
                    textColor = ctx.getColor(R.color.positive);
                    bgColor   = ctx.getColor(R.color.positive_light);
                    break;
                case StudyMaterial.KIND_NOTES:
                    label     = ctx.getString(R.string.study_type_notes);
                    textColor = ctx.getColor(R.color.subject_english);
                    bgColor   = 0xFFF3E8FF;   // light purple, no named color for this
                    break;
                case StudyMaterial.KIND_MINDMAP:
                    label     = ctx.getString(R.string.study_type_mindmap);
                    textColor = ctx.getColor(R.color.negative);
                    bgColor   = ctx.getColor(R.color.negative_light);
                    break;
                default: // KIND_PDF
                    label     = ctx.getString(R.string.study_type_pdf);
                    textColor = ctx.getColor(R.color.reliance_blue);
                    bgColor   = ctx.getColor(R.color.reliance_blue_light);
                    break;
            }
            typeBadge.setText(label);
            typeBadge.setTextColor(textColor);
            typeBadge.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        }
    }
}
