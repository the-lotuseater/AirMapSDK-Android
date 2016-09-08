package com.airmap.airmapsdk.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermitQuestion;
import com.airmap.airmapsdk.models.permits.AirMapPermitAnswer;
import com.airmap.airmapsdk.models.permits.AirMapPermitDecisionFlow;
import com.airmap.airmapsdk.models.status.AirMapStatusPermits;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.ui.adapters.QuestionsPagerAdapter;
import com.airmap.airmapsdk.ui.fragments.PermitQuestionFragment;

import java.util.ArrayList;
import java.util.List;

public class DecisionFlowActivity extends AppCompatActivity implements PermitQuestionFragment.OnFragmentInteractionListener {

    public static final String ARG_PERMIT = "permit";
    public static final String PERMIT_ID = "permitId";
    public static final int RESULT_CANT_FLY = 0;

    private QuestionsPagerAdapter adapter;
    private ViewPager viewPager;

    private AirMapStatusPermits permit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        permit = (AirMapStatusPermits) intent.getSerializableExtra(ARG_PERMIT);
        setContentView(R.layout.airmap_activity_decision_flow);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(permit.getAuthorityName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ArrayList<AirMapAvailablePermitQuestion> questions = new ArrayList<>();
        questions.add(getFirstQuestion());
        adapter = new QuestionsPagerAdapter(getSupportFragmentManager(), questions);
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);
    }

    private AirMapAvailablePermitQuestion getFirstQuestion() {
        return getQuestionForId(permit.getDecisionFlow().getFirstQuestionId());
    }

    private AirMapAvailablePermitQuestion getQuestionForId(String id) {
        AirMapPermitDecisionFlow flow = permit.getDecisionFlow();
        for (AirMapAvailablePermitQuestion question : flow.getQuestions()) {
            if (question.getId().equals(id)) {
                return question;
            }
        }
        return null; //If no question is found, return null
    }

    @Override
    public void answerSelected(final AirMapPermitAnswer answer, int indexOfQuestion) {
        invalidateFurtherQuestions(indexOfQuestion);
        if (answer.isLastQuestion()) {
            if (answer.getPermitId() != null && !answer.getPermitId().isEmpty()) {
                decisionFlowFinished(answer.getPermitId());
            } else if (answer.getMessage() != null && !answer.getMessage().isEmpty()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(viewPager, answer.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
//                setResult(RESULT_CANT_FLY);
//                finish();
            }
        } else {
            AirMapAvailablePermitQuestion nextQuestion = getQuestionForId(answer.getNextQuestionId());
            adapter.add(nextQuestion);
            viewPager.setCurrentItem(indexOfQuestion + 1, true); //Go to next page automatically
        }
    }

    private void decisionFlowFinished(String permitId) {
        AirMap.getPermit(permitId, new AirMapCallback<List<AirMapAvailablePermit>>() {
            @Override
            public void onSuccess(List<AirMapAvailablePermit> permits) {
                if (!permits.isEmpty()) {
                    Intent intent = new Intent(DecisionFlowActivity.this, CustomPropertiesActivity.class);
                    intent.putExtra(CustomPropertiesActivity.PERMIT, permits.get(0)); //There should only be one permit
                    startActivityForResult(intent, CreateFlightActivity.REQUEST_CUSTOM_PROPERTIES);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DecisionFlowActivity.this, "Error getting permit", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(AirMapException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DecisionFlowActivity.this, "Error getting permit", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataFromCustomProperties) {
        if (requestCode == CreateFlightActivity.REQUEST_CUSTOM_PROPERTIES) {
            if (resultCode == RESULT_OK) {
                Intent data = new Intent();
                data.putExtra(CustomPropertiesActivity.PERMIT, dataFromCustomProperties.getSerializableExtra(CustomPropertiesActivity.PERMIT));
                setResult(RESULT_OK, data);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, dataFromCustomProperties);
    }

    private void invalidateFurtherQuestions(int indexOfQuestion) {
        for (int i = indexOfQuestion + 1; i < adapter.getCount(); i++) {
            adapter.remove(i);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        } else {
            super.onBackPressed();
        }
    }
}