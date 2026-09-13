package com.saraswati.institute;

/** Represents one Q&A doubt posted by a student. */
public class DoubtItem {

    public String  id;
    public String  subject;
    public int     subjectColor;
    public String  question;
    public String  chapter;      // may be empty
    public String  postedAt;     // e.g. "2 hours ago"
    public boolean isAnswered;
    public String  answerText;   // null when pending
    public String  answeredBy;   // e.g. "Mr. Kumar"

    /** Pending doubt (no answer yet). */
    public DoubtItem(String id, String subject, int subjectColor,
                     String question, String chapter, String postedAt) {
        this.id           = id;
        this.subject      = subject;
        this.subjectColor = subjectColor;
        this.question     = question;
        this.chapter      = chapter;
        this.postedAt     = postedAt;
        this.isAnswered   = false;
    }

    /** Already-answered doubt. */
    public DoubtItem(String id, String subject, int subjectColor,
                     String question, String chapter, String postedAt,
                     String answerText, String answeredBy) {
        this(id, subject, subjectColor, question, chapter, postedAt);
        this.isAnswered = true;
        this.answerText = answerText;
        this.answeredBy = answeredBy;
    }
}
