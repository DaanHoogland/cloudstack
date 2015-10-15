package org.apache.cloudstack.agent.transport;

import java.io.IOException;
import java.lang.reflect.Array;

import com.cloud.agent.api.Command;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

public class CommandArrayTypeAdaptor extends GenericArrayTypeAdaptor<Command> {
    protected Command[] newArray(int size) {
        Command[] commands = (Command[])Array.newInstance(Command.class, size);
        return commands;
    }
    public void initGson(Gson gson) {
        _gson = gson;
        _adaptor = _gson.getAdapter(Command.class);
    }
    TypeAdapter<Command> _adaptor;

    @Override
    protected void writeElement(JsonWriter out, Command elem) throws IOException {
        _adaptor.write(out,elem);
    }

}
