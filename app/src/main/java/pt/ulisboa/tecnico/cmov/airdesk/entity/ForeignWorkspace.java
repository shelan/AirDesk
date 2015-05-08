package pt.ulisboa.tecnico.cmov.airdesk.entity;


public class ForeignWorkspace extends AbstractWorkspace {

    public ForeignWorkspace(String workspaceName, String ownerId, double quota) {
        super(workspaceName, ownerId, quota);
    }

    private String[] matchingTags;

    public String[] getMatchingTags() {
        return matchingTags;
    }

    public void setMatchingTags(String[] matchingTags) {
        this.matchingTags = matchingTags;
    }
}
