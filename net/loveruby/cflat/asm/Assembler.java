package net.loveruby.cflat.asm;
import net.loveruby.cflat.type.*;
import net.loveruby.cflat.utils.*;
import java.util.*;

public class Assembler {
    protected List assemblies;        // List<Assembly>
    protected Type naturalType;
    protected int commentIndentLevel;

    static public long align(long n, long alignment) {
        return (n + alignment - 1) / alignment * alignment;
    }

    public Assembler(Type naturalType) {
        this.assemblies = new ArrayList();
        this.naturalType = naturalType;
        this.commentIndentLevel = 0;
    }

    public List assemblies() {
        return this.assemblies;
    }

    public void addAll(List assemblies) {
        this.assemblies.addAll(assemblies);
    }

    public String toSource() {
        StringBuffer buf = new StringBuffer();
        Iterator asms = assemblies.iterator();
        while (asms.hasNext()) {
            Assembly asm = (Assembly)asms.next();
            buf.append(asm.toSource());
            buf.append("\n");
        }
        return buf.toString();
    }

    public void comment(String str) {
        assemblies.add(new AsmComment(str, commentIndentLevel));
    }

    public void indentComment() {
        commentIndentLevel++;
    }

    public void unindentComment() {
        commentIndentLevel--;
    }

    public void label(String sym) {
        assemblies.add(new Label(sym));
    }

    public void label(Label label) {
        assemblies.add(label);
    }

    protected void directive(String direc) {
        assemblies.add(new Directive(direc));
    }

    protected void insn(String op) {
        assemblies.add(new Instruction(op));
    }

    protected void insn(String op, AsmOperand a) {
        assemblies.add(new Instruction(op, "", a));
    }

    protected void insn(String op, String suffix, AsmOperand a) {
        assemblies.add(new Instruction(op, suffix, a));
    }

    protected void insn(Type t, String op, AsmOperand a) {
        assemblies.add(new Instruction(op, typeSuffix(t), a));
    }

    protected void insn(String op, String suffix, AsmOperand a, AsmOperand b) {
        assemblies.add(new Instruction(op, suffix, a, b));
    }

    protected void insn(Type t, String op, AsmOperand a, AsmOperand b) {
        assemblies.add(new Instruction(op, typeSuffix(t), a, b));
    }

    protected String typeSuffix(Type t1, Type t2) {
        return typeSuffix(t1) + typeSuffix(t2);
    }

    protected String typeSuffix(Type t) {
        switch ((int)t.size()) {
        case 1: return "b";
        case 2: return "w";
        case 4: return "l";
        default:
            throw new Error("unknown type size: " + t.size());
        }
    }

    //
    // directives
    //

    public void _file(String name) {
        directive(".file\t" + TextUtils.dumpString(name));
    }

    public void _text() {
        directive("\t.text");
    }

    public void _data() {
        directive("\t.data");
    }

    public void _section(String name) {
        directive("\t.section\t" + name);
    }

    public void _section(String name, String flags, String type, String group, String linkage) {
        directive("\t.section\t" + name + "," + flags + "," + type + "," + group + "," + linkage);
    }

    public void _globl(String sym) {
        directive(".globl " + sym);
    }

    public void _local(String sym) {
        directive(".local " + sym);
    }

    public void _hidden(String sym) {
        directive("\t.hidden\t" + sym);
    }

    public void _comm(String sym, long size, long alignment) {
        directive("\t.comm\t" + sym + "," + size + "," + alignment);
    }

    public void _align(long n) {
        directive("\t.align\t" + n);
    }

    public void _type(String sym, String type) {
        directive("\t.type\t" + sym + "," + type);
    }

    public void _size(String sym, long size) {
        _size(sym, new Long(size).toString());
    }

    public void _size(String sym, String size) {
        directive("\t.size\t" + sym + "," + size);
    }

    public void _byte(long n) {
        directive(".byte\t" + n);
    }

    public void _value(long n) {
        directive(".value\t" + n);
    }

    public void _long(long n) {
        directive(".long\t" + n);
    }

    public void _long(Label sym) {
        directive(".long\t" + sym);
    }

    public void _quad(long n) {
        directive(".quad\t" + n);
    }

    public void _quad(Label sym) {
        directive(".quad\t" + sym);
    }

    public void _string(String str) {
        directive("\t.string\t" + TextUtils.dumpString(str));
    }

    //
    // Instructions
    //

    public void jmp(Label label) {
        insn("jmp", new DirectMemoryReference(label));
    }

    public void jz(Label label) {
        insn("jz", new DirectMemoryReference(label));
    }

    public void jnz(Label label) {
        insn("jnz", new DirectMemoryReference(label));
    }

    public void je(Label label) {
        insn("je", new DirectMemoryReference(label));
    }

    public void jne(Label label) {
        insn("jne", new DirectMemoryReference(label));
    }

    public void cmp(Type t, AsmOperand a, Register b) {
        insn(t, "cmp", a, b);
    }

    public void sete(Register reg) {
        insn("sete", reg);
    }

    public void setne(Register reg) {
        insn("setne", reg);
    }

    public void seta(Register reg) {
        insn("seta", reg);
    }

    public void setae(Register reg) {
        insn("setae", reg);
    }

    public void setb(Register reg) {
        insn("setb", reg);
    }

    public void setbe(Register reg) {
        insn("setbe", reg);
    }

    public void setg(Register reg) {
        insn("setg", reg);
    }

    public void setge(Register reg) {
        insn("setge", reg);
    }

    public void setl(Register reg) {
        insn("setl", reg);
    }

    public void setle(Register reg) {
        insn("setle", reg);
    }

    public void test(Type type, Register a, Register b) {
        insn(type, "test", a, b);
    }

    public void push(Register reg) {
        insn("push", typeSuffix(naturalType), reg);
    }

    public void pop(Register reg) {
        insn("pop", typeSuffix(naturalType), reg);
    }

    // call function by relative address
    public void call(String sym) {
        insn("call", new DirectMemoryReference(new Label(sym)));
    }

    // call function by absolute address
    public void callAbsolute(Register reg) {
        insn("call", new AbsoluteAddress(reg));
    }

    public void ret() {
        insn("ret");
    }

    public void mov(AsmOperand src, AsmOperand dest) {
        mov(naturalType, src, dest);
    }

    public void mov(Type type, AsmOperand src, AsmOperand dest) {
        insn(type, "mov", src, dest);
    }

    public void movsx(Type t1, Type t2, AsmOperand src, AsmOperand dest) {
        insn("movs", typeSuffix(t1, t2), src, dest);
    }

    public void movsbl(AsmOperand src, AsmOperand dest) {
        insn("movs", "bl", src, dest);
    }

    public void movswl(AsmOperand src, AsmOperand dest) {
        insn("movs", "wl", src, dest);
    }

    public void movzx(Type t1, Type t2, AsmOperand src, AsmOperand dest) {
        insn("movz", typeSuffix(t1, t2), src, dest);
    }

    public void movzb(Type t, AsmOperand src, AsmOperand dest) {
        insn("movz", "b" + typeSuffix(t), src, dest);
    }

    public void movzbl(AsmOperand src, AsmOperand dest) {
        insn("movz", "bl", src, dest);
    }

    public void movzwl(AsmOperand src, AsmOperand dest) {
        insn("movz", "wl", src, dest);
    }

    public void lea(AsmOperand src, AsmOperand dest) {
        lea(naturalType, src, dest);
    }

    public void lea(Type type, AsmOperand src, AsmOperand dest) {
        insn(type, "lea", src, dest);
    }

    public void neg(Type type, Register reg) {
        insn(type, "neg", reg);
    }

    public void inc(Type type, AsmOperand reg) {
        insn(type, "inc", reg);
    }

    public void dec(Type type, AsmOperand reg) {
        insn(type, "dec", reg);
    }

    public void add(AsmOperand diff, AsmOperand base) {
        add(naturalType, diff, base);
    }

    public void add(Type type, AsmOperand diff, AsmOperand base) {
        insn(type, "add", diff, base);
    }

    public void sub(AsmOperand diff, AsmOperand base) {
        sub(naturalType, diff, base);
    }

    public void sub(Type type, AsmOperand diff, AsmOperand base) {
        insn(type, "sub", diff, base);
    }

    public void imul(AsmOperand m, Register base) {
        imul(naturalType, m, base);
    }

    public void imul(Type type, AsmOperand m, Register base) {
        insn(type, "imul", m, base);
    }

    public void cltd() {
        insn("cltd");
    }

    public void div(Type type, Register base) {
        insn(type, "div", base);
    }

    public void idiv(Type type, Register base) {
        insn(type, "idiv", base);
    }

    public void not(Type type, Register reg) {
        insn(type, "not", reg);
    }

    public void and(Type type, AsmOperand bits, Register base) {
        insn(type, "and", bits, base);
    }

    public void or(Type type, AsmOperand bits, Register base) {
        insn(type, "or", bits, base);
    }

    public void xor(Type type, AsmOperand bits, Register base) {
        insn(type, "xor", bits, base);
    }

    public void sar(Type type, Register bits, Register base) {
        insn(type, "sar", bits, base);
    }

    public void sal(Type type, Register bits, Register base) {
        insn(type, "sal", bits, base);
    }

    public void shr(Type type, Register bits, Register base) {
        insn(type, "shr", bits, base);
    }
}
