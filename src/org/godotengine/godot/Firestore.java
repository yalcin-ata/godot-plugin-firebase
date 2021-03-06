/**
 * Copyright 2017 FrogSquare. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.godotengine.godot;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.godotengine.godot.Dictionary;
import org.json.JSONException;
import org.json.JSONObject;

public class Firestore {

	public static Firestore getInstance(Activity p_activity) {
		if (mInstance == null) {
			mInstance = new Firestore(p_activity);
		}

		return mInstance;
	}

	public Firestore(Activity p_activity) {
		activity = p_activity;
	}

	public void init(FirebaseApp firebaseApp) {
		mFirebaseApp = firebaseApp;

		// Enable Firestore logging
		FirebaseFirestore.setLoggingEnabled(true);
		db = FirebaseFirestore.getInstance();

		Utils.d("GodotFirebase", "Firestore::Initialized");
	}

	public void loadDocuments(final String p_name, final int callback_id) {
		Utils.d("GodotFirebase", "Firestore::LoadData");

		db.collection(p_name).get()
				.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
					@Override
					public void onComplete(@NonNull Task<QuerySnapshot> task) {
						if (task.isSuccessful()) {
							JSONObject jobject = new JSONObject();

							try {
								JSONObject jobject_1 = new JSONObject();

								for (DocumentSnapshot document : task.getResult()) {
									jobject_1.put(document.getId(), new JSONObject(document.getData()));
								}

								jobject.put(p_name, jobject_1);
							} catch (JSONException e) {
								Utils.d("GodotFirebase", "JSON Exception: " + e.toString());
							}


							Utils.d("GodotFirebase", "Data: " + jobject.toString());

							if (callback_id == -1) {
								Utils.callScriptFunc(
										"Firestore", "Documents", jobject.toString());
							} else {
								Utils.callScriptFunc(
										callback_id, "Firestore", "Documents", jobject.toString());
							}

						} else {
							Utils.w("GodotFirebase", "Error getting documents: " + task.getException());
						}
					}
				});
	}

	public void addDocument(final String p_name, final Dictionary p_dict) {
		Utils.d("GodotFirebase", "Firestore::AddData");

		// Add a new document with a generated ID
		db.collection(p_name)
				.add(p_dict) // AutoGrenerate ID use .document("name").set(p_dict)
				.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
					@Override
					public void onSuccess(DocumentReference documentReference) {
						Utils.d("GodotFirebase", "DocumentSnapshot added with ID: " + documentReference.getId());
						Utils.callScriptFunc("Firestore", "DocumentAdded", true);
					}
				}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Utils.w("GodotFirebase", "Error adding document: " + e);
				Utils.callScriptFunc("Firestore", "DocumentAdded", false);
			}
		});
	}

	public void setData(final String p_col_name, final String p_doc_name, final Dictionary p_dict) {
		db.collection(p_col_name).document(p_doc_name)
				.set(p_dict, SetOptions.merge())
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Utils.d("GodotFirebase", "DocumentSnapshot successfully written!");
						Utils.callScriptFunc("Firestore", "DocumentAdded", true);
					}
				}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Utils.w("GodotFirebase", "Error adding document: " + e);
				Utils.callScriptFunc("Firestore", "DocumentAdded", false);
			}
		});

	}

	private FirebaseFirestore db = null;
	private static Activity activity = null;
	private static Firestore mInstance = null;

	private int script_callback_id = -1;

	private FirebaseApp mFirebaseApp = null;
}
