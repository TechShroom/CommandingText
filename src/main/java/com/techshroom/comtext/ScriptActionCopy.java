package com.techshroom.comtext;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import net.eq2online.macros.scripting.ScriptActionBase;
import net.eq2online.macros.scripting.api.APIVersion;
import net.eq2online.macros.scripting.api.IMacro;
import net.eq2online.macros.scripting.api.IMacroAction;
import net.eq2online.macros.scripting.api.IMacroActionProcessor;
import net.eq2online.macros.scripting.api.IMacroActionStackEntry;
import net.eq2online.macros.scripting.api.IReturnValue;
import net.eq2online.macros.scripting.api.IScriptAction;
import net.eq2online.macros.scripting.api.IScriptActionProvider;
import net.eq2online.macros.scripting.parser.ScriptContext;

@APIVersion(18)
public class ScriptActionCopy
        extends ScriptActionBase {

    public ScriptActionCopy() {
        super(ScriptContext.MAIN, "copy");
    }

    @Override
    public boolean isThreadSafe() {
        return false;
    }

    @Override
    public boolean isStackPushOperator() {
        return false;
    }

    @Override
    public boolean isStackPopOperator() {
        return false;
    }

    @Override
    public boolean canBePoppedBy(IScriptAction action) {
        return false;
    }

    @Override
    public boolean executeStackPush(IScriptActionProvider provider,
            IMacro macro, IMacroAction instance, String rawParams,
            String[] params) {
        return false;
    }

    @Override
    public boolean executeStackPop(IScriptActionProvider provider,
            IMacro macro, IMacroAction instance, String rawParams,
            String[] params, IMacroAction popAction) {
        return false;
    }

    @Override
    public boolean canBreak(IMacroActionProcessor processor,
            IScriptActionProvider provider, IMacro macro,
            IMacroAction instance, IMacroAction breakAction) {
        return false;
    }

    @Override
    public boolean isConditionalOperator() {
        return false;
    }

    @Override
    public boolean isConditionalElseOperator(IScriptAction action) {
        return false;
    }

    @Override
    public boolean matchesConditionalOperator(IScriptAction action) {
        return false;
    }

    @Override
    public boolean executeConditional(IScriptActionProvider provider,
            IMacro macro, IMacroAction instance, String rawParams,
            String[] params) {
        return false;
    }

    @Override
    public void executeConditionalElse(IScriptActionProvider provider,
            IMacro macro, IMacroAction instance, String rawParams,
            String[] params, IMacroActionStackEntry top) {
    }

    @Override
    public IReturnValue execute(IScriptActionProvider provider, IMacro macro,
            IMacroAction instance, String rawParams, String[] params) {
        String textToCopy = params[0];
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(textToCopy);
        c.setContents(selection, selection);
        return null;
    }

    @Override
    public boolean canExecuteNow(IScriptActionProvider provider, IMacro macro,
            IMacroAction instance, String rawParams, String[] params) {
        return true;
    }

    @Override
    public int onTick(IScriptActionProvider provider) {
        // tick tock?
        return 0;
    }

    @Override
    public boolean isClocked() {
        return true;
    }

    @Override
    public boolean isPermissable() {
        return false;
    }

    @Override
    public String getPermissionGroup() {
        return null;
    }

    @Override
    public void registerPermissions(String actionName, String actionGroup) {
    }

    @Override
    public boolean checkExecutePermission() {
        return true;
    }

    @Override
    public boolean checkPermission(String actionName, String permission) {
        return true;
    }

    @Override
    public void onStopped(IScriptActionProvider provider, IMacro macro,
            IMacroAction instance) {
    }

    @Override
    public void onInit() {
        ScriptContext.MAIN.getCore().registerScriptAction(this);
    }

}
