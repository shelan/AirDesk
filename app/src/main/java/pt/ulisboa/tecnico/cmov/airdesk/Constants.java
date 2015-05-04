package pt.ulisboa.tecnico.cmov.airdesk;

public class Constants {

    //TODO move OWNED/FOREIGN_WORKSPACE_DIR to StorageManager
    public static final String OWNED_WORKSPACE_DIR = "ownedWorkspaces";
    public static final String FOREIGN_WORKSPACE_DIR = "foreignWorkspaces";

    public static final String USER_JSON_FILE_NAME ="user.json";
    public static final String FOREIGN_WORKSPACE_SUFFIX ="-foreign";
    public static final String OWNED_SUFFIX="-owned";
    public static final int BYTES_PER_MB =1024*1024;
    public static final String JSON_SUFFIX=".json";

    public static final String WORKSPACE_NAME = "workspace";
    public static final String FILENAME = "filename";
    public static final String OWNER = "owner";
    public static final String IS_OWNED_WORKSPACE = "isOwnedWorkspace";

    // You should replace these values with your own
    // See the readme for details on what to fill in
    public static final String AWS_ACCOUNT_ID = "249859950283";
    public static final String COGNITO_POOL_ID =
            "us-east-1:8614ddd2-36ea-45b0-9639-2a03a9be3e88";
    public static final String COGNITO_ROLE_UNAUTH =
            "Cognito_airdeskUnauth_DefaultRole";
    // Note, the bucket will be created in all lower case letters
    // If you don't enter an all lower case title, any references you add
    // will need to be sanitized
    public static final String BUCKET_NAME = "ist-airdesk";
    public static final String FOLDER_SEP = "/" ;

    //broadcast actions for wifi direct
    public static final String SUBSCRIBE_TAGS_MSG = "subscribed";
    public static final String PUBLIC_WORKSPACES_FOR_TAGS = "public_ws_for_tags";
    public static final String TAGS = "tags";

    public static final int port = 10001;
    public static final int AIRDESK_SOCKET_PORT = 10011;
}
