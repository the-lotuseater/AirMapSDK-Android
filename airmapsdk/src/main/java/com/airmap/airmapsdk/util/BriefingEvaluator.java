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

    public static boolean isApplicableFlightFeature(AirMapRule rule, AirMapFlightFeature flightFeature) {
        if (rule.getStatus() != AirMapRule.Status.NotConflicting) {
            if (!flightFeature.isCalculated()) {
                return true;
            }
        }

        return false;
    }

    public static AirMapRule.Status getStatus(AirMapFlightBriefing briefing) {
        // set worst status to overall status
        AirMapRule.Status overallStatus = AirMapRule.Status.NotConflicting;
        for (AirMapRuleset ruleset : briefing.getRulesets()) {
            AirMapRule.Status rulesetStatus = getStatus(ruleset);
            if (rulesetStatus == AirMapRule.Status.Conflicting) {
                overallStatus = AirMapRule.Status.Conflicting;
                break;
            } else if (rulesetStatus == AirMapRule.Status.MissingInfo) {
                overallStatus = AirMapRule.Status.MissingInfo;
            }
        }

        return overallStatus;
    }

    public static AirMapRule.Status getStatus(AirMapRuleset ruleset) {
        // set worst status to overall status
        AirMapRule.Status overallStatus = AirMapRule.Status.NotConflicting;
        for (AirMapRule rule : ruleset.getRules()) {
            if (rule.getStatus() == AirMapRule.Status.Conflicting) {
                overallStatus = rule.getStatus();
                break;
            } else if (rule.getStatus() == AirMapRule.Status.MissingInfo) {
                overallStatus = rule.getStatus();
            }
        }

        return overallStatus;
    }

    public static LinkedHashMap<AirMapRule.Status,List<AirMapRule>> getRulesWithFlightFeatures(AirMapRuleset ruleset, AirMapEvaluation evaluation) {
        LinkedHashMap<AirMapRule.Status,List<AirMapRule>> ruleStatusMap = new LinkedHashMap<>();
        ruleStatusMap.put(AirMapRule.Status.Conflicting, new ArrayList<AirMapRule>());
        ruleStatusMap.put(AirMapRule.Status.MissingInfo, new ArrayList<AirMapRule>());
        ruleStatusMap.put(AirMapRule.Status.InformationRules, new ArrayList<AirMapRule>());
        ruleStatusMap.put(AirMapRule.Status.NotConflicting, new ArrayList<AirMapRule>());

        for (AirMapRule rule : ruleset.getRules()) {
            List<AirMapRule> rules = new ArrayList<>();
            if (ruleStatusMap.containsKey(rule.getStatus())) {
                rules = ruleStatusMap.get(rule.getStatus());
            }

            for (AirMapFlightFeature flightFeature : CopyCollections.copy(rule.getFlightFeatures())) {
                AirMapFlightFeature evaluationFlightFeature = getFlightFeatureFromEvaluation(evaluation, flightFeature);
                if (evaluationFlightFeature != null && isApplicableFlightFeature(rule, evaluationFlightFeature)) {
                    rule.getFlightFeatures().remove(flightFeature);
                    rule.getFlightFeatures().add(evaluationFlightFeature);
                } else {
                    rule.getFlightFeatures().remove(flightFeature);
                }
            }

            rules.add(rule);
            ruleStatusMap.put(rule.getStatus(), rules);
        }

        return ruleStatusMap;
    }

    private static AirMapFlightFeature getFlightFeatureFromEvaluation(AirMapEvaluation evaluation, AirMapFlightFeature flightFeature) {
        for (AirMapRuleset ruleset : evaluation.getRulesets()) {
            for (AirMapRule rule : ruleset.getRules()) {
                for (AirMapFlightFeature evaluationFlightFeature : rule.getFlightFeatures()) {
                    if (evaluationFlightFeature.getFlightFeature().equals(flightFeature.getFlightFeature())) {
                        return evaluationFlightFeature;
                    }
                }
            }
        }

        return null;
    }

}
