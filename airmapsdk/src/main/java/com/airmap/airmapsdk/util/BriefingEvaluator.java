package com.airmap.airmapsdk.util;

import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by collin@airmap.com on 10/5/17.
 */

public class BriefingEvaluator {

    public static LinkedHashMap<AirMapRule.Status, List<AirMapRule>> computeRulesViolations(AirMapFlightBriefing briefing) {
        List<AirMapRuleset> rulesets = briefing.getRulesets();
        Map<AirMapRule.Status, List<AirMapRule>> rulesMap = new HashMap<>();
        for (AirMapRuleset ruleset : rulesets) {
            for (AirMapRule rule : ruleset.getRules()) {
                List<AirMapRule> rules = rulesMap.get(rule.getStatus());
                if (rules == null) {
                    rules = new ArrayList<>();
                }

                // don't show duplicates
                boolean hasDuplicate = false;
                for (AirMapRule r : rules) {
                    if (r.toString().equals(rule.toString())) {
                        hasDuplicate = true;
                        break;
                    }
                }

                if (!hasDuplicate) {
                    rules.add(rule);
                    rulesMap.put(rule.getStatus(), rules);
                }
            }
        }

        List<Map.Entry<AirMapRule.Status, List<AirMapRule>>> entries = new ArrayList<>(rulesMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<AirMapRule.Status, List<AirMapRule>>>() {
            @Override
            public int compare(Map.Entry<AirMapRule.Status, List<AirMapRule>> o1, Map.Entry<AirMapRule.Status, List<AirMapRule>> o2) {
                if (o1.getKey().intValue() > o2.getKey().intValue()) {
                    return 1;
                } else if (o2.getKey().intValue() > o1.getKey().intValue()) {
                    return -1;
                }
                return 0;
            }
        });

        LinkedHashMap<AirMapRule.Status, List<AirMapRule>> sortedRulesMap = new LinkedHashMap<>();
        for (Map.Entry<AirMapRule.Status, List<AirMapRule>> entry : entries) {
            List<AirMapRule> rules = entry.getValue();
            Collections.sort(rules, new Comparator<AirMapRule>() {
                @Override
                public int compare(AirMapRule o1, AirMapRule o2) {
                    if (o1.getDisplayOrder() > o2.getDisplayOrder()) {
                        return 1;
                    } else if (o2.getDisplayOrder() > o1.getDisplayOrder()) {
                        return -1;
                    }
                    return o1.toString().compareTo(o2.toString());
                }
            });
            sortedRulesMap.put(entry.getKey(), rules);
        }

        return sortedRulesMap;
    }
}
