package org.bbop.apollo.gwt.client.dto;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

import java.util.List;

/**
 * Created by ndunn on 4/17/15.
 */
public class AnnotatorStateInfo implements HasJSON{

    private OrganismInfo currentOrganism ;
    private List<OrganismInfo> organismList ;
    private SequenceInfo currentSequence ;
    private List<SequenceInfo> currentSequenceList ;

    public OrganismInfo getCurrentOrganism() {
        return currentOrganism;
    }

    public void setCurrentOrganism(OrganismInfo currentOrganism) {
        this.currentOrganism = currentOrganism;
    }

    public List<OrganismInfo> getOrganismList() {
        return organismList;
    }

    public void setOrganismList(List<OrganismInfo> organismList) {
        this.organismList = organismList;
    }

    public SequenceInfo getCurrentSequence() {
        return currentSequence;
    }

    public void setCurrentSequence(SequenceInfo currentSequence) {
        this.currentSequence = currentSequence;
    }

    public List<SequenceInfo> getCurrentSequenceList() {
        return currentSequenceList;
    }

    public void setCurrentSequenceList(List<SequenceInfo> currentSequenceList) {
        this.currentSequenceList = currentSequenceList;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject returnObject = new JSONObject();

        if(currentOrganism!=null){
            returnObject.put("currentOrganism",currentOrganism.toJSON());
        }
        if(currentSequence!=null){
            returnObject.put("currentSequence",currentSequence.toJSON());
        }
        if(currentSequenceList!=null){
            JSONArray sequenceListArray = new JSONArray();
            for(SequenceInfo sequenceInfo : currentSequenceList){
                sequenceListArray.set(sequenceListArray.size(),sequenceInfo.toJSON());
            }
            returnObject.put("currentSequenceList",sequenceListArray);
        }
        if(organismList!=null){
            JSONArray organismListArray = new JSONArray();
            for(OrganismInfo organismInfo : organismList){
                organismListArray.set(organismListArray.size(),organismInfo.toJSON());
            }
            returnObject.put("organismList",organismListArray);
        }


        return returnObject ;
    }
}
