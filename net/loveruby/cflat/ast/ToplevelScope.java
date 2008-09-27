package net.loveruby.cflat.ast;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.exception.*;
import java.util.*;

public class ToplevelScope extends Scope {
    protected List staticLocalVariables;

    public ToplevelScope() {
        super(null);
    }

    public boolean isToplevel() {
        return true;
    }

    /** Searches and gets entity searching scopes upto ToplevelScope. */
    // #@@range/get{
    public Entity get(String name) throws SemanticException {
        Entity ent = (Entity)entities.get(name);
        if (ent == null) {
            throw new SemanticException("unresolved reference: " + name);
        }
        return ent;
    }
    // #@@}

    protected List staticLocalVariables() {
        if (staticLocalVariables == null) {
            staticLocalVariables = new ArrayList();
            Iterator scopes = children.iterator();
            while (scopes.hasNext()) {
                LocalScope s = (LocalScope)scopes.next();
                staticLocalVariables.addAll(s.staticLocalVariables());
            }
            Map seqTable = new HashMap();
            Iterator vars = staticLocalVariables.iterator();
            while (vars.hasNext()) {
                DefinedVariable var = (DefinedVariable)vars.next();
                Long seq = (Long)seqTable.get(var.name());
                if (seq == null) {
                    var.setSequence(0);
                    seqTable.put(var.name(), new Long(1));
                }
                else {
                    var.setSequence(seq.longValue());
                    seqTable.put(var.name(), new Long(seq.longValue() + 1));
                }
            }
        }
        return staticLocalVariables;
    }

    /** Returns a list of all global variables.
     * "All global variable" means:
     *
     *    * has global scope
     *    * defined or undefined
     *    * public or private
     */
    public List allGlobalVariables() {
        List result = new ArrayList();
        List src = new ArrayList();
        src.addAll(entities.values());
        src.addAll(staticLocalVariables());
        Iterator ents = src.iterator();
        while (ents.hasNext()) {
            Object ent = ents.next();
            if (ent instanceof Variable) {
                result.add(ent);
            }
        }
        return result;
    }

    /** Returns the list of global variables.
     *  A global variable is a variable which has
     *  global scope and is initialized.  */
    public List globalVariables() {
        List result = new ArrayList();
        List src = new ArrayList();
        src.addAll(entities.values());
        src.addAll(staticLocalVariables());
        Iterator ents = src.iterator();
        while (ents.hasNext()) {
            Object ent = ents.next();
            if (ent instanceof DefinedVariable) {
                DefinedVariable var = (DefinedVariable)ent;
                if (var.hasInitializer()) {
                    result.add(var);
                }
            }
        }
        return result;
    }

    /** Returns the list of common symbols.
     *  A common symbol is a variable which has
     *  global scope and is not initialized.  */
    public List commonSymbols() {
        List result = new ArrayList();
        List src = new ArrayList();
        src.addAll(entities.values());
        src.addAll(staticLocalVariables());
        Iterator ents = src.iterator();
        while (ents.hasNext()) {
            Object ent = ents.next();
            if (ent instanceof DefinedVariable) {
                DefinedVariable var = (DefinedVariable)ent;
                if (!var.hasInitializer()) {
                    result.add(var);
                }
            }
        }
        return result;
    }
}
