package org.ankiquest.ankiconnectandroid.routing.localaudiosource;

import org.ankiquest.ankiconnectandroid.routing.database.Entry;

public class ForvoAudioSource extends LocalAudioSource {
    public ForvoAudioSource() {
        super("forvo", "user_files/forvo_files");
    }

    @Override
    public String getSourceName(Entry entry)  {
        return "Forvo (" + entry.speaker + ")";
    }
}
