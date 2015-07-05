package com.techshroom.comtext;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Set;

import net.eq2online.macros.scripting.api.APIVersion;
import net.eq2online.macros.scripting.api.IVariableProvider;
import net.eq2online.macros.scripting.parser.ScriptContext;

import com.google.common.collect.ImmutableSet;

@APIVersion(18)
public class VariableProviderClipboardContents implements IVariableProvider {

    private static final Set<String> VARIABLES = ImmutableSet.of("CLIPBOARD");

    private String contents;

    public VariableProviderClipboardContents() {

    }

    @Override
    public void onInit() {
        ScriptContext.getAvailableContexts().stream()
                .map(ScriptContext::getCore)
                .forEach(x -> x.registerVariableProvider(this));
    }

    @Override
    public void updateVariables(boolean clock) {
        updateContents();
    }

    private void updateContents() {
        try {
            this.contents =
                    String.valueOf(Toolkit.getDefaultToolkit()
                            .getSystemClipboard().getContents(null)
                            .getTransferData(DataFlavor.stringFlavor));
        } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getVariable(String variableName) {
        if (!VARIABLES.contains(variableName)) {
            return null;
        }
        updateContents();
        return this.contents;
    }

    @Override
    public Set<String> getVariables() {
        return VARIABLES;
    }
}