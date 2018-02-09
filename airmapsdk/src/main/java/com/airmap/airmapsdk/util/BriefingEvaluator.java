package com.airmap.airmapsdk.util;

import com.airmap.airmapsdk.models.flight.AirMapEvaluation;
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.flight.AirMapFlightFeature;
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static Set<AirMapFlightFeature> getApplicableFlightFeatures(AirMapEvaluation evaluation) {
        Set<AirMapFlightFeature> flightFeatures = new HashSet<>();

        for (AirMapRuleset ruleset : evaluation.getRulesets()) {
            for (AirMapRule rule : ruleset.getRules()) {
                // Rules that are conflicting, missing information or are informational should be shown
                if (rule.getStatus() != AirMapRule.Status.NotConflicting) {
                    for (AirMapFlightFeature flightFeature : rule.getFlightFeatures()) {
                        if (!flightFeature.isCalculated()) {
                            flightFeatures.add(flightFeature);
                        }
                    }
                }
            }
        }

        return flightFeatures;
    }

    public static boolean isApplicableFlightFeature(AirMapFlightFeature flightFeature) {
        if (flightFeature.getStatus() != AirMapFlightFeature.Status.NotConflicting) {
            if (!flightFeature.isCalculated()) {
                return true;
            }
        }

        return false;
    }

    public static AirMapRule.Status getOverallStatus(List<AirMapRule> rules) {
        // set worst status to overall status
        AirMapRule.Status overallStatus = AirMapRule.Status.NotConflicting;
        for (AirMapRule rule : rules) {
            if (rule.getStatus() == AirMapRule.Status.Conflicting) {
                overallStatus = rule.getStatus();
                break;
            } else if (rule.getStatus() == AirMapRule.Status.MissingInfo) {
                overallStatus = rule.getStatus();
            }
        }

        return overallStatus;
    }

    public static LinkedHashMap<AirMapRule.Status,List<AirMapRule>> getBriefingWithFlightFeatures(AirMapRuleset ruleset, AirMapFlightPlan flightPlan, AirMapEvaluation evaluation) {
        //TODO:
        LinkedHashMap<AirMapRule.Status,List<AirMapRule>> rulesMap = new LinkedHashMap<>();

//        for (AirMapRule rule : ruleset.getRules()) {
//            AirMapRule evaluationRule = getRuleFromEvaluation(rule);
//            List<AirMapRule> rules = new ArrayList<>();
//            if (ruleStatusMap.containsKey(rule.getStatus())) {
//                rules = ruleStatusMap.get(rule.getStatus());
//            }
//
//            rules.add(evaluationRule);
//            ruleStatusMap.put(rule.getStatus(), rules);
//        }

        return rulesMap;
    }

//    private AirMapRule getRuleFromEvaluation(AirMapRule rule) {
//        for (AirMapRuleset ruleset : evaluation.getRulesets()) {
//            for (AirMapRule evaluationRule : ruleset.getRules()) {
//                if (evaluationRule.equals(rule)) {
//                    Timber.e("swapped evaluation rule: " + evaluationRule.getShortText());
//                    if (evaluationRule.getFlightFeatures() != null) {
//                        for (AirMapFlightFeature ff : CopyCollections.copy(evaluationRule.getFlightFeatures())) {
//                            if (!BriefingEvaluator.isApplicableFlightFeature(ff)) {
//                                Timber.e("remove ff: " + ff.getDescription());
//                                evaluationRule.getFlightFeatures().remove(ff);
//                            }
//                        }
//                    }
//
//
//                    return evaluationRule;
//                }
//            }
//        }
//
//        return null;
//    }
}
