package de.olafj.springws;

import java.util.StringJoiner;

public class Message {
    private String from;
    private String to;
    private String content;

    //standard constructors, getters, setters


    public Message() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("from='" + from + "'")
                .add("to='" + to + "'")
                .add("content='" + content + "'")
                .toString();
    }
}