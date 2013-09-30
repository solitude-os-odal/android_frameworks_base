/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.documentsui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.documentsui.model.DocumentInfo;

import java.io.FileNotFoundException;

/**
 * Dialog to create a new directory.
 */
public class CreateDirectoryFragment extends DialogFragment {
    private static final String TAG_CREATE_DIRECTORY = "create_directory";

    public static void show(FragmentManager fm) {
        final CreateDirectoryFragment dialog = new CreateDirectoryFragment();
        dialog.show(fm, TAG_CREATE_DIRECTORY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        final ContentResolver resolver = context.getContentResolver();

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());

        final View view = dialogInflater.inflate(R.layout.dialog_create_dir, null, false);
        final EditText text1 = (EditText) view.findViewById(android.R.id.text1);

        builder.setTitle(R.string.menu_create_dir);
        builder.setView(view);

        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String displayName = text1.getText().toString();
                new CreateDirectoryTask(displayName).execute();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    private class CreateDirectoryTask extends AsyncTask<Void, Void, DocumentInfo> {
        private final String mDisplayName;

        public CreateDirectoryTask(String displayName) {
            mDisplayName = displayName;
        }

        @Override
        protected DocumentInfo doInBackground(Void... params) {
            final DocumentsActivity activity = (DocumentsActivity) getActivity();
            final ContentResolver resolver = activity.getContentResolver();

            final DocumentInfo cwd = activity.getCurrentDirectory();
            final Uri childUri = DocumentsContract.createDocument(
                    resolver, cwd.derivedUri, Document.MIME_TYPE_DIR, mDisplayName);
            try {
                return DocumentInfo.fromUri(resolver, childUri);
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(DocumentInfo result) {
            final DocumentsActivity activity = (DocumentsActivity) getActivity();
            if (result != null) {
                // Navigate into newly created child
                activity.onDocumentPicked(result);
            } else {
                Toast.makeText(activity, R.string.create_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
