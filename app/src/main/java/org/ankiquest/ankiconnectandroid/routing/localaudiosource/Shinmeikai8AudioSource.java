package org.ankiquest.ankiconnectandroid.routing.localaudiosource;

import org.ankiquest.ankiconnectandroid.routing.database.Entry;

public class Shinmeikai8AudioSource extends LocalAudioSource {
    public Shinmeikai8AudioSource() {
        super("shinmeikai8", "user_files/shinmeikai8_files");
    }

    @Override
    public String getSourceName(Entry entry) {
        return "SMK8 " + entry.display;
    }
}
