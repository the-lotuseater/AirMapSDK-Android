package com.airmap.airmapsdktest.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdktest.R;
import com.airmap.airmapsdktest.activities.MapDemoActivity;
import com.airmap.airmapsdktest.ui.RulesetRecyclerViewAdapter;

import java.util.List;

public class RulesetsFragment extends Fragment {

    private static final String TAG = "RulesetsFragment";

    private RecyclerView rulesetsRecyclerView;
    private RulesetRecyclerViewAdapter rulesetsRecyclerAdapter;
    private View loadingView;

    public static RulesetsFragment newInstance() {
        return new RulesetsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rulesets, container, false);

        loadingView = view.findViewById(R.id.loading_view);

        rulesetsRecyclerView = view.findViewById(R.id.rulesets_recycler_view);
        rulesetsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rulesetsRecyclerAdapter = new RulesetRecyclerViewAdapter(getActivity(), new RulesetRecyclerViewAdapter.RulesetListener() {
            @Override
            public void onRulesetSelected(AirMapRuleset ruleset) {
                AirMapLog.i(TAG, "Ruleset selected: " + ruleset.getShortName());
                ((MapDemoActivity) getActivity()).selectRuleset(ruleset);
            }

            @Override
            public void onRulesetDeselected(AirMapRuleset ruleset) {
                AirMapLog.i(TAG, "Ruleset deselected: " + ruleset.getShortName());
                ((MapDemoActivity) getActivity()).deselectRuleset(ruleset);
            }

            @Override
            public void onRulesetInfoPressed(AirMapRuleset ruleset) {
                AirMapLog.i(TAG, "Ruleset info pressed: " + ruleset.getShortName());
            }

            @Override
            public void onRulesetSwitched(AirMapRuleset fromRuleset, AirMapRuleset toRuleset) {
                AirMapLog.i(TAG, "Ruleset switched from: " + fromRuleset.getShortName() + " to: " + toRuleset.getShortName());
                ((MapDemoActivity) getActivity()).switchSelectedRulesets(fromRuleset, toRuleset);
            }
        });
        rulesetsRecyclerView.setAdapter(rulesetsRecyclerAdapter);

        return view;
    }


    public void setRulesets(List<AirMapRuleset> availableRulesets, List<AirMapRuleset> selectedRulesets) {
        rulesetsRecyclerAdapter.setData(availableRulesets, selectedRulesets);
        loadingView.setVisibility(View.GONE);
    }

}
