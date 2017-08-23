package com.airmap.airmapsdk.models.rules;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by collin@airmap.com on 8/11/17.
 */

public class AirMapAuthorization implements AirMapBaseModel, Serializable{

    public enum Status {
        ACCEPTED, REJECTED, PENDING, ACCEPTED_UPON_SUBMISSION, REJECTED_UPON_SUBMISSION;

        public static Status fromText(String text) {
            switch (text) {
                case "accepted":
                    return ACCEPTED;
                case "rejected":
                    return REJECTED;
                case "pending":
                    return PENDING;
                case "accepted_upon_submission":
                    return ACCEPTED_UPON_SUBMISSION;
                case "rejected_upon_submission":
                    return REJECTED_UPON_SUBMISSION;
            }

            return null;
        }
    }

    private Status status;
    private AirMapAuthority authority;
    private String message;

    public AirMapAuthorization(JSONObject jsonObject) {
        constructFromJson(jsonObject);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        setStatus(Status.fromText(json.optString("status")));

        if (json.has("authority")) {
            setAuthority(new AirMapAuthority(json.optJSONObject("authority")));
        }

        setMessage(json.optString("message"));
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public AirMapAuthority getAuthority() {
        return authority;
    }

    public void setAuthority(AirMapAuthority authority) {
        this.authority = authority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
