package net.loveruby.cflat.compiler;
import net.loveruby.cflat.parser.*;
import net.loveruby.cflat.ast.*;
import net.loveruby.cflat.exception.*;
import java.util.*;
import java.io.*;

public class Compiler {
    static public void main(String[] args) {
        new Compiler().commandMain(args);
    }

    static final private String programId = "cbc";
    protected LibraryLoader loader;
    protected ErrorHandler handler;

    public Compiler() {
        loader = new LibraryLoader();
        handler = new ErrorHandler(programId);
    }

    public Compiler(LibraryLoader ld, ErrorHandler h) {
        loader = ld;
        handler = h;
    }

    public void commandMain(String[] args) {
        try {
            if (args.length == 0) errorExit("no argument given");
            if (args[0].equals("--dump-ast")) {
                if (args.length != 2)
                    errorExit("no file input or too many files");
                dumpFile(args[1]);
            }
            else if (args[0].equals("--check-syntax")) {
                if (args.length != 2)
                    errorExit("no file input or too many files");
                if (isValidSyntax(args[1])) {
                    System.out.println("Syntax OK");
                    System.exit(0);
                } else {
                    System.exit(1);
                }
            }
            else {
                if (args.length != 1)
                    errorExit("no file input or too many files");
                compileFile(args[0]);
            }
        }
        catch (CompileException ex) {
            System.exit(1);
        }
    }

    private void errorExit(String msg) {
        handler.error(msg);
        System.exit(1);
    }

    public boolean isValidSyntax(String path) {
        try {
            parseFile(path);
            return true;
        }
        catch (CompileException ex) {
            return false;
        }
    }

    public void dumpFile(String path) throws CompileException {
        parseFile(path).dump("");
    }

    public void compileFile(String path) throws CompileException {
        AST ast = parseFile(path);
        JumpResolver.resolve(ast, handler);
        LocalReferenceResolver.resolve(ast, handler);
        TypeResolver.resolve(ast);
        String asm = CodeGenerator.generate(ast);
        writeFile(asmFileName(path), asm);
        assemble(asmFileName(path));
    }

    public AST parseFile(String path) throws CompileException {
        return Parser.parseFile(new File(path), loader, handler);
    }

    public void assemble(String path) throws IPCException {
        String[] cmd = {"gcc", path, "-o", cmdFileName(path)};
        invoke(cmd);
    }

    public void invoke(String[] cmd) throws IPCException {
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
            if (proc.exitValue() != 0) {
                handler.error("gcc failed (assemble); status " +
                              proc.exitValue());
                throw new IPCException("compile error");
            }
        }
        catch (InterruptedException ex) {
            handler.error("gcc interrupted: " + ex.getMessage());
            throw new IPCException("compile error");
        }
        catch (IOException ex) {
            handler.error(ex.getMessage());
            throw new IPCException("compile error");
        }
    }

    protected void writeFile(String path, String str)
                                    throws FileException {
        try {
            BufferedWriter f = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path)));
            try {
                f.write(str);
            }
            finally {
                f.close();
            }
        }
        catch (FileNotFoundException ex) {
            handler.error("file not found: " + path);
            throw new FileException("file error");
        }
        catch (IOException ex) {
            handler.error("IO error" + ex.getMessage());
            throw new FileException("file error");
        }
    }

    protected String asmFileName(String orgPath) {
        return baseName(orgPath, true) + ".s";
    }

    protected String cmdFileName(String orgPath) {
        return baseName(orgPath, true);
    }

    protected String baseName(String path) {
        return new File(path).getName();
    }

    protected String baseName(String path, boolean stripExt) {
        if (stripExt) {
            return new File(path).getName().replaceFirst("\\.[^.]*$", "");
        }
        else {
            return baseName(path);
        }
    }
}
