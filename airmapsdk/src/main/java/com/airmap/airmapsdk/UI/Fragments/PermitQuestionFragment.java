package com.airmap.airmapsdk.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.airmap.airmapsdk.models.permits.AirMapPermitAnswer;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermitQuestion;
import com.airmap.airmapsdk.R;

/**
 * Created by Vansh Gandhi on 7/19/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class PermitQuestionFragment extends Fragment {

    private static final String ARG_QUESTION = "question";
    private static final String ARG_POSITION = "position";

    private OnFragmentInteractionListener mListener;

    private AirMapAvailablePermitQuestion question;
    private int indexOfQuestion;
    private TextView questionTextView;
    private RadioGroup radioGroup;
    private Button nextButton;

    public PermitQuestionFragment() {
        // Required empty public constructor
    }

    public static PermitQuestionFragment newInstance(AirMapAvailablePermitQuestion question, int position) {
        PermitQuestionFragment fragment = new PermitQuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_QUESTION, question);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airmap_fragment_permit_question, container, false);
        initializeViews(view);
        if (getArguments() != null && getArguments().getSerializable(ARG_QUESTION) != null) {
            question = (AirMapAvailablePermitQuestion) getArguments().getSerializable(ARG_QUESTION);
            indexOfQuestion = getArguments().getInt(ARG_POSITION);
            questionTextView.setText(question.getText());
            populateAnswers();
        }
        return view;
    }

    private void initializeViews(View view) {
        questionTextView = (TextView) view.findViewById(R.id.question_text);
        radioGroup = (RadioGroup) view.findViewById(R.id.question_radio_group);
        nextButton = (Button) view.findViewById(R.id.next_button);
        nextButton.setEnabled(false);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.answerSelected(getSelectedAnswer(), indexOfQuestion);
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = radioGroup.findViewById(checkedId);
                int index = radioGroup.indexOfChild(radioButton);
                mListener.answerSelected(getSelectedAnswer(index), indexOfQuestion);
                nextButton.setEnabled(true);
            }
        });
    }

    private void populateAnswers() {
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for (AirMapPermitAnswer answer : question.getAnswers()) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(answer.getText());
            radioGroup.addView(radioButton, params);
        }
    }

    private AirMapPermitAnswer getSelectedAnswer(int index) {
        return question.getAnswers().get(index);
    }

    private AirMapPermitAnswer getSelectedAnswer() {
        int id = radioGroup.getCheckedRadioButtonId();
        View radioButton = radioGroup.findViewById(id);
        int index = radioGroup.indexOfChild(radioButton);
        return question.getAnswers().get(index);
    }

    public int getIndexOfQuestion() {
        return indexOfQuestion;
    }

    public AirMapAvailablePermitQuestion getQuestion() {
        return question;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    public interface OnFragmentInteractionListener {
        void answerSelected(AirMapPermitAnswer answer, int indexOfQuestion);

    }
}
