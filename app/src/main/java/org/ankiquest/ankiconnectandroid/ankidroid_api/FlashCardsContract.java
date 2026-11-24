//noinspection MissingCopyrightHeader - explicitly not GPL

/*
 * Copying and distribution of this file, with or without modification, are permitted in any
 * medium without royalty. This file is offered as-is, without any warranty.
 *
 * Vendored from AnkiQuest's own AnkiQuest-Android api module, at commit
 * bd339154fb535bfd386f0811711f9fae07c8de0a:
 * api/src/main/java/com/ichi2/anki/FlashCardsContract.kt
 * Only the column-name and segment constants are kept; the column names are the
 * wire contract with the flashcards content provider of AnkiDroid and AnkiQuest.
 */
package org.ankiquest.ankiconnectandroid.ankidroid_api;

/** The column names of the flashcards content provider, as AnkiQuest-Android defines them. */
public final class FlashCardsContract {

    public static final class Note {
        public static final String CONTENT_URI_SEGMENT = "notes";
        public static final String CONTENT_URI_V2_SEGMENT = "notes_v2";
        public static final String _ID = "_id";
        public static final String GUID = "guid";
        public static final String MID = "mid";
        public static final String ALLOW_EMPTY = "allow_empty";
        public static final String MOD = "mod";
        public static final String USN = "usn";
        public static final String TAGS = "tags";
        public static final String FLDS = "flds";
        public static final String SFLD = "sfld";
        public static final String CSUM = "csum";
        public static final String FLAGS = "flags";
        public static final String DATA = "data";
        public static final String DECK_ID_QUERY_PARAM = "deckId";

        private Note() {}
    }

    public static final class Model {
        public static final String CONTENT_URI_SEGMENT = "models";
        public static final String CURRENT_MODEL_ID = "current";
        public static final String _ID = "_id";
        public static final String NAME = "name";
        public static final String FIELD_NAME = "field_name";
        public static final String FIELD_NAMES = "field_names";
        public static final String NUM_CARDS = "num_cards";
        public static final String CSS = "css";
        public static final String SORT_FIELD_INDEX = "sort_field_index";
        public static final String TYPE = "type";
        public static final String LATEX_POST = "latex_post";
        public static final String LATEX_PRE = "latex_pre";
        public static final String NOTE_COUNT = "note_count";
        public static final String DECK_ID = "deck_id";

        private Model() {}
    }

    public static final class Deck {
        public static final String CONTENT_ALL_URI_SEGMENT = "decks";
        public static final String CONTENT_SELECTED_URI_SEGMENT = "selected_deck";
        public static final String DECK_NAME = "deck_name";
        public static final String DECK_ID = "deck_id";
        public static final String DECK_COUNTS = "deck_count";
        public static final String OPTIONS = "options";
        public static final String DECK_DYN = "deck_dyn";
        public static final String DECK_DESC = "deck_desc";

        private Deck() {}
    }

    public static final class Card {
        public static final String CONTENT_URI_SEGMENT = "cards";
        public static final String _ID = "_id";
        public static final String NOTE_ID = "note_id";
        public static final String CARD_ORD = "ord";
        public static final String CARD_NAME = "card_name";
        public static final String DECK_ID = "deck_id";
        public static final String REPS = "reps";
        public static final String LAPSES = "lapses";
        public static final String TYPE = "type";
        public static final String ORIGINAL_DECK_ID = "original_deck_id";

        private Card() {}
    }

    public static final class AnkiMedia {
        public static final String CONTENT_URI_SEGMENT = "media";
        public static final String FILE_URI = "file_uri";
        public static final String PREFERRED_NAME = "preferred_name";

        private AnkiMedia() {}
    }

    private FlashCardsContract() {}
}
