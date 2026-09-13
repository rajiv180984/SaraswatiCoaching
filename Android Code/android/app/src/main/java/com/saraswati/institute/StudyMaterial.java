package com.saraswati.institute;

/**
 * Union model for the Study Material list.
 *   TYPE_HEADER → subject section label
 *   TYPE_ITEM   → a downloadable / viewable study resource
 */
public class StudyMaterial {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM   = 1;

    /** Material kind — used by the adapter to assign badge text & colours. */
    public static final int KIND_PDF     = 0;   // Notes, summaries
    public static final int KIND_VIDEO   = 1;   // Lecture recordings
    public static final int KIND_QPAPER  = 2;   // Previous-year question papers
    public static final int KIND_NOTES   = 3;   // Revision / short notes
    public static final int KIND_MINDMAP = 4;   // Mind-map diagrams

    public final int type;

    // ── Header ──
    public String sectionTitle;

    // ── Item ──
    public String id;
    public String title;
    public String subject;       // "Mathematics", "Science", …
    public int    subjectColor;  // colour resource id
    public int    kind;          // KIND_* constant
    public String chapter;       // e.g. "Chapter 3"
    public String meta;          // "2.4 MB" for PDF, "18:45" for Video
    public boolean isDownloaded;

    /** Section header constructor. */
    public StudyMaterial(String sectionTitle) {
        this.type         = TYPE_HEADER;
        this.sectionTitle = sectionTitle;
    }

    /** Material item constructor. */
    public StudyMaterial(String id, String title, String subject, int subjectColor,
                         int kind, String chapter, String meta) {
        this.type         = TYPE_ITEM;
        this.id           = id;
        this.title        = title;
        this.subject      = subject;
        this.subjectColor = subjectColor;
        this.kind         = kind;
        this.chapter      = chapter;
        this.meta         = meta;
        this.isDownloaded = false;
    }
}
