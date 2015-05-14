package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.entity.TextFile;
import pt.ulisboa.tecnico.cmov.airdesk.manager.UserManager;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class TextFileEditActivity extends ActionBarActivity {

    static EditText editText;
    static TextView displayText;
    static TextFile file;
    static Button saveButton;
    static MenuItem saveMenuItem;
    private static MenuItem editMenuItem;
    static ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_file_edit);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new FileEditFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_text_file_edit, menu);
        editMenuItem = menu.getItem(0);
        saveMenuItem = menu.getItem(1);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_edit_file) {
            try {
                StringBuffer content = requestText(file, true, true);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                saveMenuItem.setVisible(true);
                item.setVisible(false);

                displayText.setVisibility(View.INVISIBLE);
                editText.setVisibility(View.VISIBLE);

                editText.setBackground(null);
           /* editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            editText.setHorizontallyScrolling(false);*/
                if (content == null)
                    Toast.makeText(this, "Write lock already taken", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Editing", Toast.LENGTH_SHORT).show();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } else if (id == R.id.delete_file) {

            AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                    //set message, title, and icon
                    .setTitle("Delete")
                    .setMessage("Do you want to delete this File ?")
                    .setIcon(R.drawable.delete)

                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                new WorkspaceManager().deleteDataFile(file.getWorkspace(), file.getFileName(),
                                        file.getOwner(), new UserManager().getOwner().getUserId().equals(file.getOwner()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                            finish();
                        }

                    })

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    })
                    .create();
            myQuittingDialogBox.show();

        } else if (id == R.id.action_save_file) {
            try {
                saveFileText(file);
                displayText.setText(editText.getText().toString());
                //  requestText(file);
                saveMenuItem.setVisible(false);
                editMenuItem.setVisible(true);
                editText.setVisibility(View.INVISIBLE);
                displayText.setVisibility(View.VISIBLE);
                //editText.setInputType(InputType.TYPE_NULL);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class FileEditFragment extends Fragment {

        private final String LOG_TAG = FileEditFragment.class.getSimpleName();

        public FileEditFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_text_file_edit, container, false);
            editText = (EditText) rootView.findViewById(R.id.file_text_edit);

            editText.setVisibility(View.INVISIBLE);

            displayText = (TextView) rootView.findViewById(R.id.file_text);

            displayText.setVisibility(View.VISIBLE);
            //  editText.setInputType(InputType.TYPE_NULL);
            // editText.setHorizontallyScrolling(false);

            Intent intent = getActivity().getIntent();
            file = new TextFile();

            if (intent != null) {

                if (intent.hasExtra(Constants.WORKSPACE_NAME)) {
                    file.setWorkspace(intent.getStringExtra(Constants.WORKSPACE_NAME));
                }
                if (intent.hasExtra(Constants.OWNER)) {
                    file.setOwner(intent.getStringExtra(Constants.OWNER));
                }
                if (intent.hasExtra(Constants.FILENAME)) {
                    file.setFileName(intent.getStringExtra(Constants.FILENAME));
                }
            }

            getActivity().setTitle(file.getWorkspace() + "/" + file.getFileName());

            try {
                requestText(file, false, false);
                progressDialog = ProgressDialog
                        .show(getActivity(), "Loading", "Loading file...",true,true);

            } catch (ExecutionException e) {
                Log.d(LOG_TAG, "Error while executing file get Async task");
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "Interrupted while executing file get Async task");
            }

            return rootView;
        }

    }


    public static class FileStreamAsyncTask extends AsyncTask<TextFile, Void, StringBuffer> {

        private final String LOG_TAG = FileStreamAsyncTask.class.getSimpleName();
        boolean writeMode = false;

        public FileStreamAsyncTask(boolean writeMode) {
            this.writeMode = writeMode;
        }

        WorkspaceManager manager = new WorkspaceManager();

        @Override
        protected StringBuffer doInBackground(TextFile... params) {
            TextFile[] textFileList = params;
            StringBuffer textBuffer = new StringBuffer();

            if (textFileList != null && textFileList.length == 1) {
                TextFile textFile = textFileList[0];

                //TODO:Change this accordingly to pass isOwner parameter using and intent
                try {
                    textBuffer = manager.getDataFile(textFile.getWorkspace(), textFile.getFileName(), writeMode,
                            textFile.getOwner()
                            , (new UserManager().getOwner().getUserId().equals(textFile.getOwner())));
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Error while retrieving the file");
                }
                if (textBuffer == null) {
                    System.out.println("----------------------");
                }
            }
            return textBuffer;
        }

        @Override
        protected void onPostExecute(StringBuffer textBuffer) {
            if (textBuffer != null) {
                editText.setText(textBuffer);
                displayText.setText(textBuffer);
                progressDialog.dismiss();
            } else {
                saveMenuItem.setVisible(false);
                editMenuItem.setVisible(true);
                editText.setVisibility(View.INVISIBLE);
                displayText.setVisibility(View.VISIBLE);
                /////imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    public static class FileSaveAsynTask extends AsyncTask<TextFile, Void, Boolean> {

        private final String LOG_TAG = FileSaveAsynTask.class.getSimpleName();

        WorkspaceManager manager = new WorkspaceManager();

        @Override
        protected Boolean doInBackground(TextFile... params) {
            TextFile[] textFileList = params;

            if (textFileList != null && textFileList.length == 1) {
                TextFile textFile = textFileList[0];

                //TODO:Change this accordingly to pass isOwner parameter using and intent
                try {
                    manager.updateDataFile(textFile.getWorkspace(), textFile.getFileName(), editText.getText().toString(),
                            textFile.getOwner()
                            , new UserManager().getOwner().getUserId().equals(textFile.getOwner()));
                    return true;
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Error while saving the file");
                }
            }
            return null;
        }

    }

    static private void saveFileText(TextFile file) throws ExecutionException, InterruptedException {
        FileSaveAsynTask fileSaveAsynTask = new FileSaveAsynTask();
        fileSaveAsynTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
    }

    static StringBuffer requestText(TextFile file, boolean writeMode, boolean isBlocking) throws ExecutionException, InterruptedException {
        FileStreamAsyncTask fileStreamAsyncTask = new FileStreamAsyncTask(writeMode);
        if (isBlocking) {
            return fileStreamAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file).get();
        } else {
            // No need to return anything here but returning an empty string to be on safe side.
            fileStreamAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
            return new StringBuffer();
        }

    }
}
