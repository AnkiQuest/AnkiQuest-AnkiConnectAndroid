package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ichi2.anki.FlashCardsContract;
import com.kamwithk.ankiconnectandroid.flashcard_app.FlashcardApp;
import com.kamwithk.ankiconnectandroid.flashcard_app.FlashcardApps;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The parts of AnkiDroid's AddContentApi this app uses, talking to the chosen flashcard app.
 * AddContentApi is bound to AnkiDroid's authority, so it can't reach AnkiQuest.
 * Ported from https://github.com/ankidroid/Anki-Android/blob/v2.17alpha14/api/src/main/java/com/ichi2/anki/api/AddContentApi.kt
 * (LGPL-3.0-or-later, Timothy Rae and Mark Carter).
 */
public class ProviderApi {
    private static final String NOTES = "notes";
    private static final String NOTES_V2 = "notes_v2";
    private static final String MODELS = "models";
    private static final String DECKS = "decks";
    private static final String MEDIA = "media";
    private static final String CARDS = "cards";
    private static final String FIELD_SEPARATOR = "\u001f";
    private static final String TAG_SEPARATOR = " ";
    private static final String[] NOTE_FIELDS_PROJECTION = {FlashCardsContract.Note.FLDS};

    private final Context context;
    private final ContentResolver resolver;

    public ProviderApi(Context context) {
        this.context = context.getApplicationContext();
        this.resolver = this.context.getContentResolver();
    }

    /** Package of the flashcard app to talk to, checked on every call. */
    public String packageName() {
        return FlashcardApps.require(context).packageName;
    }

    public Uri notesUri() {
        return uri(NOTES);
    }

    public Uri notesV2Uri() {
        return uri(NOTES_V2);
    }

    public Uri mediaUri() {
        return uri(MEDIA);
    }

    public Long addNote(long modelId, long deckId, String[] fields, Set<String> tags) {
        ContentValues values = new ContentValues();
        values.put(FlashCardsContract.Note.MID, modelId);
        values.put(FlashCardsContract.Note.FLDS, String.join(FIELD_SEPARATOR, fields));
        if (tags != null) {
            values.put(FlashCardsContract.Note.TAGS, String.join(TAG_SEPARATOR, tags));
        }

        Uri noteUri = resolver.insert(notesUri(), values);
        if (noteUri == null) {
            return null;
        }

        // Move the new note's cards to the requested deck
        Uri cardsUri = Uri.withAppendedPath(noteUri, CARDS);
        Cursor cursor = resolver.query(cardsUri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        try (cursor) {
            while (cursor.moveToNext()) {
                String ord = cursor.getString(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.CARD_ORD));
                ContentValues cardValues = new ContentValues();
                cardValues.put(FlashCardsContract.Card.DECK_ID, deckId);
                resolver.update(Uri.withAppendedPath(cardsUri, ord), cardValues, null, null);
            }
        }
        return Long.parseLong(noteUri.getLastPathSegment());
    }

    /** The note's fields without trailing empty ones, as AddContentApi returned them; null if not found. */
    public String[] getNoteFields(long noteId) {
        Uri noteUri = Uri.withAppendedPath(notesUri(), Long.toString(noteId));
        Cursor cursor = resolver.query(noteUri, NOTE_FIELDS_PROJECTION, null, null, null);
        if (cursor == null) {
            return null;
        }
        try (cursor) {
            if (!cursor.moveToNext()) {
                return null;
            }
            return splitFields(cursor.getString(cursor.getColumnIndexOrThrow(FlashCardsContract.Note.FLDS)));
        }
    }

    public boolean updateNoteFields(long noteId, String[] fields) {
        Uri noteUri = Uri.withAppendedPath(notesUri(), Long.toString(noteId));
        ContentValues values = new ContentValues();
        values.put(FlashCardsContract.Note.FLDS, String.join(FIELD_SEPARATOR, fields));

        // The provider returns the number of notes with this id, whether or not fields changed
        return resolver.update(noteUri, values, null, null) > 0;
    }

    public String[] getFieldList(long modelId) {
        Uri modelUri = Uri.withAppendedPath(uri(MODELS), Long.toString(modelId));
        Cursor cursor = resolver.query(modelUri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        try (cursor) {
            if (!cursor.moveToNext()) {
                return null;
            }
            return splitFields(cursor.getString(cursor.getColumnIndexOrThrow(FlashCardsContract.Model.FIELD_NAMES)));
        }
    }

    /** Map of (id, name) of models with at least minNumFields fields; null if the query failed. */
    public Map<Long, String> getModelList(int minNumFields) {
        Cursor cursor = resolver.query(uri(MODELS), null, null, null, null);
        if (cursor == null) {
            return null;
        }
        Map<Long, String> models = new HashMap<>();
        try (cursor) {
            while (cursor.moveToNext()) {
                String fieldNames = cursor.getString(cursor.getColumnIndexOrThrow(FlashCardsContract.Model.FIELD_NAMES));
                if (splitFields(fieldNames).length < minNumFields) {
                    continue;
                }
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(FlashCardsContract.Model._ID));
                models.put(id, cursor.getString(cursor.getColumnIndexOrThrow(FlashCardsContract.Model.NAME)));
            }
        }
        return models;
    }

    public String getModelName(long modelId) {
        Map<Long, String> models = getModelList(1);
        return models == null ? null : models.get(modelId);
    }

    /** Map of (id, name) of all decks; null if the query failed. */
    public Map<Long, String> getDeckList() {
        Cursor cursor = resolver.query(uri(DECKS), null, null, null, null);
        if (cursor == null) {
            return null;
        }
        Map<Long, String> decks = new HashMap<>();
        try (cursor) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(FlashCardsContract.Deck.DECK_ID));
                decks.put(id, cursor.getString(cursor.getColumnIndexOrThrow(FlashCardsContract.Deck.DECK_NAME)));
            }
        }
        return decks;
    }

    private Uri uri(String path) {
        FlashcardApp app = FlashcardApps.require(context);
        return Uri.parse("content://" + app.authority + "/" + path);
    }

    // Drops trailing empty fields like AddContentApi's Utils.splitFields
    private static String[] splitFields(String fields) {
        if (fields.isEmpty()) {
            return new String[0];
        }
        return fields.split(FIELD_SEPARATOR);
    }
}
