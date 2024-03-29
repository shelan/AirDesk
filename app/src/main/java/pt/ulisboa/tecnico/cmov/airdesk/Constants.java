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

    public static final String WORKSPACE_NAME = "workspaceName";
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
    public static final String INTRODUCE_MSG = "introduce";
    public static final String ID_IP_MAP_REQUEST_MSG = "id_ip_map_request";
    public static final String ID_IP_MAP_REPLY_MSG = "id_ip_map_reply";
    public static final String SUBSCRIBE_TAGS_MSG = "subscribed";
    public static final String PUBLISH_TAGS_MSG = "publish_tags";
    public static final String ADD_TO_FOREIGN_WORKSPACE_MSG = "add_to_foreign_ws";
    public static final String REVOKE_ACCESS_MSG = "revoke_access_of_client";
    public static final String REQUEST_FILE_MSG = "request_file";
    public static final String SAVE_FILE_MSG = "save_file";
    public static final String DELETE_FILE_MSG = "delete_file";
    public static final String FILE_CONTENT_RESULT_MSG = "send_file";
    public static final String UPDATED_FILE_LIST_MSG = "updated_file_list";
    public static final String WORKSPACES = "workspaces";
    public static final String SENDER_ID = "senderID";
    public static final String TAGS = "tags";
    public static final String CLIENT_ID = "client_id";
    public static final String ID_IP_MAP = "id_ip_map";
    public static final String WRITE_MODE = "write_mode";
    public static final String FILE_CONTENT = "file_content";

    //workspace fields in cast json msg
    public static final String MATCHING_TAGS = "matchingTags";
    public static final String FILE_NAMES = "fileNames";
    public static final String OWNER_ID = "ownerId";
    public static final String QUOTA = "quota";


    public static final int port = 10001;
    public static final int AIRDESK_SOCKET_PORT = 10011;
}
