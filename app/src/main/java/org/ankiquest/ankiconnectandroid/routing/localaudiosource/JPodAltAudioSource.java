package org.ankiquest.ankiconnectandroid.routing.localaudiosource;

import org.ankiquest.ankiconnectandroid.routing.database.Entry;

public class JPodAltAudioSource extends LocalAudioSource {
    public JPodAltAudioSource() {
        super("jpod_alternate", "user_files/jpod_alternate_files");
    }

    @Override
    public String getSourceName(Entry entry) {
        return "JPod101 Alt";
    }
}
