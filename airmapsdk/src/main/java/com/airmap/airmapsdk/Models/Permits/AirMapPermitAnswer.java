package com.airmap.airmapsdk.Models.Permits;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Vansh Gandhi on 7/19/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapPermitAnswer implements Serializable, AirMapBaseModel {
    private String id;
    private String text;
    private String nextQuestionId;
    private String permitId;
    private String message;

    public AirMapPermitAnswer(JSONObject answerJson) {
        constructFromJson(answerJson);
    }

    public AirMapPermitAnswer() {
    }

    @Override
    public AirMapPermitAnswer constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setText(json.optString("text"));
            setPermitId(json.optString("permit_id", null));
            setNextQuestionId(json.optString("next_question_id", null));
            setMessage(json.optString("message", null));
        }
        return this;
    }

    public boolean isLastQuestion() {
        return getNextQuestionId() == null || getNextQuestionId().isEmpty();
    }

    public String getId() {
        return id;
    }

    public AirMapPermitAnswer setId(String id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public AirMapPermitAnswer setText(String text) {
        this.text = text;
        return this;
    }

    public String getNextQuestionId() {
        return nextQuestionId;
    }

    public AirMapPermitAnswer setNextQuestionId(String nextQuestionId) {
        this.nextQuestionId = nextQuestionId;
        return this;
    }

    public String getPermitId() {
        return permitId;
    }

    public AirMapPermitAnswer setPermitId(String permitId) {
        this.permitId = permitId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public AirMapPermitAnswer setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPermitAnswer && getId().equals(((AirMapPermitAnswer) o).getId());
    }

    @Override
    public String toString() {
        return getText();
    }
}