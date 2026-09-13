package com.saraswati.institute;

/**
 * Union model for the Tests tab list.
 * TYPE_HEADER → subject section divider
 * TYPE_TEST   → upcoming or completed test card
 */
public class TestItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TEST   = 1;

    public final int type;

    // ── Header fields ──
    public String sectionTitle;

    // ── Test fields ──
    public String  id;
    public String  title;
    public String  subject;       // e.g. "Mathematics"
    public int     subjectColor;  // color resource id, e.g. R.color.subject_math
    public String  dateLabel;     // e.g. "28 May · 30 Q · 60 min"
    public boolean isOnline;      // true = online test, false = offline/downloadable
    public boolean isCompleted;

    // ── Completed stats (valid only when isCompleted = true) ──
    public int   score;
    public int   maxScore;
    public float percentile;
    public int   rank;
    public int   totalStudents;

    /** Section header constructor. */
    public TestItem(String sectionTitle) {
        this.type         = TYPE_HEADER;
        this.sectionTitle = sectionTitle;
    }

    /** Test card constructor — upcoming (score fields unused). */
    public TestItem(String id, String title, String subject, int subjectColor,
                    String dateLabel, boolean isOnline) {
        this.type         = TYPE_TEST;
        this.id           = id;
        this.title        = title;
        this.subject      = subject;
        this.subjectColor = subjectColor;
        this.dateLabel    = dateLabel;
        this.isOnline     = isOnline;
        this.isCompleted  = false;
    }

    /** Test card constructor — completed (includes score stats). */
    public TestItem(String id, String title, String subject, int subjectColor,
                    String dateLabel, boolean isOnline,
                    int score, int maxScore, float percentile, int rank, int totalStudents) {
        this(id, title, subject, subjectColor, dateLabel, isOnline);
        this.isCompleted   = true;
        this.score         = score;
        this.maxScore      = maxScore;
        this.percentile    = percentile;
        this.rank          = rank;
        this.totalStudents = totalStudents;
    }
}
