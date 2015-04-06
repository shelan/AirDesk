package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.annotation.TargetApi;
import android.content.Context;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmov.airdesk.Constants;
import pt.ulisboa.tecnico.cmov.airdesk.Exception.WriteLockedException;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.entity.TextFile;
import pt.ulisboa.tecnico.cmov.airdesk.manager.WorkspaceManager;


public class TextFileEditActivity extends ActionBarActivity {

    static EditText editText;
    static TextView displayText;
    static TextFile file;
    static Button saveButton;
    static MenuItem saveMenuItem;
    private MenuItem editMenuItem;

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
        editMenuItem = menu.getItem(1);
        saveMenuItem = menu.getItem(2);
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
            imm.toggleSoftInput (InputMethodManager.SHOW_FORCED, 0);

            saveMenuItem.setVisible(true);
            item.setVisible(false);

            displayText.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.VISIBLE);

            editText.setBackground(null);
           /* editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            editText.setHorizontallyScrolling(false);*/
            Toast.makeText(this, "Editing", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.delete_file) {
            try {
                new WorkspaceManager().deleteDataFile(file.getWorkspace(), file.getFileName(),
                        file.getOwner(), true);
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.action_save_file) {
            try {
                saveFileText(file);
                setText(file);
                saveMenuItem.setVisible(false);
                editMenuItem.setVisible(true);
                editText.setVisibility(View.INVISIBLE);
                displayText.setVisibility(View.VISIBLE);
                //editText.setInputType(InputType.TYPE_NULL);
                imm.toggleSoftInput (InputMethodManager.SHOW_FORCED, 0);

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
                setText(file);
            } catch (ExecutionException e) {
                Log.d(LOG_TAG, "Error while executing file get Async task");
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "Interrupted while executing file get Async task");
            }

            return rootView;
        }

    }


    public static class FileStreamAsyncTask extends AsyncTask<TextFile, Void, FileInputStream> {

        private final String LOG_TAG = FileStreamAsyncTask.class.getSimpleName();

        WorkspaceManager manager = new WorkspaceManager();

        @Override
        protected FileInputStream doInBackground(TextFile... params) {
            TextFile[] textFileList = params;

            if (textFileList != null && textFileList.length == 1) {
                TextFile textFile = textFileList[0];

                //TODO:Change this accordingly to pass isOwner parameter using and intent
                try {
                    return manager.getDataFile(textFile.getWorkspace(), textFile.getFileName(), false,
                            textFile.getOwner()
                            , true);
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Error while retrieving the file");
                } catch (WriteLockedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(FileInputStream fileInputStream) {

            StringBuffer textBuffer = new StringBuffer();
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader buffreader = new BufferedReader(inputStreamReader);

            try {
                String readString = buffreader.readLine();
                while (readString != null) {
                    textBuffer.append(readString);
                    readString = buffreader.readLine();
                }
                inputStreamReader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            editText.setText(textBuffer);
            displayText.setText(textBuffer);
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
                            , true);
                    return true;
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Error while saving the file");
                }
            }
            return null;
        }

    }

    static private void saveFileText(TextFile file) throws ExecutionException, InterruptedException {
        FileSaveAsynTask fileSaveAsynTask = new FileSaveAsynTask();
        fileSaveAsynTask.execute(file);
    }

    static private void setText(TextFile file) throws ExecutionException, InterruptedException {
        FileStreamAsyncTask fileStreamAsyncTask = new FileStreamAsyncTask();
        fileStreamAsyncTask.execute(file);
    }
}
