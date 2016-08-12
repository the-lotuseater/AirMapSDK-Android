package com.airmap.airmapsdk.UI.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.airmap.airmapsdk.Models.Permits.AirMapAvailablePermitQuestion;
import com.airmap.airmapsdk.UI.Fragments.PermitQuestionFragment;
import com.nakama.arraypageradapter.ArrayFragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by Vansh Gandhi on 7/19/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class QuestionsPagerAdapter extends ArrayFragmentStatePagerAdapter<AirMapAvailablePermitQuestion> {

    public QuestionsPagerAdapter(FragmentManager fm, List<AirMapAvailablePermitQuestion> questions) {
        super(fm, questions);
    }

    @Override
    public Fragment getFragment(AirMapAvailablePermitQuestion item, int position) {
        return PermitQuestionFragment.newInstance(getItem(position), position);
    }
}