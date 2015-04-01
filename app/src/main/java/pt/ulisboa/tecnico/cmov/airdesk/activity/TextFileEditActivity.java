package pt.ulisboa.tecnico.cmov.airdesk.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

    static EditText fileText;
    static TextFile file;
    static Button saveButton;

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
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_edit_file) {

            saveButton.setVisibility(View.VISIBLE);
            fileText.setBackground(null);
            fileText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            Toast.makeText(this, "Editing", Toast.LENGTH_SHORT).show();
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
            fileText = (EditText) rootView.findViewById(R.id.file_text);
            fileText.setInputType(InputType.TYPE_NULL);

            Intent intent = getActivity().getIntent();
            file = new TextFile();

            if (intent != null) {

                if (intent.hasExtra(Constants.WORKSPACE)) {
                    file.setWorkspace(intent.getStringExtra(Constants.WORKSPACE));
                }
                if (intent.hasExtra(Constants.OWNER)) {
                    file.setOwner(intent.getStringExtra(Constants.OWNER));
                }
                if (intent.hasExtra(Constants.FILENAME)) {
                    file.setFileName(intent.getStringExtra(Constants.FILENAME));
                }
            }

            try {
                setFileText(file);
            } catch (ExecutionException e) {
                Log.d(LOG_TAG, "Error while executing file get Async task");
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "Interrupted while executing file get Async task");
            }

            saveButton = (Button) rootView.findViewById(R.id.file_save_btn);
            saveButton.setVisibility(View.INVISIBLE);
            saveButton.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        saveFileText(file);
                        saveButton.setVisibility(View.INVISIBLE);
                        getActivity().finish();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            return rootView;
        }

        private void setFileText(TextFile file) throws ExecutionException, InterruptedException {
            FileStreamAsyncTask fileStreamAsyncTask = new FileStreamAsyncTask();
            fileStreamAsyncTask.execute(file);
        }


        private void saveFileText(TextFile file) throws ExecutionException, InterruptedException {
            FileSaveAsynTask fileSaveAsynTask = new FileSaveAsynTask();
            fileSaveAsynTask.execute(file);
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
            fileText.setText(textBuffer);
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
                     manager.updateDataFile(textFile.getWorkspace(), textFile.getFileName(), fileText.getText().toString(),
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
}
