package com.airmap.airmapsdk.models.rules;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONObject;

import java.io.Serializable;


/**
 * Created by collin@airmap.com on 3/28/17.
 */

public class AirMapRule implements Serializable, AirMapBaseModel {

    public enum Status {
        Pass("pass"), Info("info"), MoreAction("moreaction"), Fail("fail");

        private String text;

        Status(String text) {
            this.text = text;
        }

        public static AirMapRule.Status fromString(String text) {
            switch (text.toLowerCase()) {
                case "pass":
                    return Pass;
                case "info":
                    return Info;
                case "moreaction":
                    return MoreAction;
                case "fail":
                    return Fail;
            }

            return Pass;
        }

        public String toString() {
            return text;
        }

    }

    private String id;
    private String summary;
    private Status status;

    public AirMapRule() {
    }

    public AirMapRule(JSONObject resultJson) {
        constructFromJson(resultJson);
    }

    @Override
    public AirMapRule constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setSummary(Utils.optString(json, "summary"));

            setStatus(AirMapRule.Status.fromString(json.optString("rule-status")));
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapRule setId(String id) {
        this.id = id;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public AirMapRule setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public AirMapRule.Status getStatus() {
        return status;
    }

    public AirMapRule setStatus(AirMapRule.Status status) {
        this.status = status;
        return this;
    }
}
