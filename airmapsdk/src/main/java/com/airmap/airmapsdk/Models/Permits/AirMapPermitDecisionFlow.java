package com.airmap.airmapsdk.Models.Permits;

import com.airmap.airmapsdk.Models.AirMapBaseModel;

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
public class AirMapPermitDecisionFlow implements Serializable, AirMapBaseModel {
    private String firstQuestionId;
    private List<AirMapAvailablePermitQuestion> questions;

    public AirMapPermitDecisionFlow(JSONObject flowJson) {
        constructFromJson(flowJson);
    }

    public AirMapPermitDecisionFlow() {

    }

    @Override
    public AirMapPermitDecisionFlow constructFromJson(JSONObject json) {
        if (json != null) {
            setFirstQuestionId(json.optString("first_question_id"));
            JSONArray questionsArray = json.optJSONArray("questions");
            questions = new ArrayList<>();
            for (int i = 0; i < questionsArray.length(); i++) {
                questions.add(new AirMapAvailablePermitQuestion(questionsArray.optJSONObject(i)));
            }
        }
        return this;
    }

    public String getFirstQuestionId() {
        return firstQuestionId;
    }

    public AirMapPermitDecisionFlow setFirstQuestionId(String firstQuestionId) {
        this.firstQuestionId = firstQuestionId;
        return this;
    }

    public List<AirMapAvailablePermitQuestion> getQuestions() {
        return questions;
    }

    public AirMapPermitDecisionFlow setQuestions(List<AirMapAvailablePermitQuestion> questions) {
        this.questions = questions;
        return this;
    }
}
