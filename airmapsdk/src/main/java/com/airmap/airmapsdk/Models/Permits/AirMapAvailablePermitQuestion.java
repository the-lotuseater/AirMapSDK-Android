package com.airmap.airmapsdk.models.permits;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 7/19/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapAvailablePermitQuestion implements Serializable, AirMapBaseModel {
    private String id;
    private String text;
    private List<AirMapPermitAnswer> answers;

    public AirMapAvailablePermitQuestion(JSONObject questionJson) {
        constructFromJson(questionJson);
    }

    public AirMapAvailablePermitQuestion() {

    }

    @Override
    public AirMapAvailablePermitQuestion constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setText(json.optString("text"));
            JSONArray answersArray = json.optJSONArray("answers");
            answers = new ArrayList<>();
            for (int i = 0; i < answersArray.length(); i++) {
                answers.add(new AirMapPermitAnswer(answersArray.optJSONObject(i)));
            }
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapAvailablePermitQuestion setId(String id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public AirMapAvailablePermitQuestion setText(String text) {
        this.text = text;
        return this;
    }

    public List<AirMapPermitAnswer> getAnswers() {
        return answers;
    }

    public AirMapAvailablePermitQuestion setAnswers(List<AirMapPermitAnswer> answers) {
        this.answers = answers;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapAvailablePermitQuestion && ((AirMapAvailablePermitQuestion) o).getId().equals(getId());
    }
}
