package pt.ulisboa.tecnico.cmov.airdesk.entity;

/**
 * Created by ashansa on 3/15/15.
 */
public class ForeignWorkspace extends AbstractWorkspace {

    public ForeignWorkspace(String workspaceName, String ownerId, long quota) {
        super(workspaceName, ownerId, quota);
    }
}
